package wenatchee.networking;
import wenatchee.protocol.Draft;

import java.rmi.*;
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

public class RemoteServant extends UnicastRemoteObject implements MessengerService
{
    public RemoteServant() throws RemoteException
    {
        super();
    }

//    public boolean appendEntries(Draft draft)
//    {
//        //1. Reply false if term < currentTerm (§5.1)
//        //2. Reply false if log doesn’t contain an entry at prevLogIndex
//        //whose term matches prevLogTerm (§5.3)
//        //3. If an existing entry conflicts with a new one (same index
//        //but different terms), delete the existing entry and all that
//        //follow it (§5.3)
//        //4. Append any new entries not already in the log
//        //5. If leaderCommit > commitIndex, set commitIndex =
//        //min(leaderCommit, index of last new entry)
//        return false;
//    }
//
//    public boolean requestVote(Draft draft)
//    {
////        1. Reply false if term < currentTerm (§5.1)
////        2. If votedFor is null or candidateId, and candidate’s log is at
////        least as up-to-date as receiver’s log, grant vote (§5.2, §5.4)
//        return false;
//    }

    public Draft deliverDraft(Draft draft)
    {
        return draft;
    }

    public String sendMessage(String clientMessage) throws RemoteException
    {
        return "from server " + clientMessage;
    }

}
