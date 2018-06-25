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
    Role role;
    int heartbeatPeriod;
    int term;

    RaftNode(int id, int heartbeatPeriod)
    {
        this.id = id;
        this.heartbeatPeriod = heartbeatPeriod;
        role = Role.FOLLOWER;
        this.term = 0;
    }

    void appendSet(int value, String key)
    {
        raftEntries.add(new RaftEntry(RaftEntry.OperationType.SET, value, key));
    }

    void appendRemove(String key)
    {
        raftEntries.add(new RaftEntry(RaftEntry.OperationType.REMOVE, 0, key));
    }

    Draft prepareHeartbeatDraft()
    {
        return new Draft(Draft.MessageType.HEARTBEAT, term, id, raftEntries.toArray(new RaftEntry[raftEntries.size()]));
    }

    void sendHeartbeat()
    {

    }

    void receiveHeartbeat(byte[] message)
    {

    }
}
