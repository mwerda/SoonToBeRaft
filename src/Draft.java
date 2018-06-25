import java.nio.ByteBuffer;

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

    Draft(MessageType messageType, int term, int leaderID)
    {
        this.messageType = messageType;
        this.term = term;
        this.leaderID = leaderID;
    }

    byte[] toByteArray()
    {
        int entriesSizeCounter = 0;

        for(RaftEntry entry : raftEntries)
        {
            entriesSizeCounter += entry.getSize();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(9 + entriesSizeCounter);
        byteBuffer.putShort(messageType.getValue()).putInt(term).putInt(leaderID);

        for(RaftEntry entry : raftEntries)
        {
            byteBuffer.put(entry.toByteBuffer());
        }

        return byteBuffer.array();
    }
}
