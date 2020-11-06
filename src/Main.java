import java.io.IOException;
import wenatchee.node.*;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        RaftNode node0 = new RaftNode(
                RaftNode.HEARTBEAT_TIMEOUT, 5000, "src/configuration", 0
        );
        RaftNode node1 = new RaftNode(
                RaftNode.HEARTBEAT_TIMEOUT, 5000, "src/configuration", 0
        );
        RaftNode node2 = new RaftNode(
                RaftNode.HEARTBEAT_TIMEOUT, 5000, "src/configuration", 0
        );
        node0.runNode();
        node1.runNode();
        node2.runNode();
    }
}