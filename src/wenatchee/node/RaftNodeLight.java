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
import wenatchee.networking.StreamConnectionManager;
import wenatchee.protocol.Draft;
import wenatchee.protocol.RaftEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class RaftNodeLight
{
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
        LEADER
    }

    /**
     * Listener - disabled clock, never timeouts
     */
    enum Mode
    {
        FULL_NODE,
        LISTENER
    }

    static String module = "RaftNode";

    //public final static long[] ELECTION_TIMEOUT_BOUNDS = {3000, 4000};//{150, 300};
    //public final static int HEARTBEAT_TIMEOUT = 500;
    final static int CLOCK_SLEEP_TIME_MILIS = 1;
    //final static int DEFAULT_BUFFER_SIZE = 8192;
    final static int DEFAULT_HEARTBEAT_TIMEOUT = 200;
    final static int DEFAULT_PORT = 5000;
    final static String DEFAULT_CONFIG_FILEPATH = "src/configuration";
    private final static int DEFAULT_DRAFT_ELECTION_NUMBER = -1;


    int id;
    int heartbeatTimeout;
    int nodeTerm;
    int draftNumber;
    int nodesInCluster;

    byte knownLeaderId;
    int knownTerm;
    int knownDraftNumber;
    boolean startedElection;
    int[] votedFor;

    Role role;
    NodeClock clock;

    HashMap<Byte, Boolean> votesReceived;

    BlockingQueue<RaftEntry> raftEntries;
    BlockingQueue<Draft> receivedDrafts;
    BlockingQueue<Draft> outgoingDrafts;
    BlockingQueue<RaftEntry> proposedDrafts;
    BlockingQueue<RaftEntry> log;

    ExecutorService executorService;
    StreamConnectionManager streamConnectionManager;
    Identity identity;
    Identity[] peers;
    MetaCollector metaCollector;

    public RaftNodeLight(int configId) throws IOException
    {
        this(DEFAULT_HEARTBEAT_TIMEOUT, DEFAULT_PORT, DEFAULT_CONFIG_FILEPATH, configId);
    }


    public RaftNodeLight(int heartbeatTimeout, int port, String configFilePath, int configId) throws IOException
    {
        Lg.l.appendToHashMap(RaftNodeLight.module, "Node");

        this.id = configId;

        Lg.l.info(RaftNodeLight.module, " [SET-UP] Node set-up has begun");

        this.heartbeatTimeout = heartbeatTimeout;
        this.nodeTerm = 0;
        this.draftNumber = 0;
        this.knownLeaderId = -1;
        this.knownTerm = -1;
        this.knownDraftNumber = -1;
        this.startedElection = false;
        this.votedFor = new int[]{-1, -1, -1};
        this.role = Role.FOLLOWER;

        raftEntries = new LinkedBlockingQueue<>();
        receivedDrafts = new LinkedBlockingQueue<>();
        outgoingDrafts = new LinkedBlockingQueue<>();
        proposedDrafts = new LinkedBlockingQueue<>();
        log = new LinkedBlockingQueue<>();

        this.executorService = Executors.newCachedThreadPool();

        this.votesReceived = new HashMap<>();
        discoverClusterIdentities(configFilePath, configId);

        this.clock = new NodeClock(RaftNodeLight.DEFAULT_HEARTBEAT_TIMEOUT);

        Lg.l.info(RaftNodeLight.module, " [SET-UP] RaftNodeLight was built, id: " + this.id);
    }




    public void runNode()
    {
        runNode(Mode.FULL_NODE);
    }

    public void runNode(Mode mode)
    {
        Lg.l.info(RaftNodeLight.module, " Running node");

//        executorService.execute(() ->
//        {
//            Lg.l.info(RaftNodeLight.module, " [Thread] Receiver thread started");
//            Thread.currentThread().setName("Receiver");
//            runStreamConnectionManager();
//        });

        executorService.execute(() ->
        {
            Lg.l.info(module, " [Thread] Consumer thread started");
            Thread.currentThread().setName("Consumer");
            while(true)
            {
                if(!receivedDrafts.isEmpty())
                {
                    Draft draft = receivedDrafts.poll();
                    if(metaCollector != null)
                    {
                        metaCollector.markDraftReceived(draft.getTerm());
                        metaCollector.tickMeanValue(draft.getSize());
                    }

                    if(draft.getTerm() < getNodeTerm())
                    {
                        continue;
                    }
                    else
                    {
                        if(draft.getTerm() > getNodeTerm())
                        {
                            setNodeTerm(draft.getTerm());
                        }
                    }

                    if(draft.isHeartbeat())
                    {
                        //processHeartbeat(draft);
                    }
                    else if(draft.isVoteForCandidate())
                    {
                        processVoteForCandidate(draft);
                    }
                    else if(draft.isVoteRequest())
                    {
                        if(draft.getKnownDraftNumber() == knownDraftNumber && draft.getKnownTerm() == nodeTerm + 1)
                        {
                            grantVote(draft.getLeaderID());
                        }
                    }
                }
            }
        });

        if(mode != Mode.LISTENER)
        {
            executorService.execute(() ->
            {

                Lg.l.info(module, " [Thread] Clock thread started");
                Thread.currentThread().setName("Clock");
                while(true)
                {
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
                }
            });
        }
        else
            Lg.l.info(module, " [Thread] Clock suspended from running, Node in LISTENER mode");



//        executorService.execute(() ->
//        {
//            Thread.currentThread().setName("Sender");
//            try
//            {
//                Draft draft = outgoingDrafts.take();
//            }
//            catch (InterruptedException e)
//            {
//                // logger
//            }
//            // Send outgoing drafts
//        });

//        executorService.execute(() ->
//        {
//            Thread.currentThread().setName("ConnectionManager");
//            runStreamConnectionManager();
//        });

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
        raftEntries.add(new RaftEntry(RaftEntry.OperationType.SET, value, key));
    }

    void appendRemove(String key)
    {
        raftEntries.add(new RaftEntry(RaftEntry.OperationType.REMOVE, 0, key));
    }

