import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;

public class MulticastReceiver extends ReportingMulticastSocket implements Runnable
{
    Buffer buffer;
    LinkedList<Message> receivedMessages;

    MulticastReceiver(int port, String multicastGroup, int bufferSize) throws IOException
    {
        super(port, multicastGroup);
        this.buffer = new Buffer(bufferSize);
        this.receivedMessages = new LinkedList<Message>();
        this.verbosityOptions = VerbosityFlags.FULL_VERBOSITY_OPTIONS;
    }

    MulticastReceiver(int port, String group, int bufferSize, EnumSet<VerbosityFlags> verbositySet) throws IOException
    {
        this(port, group, bufferSize);
        this.verbosityOptions = verbositySet;
    }

    @Override
    public void run()
    {
        try
        {
            multicastSocket.setSoTimeout(1000);
        }
        catch(SocketException e)
        {
            e.printStackTrace();
        }

        while(!Thread.currentThread().isInterrupted())
        {
            DatagramPacket receivedPacket = new DatagramPacket(buffer.data, buffer.data.length);
            try
            {
                multicastSocket.receive(receivedPacket);
            }
            catch(IOException e)
            {
                // On timeout - check for interruption and exit or continue
                continue;
            }

            receivedMessages.add(new Message(
                    receivedPacket.getData(),
                    receivedPacket.getAddress().toString(),
                    receivedPacket.getPort(),
                    receivedPacket.getLength()
            ));

            nullifyBuffer();
        }

        reportIfFlag(VerbosityFlags.CLOSE, "Thread interrupted - closing receiver socket", Reporter.OutputType.INFO);
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

    private void nullifyBuffer()
    {
        Arrays.fill(buffer.data, (byte) 0);
    }
}
