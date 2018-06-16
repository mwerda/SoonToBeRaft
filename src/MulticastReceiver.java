import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.EnumSet;
import java.util.LinkedList;

public class MulticastReceiver implements Runnable
{
    enum VerbosityOptions
    {
        CREATE,
        MESSAGE_RECEIVED,
        EXCEPTION,
        CLOSE;

        public static final EnumSet<VerbosityOptions> FULL_VERBOSITY_OPTIONS = EnumSet.allOf(VerbosityOptions.class);
    }

    int listeningPort;
    String listeningGroup;
    MulticastSocket multicastSocket;
    Buffer buffer;
    LinkedList<Message> receivedMessages;
    EnumSet<VerbosityOptions> verbosityOptions;


    MulticastReceiver(int port, String group, int bufferSize) throws IOException
    {
        this.listeningPort = port;
        this.listeningGroup = group;
        this.multicastSocket = new MulticastSocket(this.listeningPort);
        this.buffer = new Buffer(bufferSize);
        this.multicastSocket.joinGroup(InetAddress.getByName(this.listeningGroup));
        this.receivedMessages = new LinkedList<Message>();
        this.verbosityOptions = VerbosityOptions.FULL_VERBOSITY_OPTIONS;

        reportIfFlag(VerbosityOptions.CREATE, "Receiver socket created", Reporter.OutputType.INFO);
    }

    MulticastReceiver(int port, String group, int bufferSize, EnumSet<VerbosityOptions> verbositySet) throws IOException
    {
        this(port, group, bufferSize);
        this.verbosityOptions = verbositySet;
    }

    @Override
    public void run()
    {
        DatagramPacket receivedPacket = new DatagramPacket(buffer.data, buffer.data.length);
        int i = 1;
        while(true)
        {
            try
            {
                multicastSocket.receive(receivedPacket);
            }
            catch (IOException e)
            {
                reportIfFlag(VerbosityOptions.EXCEPTION, e.toString(), Reporter.OutputType.ERROR);
            }

            receivedMessages.add(new Message(
                    receivedPacket.getData(),
                    receivedPacket.getAddress().toString(),
                    receivedPacket.getPort())
            );

            reportIfFlag(VerbosityOptions.MESSAGE_RECEIVED, "Receiver: message received", Reporter.OutputType.INFO);

        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        multicastSocket.leaveGroup(InetAddress.getByName(listeningGroup));
        multicastSocket.close();

        reportIfFlag(VerbosityOptions.CLOSE, "Receiver socket closed", Reporter.OutputType.INFO);
    }

    private void reportIfFlag(VerbosityOptions verbosityCondition, String info, Reporter.OutputType infoType)
    {
        if(verbosityOptions.contains(verbosityCondition))
        {
            Reporter.report(info, infoType);
        }
    }
}
