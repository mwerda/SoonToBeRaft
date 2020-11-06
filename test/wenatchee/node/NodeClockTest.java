package wenatchee.node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NodeClockTest
{
    @Test
    void testClock() throws InterruptedException
    {
        System.out.println("Heartbeat timeout: " + RaftNode.HEARTBEAT_TIMEOUT);
        System.out.println("Election timeout bounds: " + RaftNode.ELECTION_TIMEOUT_BOUNDS[0] + ", " + RaftNode.ELECTION_TIMEOUT_BOUNDS[1]);

        System.out.println("Creating new clock");
        NodeClock nodeClock = new NodeClock(RaftNode.ELECTION_TIMEOUT_BOUNDS, RaftNode.HEARTBEAT_TIMEOUT);
        System.out.println("Current running time nano: " + nodeClock.getRunningTime());
        System.out.println("Current running time nano: " + nodeClock.getRunningTime());
        System.out.println("Current running time milis: " + nodeClock.getRunningTimeMilis());
        System.out.println("Time to election timeout milis: " + nodeClock.getTimeToElectionTimeoutMilis());

        System.out.println("Sleeping 20 milliseconds");
        Thread.sleep(20);
        System.out.println("Time to election timeout milis: " + nodeClock.getTimeToElectionTimeoutMilis());
        System.out.println("Resetting election timeout start moment");
        nodeClock.resetElectionTimeoutStartMoment();
        System.out.println("Time to election timeout in milis: " +nodeClock.getTimeToElectionTimeoutMilis());

        System.out.println("Resetting timeout values");
        nodeClock.resetHeartbeatTimeoutStartMoment();
        nodeClock.resetElectionTimeoutStartMoment();
        System.out.println("Sleeping for more than election timeout max value");
        Thread.sleep(RaftNode.ELECTION_TIMEOUT_BOUNDS[1] + RaftNode.HEARTBEAT_TIMEOUT + 10);
        System.out.println("Assertions: time to election timeout and to hearbeat timeout < 0: "
                + nodeClock.getTimeToElectionTimeoutMilis() + " "
                + nodeClock.getTimeToHeartbeatTimeoutMilis());
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