package protocol;

import java.nio.ByteBuffer;
import java.util.Arrays;

//TODO control over Draft size

/**
 * Draft is a protocol utilized by current implementation of Raft algorithm.
 * Draft Heartbeat messages are built as follows:
 *
 * |  4 Bytes   |        1 Byte          |   4 Bytes   |  1 Byte   |          4 Bytes         | variable length |
 * |------------|------------------------|-------------|-----------|--------------------------|-----------------|
 * | Draft size | Draft type declaration | Term number | Leader ID | No of RaftEntry elements | RaftEntry array |
 *
 * See RaftEntry javadoc for further explanation on how it is encapsulated in Draft
 */
public class Draft
{
    private static final int BYTES_PER_SIZE = Integer.SIZE / Byte.SIZE;
    private static final int BYTES_PER_MESSAGE_TYPE = 1;
    private static final int BYTES_PER_TERM = Integer.SIZE / Byte.SIZE;
    private static final int BYTES_PER_LEADER_ID = 1;
    private static final int BYTES_PER_ENTRIES_COUNT = Integer.SIZE / Byte.SIZE;

    private static final int POSITION_SIZE = 0;
    private static final int POSITION_MESSAGE_TYPE = POSITION_SIZE + BYTES_PER_SIZE;
    private static final int POSITION_TERM = POSITION_MESSAGE_TYPE + BYTES_PER_MESSAGE_TYPE;
    private static final int POSITION_LEADER_ID = POSITION_TERM + BYTES_PER_TERM;
    private static final int POSITION_ENTRIES_COUNT = POSITION_LEADER_ID + BYTES_PER_LEADER_ID;
    private static final int POSITION_RAFT_ENTRIES = POSITION_ENTRIES_COUNT + BYTES_PER_ENTRIES_COUNT;

    public enum DraftType
    {
        HEARTBEAT((byte) 1),
        VOTE_FOR_CANDIDATE((byte) 2),
        REQUEST_VOTE((byte) 3);

        private byte value;

        DraftType(byte value)
        {
            this.value = value;
        }

        public byte getValue()
        {
            return value;
        }

        public static DraftType fromByte(byte i)
        {
            for (DraftType draftType : DraftType.values())
            {
                if (draftType.getValue() == i)
                {
                    return draftType;
                }
            }
            return null;
        }
    }

    private int size;
    private DraftType draftType;
    private int term;
    private byte leaderID;
    private RaftEntry[] raftEntries;
    private int entriesCount;

    public Draft(DraftType draftType, int term, byte leaderID, RaftEntry[] raftEntries)
    {
        int aggregatedEntriesSize = 0;
        for(RaftEntry entry : raftEntries)
        {
            aggregatedEntriesSize += entry.getSize();
        }

        this.size = BYTES_PER_SIZE
                + BYTES_PER_MESSAGE_TYPE
                + BYTES_PER_TERM
                + BYTES_PER_LEADER_ID
                + BYTES_PER_ENTRIES_COUNT
                + aggregatedEntriesSize;

        this.draftType = draftType;
        this.term = term;
        this.leaderID = leaderID;
        this.raftEntries = raftEntries;
        this.entriesCount = raftEntries.length;
    }

    public static Draft fromByteArray(byte[] array)
    {
        DraftType draftType = DraftType.fromByte(array[POSITION_MESSAGE_TYPE]);
        int term = ByteBuffer.wrap(Arrays.copyOfRange(array, POSITION_TERM, POSITION_TERM + BYTES_PER_TERM)).getInt();
        byte leaderID = array[POSITION_LEADER_ID];
        int entriesCount = ByteBuffer.wrap(
                Arrays.copyOfRange(array, POSITION_ENTRIES_COUNT, POSITION_ENTRIES_COUNT + BYTES_PER_ENTRIES_COUNT)
        ).getInt();

        RaftEntry[] decodedEntries = new RaftEntry[entriesCount];
        int currentPos = POSITION_RAFT_ENTRIES;
        for(int i = 0; i < entriesCount; i++)
        {
            int frameStartingPos = currentPos;
            int frameLength = ByteBuffer.wrap(
                    Arrays.copyOfRange(array, currentPos, currentPos + RaftEntry.BYTES_PER_LENGTH_DECLARATION)
            ).getInt();

            currentPos += RaftEntry.BYTES_PER_LENGTH_DECLARATION;
            RaftEntry.OperationType operationType = RaftEntry.OperationType.fromByte(array[currentPos]);

            currentPos += RaftEntry.BYTES_PER_OPERATION_TYPE;
            int value = ByteBuffer.wrap(
                    Arrays.copyOfRange(array, currentPos, currentPos + RaftEntry.BYTES_PER_VALUE)
            ).getInt();

            currentPos += RaftEntry.BYTES_PER_VALUE;
            String key = new String(Arrays.copyOfRange(array, currentPos, frameStartingPos + frameLength));

            decodedEntries[i] = new RaftEntry(operationType, value, key);
            currentPos = frameStartingPos + frameLength;
        }
        return new Draft(draftType, term, leaderID, decodedEntries);
    }

    public byte[] toByteArray()
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.putInt(size).put(draftType.getValue()).putInt(term).put(leaderID).putInt(entriesCount);

        for(RaftEntry entry : raftEntries)
        {
            byteBuffer.put(entry.toByteBuffer());
        }

        return byteBuffer.array();
    }

    public boolean isEquivalentTo(Draft comparedDraft)
    {
        boolean shallowEquivalence =
            this.size == comparedDraft.size
            && this.draftType == comparedDraft.draftType
            && this.term == comparedDraft.term
            && this.leaderID == comparedDraft.leaderID
            && this.entriesCount == comparedDraft.entriesCount;

        if(!shallowEquivalence)
            return false;

        for(int i = 0; i < this.entriesCount; i++)
        {
            if(!this.raftEntries[i].isEquivalentTo(comparedDraft.raftEntries[i]))
                return false;
        }
        return true;
    }

    public int getTerm()
    {
        return term;
    }

    public int getSize()
    {
        return size;
    }
}
