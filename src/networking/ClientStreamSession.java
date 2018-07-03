package networking;

// Questions to answer:
// Should non-blocking read be more effective?
// WHat is the thrpughput of current solution?

import protocol.Draft;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientStreamSession
{
    //SelectionKey selectionKey;
    SocketChannel channel;
    ByteBuffer receiveBuffer;
    ByteBuffer sendBuffer;
    BlockingQueue<Draft> draftQueue;

    ClientStreamSession(SocketChannel channel, int bufferSize) throws IOException
    {
        //this.selectionKey = selectionKey;
        this.channel = channel;
        this.channel.configureBlocking(true);
        this.receiveBuffer = ByteBuffer.allocateDirect(bufferSize);
        this.receiveBuffer.clear();
        this.sendBuffer = ByteBuffer.allocateDirect(bufferSize);
        this.sendBuffer.clear();
        this.draftQueue = new LinkedBlockingQueue<>();
    }

    public void readDraft() throws IOException
    {
        channel.read(receiveBuffer);
        receiveBuffer.flip();
        int draftLength = receiveBuffer.getInt();
        receiveBuffer.position(0);
        if(receiveBuffer.limit() >= draftLength - 1)
        {
            byte[] draftBytes = new byte[draftLength];
            receiveBuffer.get(draftBytes);
            draftQueue.add(Draft.fromByteArray(draftBytes));
        }
        receiveBuffer.compact();
    }
}
