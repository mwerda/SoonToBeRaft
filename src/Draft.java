/**
 * Draft is a protocol utilized by current implementation of Raft algorithm.
 * Draft Heartbeat messages are built as follows:
 *
 * |        1 Byte          |   4 Bytes   |  1 Byte   |          4 Bytes         | variable length |
 * |------------------------|-------------|-----------|--------------------------|-----------------|
 * | Draft type declaration | Term number | Leader ID | No of RaftEntry elements | RaftEntry array |
 *
 * See RaftEntry javadoc for further explanation on how it is encapsulated in Draft
 */

import java.nio.ByteBuffer;
import java.util.Arrays;

class Draft
{
    enum DraftType
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

    DraftType draftType;
    int term;
    byte leaderID;
    RaftEntry[] raftEntries;
    int entriesCount;

    //TODO: automate calculations
    static final int BYTES_PER_MESSAGE_TYPE = 1;
    static final int BYTES_PER_TERM = Integer.SIZE / Byte.SIZE;
    static final int BYTES_PER_LEADER_ID = 1;
    static final int BYTES_PER_ENTRIES_COUNT = Integer.SIZE / Byte.SIZE;

    static final int POSITION_MESSAGE_TYPE = 0;
    static final int POSITION_TERM = POSITION_MESSAGE_TYPE + BYTES_PER_MESSAGE_TYPE;
    static final int POSITION_LEADER_ID = POSITION_TERM + BYTES_PER_TERM;
    static final int POSITION_ENTRIES_COUNT = POSITION_LEADER_ID + BYTES_PER_LEADER_ID;
    static final int POSITION_RAFT_ENTRIES = POSITION_ENTRIES_COUNT + BYTES_PER_ENTRIES_COUNT;

    Draft(DraftType draftType, int term, byte leaderID, RaftEntry[] raftEntries)
    {
        this.draftType = draftType;
        this.term = term;
        this.leaderID = leaderID;
        this.raftEntries = raftEntries;
        this.entriesCount = raftEntries.length;
    }

    static Draft fromByteArray(byte[] array)
    {
        DraftType draftType = DraftType.fromByte(array[POSITION_MESSAGE_TYPE]);
        int term = ByteBuffer.wrap(Arrays.copyOfRange(array, POSITION_TERM, POSITION_TERM + BYTES_PER_TERM)).getInt();
        byte leaderID = array[POSITION_LEADER_ID];
        int entriesCount = ByteBuffer.wrap(
                Arrays.copyOfRange(array, POSITION_ENTRIES_COUNT, POSITION_ENTRIES_COUNT + BYTES_PER_ENTRIES_COUNT)
        ).getInt();

        RaftEntry[] decodedEntries = new RaftEntry[entriesCount];
        int currentPos = POSITION_RAFT_ENTRIES;
        for(int i = 0; i <= entriesCount; i++)
        {
            int frameStartingPos = currentPos;
            int frameLength = ByteBuffer.wrap(
                    Arrays.copyOfRange(array, i, i + RaftEntry.BYTES_PER_LENGTH_DECLARATION)
            ).getInt();

            currentPos += RaftEntry.BYTES_PER_LENGTH_DECLARATION;
            RaftEntry.OperationType operationType = RaftEntry.OperationType.fromByte(array[currentPos]);

            currentPos += RaftEntry.BYTES_PER_OPERATION_TYPE;
            int value = ByteBuffer.wrap(
                    Arrays.copyOfRange(array, currentPos, currentPos + RaftEntry.BYTES_PER_VALUE)
            ).getInt();

            currentPos += RaftEntry.BYTES_PER_VALUE;
            String key = new String(Arrays.copyOfRange(array, currentPos, i + frameLength));

            decodedEntries[i] = new RaftEntry(operationType, value, key);
            currentPos = frameStartingPos + frameLength;
        }
        return new Draft(draftType, term, leaderID, decodedEntries);
    }

    byte[] toByteArray()
    {
        int aggregatedEntriesSize = 0;

        for(RaftEntry entry : raftEntries)
        {
            aggregatedEntriesSize += entry.getSize();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(
                BYTES_PER_MESSAGE_TYPE + BYTES_PER_TERM + BYTES_PER_LEADER_ID + BYTES_PER_ENTRIES_COUNT + aggregatedEntriesSize
        );
        byteBuffer.put(draftType.getValue()).putInt(term).put(leaderID).putInt(entriesCount);

        for(RaftEntry entry : raftEntries)
        {
            byteBuffer.put(entry.toByteBuffer());
        }

        return byteBuffer.array();
    }
}
