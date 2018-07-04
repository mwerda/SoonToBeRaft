package networking;

public class Identity
{
    String ipAddress;
    byte id;
    int listeningPort;

    public Identity(String ipAddress, int listeningPort, byte id)
    {
        this.ipAddress = ipAddress;
        this.listeningPort = listeningPort;
        this.id = id;
    }

    public Identity(Identity anotherIdentity)
    {
        this.ipAddress = anotherIdentity.ipAddress;
        this.listeningPort = anotherIdentity.listeningPort;
        this.id = anotherIdentity.id;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }
}
