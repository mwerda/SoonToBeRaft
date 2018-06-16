public class Message
{
    byte[] payload;
    String address;
    int port;

    Message(byte[] payload)
    {
        this.payload = payload;
    }

    Message(byte[] payload, String address, int port)
    {
        this(payload);
        this.address = address;
        this.port = port;
    }

}
