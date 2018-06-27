package networking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientSession
{
    //SelectionKey selectionKey;
    SocketChannel channel;
    ByteBuffer buffer;

    ClientSession(SocketChannel channel, int bufferSize) throws IOException
    {
        //this.selectionKey = selectionKey;
        this.channel = channel;
        this.channel.configureBlocking(true);
        this.buffer = ByteBuffer.allocateDirect(bufferSize);
    }

    public void readDraft() throws IOException
    {
        channel.read(buffer);
        int draftLength = buffer.getInt();
    }
}
