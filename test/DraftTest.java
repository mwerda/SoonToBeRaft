import protocol.Draft;
import protocol.RaftEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DraftTest
{
    @Test
    void testToByteArrayAndForth()
    {
        RaftEntry[] raftEntryArray = new RaftEntry[3];
        raftEntryArray[0] = new RaftEntry(RaftEntry.OperationType.SET, 300, "test1");
        raftEntryArray[1] = new RaftEntry(RaftEntry.OperationType.REMOVE, 0, "test2");
        raftEntryArray[2] = new RaftEntry(RaftEntry.OperationType.SET, 200, "test3");

        Draft draftA = new Draft(Draft.DraftType.HEARTBEAT, 10, (byte) 12, raftEntryArray);
        Draft draftB = Draft.fromByteArray(draftA.toByteArray());
        assertTrue(draftA.isEquivalentTo(draftB));
    }

    @Test
    void testIsEquivalentTo()
    {
        Draft draftA = new Draft(Draft.DraftType.HEARTBEAT, 10, (byte) 12, new RaftEntry[0]);
        Draft draftB = new Draft(Draft.DraftType.HEARTBEAT, 10, (byte) 12, new RaftEntry[0]);
        assertTrue(draftA.isEquivalentTo(draftB));
        assertTrue(draftB.isEquivalentTo(draftA));

        Draft draftC = new Draft(Draft.DraftType.REQUEST_VOTE, 10, (byte) 12, new RaftEntry[0]);
        assertFalse(draftA.isEquivalentTo(draftC));
        assertFalse(draftC.isEquivalentTo(draftA));
    }

}