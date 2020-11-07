package wenatchee.networking;
import wenatchee.logging.Lg;
import wenatchee.node.RaftNode;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RemoteServer
{
    static String module = "RemoteServer";
//    public static void main(String[] args) throws RemoteException
//    {
//        Registry registry = LocateRegistry.createRegistry(5100);
//        registry.rebind("hello", new RemoteServant());
//        registry.rebind("deliverdraft", new RemoteServant());
//    }

    //TODO is it at all needed? RemoteServer could remain static
    //RemoteServant rmiServant

    public RemoteServer(int rmiPort, RemoteServant rmiServant) throws RemoteException
    {
        Lg.l.appendToHashMap(module, "Node");

        Lg.l.info(module, "Creating RemoteServer, binding to RMI port " + rmiPort);
        Registry registry = LocateRegistry.createRegistry(rmiPort);
        //registry.rebind("hello", new RemoteServant());
        registry.rebind("deliverdraft", rmiServant);
    }

}
