import java.io.IOException;
import java.net.*;
import java.util.*;

//externalizable
//kopiowanie tablicy
//self4j

public class MulticastSender extends ReportingMulticastSocket implements Runnable
{
    private Buffer buffer;
    private LinkedList<String> messageQueue;
    private int interval = 0;
    private long sentCount = 0;
    private int reportingSentCountInterval = 100000;
    private long time = System.nanoTime();
    public final Object lock = new Object();

    MulticastSender(int port, String multicastGroup, int bufferSize) throws IOException
    {
        super(port, multicastGroup);
        this.buffer = new Buffer(bufferSize);
        this.messageQueue = new LinkedList<String>();
    }

    MulticastSender(int port, String multicastGroup, int bufferSize, int interval) throws IOException
    {
        this(port, multicastGroup, bufferSize);
        this.interval = interval;
    }

    MulticastSender(int port, String multicastGroup, int bufferSize, EnumSet<VerbosityFlags> verbosityOptions) throws IOException
    {
        this(port, multicastGroup, bufferSize);
        this.verbosityOptions = verbosityOptions;
    }

    MulticastSender(int port, String multicastGroup, int bufferSize, int interval, EnumSet<VerbosityFlags> verbosityOptions) throws IOException
    {
        this(port, multicastGroup, bufferSize, interval);
        this.verbosityOptions = verbosityOptions;
    }

    void broadcastMessage(byte[] data) throws IOException
    {
        buffer.loadBuffer(data);
        DatagramPacket packet = new DatagramPacket(
                buffer.data,
                buffer.lastUsedByteIndex,
                InetAddress.getByName(multicastGroup),
                port
        );
        multicastSocket.send(packet);

        sentCount++;
        if(sentCount % reportingSentCountInterval == 0)
        {
            long timeDiff = System.nanoTime() - time;
            reportIfFlag(
                    VerbosityFlags.INFO,
                    "Sent " + Long.toString(sentCount) + " messages, last " + reportingSentCountInterval + " in "
                            + timeDiff / (float) 1000000 + " ms "
                            + "with average interval of " + timeDiff / (float) 1000000 / (float) reportingSentCountInterval + " ms",
                    Reporter.OutputType.INFO
            );
            time = System.nanoTime();
        }
    }

    void addMessageToQueue(String message)
    {
        messageQueue.add(message);
    }

    @Override
    public void run()
    {
        while(!Thread.currentThread().isInterrupted())
        {
            synchronized(lock)
            {
                while(!messageQueue.isEmpty())
                {
                    String message = (String) messageQueue.poll();
                    byte[] data = message.getBytes();
                    try
                    {
                        broadcastMessage(data);
                    } catch (IOException e)
                    {
                        reportIfFlag(
                                VerbosityFlags.EXCEPTION,
                                "Broadcast failed.\n" + e.toString(),
                                Reporter.OutputType.INFO
                        );
                    }

//                    if (interval != 0)
//                    {
//                        try
//                        {
//                            Thread.sleep(interval);
//                        } catch (InterruptedException e)
//                        {
//                            reportIfFlag(
//                                    VerbosityFlags.EXCEPTION,
//                                    "Sender failed on Thread.sleep().\n" + e.toString(),
//                                    Reporter.OutputType.INFO
//                            );
//                        }
//                    }
                }
                lock.notifyAll();
            }
        }

        reportIfFlag(VerbosityFlags.CLOSE, "Thread interrupted - closing sender socket", Reporter.OutputType.INFO);
        try
        {
            multicastSocket.leaveGroup(InetAddress.getByName(multicastGroup));
        }
        catch(IOException e)
        {
            reportIfFlag(VerbosityFlags.EXCEPTION, "Unknown multicast group", Reporter.OutputType.ERROR);
        }
        multicastSocket.close();
    }
}
