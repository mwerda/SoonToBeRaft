package wenatchee.networking;

import wenatchee.logging.Lg;
import wenatchee.protocol.Draft;

public class Identity
{
    static String module = "Identity";

    String ipAddress;
    byte id;
    int listeningPort;
    String remoteAddress;

    int termNumber;
    int draftNumber;

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

        this.termNumber = -1;
        this.draftNumber = -1;
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

    public void updateTermAndDraft(Draft draft)
    {
        this.termNumber = draft.getNodeTerm();
        this.draftNumber = draft.getNodeDraftNumber();
    }
}
