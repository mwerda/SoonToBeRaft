package wenatchee.networking;

import wenatchee.node.RaftNode;
import wenatchee.protocol.Draft;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

// TODO Reconnets

public class StreamConnectionManager implements Runnable
{
    HashMap<Byte, Identity> idToIdentityMap;
    HashMap<Byte, ClientStreamSession> idToPeerSessionMap;
    HashMap<String, Identity> addressToIdentityMap;
    ArrayList<Identity> remoteIdentities;
    BlockingQueue<Draft> receivedDrafts;
    ServerSocketChannel serverSocket;
    Selector selector;
    SelectionKey serverSelectionKey;
    int port;
    int bufferSize;
    int peerCount;
    int connectedPeerCount;
    boolean peersConnected;
    Logger logger;
    Identity ownIdentity;

    public void getInfo()
    {
        return;
    }

    //TODO: think about unifying remoteIdentities representation as map
    public StreamConnectionManager(
            Identity[] remoteIdentities, Identity ownIdentity, int port, int bufferSize,
            BlockingQueue<Draft> receivedDrafts, Logger logger
    ) throws IOException
    {
        this.ownIdentity = ownIdentity;
        this.logger = logger;
        this.idToIdentityMap = new HashMap<>();
        this.idToPeerSessionMap = new HashMap<>();
        this.addressToIdentityMap = new HashMap<>();
        this.remoteIdentities = new ArrayList<Identity>(Arrays.asList(remoteIdentities));
        for(Identity identity : remoteIdentities)
        {
            // TODO Guava offers bidirectional maps
            idToIdentityMap.put(identity.id, identity);
            addressToIdentityMap.put(identity.ipAddress, identity);
        }
        this.port = port;
        this.bufferSize = bufferSize;
        this.peerCount = remoteIdentities.length;
        this.receivedDrafts = receivedDrafts;
        // TODO multithreading race condition
        this.connectedPeerCount = 0;
        this.peersConnected = false;

        serverSocket = null;
        selector = null;
        serverSelectionKey = null;
        try
        {
            selector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(this.ownIdentity.getIpAddress(), port));
            serverSocket.configureBlocking(false);
            serverSelectionKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Thread t = new Thread(() ->
        {
            this.logger.info(this.ownIdentity.getIpAddress() + ": [SET-UP] ConnManager started a new thread to connect with peers");
            try
            {
                establishConnectionWithPeers();
                peersConnected = true;
            }
            catch (IOException | InterruptedException e)
            {
                e.printStackTrace();
            }
            this.logger.info(this.ownIdentity.getIpAddress() + ": [SET-UP] ConnManager successfully connected with peers");
            this.logger.info(this.ownIdentity.getIpAddress() + ": [SET-UP] Closing connector, since connections are created");
        });

        t.start();
    }

    @Override
    public void run()
    {
        Thread.currentThread().setName("ConnectionManager");
//        ServerSocketChannel serverSocket = null;
//        Selector selector = null;
//        SelectionKey serverSelectionKey = null;
//        try
//        {
//            selector = Selector.open();
//            serverSocket = ServerSocketChannel.open();
//            serverSocket.bind(new InetSocketAddress(port));
//            serverSocket.configureBlocking(false);
//            serverSelectionKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }

        //In response to
        ClientStreamSession session = null;
        int ctr = 0;
        while(true)
        {
            try
            {
                if(selector.select(100) != 0)
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
                                //here socketchannel is created
                                SocketChannel client = serverSocket.accept();
                                client.configureBlocking(false);

                                // If for given ID connection was created before, new connection invalidates the older;
                                // Thus, channel is closed and replaced
                                String remoteAddress = ((InetSocketAddress) client.getRemoteAddress()).getAddress().toString().replace("/", "");
                                this.logger.info(this.ownIdentity.getIpAddress() + ": [N] Incoming connection from " + remoteAddress);
                                byte id = this.addressToIdentityMap.get(remoteAddress).id;
                                ClientStreamSession newPeerSession = new ClientStreamSession(client, bufferSize, receivedDrafts);
                                if(idToPeerSessionMap.get(id) != null)
                                {
                                    idToPeerSessionMap.get(id).close();
                                    idToPeerSessionMap.remove(id);
                                }
                                idToPeerSessionMap.put(id, newPeerSession);
                                connectedPeerCount += 1;
                                client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, newPeerSession);
                            }
                        }
                        else
                        {
                            session = (ClientStreamSession) key.attachment();
                            if(key.isReadable())
                            {
                                session.readAvailableDrafts();
                            }
                            else if(key.isWritable())
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
        for(Identity identity : this.remoteIdentities)
        {
            //TODO idToPeerSessionMap race condition may apply
            if(idToPeerSessionMap.get(identity.id) == null)
            {
                tryEstablishConnection(identity);
            }
        }

    }


    // Albo nie Powazny blad, socketchannel jest otwierany na wejsciu do serversocketchannel
    private void tryEstablishConnection(Identity identity) throws IOException, InterruptedException
    {
        SocketChannel senderSocket = SocketChannel.open();
        boolean connected = false;
        while(!(connected || connectedPeerCount == peerCount))
        {
            try
            {
                senderSocket.connect(new InetSocketAddress(identity.ipAddress, port));
                this.logger.info(this.ownIdentity.getIpAddress() + ": [N] Connected to " + identity.ipAddress);
                ClientStreamSession newPeerSession = new ClientStreamSession(senderSocket, bufferSize, receivedDrafts);
                senderSocket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, newPeerSession);
                idToPeerSessionMap.put(identity.id, newPeerSession);
                connected = true;
                connectedPeerCount += 1;
            }
            catch(ConnectException | ClosedChannelException ce)
            {
                //System.out.println(ce);
                Thread.sleep(50);
            }
        }
    }
}
