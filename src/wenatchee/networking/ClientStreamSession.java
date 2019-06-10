package wenatchee.networking;

// Questions to answer:
// WHat is the thrpughput of current solution?
// Congestion control

import wenatchee.protocol.Draft;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientStreamSession
{
    //SelectionKey selectionKey;
    byte id;
    SocketChannel channel;
    ByteBuffer receiveBuffer;
    ByteBuffer sendBuffer;
    // receivedDrafts points to RaftNode's queue
    BlockingQueue<Draft> receivedDrafts;
    BlockingQueue<Draft> outgoingDrafts;

    ClientStreamSession(SocketChannel channel, int bufferSize, BlockingQueue<Draft> receivedDrafts) throws IOException
    {
        //this.selectionKey = selectionKey;
        this.channel = channel;
        this.channel.configureBlocking(false);

        this.receiveBuffer = ByteBuffer.allocateDirect(bufferSize);
        this.receiveBuffer.clear();

        this.sendBuffer = ByteBuffer.allocateDirect(bufferSize);
        this.sendBuffer.clear();

        this.receivedDrafts = receivedDrafts;
        this.outgoingDrafts = new LinkedBlockingQueue<>();
    }

    public void readAvailableDrafts() throws IOException
    {
        boolean readAgain = true;
        while(readAgain)
        {
            channel.read(receiveBuffer);
            receiveBuffer.flip();
            int currentPos = receiveBuffer.position();
            if(receiveBuffer.limit() - receiveBuffer.position() >= 4)
            {
                int draftLength = receiveBuffer.getInt();
                receiveBuffer.position(currentPos);
                boolean isEntireDraftReadable = receiveBuffer.limit() - receiveBuffer.position() >= draftLength;
                if(isEntireDraftReadable)
                {
                    byte[] draftBytes = new byte[draftLength];
                    receiveBuffer.get(draftBytes);
                    receivedDrafts.add(Draft.fromByteArray(draftBytes));
                }
                readAgain = isEntireDraftReadable;
            }
            receiveBuffer.compact();
        }
    }

    public void addDraftToSend(Draft draft)
    {
        outgoingDrafts.add(draft);
    }

    public void sendPendingDrafts() throws IOException
    {
        if(outgoingDrafts.size() != 0)
        {
            while(outgoingDrafts.size() > 0)
            {
                sendBuffer.put(outgoingDrafts.poll().toByteArray());
                sendBuffer.flip();
                channel.write(sendBuffer);
                sendBuffer.clear();
            }
        }
    }

    public void setId(byte id)
    {
        this.id = id;
    }

    public void close() throws IOException
    {
        this.channel.close();
    }
}
