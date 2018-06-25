import java.util.LinkedList;

public class RaftNode
{
    enum Role
    {
        FOLLOWER,
        CANDIDATE,
        LEADER
    }

    LinkedList<RaftEntry> raftEntries;
    int id;


}
