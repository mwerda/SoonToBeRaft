import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.*;

class RaftNode
{
    enum Role
    {
        FOLLOWER,
        CANDIDATE,
        LEADER
    }

    byte id;
    int heartbeatPeriod;
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

    final static long[] ELECTION_TIMEOUT_BOUNDS = {150, 300};
    final static long HEARTBEAT_TIMEOUT = 40;

    final static int CLOCK_CHECKING_FREQUENCY = 1;

    RaftNode(byte id, int heartbeatPeriod, int port) throws IOException
    {
        this.id = id;
        this.heartbeatPeriod = heartbeatPeriod;
        this.term = 0;
        this.role = Role.FOLLOWER;

        receivedDrafts = new LinkedBlockingQueue<>();
        outgoingDrafts = new LinkedBlockingQueue<>();
        pendingChanges = new LinkedBlockingQueue<>();
        log = new LinkedBlockingQueue<>();

        this.executorService = Executors.newFixedThreadPool(4);
        //this.socket = new ServerSocket(port);
        this.clock = new NodeClock(RaftNode.ELECTION_TIMEOUT_BOUNDS, RaftNode.HEARTBEAT_TIMEOUT);
    }

    void runNode()
    {
        executorService.execute(() ->
        {
            Thread.currentThread().setName("Clock");
            while(true)
            {
                if(clock.electionTimeouted())
                {
                    // TODO
                    // HandleElectionTimeout()
                    clock.resetElectionTimeoutStartMoment();
                }

                if(role == Role.LEADER && clock.heartbeatTimeouted())
                {
                    // TODO
                    // handleHeartbeatTimeout()
                    clock.resetHeartbeatTimeoutStartMoment();
                }
            }
        });

        executorService.execute(() ->
        {
            Thread.currentThread().setName("Consumer");
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

    void sendHeartbeat()
    {

    }

    void receiveHeartbeat(byte[] message)
    {

    }
}
