import java.io.IOException;
import java.rmi.NotBoundException;

import wenatchee.cluster.RaftCluster;
import wenatchee.networking.RemoteClient;
import wenatchee.networking.RemoteServant;
import wenatchee.networking.RemoteServer;
import wenatchee.node.Node;
import wenatchee.node.RaftNode;
import wenatchee.node.RaftNodeLight;
import wenatchee.protocol.Draft;
import wenatchee.protocol.RaftEntry;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        //Node node0 = new Node(0, 5000);
        RaftNodeLight node0 = new RaftNodeLight(200, 5000, "src/configuration", 0);
        RaftNodeLight node1 = new RaftNodeLight(200, 5000, "src/configuration", 1);
        RaftNodeLight node2 = new RaftNodeLight(200, 5000, "src/configuration", 2);

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
        Draft d =  new Draft(
                Draft.DraftType.HEARTBEAT, (byte)0, (byte)1, 2, 2, 3, 3, new RaftEntry[0]
        );
            client0.deliverDraft("rmi://169.254.204.245:5100/", d);
            client1.deliverDraft("rmi://169.254.88.42:5101/", d);
            client2.deliverDraft("rmi://169.254.186.115:5102/", d);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
//
//        RemoteClient client = new RemoteClient();
//        try
//        {
//            client.start();
//        }
//        catch(Exception e)
//        {
//            System.out.println(e.toString());
//        }
//
////        RaftNode node0 = new RaftNode(
////                RaftNode.HEARTBEAT_TIMEOUT, 5000, "src/configuration", 0
////        );
////        RaftNode node1 = new RaftNode(
////                RaftNode.HEARTBEAT_TIMEOUT, 5001, "src/configuration", 1
////        );
////        RaftNode node2 = new RaftNode(
////                RaftNode.HEARTBEAT_TIMEOUT, 5002, "src/configuration", 2
////        );
////
////        node0.runNode();
////        node1.runNode();
////        node2.runNode();
////
//////        RaftCluster cluster = new RaftCluster();
//////        cluster.addNode(node0);
//////        cluster.addNode(node1);
//////        cluster.addNode(node2);
//
//        Thread.sleep(300000);
    }
}