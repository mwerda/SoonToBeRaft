package networking;

import com.sun.deploy.util.SessionState;
import protocol.Draft;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class StreamConnectionManager implements Runnable
{
    HashMap<Byte, Identity> identityMap;
    HashMap<Byte, ClientStreamSession> idToPeerSession;
    int port;

    //TODO: think about unifying identities representation as map
    public StreamConnectionManager(Identity[] identities, int port) throws IOException
    {
        identityMap = new HashMap<>();
        for(Identity identity : identities)
        {
            identityMap.put(identity.id, identity);
        }
        this.idToPeerSession = new HashMap<>();
        this.port = port;
    }


    @Override
    public void run()
    {
        Thread.currentThread().setName("ConnectionManager");
        ServerSocketChannel serverSocket = null;
        Selector selector = null;
        SelectionKey serverSelectionKey = null;
        try
        {
            selector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false);
            serverSelectionKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        while(true)
        {
            try
            {
                if(selector.select() != 0)
                {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                    while(keyIterator.hasNext())
                    {
                        SelectionKey key = keyIterator.next();
                        if(key.equals(serverSelectionKey))
                        {
                            if(key.isAcceptable())
                            {
                                //TODO serversocket responds before accept?
                                SocketChannel client = serverSocket.accept();
                                client.configureBlocking(false);
                                client.register(selector, SelectionKey.OP_READ);
                                ClientStreamSession newPeerSession = new ClientStreamSession(client, 8192);
                                //peerSessions.add(newPeerSession);

                            }
                        }
                        else
                        {
                            if (key.isReadable())
                            {
                                System.out.println();

                            } else if (key.isWritable())
                            {
                                // a channel is ready for writing
                            }
                        }
                    }
                    keyIterator.remove();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
//            try
//            {
//                ClientStreamSession newPeerSession = new ClientStreamSession(serverSocket.accept(), 2048);
//
//                peerSessions.add(newPeerSession);
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
        }
    }

    public void sendToAll(Draft draft)
    {
        for(ClientStreamSession peerSession : peerSessions)
        {
            peerSession.addToOutgoingDrafts(draft);
        }
    }

    public void sendToId(Draft draft, byte id)
    {
        //TODO: less primitive
        for(ClientStreamSession peerSession : peerSessions)
        {
            if(peerSession.id == id)
            {
                peerSession.addToOutgoingDrafts(draft);
                break;
            }
        }
    }
}
