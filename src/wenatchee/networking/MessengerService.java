package wenatchee.networking;

import wenatchee.protocol.Draft;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessengerService extends Remote
{
    //public String sendMessage(String clientMessage) throws RemoteException;
    public void deliverDraft(Draft draft) throws RemoteException;
}
