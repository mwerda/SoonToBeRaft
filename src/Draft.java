/**
 * Draft is a protocol utilized by current implementation of Raft algorithm.
 * Draft Heartbeat messages are built as follows:
 *
 * |         1 Byte           |   4 Bytes   | 4 Bytes   | variable length |
 * |--------------------------|-------------|-----------|-----------------|
 * | Message type declaration | Term number | Leader ID | RaftEntry array |
 *
 * See RaftEntry javadoc for further explanation on how it is encapsulated in Draft
 */

import java.nio.ByteBuffer;
import java.util.Arrays;

class Draft
{
    enum MessageType
    {
        HEARTBEAT((byte) 1),
        VOTE_FOR_CANDIDATE((byte) 2),
        REQUEST_VOTE((byte) 3);

        private byte value;

        MessageType(byte value)
        {
            this.value = value;
        }

        public byte getValue()
        {
            return value;
        }

        public static MessageType fromByte(byte i)
        {
            for (MessageType messageType : MessageType.values())
            {
                if (messageType.getValue() == i)
                {
                    return messageType;
                }
            }
            return null;
        }
    }

    MessageType messageType;
    int term;
    int leaderID;
    RaftEntry[] raftEntries;

    //TODO: automate calculations
    final int BYTES_PER_MESSAGE_TYPE = 1;
    final int BYTES_PER_TERM = Integer.SIZE / Byte.SIZE;
    final int BYTES_PER_LEADER_ID = Integer.SIZE / Byte.SIZE;;

    final int POSITION_MESSAGE_TYPE = 0;
    final int POSITION_TERM = POSITION_MESSAGE_TYPE + BYTES_PER_MESSAGE_TYPE;
    final int POSITION_LEADER_ID = POSITION_TERM + BYTES_PER_TERM;
    final int POSITION_RAFT_ENTRIES = POSITION_LEADER_ID + BYTES_PER_LEADER_ID;

    Draft(MessageType messageType, int term, int leaderID, RaftEntry[] raftEntries)
    {
        this.messageType = messageType;
        this.term = term;
        this.leaderID = leaderID;
        this.raftEntries = raftEntries;
    }

    Draft(byte[] array)
    {
        MessageType messageType = MessageType.fromByte(array[POSITION_MESSAGE_TYPE]);
        int term = ByteBuffer.wrap(Arrays.copyOfRange(array, POSITION_TERM, POSITION_TERM + BYTES_PER_TERM)).getInt();
        int leaderID = ByteBuffer.wrap(Arrays.copyOfRange(array, POSITION_LEADER_ID, POSITION_LEADER_ID + BYTES_PER_LEADER_ID)).getInt();

        if(array.length > POSITION_RAFT_ENTRIES)
        {
            int i = POSITION_RAFT_ENTRIES;
            while(i < array.length)
            {
                int currentPos = i;
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

                i += frameLength;
            }

        }
    }

    byte[] toByteArray()
    {
        int entriesSizeCounter = 0;

        for(RaftEntry entry : raftEntries)
        {
            entriesSizeCounter += entry.getSize();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(9 + entriesSizeCounter);
        byteBuffer.put(messageType.getValue()).putInt(term).putInt(leaderID);

        for(RaftEntry entry : raftEntries)
        {
            byteBuffer.put(entry.toByteBuffer());
        }

        return byteBuffer.array();
    }
}