//    Draft prepareHeartbeatDraft()
//    {
//        //return new Draft(Draft.DraftType.HEARTBEAT, nodeTerm, id, raftEntries.toArray(new RaftEntry[raftEntries.size()]));
//    }

    void startNewElections()
    {
        Lg.l.info(module, " Election timeout reached: starting new election");
        clock.resetElectionTimeoutStartMoment();
        Lg.l.info(RaftNodeLight.module, " Node switching to CANDIDATE state");
        role = Role.CANDIDATE;

        startedElection = true;
        knownLeaderId = -1;
        nodeTerm++;
        // sclitreamConnectionManager.sendToAll(draftNewElection());
        Lg.l.info(RaftNodeLight.module, " Sent to all: new election request");
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
            if(!(ownIdentity.getIpAddress().equals(clusterIdentities.get(i).getIpAddress())))
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
            votesReceived.put((byte) Integer.parseInt(line[2]), Boolean.FALSE);
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

    private RaftEntry[] movePendingToProposed()
    {
        RaftEntry[] output = new RaftEntry[raftEntries.size()];
        int i = 0;
        synchronized(proposedDrafts)
        {
            for(RaftEntry entry : raftEntries)
            {
                proposedDrafts.add(entry);
                output[i] = entry;
                i++;
            }
        }
        raftEntries.clear();
        return output;
    }

    private Draft draftNewElection()
    {
        draftNumber++;
        return new Draft(
                Draft.DraftType.REQUEST_VOTE,
                (byte)id,
                knownLeaderId,
                nodeTerm,
                draftNumber,
                knownTerm,
                knownDraftNumber,
                new RaftEntry[0]
        );
    }

    private void grantVote(byte leaderId)
    {
        nodeTerm++;
        votedFor[0] = nodeTerm;
        votedFor[1] = leaderId;
        streamConnectionManager.sendToId(draftGrantVote(leaderId), leaderId);
    }

    private Draft draftGrantVote(byte proposedLeaderId)
    {
        return new Draft(
                Draft.DraftType.VOTE_FOR_CANDIDATE,
                (byte)id,
                proposedLeaderId,
                nodeTerm,
                DEFAULT_DRAFT_ELECTION_NUMBER,
                knownTerm,
                knownDraftNumber,
                new RaftEntry[0]
        );
    }

    private void processVoteForCandidate(Draft draft)
    {
        if(startedElection && draft.getTerm() == nodeTerm)
        {
            Lg.l.info(RaftNodeLight.module, " Received a vote for term " + draft.getTerm() + " from node of id: " + draft.getAuthorId());
            votesReceived.put(draft.getAuthorId(), true);
        }
        if(hasMajorityVotes())
        {
            Lg.l.info(RaftNodeLight.module, " Got majority of votes - acquiring leadership");
            acquireLeadership();
        }
    }

    private void acquireLeadership()
    {
        role = Role.LEADER;
        Lg.l.info(RaftNodeLight.module, " Role switched to LEADER");
        streamConnectionManager.sendToAll(draftEmptyHeartbeat());
        Lg.l.info(RaftNodeLight.module, " Sent first heartbeat after acquiring leadership.");
    }

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
                knownDraftNumber,
                new RaftEntry[0]
        );
    }
}

// NO VOTES WILL COME FROM LAGGING NODES
// Otherwise, they would have to discard catch-up entries to bump up current term
class VotingModule
{
    String module = "VotingModule";

    //int[] votes;
    int term;
    int lastKnownDraft;
    int requestorId;
    int clusterSize;
    HashMap<Integer, Draft> votes;

    public VotingModule(int clusterSize)
    {
        Lg.l.appendToHashMap(module, "RaftNodeLight");
        Lg.l.info(module, "Creating Voting Module");
        this.clusterSize = clusterSize;
        votes = this.nullifyVotes();
    }

    public void(int term, int lastKnownDraft, int requestorId)
    {
        Lg.l.info(module, "Resetting voting module. " +
                " term: " + term +
                " lastKnownDraftId: " + lastKnownDraft +
                " requestorId: " + requestorId +
                " Nullifying votes.");
        this.term = term;
        this.lastKnownDraft = lastKnownDraft;
        this.requestorId = requestorId;
        this.votes = nullifyVotes();
    }

    public HashMap<Integer, Draft> nullifyVotes()
    {
        HashMap<Integer, Draft> nullMap = new HashMap<>();
        for(int i = 0; i < this.clusterSize; i++)
        {
            // This approach lets definitively know when negative answer is provided.
            nullMap.put(i, null);
        }
        return nullMap;
    }

    public void updateVote(int id, Draft vote)
    {
        this.votes.put(id, vote);
    }

    // -1 undefined
    // 0 definitive no
    // 1 accepted as leader
    // Run every single time when a vote comes in or incoming term is higher
    public int getVotingStatus()
    {
        int grantCount = 0;
        int refuseCount = 0;
        float threshold = (float)this.votes.length / 2;

        for(int vote : this.votes)
        {
            if(vote == 1)
            {
                oneCount += 1;
            }
            else if(vote == 0)
            {
                zeroCount += 1;
            }

            // If zeroCount > threshold, restart voting or give up if this node is the one lagging behind
            // If oneCount > threshold, acquire leadership
            // If neither, wait for more votes or term change
            if(zeroCount > threshold)
            {
                return RaftNodeLight.VOTES_DENIED
            }
        }
    }
}