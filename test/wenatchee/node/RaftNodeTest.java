package wenatchee.node;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

import wenatchee.node.Node;
import wenatchee.node.RaftNodeLight;

public class RaftNodeTest
{
    @Test
    public void test1Node () throws InterruptedException {
        /**
         * Create a single node
         */
        Node node0 = new Node(0, 5001, 6100, false);
        node0.startNode();

        Thread.sleep(10000000);
    }

    @Test
    public void testLeaderElection () throws InterruptedException {
        /**
         * This test verifies a setup of three node cluster
         */
        // ListenerNode - defines a node which will not try to become leader
        Node node0 = new Node(0, 5001, 6100, false);
        Node node1 = new Node(1, 5002, 6101, true);
        Node node2 = new Node(2, 5003, 6102, true);

        node0.startNode();
        node1.startNode();
        node2.startNode();

        //RaftNodeLight[] nodeTable = {node0.raftNodeLight, node1.raftNodeLight, node2.raftNodeLight};
        //NodeMetrics m = new NodeMetrics(nodeTable);

        System.out.println("Node0 is leader?" + node0.raftNodeLight.isLeader());
        System.out.println("Sleeping for 10 seconds");
        Thread.sleep(30000);
        System.out.println("Node0 is leader?" + node0.raftNodeLight.isLeader());
    }

    @Test
    public void testAtomicStateChange () throws InterruptedException {
        RaftNodeLight node0 = new Node(0, 5001, 6100, false).raftNodeLight;

        // Assertions - initial state is expected to be zeroed
        Assertions.assertTrue(node0.knownLeaderId == -1);
        Assertions.assertTrue(node0.role == RaftNodeLight.Role.FOLLOWER);
        Assertions.assertTrue(node0.getNodeTerm() == 0);

        node0.presentNodeState();
        node0.atomicStateChange(new HashMap<String, Integer>()
        {{
            put("setLeaderId", -1);
            put("setRole", 1);
            put("addTerm", 1);
            //put("addDraftCount", 1);
            //put("askedForVotes", 1);
        }});

        node0.presentNodeState();
        Assertions.assertTrue(node0.knownLeaderId == -1);
        Assertions.assertTrue(node0.role == RaftNodeLight.Role.CANDIDATE);
        Assertions.assertTrue(node0.getNodeTerm() == 1);
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
