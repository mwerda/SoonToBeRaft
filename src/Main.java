import java.io.IOException;
import wenatchee.node.*;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        RaftNode node = new RaftNode(RaftNode.HEARTBEAT_TIMEOUT, 5000, "src/configuration");
    }
}