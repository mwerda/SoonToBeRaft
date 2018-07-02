package obsolete;
public class Message
{
    byte[] payload;
    String address;
    int port;
    int length;

    Message(byte[] payload)
    {
        this.payload = payload;
    }

    Message(byte[] payload, String address, int port, int length)
    {
        this(payload);
        this.address = address;
        this.port = port;
        this.length = length;
    }

}
