package wenatchee.protocol;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Draft is a protocol utilized by current implementation of Raft algorithm.
 * Draft Heartbeat messages are built as follows:
 *
 *  ___________________________________________________________________________________________________________________________________________________________________
 * |                     CURRENT MESSAGE METADATA                                 |   PREVIOUS MESSAGE COMPLIANCE CHECK   |           STATE MACHINE PAYLOAD            |
 * |______________________________________________________________________________|_______________________________________|____________________________________________|
 * |  4 Bytes   |    1 Byte   |        1 Byte          |   4 Bytes   |  1 Byte   |   4 Bytes    |      4 Bytes     |       4 Bytes      |          4 Bytes         | variable length |
 * |------------|-------------|------------------------|-------------|-----------|--------------|------------------|--------------------|--------------------------|-----------------|
 * | Draft size | Author's ID | Draft type declaration | Term number | Leader ID | Draft number | Known Draft term | Known Draft number | No of RaftEntry elements | RaftEntry array |
 * |-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
 *
 * Overall 27 bytes at minimum. See RaftEntry javadoc for further explanation on how it is encapsulated in Draft.
 * 4 bytes describing Draft number are enough for 397 hours of communication with 1500 new Drafts emerging every second.
 */
public class Draft implements Serializable
{
    private static final int BYTES_PER_SIZE = Integer.SIZE / Byte.SIZE;
    private static final int BYTES_PER_AUTHOR_ID = 1;
    private static final int BYTES_PER_MESSAGE_TYPE = 1;
    private static final int BYTES_PER_TERM = Integer.SIZE / Byte.SIZE;
    private static final int BYTES_PER_LEADER_ID = 1;
    private static final int BYTES_PER_DRAFT_NUMBER = Integer.SIZE / Byte.SIZE;
    private static final int BYTES_PER_KNOWN_TERM_NUMBER = Integer.SIZE / Byte.SIZE;
    private static final int BYTES_PER_KNOWN_DRAFT_NUMBER = Integer.SIZE / Byte.SIZE;
    private static final int BYTES_PER_ENTRIES_COUNT = Integer.SIZE / Byte.SIZE;

    private static final int POSITION_SIZE = 0;
    private static final int POSITION_AUTHOR_ID = POSITION_SIZE + BYTES_PER_SIZE;
    private static final int POSITION_MESSAGE_TYPE = POSITION_SIZE + BYTES_PER_SIZE;
    private static final int POSITION_LEADER_ID = POSITION_MESSAGE_TYPE + BYTES_PER_MESSAGE_TYPE;
    private static final int POSITION_TERM = POSITION_LEADER_ID + BYTES_PER_LEADER_ID;
    private static final int POSITION_DRAFT_NUMBER = POSITION_TERM + BYTES_PER_TERM;
    private static final int POSITION_KNOWN_TERM_NUMBER = POSITION_DRAFT_NUMBER + BYTES_PER_DRAFT_NUMBER;
    private static final int POSITION_KNOWN_DRAFT_NUMBER = POSITION_KNOWN_TERM_NUMBER + BYTES_PER_KNOWN_TERM_NUMBER;
    private static final int POSITION_ENTRIES_COUNT = POSITION_KNOWN_DRAFT_NUMBER + BYTES_PER_KNOWN_DRAFT_NUMBER;
    private static final int POSITION_RAFT_ENTRIES = POSITION_ENTRIES_COUNT + BYTES_PER_ENTRIES_COUNT;

    public enum DraftType
    {
        HEARTBEAT((byte) 1),
        VOTE_FOR_CANDIDATE((byte) 2),
        REQUEST_VOTE((byte) 3),
        VOTE_FALSE((byte) 4),
        FALSE((byte) 5),
        ACK((byte) 6);

        public byte value;

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
    private byte authorId;
    private DraftType draftType;
    private byte leaderID;
    private int nodeTerm;
    private int nodeDraftNumber;
    private int knownTermLEGACY;
    private int knownDraftNumberLEGACY;
    private RaftEntry[] raftEntries;
    private int entriesCount;

