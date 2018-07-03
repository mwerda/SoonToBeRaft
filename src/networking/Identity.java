package networking;

public class Identity
{
    String ipAddress;
    byte id;
    int listeningPort;

    public Identity(String ipAddress, byte id, int listeningPort)
    {
        this.ipAddress = ipAddress;
        this.id = id;
        this.listeningPort = listeningPort;
    }

    public Identity(Identity anotherIdentity)
    {
        this.ipAddress = anotherIdentity.ipAddress;
        this.id = anotherIdentity.id;
        this.listeningPort = anotherIdentity.listeningPort;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }
}
