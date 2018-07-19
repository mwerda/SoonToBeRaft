package networking;

import protocol.Draft;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.BlockingQueue;

// TODO Reconnets

public class StreamConnectionManager implements Runnable
{
    HashMap<Byte, Identity> idToIdentityMap;
    HashMap<Byte, ClientStreamSession> idToPeerSessionMap;
    HashMap<String, Identity> addressToIdentityMap;
    ArrayList<Identity> identities;
    BlockingQueue<Draft> receivedDrafts;
    int port;
    int bufferSize;
    int peerCount;
    boolean peersConnected;

    //TODO: think about unifying identities representation as map
    public StreamConnectionManager(Identity[] identities, int port, int bufferSize, BlockingQueue<Draft> receivedDrafts) throws IOException
    {
        this.idToIdentityMap = new HashMap<>();
        this.idToPeerSessionMap = new HashMap<>();
        this.addressToIdentityMap = new HashMap<>();
        this.identities = new ArrayList<Identity>(Arrays.asList(identities));
        for(Identity identity : identities)
        {
            // TODO Guava offers bidirectional maps
            idToIdentityMap.put(identity.id, identity);
            addressToIdentityMap.put(identity.ipAddress, identity);
        }
        this.port = port;
        this.bufferSize = bufferSize;
        this.peerCount = identities.length;
        this.receivedDrafts = receivedDrafts;
        this.peersConnected = false;
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


        ClientStreamSession session = null;
        while(true)
        {
            try
            {
                if(!peersConnected)
                {
                    establishConnectionWithPeers();
                }

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

                                // If for given ID connection was created before, new connection invalidates the older;
                                // Thus, channel is closed and replaced
                                String remoteAddress = ((InetSocketAddress) client.getRemoteAddress()).getAddress().toString().replace("/", "");
                                System.out.println("Got a connection from " + remoteAddress);
                                byte id = addressToIdentityMap.get(remoteAddress).id;
                                ClientStreamSession newPeerSession = new ClientStreamSession(client, bufferSize, receivedDrafts);
                                if(idToPeerSessionMap.get(id) != null)
                                {
                                    idToPeerSessionMap.get(id).close();
                                    idToPeerSessionMap.remove(id);
                                }
                                idToPeerSessionMap.put(id, newPeerSession);

                                client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, newPeerSession);
                            }
                        }
                        else
                        {
                            session = (ClientStreamSession) key.attachment();
                            if(key.isReadable())
                            {
                                session.readAvailableDrafts();
                            } else if(key.isWritable())
                            {
                                session.sendPendingDrafts();
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
        for(Byte key : idToPeerSessionMap.keySet())
        {
            idToPeerSessionMap.get(key).addDraftToSend(draft);
        }
    }

    public void sendToId(Draft draft, byte id)
    {
        //TODO: less primitive
//        for(ClientStreamSession peerSession : peerSessions)
//        {
//            if(peerSession.id == id)
//            {
//                peerSession.addToOutgoingDrafts(draft);
//                break;
//            }
//        }
    }

    private void establishConnectionWithPeers() throws IOException
    {
        for(Identity identity : identities)
        {
            if(idToPeerSessionMap.get(identity.id) == null)
            {
                tryEstablishConnection(identity);
            }
        }
    }

    private void tryEstablishConnection(Identity identity) throws IOException
    {
        SocketChannel senderSocket = SocketChannel.open();
        senderSocket.connect(new InetSocketAddress(identity.ipAddress, port));
        ClientStreamSession newPeerSession = new ClientStreamSession(senderSocket, bufferSize, receivedDrafts);
        idToPeerSessionMap.put(identity.id, newPeerSession);
    }
}
