import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        Draft d = new Draft(Draft.MessageType.APPEND_ENTRIES, 10, 20);
        System.out.println(d.toByteArray());

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