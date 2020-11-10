package wenatchee.node; /**
 * RaftNode represents a single node in cluster. It orchestrates four distinct threads:
 * 1. Clock, responsible for signalling timeouts on election and heartbeat and measuring elapsed time
 * It checks for timeouts every CLOCK_SLEEP_TIME, by default 1 ms
 * 2. Receiver, doing his job by listening to incoming Drafts, rebuilding them and passing to receivedDrafts
 * 3. Consumer, deciding on how to process incoming Drafts
 * 4. Sender, polling Drafts from outgoingDrafts and sending them to other nodes
 */

//TODO build a logger
//TODO dumping replicated log to file
//TODO FILE SEPARATOR DEPENDING ON THE OS TYPE!
//TODO remove id from constructor
//TODO buffer underflow exception happens randomly - compaction problem?

import wenatchee.exceptions.IdentityUnknownException;
import wenatchee.logging.Lg;
import wenatchee.networking.Identity;
import wenatchee.networking.RemoteClient;
import wenatchee.protocol.Draft;
import wenatchee.protocol.RaftEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.*;

public class RaftNodeLight
{
    static ArrayList<RaftNodeLight> nodes = new ArrayList<>();

    static void presentMetaCounters()
    {
        System.out.println();
        System.out.println();
        System.out.println("*************");
        System.out.println("Presenting metadata for nodes");
        for(RaftNodeLight node : RaftNodeLight.nodes)
        {
            System.out.println();
            node.presentMeta();
            System.out.println();
        }
        System.out.println();
        System.out.println();
        System.out.println("*************");
    }

    private void presentMeta()
    {
        System.out.println(this.toString());
        System.out.println("Node ID: " + this.id);
        System.out.println("Incoming messages counters:");

        this.metaCollector.incomingMessagesCtr.entrySet().forEach(entry->{
            System.out.println(entry.getKey() + " " + entry.getValue());
        });

        System.out.println("Outgoing messages counters:");
        this.metaCollector.incomingMessagesCtr.entrySet().forEach(entry->{
            System.out.println(entry.getKey() + " " + entry.getValue());
        });
    }

    private boolean slowdown = true;
    private Mode mode;

    //TODO
    public boolean appendEntries(Draft draft)
    {
        return false;
        // Append to end of blocking queue and pass for processing
    }

    enum Role
    {
        FOLLOWER,
        CANDIDATE,
        LEADER;
    }

    /**
     * Listener - disabled clock, never timeouts
     */
    enum Mode
    {
        FULL_NODE,
        LISTENER
    }

    static String module = "RaftNodeLight";

    public final static long[] ELECTION_TIMEOUT_BOUNDS = {3000, 4000};//{150, 300};
    //public final static int HEARTBEAT_TIMEOUT = 500;
    final static int CLOCK_SLEEP_TIME_MILIS = 1;
    //final static int DEFAULT_BUFFER_SIZE = 8192;
    final static int DEFAULT_HEARTBEAT_TIMEOUT = 200;
    //final static int DEFAULT_CATCHUP_TIMEOUT = 50;
    final static int DEFAULT_PORT = 5000;
    final static String DEFAULT_CONFIG_FILEPATH = "src/configuration";
    private final static int DEFAULT_DRAFT_ELECTION_NUMBER = -1;


    int id;
    int heartbeatTimeout;
    int nodeTerm;
    int draftNumber;
    int nodesInCluster;
    private boolean freshLeader;

    byte knownLeaderId;
    int knownTerm;
    int lastCommittedDraft;
    boolean startedElection;
    int[] votedFor;

    VotingModule votingModule;
    ErrorInjectionAndMetricsModule em;

    Role role;
    NodeClock clock;

    HashMap<Byte, Boolean> votesReceived;

    BlockingQueue<RaftEntry> proposedEntries;
    BlockingQueue<Draft> receivedDrafts;
    BlockingQueue<Draft> outgoingDrafts;
    BlockingQueue<Draft> proposedDrafts;
    BlockingQueue<RaftEntry> committedLog;

    boolean catchupMode = false;
    ExecutorService executorService;
    Identity identity;
    Identity[] peers;
    MetaCollector metaCollector;

