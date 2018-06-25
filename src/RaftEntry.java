import java.nio.ByteBuffer;

public class RaftEntry
{
    //TODO RaftEntry and Draft enums to class, which will be extended
    enum OperationType
    {
        SET((byte) 1),
        REMOVE((byte) 2);

        private byte value;

        OperationType(byte value)
        {
            this.value = value;
        }

        public byte getValue()
        {
            return value;
        }

        public static OperationType fromByte(byte i)
        {
            for (OperationType operationType : OperationType.values())
            {
                if (operationType.getValue() == i)
                {
                    return operationType;
                }
            }
            return null;
        }
    }

    public final int terminationSequence = Integer.MAX_VALUE;
    OperationType operationType;
    int value;
    String key;

    RaftEntry(OperationType operationType, int value, String key)
    {
        this.operationType = operationType;
        this.value = value;
        this.key = key;
    }

    ByteBuffer toByteBuffer()
    {
        //size of OperationType + size of value + size of key + termination sequence
        ByteBuffer byteBuffer = ByteBuffer.allocate(1 + 4 + key.length() + 4);
        return byteBuffer.put(operationType.value).putInt(value).put(key.getBytes()).putInt(terminationSequence);
    }

    public int getSize()
    {
        return 9 + key.length();
    }
}
