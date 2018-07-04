package networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class StreamNetwork
{
    public static void main(String[] args) throws IOException
    {
        boolean active = true;
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(5000));
        ClientStreamSession receiverSession = new ClientStreamSession(serverSocketChannel.accept(), 2048);

//        while(active)
//        {
//            receiverSession.readDraft();
//        }

        ByteBuffer senderBuffer = ByteBuffer.allocateDirect(2048);
        Draft draft = new Draft(Draft.DraftType.HEARTBEAT, 12, (byte) 15, new RaftEntry[0]);

        senderBuffer.put(draft.toByteArray());
        senderBuffer.flip();

        //senderSocket.write(senderBuffer);
    }
}
