import wenatchee.node.Node;
import wenatchee.node.RaftNodeLight;

public class NodeSetupTest
{
    public static void main (String[] args) throws InterruptedException {
        Node node0 = new Node(0, 5001, 6100, false);
        Node node1 = new Node(1, 5002, 6101, true);
        Node node2 = new Node(2, 5003, 6102, true);

        node0.startNode();
        node1.startNode();
        node2.startNode();

        RaftNodeLight[] nodeTable = {node0.raftNodeLight, node1.raftNodeLight, node2.raftNodeLight};
        NodeMetrics m = new NodeMetrics(nodeTable);

        Thread.sleep(10000000);
    }

}
class NodeMetrics
{
    RaftNodeLight[] nodes;
    public NodeMetrics(RaftNodeLight[] nodes)
    {
        this.nodes = nodes;
    }

//    public void represent()
//    {
//        for(RaftNodeLight node : this.nodes)
//        {
//            System.out.println(node.present());
//        }
//    }
}
