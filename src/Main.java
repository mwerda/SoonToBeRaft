import java.io.IOException;

import wenatchee.cluster.RaftCluster;
import wenatchee.networking.RemoteClient;
import wenatchee.networking.RemoteServant;
import wenatchee.networking.RemoteServer;
import wenatchee.node.RaftNodeLight;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
//        int port = 5100;
//        RemoteServant servant = new RemoteServant();
//        RemoteServer.startServer(port);
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