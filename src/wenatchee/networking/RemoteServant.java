package wenatchee.networking;
import wenatchee.logging.Lg;
import wenatchee.node.RaftNodeLight;
import wenatchee.protocol.Draft;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class RemoteServant extends UnicastRemoteObject implements MessengerService
{
    public long receivedDrafts = 0;
    static String module = "RemoteServant";
    RaftNodeLight node;

    public RemoteServant(RaftNodeLight node) throws RemoteException
    {
        super();
        this.receivedDrafts = 0;
        Lg.l.appendToHashMap(module, "Node");
        this.node = node;
    }

//    public Draft deliverDraft(Draft draft)
//    {
//        return draft;
//    }
//
//    public String sendMessage(String clientMessage) throws RemoteException
//    {
//        return "from server " + clientMessage;
//    }
//



//    public boolean appendEntries(Draft draft)
//    {
//        return node.appendEntries(draft);
//    }
//
//    // Request vote is called on remote machine from client. requestVote function takes Draft and returns Draft.
//    public Draft requestVote(Draft draft)
//    {
//        // Here, requestVote was asked from us. We are enqueueing the Draft for processing.
//        node.enqueue(draft);
//        return null;
//        // set listener
//        // either return Draft or throw exception
//    }
//
////    public boolean requestVote(Draft draft)
////    {
////
////    }

    public void deliverDraft(Draft draft) throws RemoteException
    {
        this.node.enqueue(draft);
        System.out.println(draft);
        this.receivedDrafts += 1;
        //return draft;
    }
}
