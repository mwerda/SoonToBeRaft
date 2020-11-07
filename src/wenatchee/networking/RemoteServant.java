package wenatchee.networking;
import wenatchee.logging.Lg;
import wenatchee.node.RaftNodeLight;
import wenatchee.protocol.Draft;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class RemoteServant extends UnicastRemoteObject implements MessengerService
{
    static String module = "RemoteServant";
    RaftNodeLight node;
    public RemoteServant(RaftNodeLight node) throws RemoteException
    {
        super();
        Lg.l.appendToHashMap(module, "Node");
        this.node = node;
    }

    public Draft deliverDraft(Draft draft)
    {
        return draft;
    }

    public String sendMessage(String clientMessage) throws RemoteException
    {
        return "from server " + clientMessage;
    }

    public boolean appendEntries(Draft draft)
    {
        return node.appendEntries(draft);
    }

//    public boolean requestVote(Draft draft)
//    {
//
//    }

}
