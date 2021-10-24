package wenatchee.node;

import wenatchee.logging.Lg;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

//TODO chain .toMilis()

public class NodeClock
{

    static int CLOCK_SLEEP_TIME_MILIS = 1;

    static String module = "NodeClock";
    static long[] electionTimeoutBounds = {3000, 4000};
    static long[] heartbeatTimeoutBounds = {1500, 2000};
    static boolean slowdown = true;
    static int slowdownMilis = 2000;

    private final long startTime;
    private long electionTimeoutStartMoment;
    private long electionTimeoutMilis;
    private long heartbeatTimeoutStartMoment;
    private long heartbeatTimeoutMilis;
    private long unboundTimer;
    private long eventTimer;
    //private long[] electionTimeoutBounds;
    private Random randomizer;


    NodeClock()
    {
        this.startTime = System.currentTimeMillis();
        this.unboundTimer = startTime;

        this.randomizer = new Random();

        this.randomizeElectionTimeout();
        this.randomizeHeartbeatTimeout();

        //this.logClockEvent("ClockCreation");
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

    public void logNodeEvent(String message)
    {
        //this.nodeEvents.add(new NodeEvent(message));
    }

    public long measureUnbound()
    {
        long elapsed = System.currentTimeMillis() - unboundTimer;
        unboundTimer = System.currentTimeMillis();
        return elapsed;
    }

    public boolean electionTimeouted()
    {
        if(getTimeToElectionTimeoutMilis() < 0)
        {
            this.logNodeEvent("ElectionTimeout");
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean heartbeatTimeouted()
    {
        if(getTimeToHeartbeatTimeoutMilis() < 0)
        {
            //this.logClockEvent("ElectionTimeout");
            return true;
        }
        else
        {
            return false;
        }
    }

    public void resetElectionTimeoutStartMoment()
    {
        electionTimeoutStartMoment = System.currentTimeMillis();
        //logClockEvent("Election timer reset");
    }

    public void resetHeartbeatTimeoutStartMoment()
    {
        heartbeatTimeoutStartMoment = System.currentTimeMillis();
        //logClockEvent("Heartbeat timer reset");
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
        //this.logClockEvent("Force Heartbeat");
        heartbeatTimeoutStartMoment = System.currentTimeMillis() - heartbeatTimeoutMilis;
        this.resetTimeouts();
    }

    protected void resetTimeouts()
    {
        //logClockEvent("Reset both timeouts");
        this.resetHeartbeatTimeoutStartMoment();
        this.resetElectionTimeoutStartMoment();
    }



//    @Override
//    public void run()
//    {
//        Lg.l.appendToHashMap("Clock", "RaftNodeLight");
//        this.resetTimeouts();
//
//        Lg.l.info(module, " [Thread] Clock thread started");
//        Thread.currentThread().setName("Clock");
//        while(true)
//        {
//            if(this.heartbeat)
//            {
//                this.sendHeartbeat();
//            }
//
//            if(clock.electionTimeouted())
//            {
//                startNewElections();
//            }
//
////                    if(role == Role.LEADER && clock.heartbeatTimeouted())
////                    {
////                        // TODO
////                        // handleHeartbeatTimeout()
////                        clock.resetHeartbeatTimeoutStartMoment();
////                    }
//
//            try
//            {
//                Thread.sleep(CLOCK_SLEEP_TIME_MILIS);
//            }
//            catch (InterruptedException e)
//            {
//                e.printStackTrace();
//            }
//
//            if(this.slowdown)
//            {
//                Lg.l.info("Clock", "Starting clock loop " +
//                        " Clock loops: " + this.em.clockThreadSpins);
//                try {
//                    Thread.sleep(this.em.threadLoopSlowdown);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                this.em.clockThreadSpins += 1;
//            }
//        }
//    }
}
