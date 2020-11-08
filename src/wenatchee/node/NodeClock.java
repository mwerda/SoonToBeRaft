package wenatchee.node;

import java.util.Random;
import java.util.concurrent.TimeUnit;

//TODO chain .toMilis()

public class NodeClock
{
    static long[] electionTimeoutBounds = {3000, 4000};
    static long[] heartbeatTimeoutBounds = {1500, 2000};

    private final long startTime;
    private long electionTimeoutStartMoment;
    private long electionTimeoutMilis;
    private long heartbeatTimeoutStartMoment;
    private long heartbeatTimeoutMilis;
    private long unboundTimer;
    //private long[] electionTimeoutBounds;
    private Random randomizer;


    NodeClock()
    {
        this.randomizer = new Random();
        this.startTime = System.currentTimeMillis();

        this.electionTimeoutStartMoment = this.startTime;
        this.heartbeatTimeoutStartMoment = this.startTime;
        this.unboundTimer = startTime;

        this.randomizeElectionTimeout();
        this.randomizeHeartbeatTimeout();
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
        //return electionTimeoutMilis - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - electionTimeoutStartMoment);
        return electionTimeoutMilis - (System.currentTimeMillis() - this.electionTimeoutStartMoment);
    }

    public long getTimeToHeartbeatTimeoutMilis()
    {
        //return heartbeatTimeoutMilis - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - heartbeatTimeoutStartMoment);
        return heartbeatTimeoutMilis - (System.currentTimeMillis() - heartbeatTimeoutStartMoment);
    }

    public long getRunningTime()
    {
        return System.currentTimeMillis() - this.startTime;
    }

//    public long getRunningTimeMilis()
//    {
//        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - this.startTime);
//    }

    public long measureUnbound()
    {
        long elapsed = System.currentTimeMillis() - unboundTimer;
        unboundTimer = System.currentTimeMillis();
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
        electionTimeoutStartMoment = System.currentTimeMillis();
    }

    public void resetHeartbeatTimeoutStartMoment()
    {
        heartbeatTimeoutStartMoment = System.currentTimeMillis();
    }

    public void randomizeElectionTimeout()
    {
        this.electionTimeoutMilis = (Math.abs(randomizer.nextLong())
                % (electionTimeoutBounds[1] - electionTimeoutBounds[0]))
                + electionTimeoutBounds[0];
    }

    public void randomizeHeartbeatTimeout()
    {
        this.heartbeatTimeoutMilis = (Math.abs(randomizer.nextLong())
                % (heartbeatTimeoutBounds[1] - heartbeatTimeoutBounds[0]))
                + heartbeatTimeoutBounds[0];
    }

    public void forceHeartbeat()
    {
        heartbeatTimeoutStartMoment = System.currentTimeMillis() - heartbeatTimeoutMilis;
    }
}
