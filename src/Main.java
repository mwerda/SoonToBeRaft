import java.io.IOException;

import wenatchee.cluster.RaftCluster;
import wenatchee.node.*;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        RaftNode node0 = new RaftNode(
                RaftNode.HEARTBEAT_TIMEOUT, 5000, "src/configuration", 0
        );
        RaftNode node1 = new RaftNode(
                RaftNode.HEARTBEAT_TIMEOUT, 5000, "src/configuration", 1
        );
        RaftNode node2 = new RaftNode(
                RaftNode.HEARTBEAT_TIMEOUT, 5000, "src/configuration", 2
        );

        node0.runNode();
        node1.runNode();
        node2.runNode();

//        RaftCluster cluster = new RaftCluster();
//        cluster.addNode(node0);
//        cluster.addNode(node1);
//        cluster.addNode(node2);

        Thread.sleep(30000);
    }
}