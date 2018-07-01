package networking;

// Questions to answer:
// Should non-blocking read be more effective?
// WHat is the thrpughput of current solution?

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientStreamSession
{
    //SelectionKey selectionKey;
    SocketChannel channel;
    ByteBuffer buffer;
    BlockingQueue<Draft> draftQueue;

    ClientStreamSession(SocketChannel channel, int bufferSize) throws IOException
    {
        //this.selectionKey = selectionKey;
        this.channel = channel;
        this.channel.configureBlocking(true);
        this.buffer = ByteBuffer.allocateDirect(bufferSize);
        this.buffer.clear();
        this.draftQueue = new LinkedBlockingQueue<>();
    }

    public void readDraft() throws IOException
    {
        channel.read(buffer);
        buffer.flip();
        int draftLength = buffer.getInt();
        buffer.position(0);
        if(buffer.limit() >= draftLength - 1)
        {
            byte[] draftBytes = new byte[draftLength];
            buffer.get(draftBytes);
            draftQueue.add(Draft.fromByteArray(draftBytes));
            buffer.compact();
        }
    }
}
