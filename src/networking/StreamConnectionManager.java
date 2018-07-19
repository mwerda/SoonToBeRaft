package networking;

import protocol.Draft;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import org.apache.log4j.Logger;

// TODO Reconnets

public class StreamConnectionManager implements Runnable
{
    final Logger logger = Logger.getLogger(StreamConnectionManager.class);

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
        logger.info("[SET-UP] Building connection manager");
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
                    Thread t = new Thread(() ->
                    {
                        logger.info("[SET-UP] ConnManager started a new thread to connect with peers");
                        try
                        {
                            establishConnectionWithPeers();
                            peersConnected = true;
                        }
                        catch (IOException | InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        logger.info("[SET-UP] ConnManager successfully connected with peers");
                    });

                    t.start();

//                    try
//                    {
//                        establishConnectionWithPeers();
//                        peersConnected = true;
//                    }
//                    catch(Exception e)
//                    {
//                        logger.error("Cannot establish connections");
//                    }
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
                                logger.info("[N] Incoming connection from " + remoteAddress);
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

    private void establishConnectionWithPeers() throws IOException, InterruptedException
    {
        for(Identity identity : identities)
        {
            //TODO idToPeerSessionMap race condition may apply
            if(idToPeerSessionMap.get(identity.id) == null)
            {
                tryEstablishConnection(identity);
            }
        }
    }

    private void tryEstablishConnection(Identity identity) throws IOException, InterruptedException
    {
        SocketChannel senderSocket = SocketChannel.open();
        boolean connected = false;
        while(!connected)
        {
            try
            {
                senderSocket.connect(new InetSocketAddress(identity.ipAddress, port));
                connected = true;
            }
            catch(ConnectException | ClosedChannelException ce)
            {
                //System.out.println(ce);
                Thread.sleep(50);
            }
        }
        logger.info("[N] Established connection with " + identity.ipAddress);
        ClientStreamSession newPeerSession = new ClientStreamSession(senderSocket, bufferSize, receivedDrafts);
        idToPeerSessionMap.put(identity.id, newPeerSession);
    }
}
