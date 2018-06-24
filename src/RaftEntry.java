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

        public static OperationType fromByte(short i)
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

    OperationType operationType;
    int value;
    String key;


}
