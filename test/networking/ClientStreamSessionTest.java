package networking;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.junit.jupiter.api.Assertions.*;

class ClientStreamSessionTest
{
    // Set up connection between two nodes, mock some messages, send them over network and check
    // for equivalency of Drafts
    @Test
    void testTwoNodesSomeMessages() throws IOException
    {
        // SENDER code
        SocketChannel senderSocket = SocketChannel.open();
        senderSocket.connect(new InetSocketAddress("192.168.1.109", 5000));

        ByteBuffer senderBuffer = ByteBuffer.allocateDirect(2048);
        Draft draft = new Draft(Draft.DraftType.HEARTBEAT, 12, (byte) 15, new RaftEntry[0]);

        senderBuffer.put(draft.toByteArray());
        senderBuffer.flip();

        senderSocket.write(senderBuffer);
    }
}