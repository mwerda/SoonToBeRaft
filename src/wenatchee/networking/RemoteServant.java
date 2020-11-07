package wenatchee.networking;
import java.rmi.*;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
//
//public class MessengerServiceImpl implements MessengerService {
//
//    @Override
//    public String sendMessage(String clientMessage) {
//        return "Client Message".equals(clientMessage) ? "Server Message" : null;
//    }
//
//    public String unexposedMethod() { /* code */ }
//}

public class RemoteServer extends UnicastRemoteObject implements MessengerService
{
    @Override
    public String sendMessage(String clientMessage) throws RemoteException
    {
        super();
    }

    public RemoteServer
}
