public class Buffer
{
    byte[] data;
    int lastUsedByteIndex;

    Buffer(int size)
    {
        this.data = new byte[size];
        this.lastUsedByteIndex = -1;
    }

    void loadBuffer(byte[] newData)
    {
        int packetByteCount = newData.length > this.data.length ? this.data.length : newData.length;
        this.lastUsedByteIndex = packetByteCount;
        System.arraycopy(newData, 0, this.data, 0, packetByteCount);
    }
}