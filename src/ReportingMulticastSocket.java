import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.EnumSet;
import java.util.LinkedList;

class ReportingMulticastSocket
{
    enum VerbosityOptions
    {
        CREATE,
        MESSAGE_RECEIVED,
        MESSAGE_SENT,
        EXCEPTION,
        CLOSE;

        public static final EnumSet<MulticastReceiver.VerbosityOptions> FULL_VERBOSITY_OPTIONS = EnumSet.allOf(MulticastReceiver.VerbosityOptions.class);
    }

    int port;
    String multicastGroup;
    MulticastSocket multicastSocket;
    EnumSet<MulticastReceiver.VerbosityOptions> verbosityOptions;


    ReportingMulticastSocket(int port, String group, int bufferSize) throws IOException
    {
        this.port = port;
        this.multicastGroup = group;
        this.multicastSocket = new java.net.MulticastSocket(this.port);
        this.multicastSocket.joinGroup(InetAddress.getByName(this.multicastGroup));
        this.verbosityOptions = VerbosityOptions.FULL_VERBOSITY_OPTIONS;

        reportIfFlag(MulticastReceiver.VerbosityOptions.CREATE, "Socket created", Reporter.OutputType.INFO);
    }

    ReportingMulticastSocket(int port, String group, int bufferSize, EnumSet<VerbosityOptions> verbosityOptions) throws IOException
    {
        this(port, group, bufferSize);
        this.verbosityOptions = verbosityOptions;
    }

    private void reportIfFlag(MulticastReceiver.VerbosityOptions verbosityCondition, String info, Reporter.OutputType infoType)
    {
        if(verbosityOptions.contains(verbosityCondition))
        {
            Reporter.report(info, infoType);
        }
    }
}