    public Draft(DraftType draftType, byte authorId, byte leaderID, int term, int nodeDraftNumber, int knownTermLEGACY, int knownDraftNumberLEGACY, RaftEntry[] raftEntries)
    {
        int aggregatedEntriesSize = 0;
        for(RaftEntry entry : raftEntries)
        {
            aggregatedEntriesSize += entry.getSize();
        }

        this.size = BYTES_PER_SIZE
                + BYTES_PER_AUTHOR_ID
                + BYTES_PER_MESSAGE_TYPE
                + BYTES_PER_TERM
                + BYTES_PER_LEADER_ID
                + BYTES_PER_DRAFT_NUMBER
                + BYTES_PER_KNOWN_TERM_NUMBER
                + BYTES_PER_KNOWN_DRAFT_NUMBER
                + BYTES_PER_ENTRIES_COUNT
                + aggregatedEntriesSize;

        this.draftType = draftType;
        this.authorId = authorId;
        this.nodeTerm = term;
        this.leaderID = leaderID;
        this.nodeDraftNumber = nodeDraftNumber;
        this.knownTermLEGACY = knownTermLEGACY;
        this.knownDraftNumberLEGACY = knownDraftNumberLEGACY;
        this.raftEntries = raftEntries;
        this.entriesCount = raftEntries.length;
    }

    public static Draft fromByteArray(byte[] array)
    {
        DraftType draftType = DraftType.fromByte(array[POSITION_MESSAGE_TYPE]);
        byte authorId = array[POSITION_AUTHOR_ID];
        byte leaderId = array[POSITION_LEADER_ID];
        int term = ByteBuffer.wrap(Arrays.copyOfRange(array, POSITION_TERM, POSITION_TERM + BYTES_PER_TERM)).getInt();
        int draftNumber = ByteBuffer.wrap(Arrays.copyOfRange(array, POSITION_DRAFT_NUMBER, POSITION_DRAFT_NUMBER + BYTES_PER_DRAFT_NUMBER)).getInt();
        int knownTerm = ByteBuffer.wrap(Arrays.copyOfRange(array, POSITION_KNOWN_TERM_NUMBER, POSITION_KNOWN_TERM_NUMBER + BYTES_PER_KNOWN_TERM_NUMBER)).getInt();
        int knownDraftNumber = ByteBuffer.wrap(Arrays.copyOfRange(array, POSITION_KNOWN_DRAFT_NUMBER, POSITION_KNOWN_DRAFT_NUMBER + BYTES_PER_KNOWN_DRAFT_NUMBER)).getInt();
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
        return new Draft(draftType, authorId, leaderId, term, draftNumber, knownTerm, knownDraftNumber, decodedEntries);
    }

    public byte[] toByteArray()
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer
                .putInt(size)
                .put(draftType.getValue())
                .put(leaderID)
                .putInt(nodeTerm)
                .putInt(nodeDraftNumber)
                .putInt(knownTermLEGACY)
                .putInt(knownDraftNumberLEGACY)
                .putInt(entriesCount);

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
            && this.nodeTerm == comparedDraft.nodeTerm
            && this.leaderID == comparedDraft.leaderID
            && this.nodeDraftNumber == comparedDraft.nodeDraftNumber
            && this.knownTermLEGACY == comparedDraft.knownTermLEGACY
            && this.knownDraftNumberLEGACY == comparedDraft.knownDraftNumberLEGACY
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

    public int getEntriesCount()
    {
        return this.entriesCount;
    }

    public DraftType getType()
    {
        return draftType;
    }

    public int getNodeTerm()
    {
        return nodeTerm;
    }

    public int getSize()
    {
        return size;
    }

    public boolean isHeartbeat()
    {
        return draftType == DraftType.HEARTBEAT;
    }

    public boolean isVoteForCandidate()
    {
        return draftType == DraftType.VOTE_FOR_CANDIDATE;
    }

    public boolean isVoteRequest()
    {
        return draftType == DraftType.REQUEST_VOTE;
    }

    public byte getAuthorId()
    {
        return authorId;
    }

    public int getNodeDraftNumber()
    {
        return nodeDraftNumber;
    }

    public int getKnownDraftNumberLEGACY()
    {
        return knownDraftNumberLEGACY;
    }

    public int getKnownTermLEGACY()
    {
        return knownTermLEGACY;
    }

    public byte getLeaderID()
    {
        return leaderID;
    }
}
