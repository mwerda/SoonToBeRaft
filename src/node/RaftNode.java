package node; /**
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

import networking.Identity;
import networking.StreamConnectionManager;
import protocol.Draft;
import protocol.RaftEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.*;

public class RaftNode
{
    enum Role
    {
        FOLLOWER,
        CANDIDATE,
        LEADER
    }

    byte id;
    int heartbeatTimeout;
    int term;

    Role role;
    LinkedList<RaftEntry> raftEntries;
    BlockingQueue<Draft> receivedDrafts;
    BlockingQueue<Draft> outgoingDrafts;
    BlockingQueue<RaftEntry> pendingChanges;
    BlockingQueue<RaftEntry> log;
    ExecutorService executorService;
    //ServerSocket socket;
    NodeClock clock;

    //ServerSocketChannel serverSocketChannel;
    StreamConnectionManager streamConnectionManager;

    Identity identity;
    Identity[] peers;

    public final static long[] ELECTION_TIMEOUT_BOUNDS = {150, 300};
    public final static int HEARTBEAT_TIMEOUT = 40;

    final static int CLOCK_SLEEP_TIME = 1;
    final static int DEFAULT_BUFFER_SIZE = 8192;

    public RaftNode(byte id, int heartbeatTimeout, int port, String configFilePath) throws IOException
    {
        this.heartbeatTimeout = heartbeatTimeout;
        this.term = 0;
        this.role = Role.FOLLOWER;

        receivedDrafts = new LinkedBlockingQueue<>();
        outgoingDrafts = new LinkedBlockingQueue<>();
        pendingChanges = new LinkedBlockingQueue<>();
        log = new LinkedBlockingQueue<>();

        this.executorService = Executors.newCachedThreadPool();
        //this.socket = new ServerSocket(port);
        this.clock = new NodeClock(RaftNode.ELECTION_TIMEOUT_BOUNDS, RaftNode.HEARTBEAT_TIMEOUT);


        discoverClusterIdentities(configFilePath);
        streamConnectionManager = new StreamConnectionManager(peers, port, DEFAULT_BUFFER_SIZE, receivedDrafts);
        //log server started
        this.id = this.identity.getId();
    }

    public void runNode()
    {
        executorService.execute(() ->
        {
            Thread.currentThread().setName("Clock");
            while(true)
            {
                if(clock.electionTimeouted())
                {
                    // TODO
                    handleElectionTimeout();
                    clock.resetElectionTimeoutStartMoment();
                }

                if(role == Role.LEADER && clock.heartbeatTimeouted())
                {
                    // TODO
                    // handleHeartbeatTimeout()
                    clock.resetHeartbeatTimeoutStartMoment();
                }

                try
                {
                    Thread.sleep(CLOCK_SLEEP_TIME);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });

        executorService.execute(() ->
        {
            Thread.currentThread().setName("Receiver");
            runStreamConnectionManager();
        });

        executorService.execute(() ->
        {
            // MOCK
            Thread.currentThread().setName("Consumer");
            int counter = 0;
            while(true)
            {
                if(!receivedDrafts.isEmpty())
                {
                    counter += 1;
                    System.out.println("Consumed " + counter + " Drafts");
                    receivedDrafts.poll();
                }
            }
        });

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

        executorService.execute(() ->
        {
            Thread.currentThread().setName("ConnectionManager");
            //while(serverSocketChannel.)
        });

    }

    void appendSet(int value, String key)
    {
        raftEntries.add(new RaftEntry(RaftEntry.OperationType.SET, value, key));
    }

    void appendRemove(String key)
    {
        raftEntries.add(new RaftEntry(RaftEntry.OperationType.REMOVE, 0, key));
    }

    Draft prepareHeartbeatDraft()
    {
        return new Draft(Draft.DraftType.HEARTBEAT, term, id, raftEntries.toArray(new RaftEntry[raftEntries.size()]));
    }

    void receiveHeartbeat(byte[] message)
    {

    }

    void handleElectionTimeout()
    {
        role = Role.CANDIDATE;
        //outgoingDrafts.add(new Draft(Draft.DraftType.REQUEST_VOTE, term))
    }

    void respondToHeartBeat()
    {

    }

    void respondToRequestVote()
    {

    }

    void sendHeartbeat()
    {

    }

    private void discoverClusterIdentities(String configFilePath) throws FileNotFoundException
    {
        LinkedList<String> interfacesAddresses = getIpAddresses();
        LinkedList<Identity> clusterIdentities = getClusterIdentities(configFilePath);

        for(Identity identity : clusterIdentities)
        {
            for(String address : interfacesAddresses)
            {
                if(address != null && identity != null && address.equals(identity.getIpAddress()))
                {
                    this.identity = new Identity(identity);
                    clusterIdentities.remove(identity);
                    break;
                }
            }
        }

        //TODO make a function to cast linked list to array, class as arg
        Identity[] identityArray = new Identity[clusterIdentities.size()];
        for(int i = 0; i < clusterIdentities.size(); i++)
        {
            identityArray[i] = clusterIdentities.get(i);
        }

        peers = identityArray;
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
        }

        inputStream.close();
        return identities;
    }

    private static LinkedList<String> getIpAddresses()
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
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements())
                {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    addressesList.add(ip);
                    //System.out.println(iface.getDisplayName() + " " + ip);
                }
            }
        }
        catch (SocketException e)
        {
            throw new RuntimeException(e);
        }
        return addressesList;
    }

    public void runStreamConnectionManager()
    {
        streamConnectionManager.run();
    }

    public BlockingQueue<Draft> getReceivedDrafts()
    {
        return receivedDrafts;
    }
}
