package wenatchee.cluster;

import wenatchee.node.RaftNode;

import java.util.ArrayList;

public class RaftCluster
{
    ArrayList<RaftNode> nodes = new ArrayList<>();

    public void addNode(RaftNode node)
    {
        nodes.add(node);
    }

    public void startCluster()
    {
        for(RaftNode node : this.nodes)
        {
            node.runNode();
        }
    }
}