    public RaftNodeLight(int configId) throws IOException
    {
        this(DEFAULT_HEARTBEAT_TIMEOUT, DEFAULT_PORT, DEFAULT_CONFIG_FILEPATH, configId);
    }

    public RaftNodeLight(int heartbeatTimeout, int port, String configFilePath, int configId, boolean listener) throws IOException
    {
        this(heartbeatTimeout, port, configFilePath, configId);
        if(listener)
        {
            this.mode = Mode.LISTENER;
        }
    }

    public RaftNodeLight(int heartbeatTimeout, int port, String configFilePath, int configId) throws IOException
    {
        RaftNodeLight.nodes.add(this);

        Lg.l.appendToHashMap(RaftNodeLight.module, "Node");
        this.em = new ErrorInjectionAndMetricsModule();

        this.id = configId;

        Lg.l.info(RaftNodeLight.module, " [SET-UP] Node set-up has begun");

        this.metaCollector = new MetaCollector(1);
        this.heartbeatTimeout = heartbeatTimeout;
        this.nodeTerm = 0;
        this.draftNumber = 0;
        this.knownLeaderId = -1;
        this.knownTerm = -1;
        this.lastCommittedDraft = -1;
        this.startedElection = false;
        this.votedFor = new int[]{-1, -1, -1};
        this.role = Role.FOLLOWER;

        proposedEntries = new LinkedBlockingQueue<>();
        receivedDrafts = new LinkedBlockingQueue<>();
        outgoingDrafts = new LinkedBlockingQueue<>();
        proposedDrafts = new LinkedBlockingQueue<>();
        committedLog = new LinkedBlockingQueue<>();

        this.executorService = Executors.newCachedThreadPool();

        //this.votesReceived = new HashMap<>();
        discoverClusterIdentities(configFilePath, configId);

        this.votingModule = new VotingModule(this.peers.length + 1, (int)this.id);

        this.clock = new NodeClock();

        Lg.l.info(RaftNodeLight.module, " [SET-UP] RaftNodeLight was built, id: " + this.id);
    }




    public void runNode()
    {
        runNode(Mode.FULL_NODE);
    }

