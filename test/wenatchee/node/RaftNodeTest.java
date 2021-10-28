package wenatchee.node;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.HashMap;

import wenatchee.networking.RemoteClient;
import wenatchee.networking.RemoteServant;
import wenatchee.networking.RemoteServer;
import wenatchee.node.Node;
import wenatchee.node.RaftNodeLight;
import wenatchee.protocol.Draft;
import wenatchee.protocol.RaftEntry;

public class RaftNodeTest {
    @Test
    public void test1Node() throws InterruptedException {
        /**
         * Create a single node
         */
        Node node0 = new Node(0, 5001, 6100, false);
        node0.startNode();

        Thread.sleep(10000000);
    }

    @Test
    public void testLeaderElection() throws InterruptedException {
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
    public void testAtomicStateChange() throws InterruptedException {
        /**
         * Verify that atomicStateChange function can indeed change internal state of the node;
         * The assumption here is that change of the state issued as map will be seen in the fields
         */
        RaftNodeLight node0 = new Node(0, 5001, 6100, false).raftNodeLight;

        // Assertions - initial state is expected to be zeroed
        Assertions.assertTrue(node0.knownLeaderId == -1);
        Assertions.assertTrue(node0.role == RaftNodeLight.Role.FOLLOWER);
        Assertions.assertTrue(node0.getNodeTerm() == 0);

        node0.presentNodeState();
        node0.atomicStateChange(new HashMap<String, Integer>() {{
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


    @Test
    public void testRMIDelivery() throws InterruptedException, IOException {
        /**
         * A single servant is (currently) a Java RMI client. Java RMI receiver (RemoteServant) contains RaftNodeLight.
         * It will add
         */
        RaftNodeLight node0 = new RaftNodeLight(200, 5100, "src/configuration", 0);
        RaftNodeLight node1 = new RaftNodeLight(200, 5101, "src/configuration", 1);
        RaftNodeLight node2 = new RaftNodeLight(200, 5102, "src/configuration", 2);

        RemoteServant servant0 = new RemoteServant(node0);
        RemoteServant servant1 = new RemoteServant(node1);
        RemoteServant servant2 = new RemoteServant(node2);
        RemoteServer rs0 = new RemoteServer(5100, servant0);
        RemoteServer rs1 = new RemoteServer(5101, servant1);
        RemoteServer rs2 = new RemoteServer(5102, servant2);


        RemoteClient client0 = new RemoteClient();
        RemoteClient client1 = new RemoteClient();
        RemoteClient client2 = new RemoteClient();

        try {
            Draft d = new Draft(
                    Draft.DraftType.HEARTBEAT, (byte) 0, (byte) 1, 2, 2, 3, 3, new RaftEntry[0]
            );
            client0.deliverDraft("rmi://169.254.204.245:5100/", d);
            client1.deliverDraft("rmi://169.254.88.42:5101/", d);
            client2.deliverDraft("rmi://169.254.186.115:5102/", d);

            System.out.println("STRAIGHT AFTER DELIVERY NODE STATUS");
            node0.presentNodeState();
            node1.presentNodeState();
            node2.presentNodeState();

            System.out.println("10 SECONDS AFTER DELIVERY NODE STATUS");
            Thread.sleep(10000);
            node0.presentNodeState();
            node1.presentNodeState();
            node2.presentNodeState();

            System.out.println(servant0.receivedDrafts);
            System.out.println(servant1.receivedDrafts);
            System.out.println(servant2.receivedDrafts);

        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIncremental0DraftProcessingWhenDelivered() throws InterruptedException, IOException {
        /**
        * This test is based off previous testRMIDelivery.
         * Issue found in previous test is that even though the delivered Draft is enqueued in all of receiver's
         * servants, it is not picked up by periodic consumer fired up by Clock.
         **/
        RaftNodeLight node0 = new RaftNodeLight(200, 5100, "src/configuration", 0);
        RaftNodeLight node1 = new RaftNodeLight(200, 5101, "src/configuration", 1);
        RaftNodeLight node2 = new RaftNodeLight(200, 5102, "src/configuration", 2);

// commenting out as this enables old code paths
//        node0.runNode();
//        node1.runNode();
//        node2.runNode();

        RemoteServant servant0 = new RemoteServant(node0);
        RemoteServant servant1 = new RemoteServant(node1);
        RemoteServant servant2 = new RemoteServant(node2);
        RemoteServer rs0 = new RemoteServer(5100, servant0);
        RemoteServer rs1 = new RemoteServer(5101, servant1);
        RemoteServer rs2 = new RemoteServer(5102, servant2);


        RemoteClient client0 = new RemoteClient();
        RemoteClient client1 = new RemoteClient();
        RemoteClient client2 = new RemoteClient();

        try {
            Draft d = new Draft(
                    Draft.DraftType.HEARTBEAT, (byte) 0, (byte) 1, 2, 2, 3, 3, new RaftEntry[0]
            );
            client0.deliverDraft("rmi://169.254.204.245:5100/", d);
            client1.deliverDraft("rmi://169.254.88.42:5101/", d);
            client2.deliverDraft("rmi://169.254.186.115:5102/", d);
            client0.deliverDraft("rmi://169.254.204.245:5100/", d);
            client0.deliverDraft("rmi://169.254.204.245:5102/", d);


            System.out.println("STRAIGHT AFTER DELIVERY NODE STATUS");
            node0.presentNodeState();
            node1.presentNodeState();
            node2.presentNodeState();

            System.out.println("10 SECONDS AFTER DELIVERY NODE STATUS");
            Thread.sleep(10000);
            client0.deliverDraft("rmi://169.254.204.245:5100/", d);
            client0.deliverDraft("rmi://169.254.204.245:5100/", d);
            client0.deliverDraft("rmi://169.254.204.245:5100/", d);


            node0.presentNodeState();
            node1.presentNodeState();
            node2.presentNodeState();

            System.out.println(servant0.receivedDrafts);
            System.out.println(servant1.receivedDrafts);
            System.out.println(servant2.receivedDrafts);

        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNewNode() throws InterruptedException, IOException {
        /**
         * This test is based off previous testRMIDelivery.
         * Issue found in previous test is that even though the delivered Draft is enqueued in all of receiver's
         * servants, it is not picked up by periodic consumer fired up by Clock.
         **/
        RaftNodeLight node0 = new RaftNodeLight(200, 5100, "src/configuration", 0);
        RaftNodeLight node1 = new RaftNodeLight(200, 5101, "src/configuration", 1);
        RaftNodeLight node2 = new RaftNodeLight(200, 5102, "src/configuration", 2);

// commenting out as this enables old code paths
//        node0.runNode();
//        node1.runNode();
//        node2.runNode();

        RemoteServant servant0 = new RemoteServant(node0);
        RemoteServant servant1 = new RemoteServant(node1);
        RemoteServant servant2 = new RemoteServant(node2);
        RemoteServer rs0 = new RemoteServer(5100, servant0);
        RemoteServer rs1 = new RemoteServer(5101, servant1);
        RemoteServer rs2 = new RemoteServer(5102, servant2);


        RemoteClient client0 = new RemoteClient();
        RemoteClient client1 = new RemoteClient();
        RemoteClient client2 = new RemoteClient();

        try {
            Draft d = new Draft(
                    Draft.DraftType.HEARTBEAT, (byte) 0, (byte) 1, 2, 2, 3, 3, new RaftEntry[0]
            );
            client0.deliverDraft("rmi://169.254.204.245:5100/", d);
            client1.deliverDraft("rmi://169.254.88.42:5101/", d);
            client2.deliverDraft("rmi://169.254.186.115:5102/", d);
            client0.deliverDraft("rmi://169.254.204.245:5100/", d);
            client0.deliverDraft("rmi://169.254.204.245:5102/", d);


            System.out.println("STRAIGHT AFTER DELIVERY NODE STATUS");
            node0.presentNodeState();
            node1.presentNodeState();
            node2.presentNodeState();

            System.out.println("10 SECONDS AFTER DELIVERY NODE STATUS");
            Thread.sleep(10000);
            client0.deliverDraft("rmi://169.254.204.245:5100/", d);
            client0.deliverDraft("rmi://169.254.204.245:5100/", d);
            client0.deliverDraft("rmi://169.254.204.245:5100/", d);


            node0.presentNodeState();
            node1.presentNodeState();
            node2.presentNodeState();

            System.out.println(servant0.receivedDrafts);
            System.out.println(servant1.receivedDrafts);
            System.out.println(servant2.receivedDrafts);

        } catch (NotBoundException e) {
            e.printStackTrace();
        }
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
