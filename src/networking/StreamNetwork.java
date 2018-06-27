package networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class StreamNetwork
{
    public static void main(String[] args) throws IOException
    {
        boolean active = true;
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(5000));
        ClientSession receiverSession = new ClientSession(serverSocketChannel.accept(), 2048);

        while(active)
        {
            receiverSession.readDraft();
        }
    }
}