    public void runNode(Mode mode)
    {

        Lg.l.appendToHashMap("Receiver", "RaftNodeLight");
        Lg.l.info(RaftNodeLight.module, " Running node");

        executorService.execute(() ->
        {
            Lg.l.info(RaftNodeLight.module, " [Thread] Receiver thread started");
            Thread.currentThread().setName("Receiver");
            while(true)
            {
                if(this.slowdown)
                {
                    Lg.l.info("Receiver", "Starting receiver loop. receivedDrafts count: " +
                            this.receivedDrafts.size() + " Receiver loops: " + this.em.consumerThreadSpins);
                    try {
                        Thread.sleep(this.em.threadLoopSlowdown);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    this.em.receiverThreadSpins += 1;
                }

                if(!receivedDrafts.isEmpty())
                {
                    processDraftMessage(receivedDrafts.poll());
                }
            }
        });

//        executorService.execute(() ->
//        {
//            Lg.l.info(module, " [Thread] Consumer thread started");
//            Thread.currentThread().setName("Consumer");
//            while(true)
//            {
//                if(!receivedDrafts.isEmpty())
//                {
//                    Draft draft = receivedDrafts.poll();
//                    if(metaCollector != null)
//                    {
//                        metaCollector.markDraftReceived(draft.getTerm());
//                        metaCollector.tickMeanValue(draft.getSize());
//                    }
//
//                    if(draft.getTerm() < getNodeTerm())
//                    {
//                        continue;
//                    }
//                    else
//                    {
//                        if(draft.getTerm() > getNodeTerm())
//                        {
//                            setNodeTerm(draft.getTerm());
//                        }
//                    }
//
//                    if(draft.isHeartbeat())
//                    {
//                        //processHeartbeat(draft);
//                    }
//                    else if(draft.isVoteForCandidate())
//                    {
//                        processVoteForCandidate(draft);
//                    }
//                    else if(draft.isVoteRequest())
//                    {
//                        if(draft.getKnownDraftNumber() == knownDraftNumber && draft.getKnownTerm() == nodeTerm + 1)
//                        {
//                            grantVote(draft.getLeaderID());
//                        }
//                    }
//                }
//            }
//        });

        if(this.mode != Mode.LISTENER)
        {
            executorService.execute(() ->
            {
                Lg.l.appendToHashMap("Clock", "RaftNodeLight");
                this.clock.resetElectionTimeoutStartMoment();
                this.clock.resetHeartbeatTimeoutStartMoment();

                Lg.l.info(module, " [Thread] Clock thread started");
                Thread.currentThread().setName("Clock");
                while(true)
                {
                    if(clock.heartbeatTimeouted())
                    {
                        this.sendHeartbeat();
                    }

                    if(clock.electionTimeouted())
                    {
                        startNewElections();
                    }

//                    if(role == Role.LEADER && clock.heartbeatTimeouted())
//                    {
//                        // TODO
//                        // handleHeartbeatTimeout()
//                        clock.resetHeartbeatTimeoutStartMoment();
//                    }

                    try
                    {
                        Thread.sleep(CLOCK_SLEEP_TIME_MILIS);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    if(this.slowdown)
                    {
                        Lg.l.info("Clock", "Starting clock loop " +
                                " Clock loops: " + this.em.clockThreadSpins);
                        try {
                            Thread.sleep(this.em.threadLoopSlowdown);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        this.em.clockThreadSpins += 1;
                    }
                }
            });
        }
        else
            Lg.l.info(module, " [Thread] Clock suspended from running, Node in LISTENER mode");



        executorService.execute(() ->
        {
            Thread.currentThread().setName("Sender");
            try
            {
                Draft draft = outgoingDrafts.take();
            }
            catch (InterruptedException e)
            {
                // logger
            }
            // Send outgoing drafts
        });

//        executorService.execute(() ->
//        {
//            Thread.currentThread().setName("ConnectionManager");
//            runStreamConnectionManager();
//        });

    }

    private void sendHeartbeat()
    {
        Draft heartbeat;// = draftEmptyHeartbeat();
        if(this.freshLeader)
        {
            this.atomicStateChange(new HashMap<>()
            {{
                put("freshLeader", 0);
            }});
            heartbeat = draftEmptyHeartbeat();
        }
        else
        {
            heartbeat = draftHeartbeat();
        }
        sendDraft(heartbeat, -1);
    }

//    public void runStreamConnectionManager()
//    {
//        streamConnectionManager.run();
//    }
//
//    public BlockingQueue<Draft> getReceivedDrafts()
//    {
//        return receivedDrafts;
//    }

    public int getNodeTerm()
    {
        return nodeTerm;
    }

//    public byte getId()
//    {
//        return id;
//    }

    public void setNodeTerm(int nodeTerm)
    {
        this.nodeTerm = nodeTerm;
    }

    public Role getRole()
    {
        return role;
    }

    public void setRole(Role role)
    {
        this.role = role;
    }

    public boolean isFollower()
    {
        return role == Role.FOLLOWER;
    }

    public boolean isCandidate()
    {
        return role == Role.CANDIDATE;
    }

    public boolean isLeader()
    {
        return role == Role.LEADER;
    }

    void appendSet(int value, String key)
    {
        proposedEntries.add(new RaftEntry(RaftEntry.OperationType.SET, value, key));
    }

    void appendRemove(String key)
    {
        proposedEntries.add(new RaftEntry(RaftEntry.OperationType.REMOVE, 0, key));
    }

//    Draft prepareHeartbeatDraft()
//    {
//        //return new Draft(Draft.DraftType.HEARTBEAT, nodeTerm, id, raftEntries.toArray(new RaftEntry[raftEntries.size()]));
//    }

    void processDraftMessage(Draft draft)
    {
        // end of communication cases
        if(draft.getNodeTerm() < this.nodeTerm)
        {
            // drop the message
            return;
        }

        // If Draft's term is higher, leader is likely known - and not,s et elader it to -1
        if(draft.getNodeTerm() > this.nodeTerm)
        {
            int currentTerm = this.nodeTerm;
            int knownLeaderId = draft.getLeaderID();
            //step down
            HashMap<String, Integer> atomicMap = new HashMap<String, Integer>()
            {{
                put("addTerm", draft.getNodeTerm() - currentTerm);
                put("setRole", 0);
                put("setLeaderId", knownLeaderId);
            }};
            String context = "Bumping up current term as incoming Draft knows higher term.";
//            int draftDifference = draft.getNodeDraftNumber() - this.draftNumber;
            // This is leader's responsibility
//            if(draftDifference > 1 && draft.getAuthorId() == draft.getLeaderID())
//            {
//                atomicMap.put("setCatchupStart", this.draftNumber);
//                context += " Known Draft number is also higher - starting catchup mechanism.";
//                atomicMap.put("resetVotingModule", 1);
//            }
            atomicStateChange(atomicMap);
        }

        if(draft.getType() == Draft.DraftType.VOTE_FALSE || draft.getType() == Draft.DraftType.VOTE_FOR_CANDIDATE)
        {
            processVote(draft);
            return;
        }

        if(draft.getType() == Draft.DraftType.HEARTBEAT)
        {
            processHeartbeat(draft);
        }
        if(draft.getType() == Draft.DraftType.REQUEST_VOTE)
        {
            processVoteRequest(draft);
            return;
        }
        if(draft.getType() == Draft.DraftType.ACK && this.role == Role.LEADER)
        {
            Identity peer = findPeerOfId(draft.getAuthorId());
            peer.updateTermAndDraft(draft);
            this.catchup(peer);
        }
    }

    private void processHeartbeat(Draft draft)
    {
        if(this.knownLeaderId != draft.getLeaderID())
        {
            setKnownLeaderId(draft.getLeaderID());
        }

        if(draft.getEntriesCount() > 0)
        {
            this.processEntries(draft);

            this.sendDraft(draftAck(), this.knownLeaderId);
        }
    }

    private void processEntries(Draft draft)
    {
    }

    private void catchup(Identity peer)
    {
        //TODO: sync missing Drafts
        return;
    }

    Identity findPeerOfId(int id)
    {
        for(Identity identity : this.peers)
        {
            if(identity.getId() == id)
            {
                return identity;
            }
        }
        return null;
    }

    void startNewElections()
    {
        Lg.l.info(module, " Election timeout reached: starting new election");
        clock.resetElectionTimeoutStartMoment();
        Lg.l.info(RaftNodeLight.module, " Node switching to CANDIDATE state");

        atomicStateChange(new HashMap<String, Integer>()
        {{
            put("setLeaderId", -1);
            put("setRole", 1);
            put("addTerm", 1);
            //put("addDraftCount", 1);
            put("askedForVotes", 1);
        }});
        this.votingModule.startElectionsForMe(this.nodeTerm, this.draftNumber, this.id);
        boolean selfVote = this.votingModule.tryGrantVote(this.nodeTerm, this.draftNumber, this.id);
        remoteRequestVotes();
        Lg.l.info(RaftNodeLight.module, " Sent to all: new election request");
    }

//    put("setLeaderId", -1),
//    put("setRole", 2),
//    put("addTerm", 1),
//    put("addDraftCount", 1)
//    put("askedForVotes", 1);
//atomicMap.put("setCatchupStart", this.draftNumber);
//atomicMap.put("resetVotingModule", 1);
//    "finishedElections"
    // To make sure there is no conflict of interest between threads modifying state of node
    //put"freshLeader", 1
    private synchronized void atomicStateChange(HashMap<String, Integer> changedValues)
    {
        Lg.l.appendToHashMap("atomicStateChange", "RaftNodeLight");
        if(this.slowdown)
        {
            this.em.atomicSetCalls += 1;
            Lg.l.info("atomicStateChange", "atomicStateChange calls: " + this.em.atomicSetCalls);
        }

        if(changedValues.get("context") != null)
        {
            Lg.l.info(module, "atomicStateChange, context: " + changedValues.get("context"));
        }

        if(changedValues.get("setLeaderId") != null)
        {
            this.knownLeaderId = changedValues.get("setLeaderId").byteValue();
            Lg.l.info(module, "atomic setLeaderId: " + changedValues.get("setLeaderId").byteValue());
        }
        if(changedValues.get("setRole") != null)
        {
            this.setRole(Role.values()[changedValues.get("setRole")]);
            Lg.l.info(module, "atomic setRole: " + Role.values()[changedValues.get("setRole")]);
        }
        if(changedValues.get("addTerm") != null)
        {
            this.nodeTerm += changedValues.get("addTerm");
            Lg.l.info(module, "atomic addTerm: " + changedValues.get("addTerm"));
        }
        if(changedValues.get("addDraftCount") != null)
        {
            this.draftNumber += changedValues.get("addDraftCount");
            Lg.l.info(module, "atomic addDraftCount: " + changedValues.get("addDraftCount"));
        }
        if(changedValues.get("askedForVotes") != null)
        {
            this.startedElection = true;
            Lg.l.info(module, "atomic startedElection = true");
        }
        if(changedValues.get("finishedElections") != null)
        {
            this.startedElection = false;
            Lg.l.info(module, "atomic startedElection = false");
        }
        if(changedValues.get("setCatchupStart") != null)
        {
            this.catchupMode = true;
            Lg.l.info(module, "atomic starting catchup mode");
        }
        if(changedValues.get("resetVotingModule") != null)
        {
            this.votingModule.restartVotingModule(this.nodeTerm, this.draftNumber, -1);
        }
        if(changedValues.get("freshLeader") != null)
        {
            this.freshLeader = changedValues.get("freshLeader") == 0;
        }
        //atomicMap.put("setCatchupStart", this.draftNumber);
//atomicMap.put("resetVotingModule", 1);
    }

    private void setKnownLeaderId(int id)
    {
        HashMap<String, Integer> map = new HashMap<>()
        {{
            put("setLeaderId", id);
        }};
        atomicStateChange(map);
    }

//    private void processHeartbeat(Draft draft)
//    {
//        if(!isFollower())
//        {
//            setRole(Role.FOLLOWER);
//        }
//    }

    /**
     * Separates config file into peers and own identity.
     */
    private void discoverClusterIdentities(String configFilePath, int configId) throws FileNotFoundException
    {
        Lg.l.info(RaftNodeLight.module, " [SET-UP] Reading config file");
        LinkedList<String> myNetworkAddresses = getMyNetworkAddresses();
        LinkedList<Identity> clusterIdentities = getClusterIdentities(configFilePath);
        Identity ownIdentity = clusterIdentities.get(configId);
        setOwnIdentity(myNetworkAddresses, ownIdentity);

        //TODO make a function to cast linked list to array, class as arg
        Identity[] identityArray = new Identity[clusterIdentities.size() - 1];
        int minus = 0;
        for(int i = 0; i < clusterIdentities.size(); i++)
        {
            int k = i - minus;
            if(!(ownIdentity.getId() == i))//!(ownIdentity.getIpAddress().equals(clusterIdentities.get(i).getIpAddress())))
            {
                identityArray[k] = clusterIdentities.get(i);
            }
            else
            {
                minus += 1;
            }
        }
        this.peers = identityArray;
        Lg.l.info(RaftNodeLight.module, " [SET-UP] Built identity array");
    }

    private void setOwnIdentity(
            LinkedList<String> myNetworkAddresses,
            Identity ownIdentity
    ) throws IdentityUnknownException
    {
        boolean ownIdentitySet = false;
        for(String address : myNetworkAddresses)
        {
            if(address != null && ownIdentity != null && address.equals(ownIdentity.getIpAddress()))
            {
                this.identity = new Identity(ownIdentity);
//                clusterIdentities.remove(identity);
                ownIdentitySet = true;
                // micro-opt
                break;
            }
        }

        if(!ownIdentitySet)
        {
            throw new IdentityUnknownException(
                    "FATAL: setOwnIdentity failed; own identity not found on list of known identities"
            );
        }
    }

    private LinkedList<Identity> getClusterIdentities(String configFilePath) throws FileNotFoundException
    {
        LinkedList<Identity> identities = new LinkedList<>();

        File configFile = new File(configFilePath);
        Scanner inputStream = new Scanner(configFile);
        while(inputStream.hasNext())
        {
            String[] line = inputStream.next().split(",");
            identities.add(new Identity(line[0], Integer.parseInt(line[1]), (byte) Integer.parseInt(line[2])));
            nodesInCluster++;
            //votesReceived.put((byte) Integer.parseInt(line[2]), Boolean.FALSE);
        }

        inputStream.close();
        return identities;
    }

    private static LinkedList<String> getMyNetworkAddresses()
    {
        String ip;
        LinkedList<String> addressesList = new LinkedList<>();
        try
        {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements())
            {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if(iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements())
                {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    addressesList.add(ip);
                }
            }
        }
        catch (SocketException e)
        {
            throw new RuntimeException(e);
        }
        return addressesList;
    }

//    private Draft draftLeaderHeartbeat()
//    {
//        RaftEntry[] entries = movePendingToProposed();
//        return new Draft(
//                Draft.DraftType.HEARTBEAT,
//                id,
//                knownLeaderId,
//                nodeTerm,
//                draftNumber,
//                knownTerm,
//                knownDraftNumber,
//                entries
//        );
//    }

//    private RaftEntry[] movePendingToProposed()
//    {
//        RaftEntry[] output = new RaftEntry[proposedEntries.size()];
//        int i = 0;
//        synchronized(proposedDrafts)
//        {
//            for(RaftEntry entry : proposedEntries)
//            {
//                proposedDrafts.add(entry);
//                output[i] = entry;
//                i++;
//            }
//        }
//        proposedEntries.clear();
//        return output;
//    }

    private Draft draftNewElection()
    {
        //draftNumber++;
        return new Draft(
                Draft.DraftType.REQUEST_VOTE,
                (byte)this.id,
                (byte) -1,
                nodeTerm,
                draftNumber,
                knownTerm,
                lastCommittedDraft,
                new RaftEntry[0]
        );
    }

    private Draft draftAck()
    {
        return new Draft(
                Draft.DraftType.ACK,
                (byte)this.id,
                this.knownLeaderId,
                this.nodeTerm,
                this.draftNumber,
                this.knownTerm,
                this.lastCommittedDraft,
                new RaftEntry[0]
        );
    }

    private void grantVote(byte leaderId)
    {
        nodeTerm++;
        votedFor[0] = nodeTerm;
        votedFor[1] = leaderId;
    }

    public Draft draftGrantVote()
    {
        return new Draft(
                Draft.DraftType.VOTE_FOR_CANDIDATE,
                (byte)id,
                knownLeaderId,
                nodeTerm,
                draftNumber,
                knownTerm,
                lastCommittedDraft,
                new RaftEntry[0]
        );
    }
    public Draft draftRefuseVote()
    {
        return new Draft(
                Draft.DraftType.FALSE,
                (byte)id,
                knownLeaderId,
                nodeTerm,
                draftNumber,
                knownTerm,
                lastCommittedDraft,
                new RaftEntry[0]
        );
    }

//    private void processVoteForCandidate(Draft draft)
//    {
//        if(startedElection && draft.getTerm() == nodeTerm)
//        {
//            Lg.l.info(RaftNodeLight.module, " Received a vote for term " + draft.getTerm() + " from node of id: " + draft.getAuthorId());
//            votesReceived.put(draft.getAuthorId(), true);
//        }
//        if(hasMajorityVotes())
//        {
//            Lg.l.info(RaftNodeLight.module, " Got majority of votes - acquiring leadership");
//            acquireLeadership();
//        }
//    }

//    private void acquireLeadership()
//    {
//        role = Role.LEADER;
//        Lg.l.info(RaftNodeLight.module, " Role switched to LEADER");
//        streamConnectionManager.sendToAll(draftEmptyHeartbeat());
//        Lg.l.info(RaftNodeLight.module, " Sent first heartbeat after acquiring leadership.");
//    }

    private boolean hasMajorityVotes()
    {
        float positives = 0;
        float count = 0;
        for(Byte key : votesReceived.keySet())
        {
            if(votesReceived.get(key))
            {
                positives++;
            }
            count++;
        }
        return((positives / count) > 0.5);
    }

    private Draft draftEmptyHeartbeat()
    {
        return new Draft(
                Draft.DraftType.HEARTBEAT,
                (byte)id,
                (byte)id,
                nodeTerm,
                draftNumber,
                knownTerm,
                lastCommittedDraft,
                new RaftEntry[0]
        );
    }

    private Draft draftHeartbeat()
    {
        return new Draft(
                Draft.DraftType.HEARTBEAT,
                (byte)id,
                (byte)id,
                nodeTerm,
                draftNumber,
                knownTerm,
                lastCommittedDraft,
                new RaftEntry[0]//this.entriesForHeartbeat()
        );
    }

//    private RaftEntry[] entriesForHeartbeat()
//    {
//
//    }

    void processVote(Draft incomingDraft)
    {
        if(incomingDraft.getNodeTerm() == votingModule.myVoteTerm)
        {
            this.votingModule.addVote(incomingDraft);
            VotingCondition outcome = this.votingModule.getVotingStatus();
            // double check just in case other thread changed term in meantime
            if(outcome == VotingCondition.ACCEPTED && this.nodeTerm == votingModule.myVoteTerm)
            {
                // change internal state and
                acquireLeadership();
            }
        }
    }

    private void acquireLeadership()
    {
        Lg.l.info(module, " Acquired leadership.  Node switching to LEADER state.");

        clock.resetElectionTimeoutStartMoment();


        int myId = this.id;
        atomicStateChange(new HashMap<String, Integer>()
        {{
            put("setLeaderId", myId);
            put("setRole", 2);
            put("addTerm", 1);
            //put("addDraftCount", 1);
            put("finishedElections", 1);
            put("freshLeader", 1);
        }});
        clock.forceHeartbeat();
    }

    void processVoteRequest(Draft incomingDraft)
    {
        Draft reply;
        // TODO numer wiadomosci ostatniej zacommitowanej
        if(this.votingModule.tryGrantVote(incomingDraft.getNodeTerm(), incomingDraft.getNodeDraftNumber(), incomingDraft.getAuthorId()))
        {
            reply = draftGrantVote();
        }
        else
        {
            reply = draftRefuseVote();
        }
        sendDraft(reply, incomingDraft.getAuthorId());
    }

    public void sendDraft(Draft draft, int id)
    {
        // send to all
        if(id == -1)
        {
            for(Identity i : this.peers)
            {
                this.executorService.execute(new sendDraftToIdentity(i, draft));
                this.metaCollector.tickOutgoingMessages(i.getId());
            }
        }
        else
        {
            Identity target = null;
            for(Identity i : this.peers)
            {
                if(i.getId() == id)
                {
                    target = i;
                }
            }
            if(target != null)
            {
                this.executorService.execute(new sendDraftToIdentity(target, draft));
                this.metaCollector.tickOutgoingMessages(target.getId());
            }
        }
    }

    public void remoteRequestVotes()
    {
        sendDraft(draftNewElection(), -1);
    }

//    public String present()
//    {
//        String s = "configId: " + this.conf
//    }

    public class sendDraftToIdentity implements Runnable
    {
        Identity identity;
        Draft draft;
        public sendDraftToIdentity(Identity identity, Draft draft)
        {
            this.identity = identity;
            this.draft = draft;
        }

        @Override
        public void run()
        {
            try {
                System.out.println(clock.measureUnbound());
                new RemoteClient().deliverDraft(this.identity.getRemoteAddress(), this.draft);
            } catch (NotBoundException e) {
                e.printStackTrace();
                Lg.l.severe("Draft send exception", e.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Lg.l.severe("Draft send exception", e.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
                Lg.l.severe("Draft send exception", e.toString());
            }
        }
    }

    public void enqueue(Draft draft)
    {
        RaftNodeLight.presentMetaCounters();
        this.metaCollector.tickIncomingMessages((int)draft.getAuthorId());
        this.receivedDrafts.add(draft);
    }

}

class ErrorInjectionAndMetricsModule
{
    long draftsInOutQueue;
    long draftsInInQueue;
    long clockThreadSpins;
    long receiverThreadSpins;
    long consumerThreadSpins;
    long atomicSetCalls;

    long threadLoopSlowdown = 1000;

    public ErrorInjectionAndMetricsModule()
    {
        this.draftsInInQueue = 0;
        this.draftsInOutQueue = 0;
        this.clockThreadSpins = 0;
        this.receiverThreadSpins = 0;
        this.consumerThreadSpins = 0;
        this.atomicSetCalls = 0;
    }
}



// NO VOTES WILL COME FROM LAGGING NODES
// Otherwise, they would have to discard catch-up entries to bump up current term
class VotingModule
{



    String module = "VotingModule";

    //int[] votes;
    int myVoteTerm;
    int myVoteId;
    int myId;
    int term;
    int lastKnownDraft;
    int requestorId;
    int clusterSize;

    int myRequestTerm;
    int myRequestDraft;
    HashSet<Draft> votes;

    public VotingModule(int clusterSize, int id)
    {
        Lg.l.appendToHashMap(module, "RaftNodeLight");
        Lg.l.info(module, "Creating Voting Module");
        this.clusterSize = clusterSize;
        votes = new HashSet<>();
        myVoteId = -1;
        myVoteTerm = -1;
        myId = id;
    }

    public void restartVotingModule(int term, int lastKnownDraft, int requestorId)
    {
        Lg.l.info(module, "Resetting voting module. " +
                " term: " + term +
                " lastKnownDraftId: " + lastKnownDraft +
                " requestorId: " + requestorId +
                " Nullifying votes.");
        this.term = term;
        this.lastKnownDraft = lastKnownDraft;
        this.requestorId = requestorId;
        this.votes = new HashSet<>();
    }

    public boolean tryGrantVote(int term, int lastCommitedDraft, int id)
    {

        //todo check this condition
        if(this.myVoteTerm < term && this.lastKnownDraft <= lastCommitedDraft)
        {
            this.myVoteId = id;
            this.myVoteTerm = term;
            if(id == this.myId)
            {
                this.addVote(new Draft(
                    Draft.DraftType.VOTE_FOR_CANDIDATE,
                    (byte)this.myId,
                    (byte) -1,
                    this.myRequestTerm,
                    this.myRequestDraft,
                    -1,
                    -1,
                    new RaftEntry[0]
                ));
            }
            return true;
        }
        return false;
    }


    public void addVote(Draft vote)
    {
        this.votes.add(vote);
    }

    // -1 undefined
    // 0 definitive no
    // 1 accepted as leader
    // Run every single time when a vote comes in or incoming term is higher
    VotingCondition getVotingStatus()
    {
        int grantCount = 0;
        int refuseCount = 0;
        float threshold = (float)this.clusterSize / 2;

        for(Draft draft : this.votes)
        {
            if(draft.getType() == Draft.DraftType.VOTE_FOR_CANDIDATE)
            {
                grantCount += 1;
            }
            // if REFUSE was provided, current leader ID will also be provided
            else if(draft.getType() == Draft.DraftType.FALSE)
            {
                refuseCount += 1;
            }

            // If zeroCount > threshold, restart voting or give up if this node is the one lagging behind
            // If oneCount > threshold, acquire leadership
            // If neither, wait for more votes or term change
        }
        // TODO co to oznacza?
        if(refuseCount > threshold)
            return VotingCondition.REFSUED;
        else if(grantCount > threshold)
            return VotingCondition.ACCEPTED;
        else return VotingCondition.UNDETERMINED;
    }

    public void startElectionsForMe(int nodeTerm, int draftNumber, int id)
    {
        this.myRequestTerm = nodeTerm;
        this.myRequestDraft = draftNumber;
        this.restartVotingModule(nodeTerm, draftNumber, id);
    }
}

enum VotingCondition
{
    REFSUED,
    ACCEPTED,
    LEADER_KNOWN,
    UNDETERMINED
}