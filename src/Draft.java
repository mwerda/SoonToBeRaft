import java.nio.ByteBuffer;

class Draft
{
    enum MessageType
    {
        APPEND_ENTRIES((byte) 1),
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
    //RaftEntry[] entries;

    Draft(MessageType messageType, int term, int leaderID)
    {
        this.messageType = messageType;
        this.term = term;
        this.leaderID = leaderID;
    }

    byte[] toByteArray()
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(9);
        byteBuffer.putShort(messageType.getValue()).putInt(term).putInt(leaderID);
        return byteBuffer.array();
    }
}
