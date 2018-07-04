package networking;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ClientStreamSessionTest
{
    // Set up connection between two nodes, mock some messages, send them over network and check
    // for equivalency of Drafts. Test expected to be run on a pair of nodes.
    @Test
    void testTwoNodesSomeMessages() throws IOException, InterruptedException
    {
        final int bufferSize = 8192;
        final int draftsExpectedCount = 10000;
        final String hostname = "192.168.1.109";
        final int port = 5000;
        final int waitForMessagesTimeCap = 30000;

        // RECEIVER code

//        BlockingQueue<Draft> incomingDrafts = new LinkedBlockingQueue<>();
//        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//        serverSocketChannel.socket().bind(new InetSocketAddress(5000));
//        ClientStreamSession receiverSession = new ClientStreamSession(serverSocketChannel.accept(), bufferSize);
//
//        Thread receiverThread = new Thread("ReceiverThread")
//        {
//            public void run()
//            {
//                try
//                {
//                    receiverSession.readDraft();
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        };
//
//        receiverThread.start();
//        int receiverQueueSize = receiverSession.draftQueue.size();
//        long eventTime = System.nanoTime();
//        long elapsedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - eventTime);
//        while(!(receiverQueueSize == draftsExpectedCount || elapsedTime >= waitForMessagesTimeCap))
//        {
//            elapsedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - eventTime);
//            System.out.println(elapsedTime);
//            Thread.sleep(5000);
//        }
//
//        System.out.println("Performing content check");
//        // TODO check

        // SENDER code
        BlockingQueue<Draft> draftsToSend = new LinkedBlockingQueue<>();
        for(int i = 0; i < draftsExpectedCount; i++)
        {
            draftsToSend.add(MessageRandomizer.generateDraft());
        }

        SocketChannel senderSocket = SocketChannel.open();
        senderSocket.connect(new InetSocketAddress(hostname, port));

        System.out.println(senderSocket.isConnected());
        System.out.println(senderSocket);
        System.out.println();

        // sanity breakpoint
        ByteBuffer senderBuffer = ByteBuffer.allocateDirect(bufferSize);
        while(draftsToSend.size() > 0)
        {
            senderBuffer.put(draftsToSend.poll().toByteArray());
            senderBuffer.flip();
            senderSocket.write(senderBuffer);
            senderBuffer.clear();

            if(draftsToSend.size() % 10 == 0)
            {
                System.out.println(draftsToSend.size() + " left to send");
            }
        }
        Thread.sleep(100000);
    }
}