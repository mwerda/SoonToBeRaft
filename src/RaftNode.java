import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    ExecutorService executorService;
    //ServerSocket socket;
    NodeClock clock;

    public final static long[] ELECTION_TIMEOUT_BOUNDS = {150, 300};
    public final static long HEARTBEAT_TIMEOUT = 40;

    RaftNode(byte id, int heartbeatPeriod, int port) throws IOException
    {
        this.id = id;
        this.heartbeatPeriod = heartbeatPeriod;
        this.term = 0;
        this.role = Role.FOLLOWER;
        this.executorService = Executors.newFixedThreadPool(4);
        //this.socket = new ServerSocket(port);
        this.clock = new NodeClock(RaftNode.ELECTION_TIMEOUT_BOUNDS, RaftNode.HEARTBEAT_TIMEOUT);
    }

    void runNode()
    {
        executorService.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Thread.currentThread().setName("Clock");
                while(true)
                {
                    if(clock.electionTimeouted())
                    {
                        System.out.println("Election timeouted");
                        System.out.println("Overall running time: " + clock.getRunningTimeMilis());
                        clock.resetElectionTimeoutStartMoment();
                    }
                }
            }
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
