package obsolete;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.EnumSet;

class ReportingMulticastSocket
{
    enum VerbosityFlags
    {
        CREATE,
        INFO,
        MESSAGE_RECEIVED,
        MESSAGE_SENT,
        QUEUE_EMPTY,
        EXCEPTION,
        CLOSE;

        public static final EnumSet<VerbosityFlags> FULL_VERBOSITY_OPTIONS = EnumSet.allOf(VerbosityFlags.class);
    }

    int port;
    String multicastGroup;
    MulticastSocket multicastSocket;
    EnumSet<VerbosityFlags> verbosityOptions = VerbosityFlags.FULL_VERBOSITY_OPTIONS;


    ReportingMulticastSocket(int port, String group) throws IOException
    {
        this.port = port;
        this.multicastGroup = group;
        this.multicastSocket = new java.net.MulticastSocket(this.port);
        this.multicastSocket.joinGroup(InetAddress.getByName(this.multicastGroup));
    }

    void reportIfFlag(VerbosityFlags verbosityCondition, String info, Reporter.OutputType infoType)
    {
        if(verbosityOptions.contains(verbosityCondition))
        {
            Reporter.report(info, infoType);
        }
    }
}
