import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(5000));
        while(true)
        {
            serverSocketChannel.accept();
        }

//        RaftNode node = new RaftNode((byte) 1, 40, 3000);
//        node.runNode();
//        while(!node.executorService.isTerminated())
//        {
//            Thread.sleep(1000);
//        }

//        MulticastReceiver receiver = new MulticastReceiver(5000, "225.4.5.6", 2048);
//        Thread receiverThread = new Thread(receiver, "Receiver");
//        receiverThread.start();
//
//
//        MulticastSender sender = new MulticastSender(5000, "225.4.5.6", 2048);
//        Thread senderThread = new Thread(sender, "Sender");
//        for (int i = 0; i < 1000000; i++)
//        {
//            sender.addMessageToQueue(Integer.toString(i));
//        }
//        senderThread.start();
//        while(true)
//        {
//            //sender.addMessageToQueue("a");
//        }
    }

    public static SenderThreadPair buildSenderThreadPair(int packets) throws IOException
    {
        MulticastSender sender = new MulticastSender(5000, "225.4.5.6", 2048);
        Thread senderThread = new Thread(sender, "Sender");
        for (int i = 0; i < packets; i++)
        {
            sender.addMessageToQueue(Integer.toString(i));
        }
        return new SenderThreadPair(sender, senderThread);
    }

    public static ReceiverThreadPair buildReceiverThreadPair() throws IOException
    {
        MulticastReceiver receiver = new MulticastReceiver(5000, "225.4.5.6", 2048);
        Thread receiverThread = new Thread(receiver, "Receiver");
        receiverThread.start();
        return new ReceiverThreadPair(receiver, receiverThread);
    }

}

class SenderThreadPair
{
    Thread thread;
    MulticastSender sender;

    SenderThreadPair(MulticastSender sender, Thread thread)
    {
        this.thread = thread;
        this.sender = sender;
    }
}

class ReceiverThreadPair
{
    Thread thread;
    MulticastReceiver receiver;

    ReceiverThreadPair(MulticastReceiver receiver, Thread thread)
    {
        this.thread = thread;
        this.receiver = receiver;
    }
}