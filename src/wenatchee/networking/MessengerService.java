package wenatchee.networking;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessengerService extends Remote {
    public String sendMessage(String clientMessage) throws RemoteException;
}
