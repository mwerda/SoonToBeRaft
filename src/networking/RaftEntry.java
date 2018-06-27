package networking; /**
 * RaftEntry contains a trace of a single change made on replicated state machine.
 * Each RaftEntry is built as follows:
 * |            4 Bytes             |          1 Byte            |   4 Bytes   | variable length |
 * |--------------------------------|----------------------------|-------------|-----------------|
 * | Whole frame length declaration | Operation type declaration |    Value    |       Key       |
 *
 * Key and value are switched in positions contrary to usual customs in order to make reading easier;
 * Last field of variable length starts at fixed position and ends at last byte of the frame
 */

import java.nio.ByteBuffer;

public class RaftEntry
{
    //TODO RaftEntry and Draft enums to class, which will be extended
    public enum OperationType
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

    static final int BYTES_PER_LENGTH_DECLARATION = Integer.SIZE / Byte.SIZE;
    static final int BYTES_PER_OPERATION_TYPE = 1;
    static final int BYTES_PER_VALUE = Integer.SIZE / Byte.SIZE;

    int size;
    OperationType operationType;
    int value;
    String key;

    public RaftEntry(OperationType operationType, int value, String key)
    {
        this.operationType = operationType;
        this.value = value;
        this.key = key;
        this.size = BYTES_PER_LENGTH_DECLARATION + BYTES_PER_OPERATION_TYPE + BYTES_PER_VALUE + key.length();
    }

    ByteBuffer toByteBuffer()
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.putInt(size).put(operationType.value).putInt(value).put(key.getBytes()).position(0);
        return byteBuffer;
    }

    public int getSize()
    {
        return BYTES_PER_LENGTH_DECLARATION + BYTES_PER_OPERATION_TYPE + BYTES_PER_VALUE + key.length();
    }

    public boolean isEquivalentTo(RaftEntry comparedRaftEntry)
    {
        return this.size == comparedRaftEntry.size
        && this.operationType == comparedRaftEntry.operationType
        && this.value == comparedRaftEntry.value
        && this.key.equals(comparedRaftEntry.key);
    }
}
