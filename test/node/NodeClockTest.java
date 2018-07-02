package node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NodeClockTest
{
    @Test
    void testClock() throws InterruptedException
    {
        NodeClock nodeClock = new NodeClock(RaftNode.ELECTION_TIMEOUT_BOUNDS, RaftNode.HEARTBEAT_TIMEOUT);
        System.out.println(nodeClock.getRunningTime());
        System.out.println(nodeClock.getRunningTime());
        System.out.println(nodeClock.getRunningTimeMilis());
        System.out.println(nodeClock.getTimeToElectionTimeoutMilis());

        Thread.sleep(20);
        System.out.println(nodeClock.getTimeToElectionTimeoutMilis());
        nodeClock.resetElectionTimeoutStartMoment();
        System.out.println(nodeClock.getTimeToElectionTimeoutMilis());

        nodeClock.resetHeartbeatTimeoutStartMoment();
        nodeClock.resetElectionTimeoutStartMoment();
        Thread.sleep(RaftNode.ELECTION_TIMEOUT_BOUNDS[1] + RaftNode.HEARTBEAT_TIMEOUT + 10);
        Assertions.assertTrue(nodeClock.getTimeToElectionTimeoutMilis() < 0);
        Assertions.assertTrue(nodeClock.getTimeToHeartbeatTimeoutMilis() < 0);
        Assertions.assertTrue(nodeClock.electionTimeouted());
        Assertions.assertTrue(nodeClock.heartbeatTimeouted());

        nodeClock.resetHeartbeatTimeoutStartMoment();
        nodeClock.resetElectionTimeoutStartMoment();
        Thread.sleep(RaftNode.HEARTBEAT_TIMEOUT + 2);
        Assertions.assertTrue(nodeClock.heartbeatTimeouted());
        Assertions.assertFalse(nodeClock.electionTimeouted());
    }

    @Test
    void testRandomElectionTime()
    {
        NodeClock nodeClock = new NodeClock(RaftNode.ELECTION_TIMEOUT_BOUNDS, RaftNode.HEARTBEAT_TIMEOUT);
        for(int i = 0; i < 1000; i++)
        {
            nodeClock.randomizeElectionTimeout();
            Assertions.assertTrue(
                    nodeClock.getElectionTimeoutMilis() >= nodeClock.getElectionTimeoutBounds()[0]
                            && nodeClock.getElectionTimeoutMilis() <= nodeClock.getElectionTimeoutBounds()[1]
            );
        }
    }
}