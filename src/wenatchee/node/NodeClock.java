package wenatchee.node;

import java.util.Random;
import java.util.concurrent.TimeUnit;

//TODO chain .toMilis()

public class NodeClock
{
    private long startTime;
    private long electionTimeoutStartMoment;
    private long electionTimeoutMilis;
    private long heartbeatTimeoutStartMoment;
    private long heartbeatTimeoutMilis;
    private long unboundTimer;
    private long[] electionTimeoutBounds;
    private Random randomizer;


    NodeClock(long[] electionTimeoutBoundsMilis, long heartbeatTimeoutMilis)
    {
        this.randomizer = new Random();
        this.electionTimeoutBounds = electionTimeoutBoundsMilis;
        this.heartbeatTimeoutMilis = heartbeatTimeoutMilis;
        this.startTime = System.nanoTime();

        this.electionTimeoutStartMoment = startTime;
        this.heartbeatTimeoutStartMoment = startTime;
        this.unboundTimer = startTime;

        this.randomizeElectionTimeout();
    }

    public long getElectionTimeoutMilis()
    {
        return electionTimeoutMilis;
    }

    public long[] getElectionTimeoutBounds()
    {
        return electionTimeoutBounds;
    }

    public long getTimeToElectionTimeoutMilis()
    {
        return electionTimeoutMilis - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - electionTimeoutStartMoment);
    }

    public long getTimeToHeartbeatTimeoutMilis()
    {
        return heartbeatTimeoutMilis - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - heartbeatTimeoutStartMoment);
    }

    public long getRunningTime()
    {
        return System.nanoTime() - this.startTime;
    }

    public long getRunningTimeMilis()
    {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - this.startTime);
    }

    public long measureUnbound()
    {
        long elapsed = System.nanoTime() - unboundTimer;
        unboundTimer = System.nanoTime();
        return elapsed;
    }

    public boolean electionTimeouted()
    {
        return getTimeToElectionTimeoutMilis() < 0;
    }

    public boolean heartbeatTimeouted()
    {
        return getTimeToHeartbeatTimeoutMilis() < 0;
    }

    public void resetElectionTimeoutStartMoment()
    {
        electionTimeoutStartMoment = System.nanoTime();
    }

    public void resetHeartbeatTimeoutStartMoment()
    {
        heartbeatTimeoutStartMoment = System.nanoTime();
    }

    public void randomizeElectionTimeout()
    {
        electionTimeoutMilis = (Math.abs(randomizer.nextLong())
                % (electionTimeoutBounds[1] - electionTimeoutBounds[0]))
                + electionTimeoutBounds[0];
    }
}
