import java.io.IOException;
import java.net.*;
import java.util.EnumSet;
import java.util.LinkedList;

//TODO message contains more data than buffer allows
//TODO string to messages?
//TODO

public class MulticastSender implements Runnable
{
    enum VerbosityOptions
    {
        CREATE,
        MESSAGE_SENT,
        EXCEPTION,
        CLOSE;

        public static final EnumSet<MulticastReceiver.VerbosityOptions> FULL_VERBOSITY_OPTIONS
                = EnumSet.allOf(MulticastReceiver.VerbosityOptions.class);
    }

    int port;
    String receiverGroup;
    int ttl;
    ReportingMulticastSocket multicastSocket;

    private Buffer buffer;
    private LinkedList<String> messageQueue;
    private int interval = 0;

    VerbosityOptions verbosityOptions = VerbosityOptions.FULL_VERBOSITY_OPTIONS;

    MulticastSender(int port, java.lang.String receiverGroup, int ttl, int bufferSize) throws IOException
    {
        this.port = port;
        this.receiverGroup = receiverGroup;
        this.ttl = ttl;
        this.buffer = new Buffer(bufferSize);
        this.multicastSocket = new ReportingMulticastSocket();
        this.messageQueue = new LinkedList<String>();
        multicastSocket.joinGroup(InetAddress.getByName(receiverGroup));
    }

    MulticastSender(int port, java.lang.String receiverGroup, int ttl, int bufferSize, int interval) throws IOException
    {
        this(port, receiverGroup, ttl, bufferSize);
        this.interval = interval;
    }

    void broadcastMessage(byte[] data) throws IOException
    {
        buffer.loadBuffer(data);
        DatagramPacket packet = new DatagramPacket(
                buffer.data,
                buffer.lastUsedByteIndex,
                InetAddress.getByName(receiverGroup),
                port
        );

        multicastSocket.send(packet);
    }

    public void addMessageToQueue(String message)
    {
        messageQueue.add(message);
    }

    @Override
    public void run()
    {
        while(true)
        {
            if (!messageQueue.isEmpty())
            {
                String message = messageQueue.poll();
                byte[] data = message.getBytes();
                try
                {
                    broadcastMessage(data);
                }
                catch(IOException e)
                {
                    Reporter.reportError("Failed to broadcast message.");
                }

                if(interval != 0)
                {
                    try
                    {
                        Thread.sleep(interval);
                    }
                    catch(InterruptedException e)
                    {
                        Reporter.reportError("Sender failed to sleep for interval.\n" + e.toString());
                    }
                }
            }
        }
    }

    private void reportIfFlag(VerbosityOptions verbosityCondition, String info, Reporter.OutputType infoType)
    {
        if(verbosityOptions.contains(verbosityCondition))
        {
            Reporter.report(info, infoType);
        }
    }
}
