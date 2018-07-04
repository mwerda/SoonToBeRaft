import java.io.IOException;
import node.*;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        RaftNode node = new RaftNode((byte) 1, RaftNode.HEARTBEAT_TIMEOUT, 5000, "src/configuration");
    }
}