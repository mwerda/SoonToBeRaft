package wenatchee.networking;

import wenatchee.logging.Lg;

public class Identity
{
    static String module = "Identity";

    String ipAddress;
    byte id;
    int listeningPort;
    String remoteAddress;

    public Identity(String ipAddress, int listeningPort, byte id)
    {
        Lg.l.info(module, "Creating identity.");

        this.ipAddress = ipAddress;
        this.listeningPort = listeningPort;
        this.id = id;
        this.remoteAddress = "rmi://" + ipAddress + ":" + String.valueOf(listeningPort) + "/";

        Lg.l.info(module, "Created identity. Ip address: " + ipAddress + " listeningPort: "
                + listeningPort + " id: " + id + " remoteAddress: " + this.remoteAddress);
    }

    public Identity(Identity anotherIdentity)
    {
        this.ipAddress = anotherIdentity.ipAddress;
        this.listeningPort = anotherIdentity.listeningPort;
        this.id = anotherIdentity.id;
        this.remoteAddress = anotherIdentity.remoteAddress;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public byte getId()
    {
        return id;
    }

    public String getRemoteAddress() { return this.remoteAddress; }
}
