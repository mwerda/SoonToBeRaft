import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientSession
{
    SelectionKey selectionKey;
    SocketChannel channel;
    ByteBuffer buffer;

    ClientSession(SelectionKey selectionKey, SocketChannel channel, int bufferSize)
    {
        this.selectionKey = selectionKey;
        this.channel = channel;
        this.buffer = ByteBuffer.allocateDirect(bufferSize);
    }
}
