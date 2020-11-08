package wenatchee.node;

import wenatchee.networking.Identity;
import wenatchee.protocol.Draft;

import java.util.concurrent.BlockingQueue;

public class RemoteConnectionManager
{
    BlockingQueue<Draft> incomingQueue;
    BlockingQueue<Draft> outgoingQueue;
    Identity identity;

    public RemoteConnectionManager(Identity identity)
    {
        this.identity = identity;
    }
}
