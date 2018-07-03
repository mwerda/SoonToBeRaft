package networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class StreamConnectionManager implements Runnable
{
    public StreamConnectionManager(String configFilePath) throws IOException
    {

    }
//
//    boolean active = true;
//    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//    serverSocketChannel.socket().bind(new InetSocketAddress(5000) throws IOException);
//    ClientStreamSession receiverSession = new ClientStreamSession(serverSocketChannel.accept(), 2048);
//
//    while(active)
//    {
//        receiverSession.readDraft();
//    }

    @Override
    public void run()
    {

    }
}
