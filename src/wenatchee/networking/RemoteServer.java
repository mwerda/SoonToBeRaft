package wenatchee.networking;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RemoteServer
{
    public static void main(String[] args) throws RemoteException
    {
        Registry registry = LocateRegistry.createRegistry(5100);
        registry.rebind("hello", new RemoteServant());
        registry.rebind("deliverdraft", new RemoteServant());
    }

    public void startServer(int port) throws RemoteException
    {
        Registry registry = LocateRegistry.createRegistry(5100);
        registry.rebind("hello", new RemoteServant());
        registry.rebind("deliverdraft", new RemoteServant());
    }
}
