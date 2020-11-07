package wenatchee.node;

import wenatchee.logging.Lg;
import wenatchee.networking.RemoteServant;
import wenatchee.networking.RemoteServer;
import wenatchee.node.*;

import java.rmi.RemoteException;

public class Node
{
    String module;

    int configId;
    int rmiPort;
    int udpPort;

    RemoteServer rmiServer;
    RemoteServant rmiServant;

    public RaftNodeLight raftNodeLight;

    public Node(int configId, int rmiPort, int udpPort, boolean listenerNode)
    {
        module = "Node" + String.valueOf(configId);
        Lg.l.appendToHashMap(module, "");
        Lg.l.info(module, "Creating a Node. configId, rmiPort, udpPort: "
                + configId + ", " + rmiPort + ", " + udpPort
        );

        this.configId = configId;
        this.rmiPort = rmiPort;
        this.udpPort = udpPort;

        try
        {
            this.raftNodeLight = new RaftNodeLight(
                    RaftNodeLight.DEFAULT_HEARTBEAT_TIMEOUT * 100,
                    this.udpPort,
                    RaftNodeLight.DEFAULT_CONFIG_FILEPATH,
                    this.configId,
                    listenerNode
            );
        }
        catch(Exception e)
        {
            Lg.l.severe(this.module, e.toString());
            Lg.l.severe(this.module, "System can not function on failure of Raft node. Terminating.");
            return;
        }

        try 
        {
            this.rmiServant = new RemoteServant(this.raftNodeLight);
        } catch (RemoteException e) 
        {
            Lg.l.severe(this.module, e.toString());
        }
        try 
        {
            this.rmiServer = new RemoteServer(this.rmiPort, this.rmiServant);
        } catch (RemoteException e) 
        {
            Lg.l.severe(this.module, e.toString());
        }
    }

    public void startNode()
    {
        this.raftNodeLight.runNode();
    }
}
