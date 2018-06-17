import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.LinkedList;

class MainTest
{
    @Test
    void testMulticast1000() throws IOException, InterruptedException
    {
        System.out.println("Multicast: 1000 packets\n");
        parametrizedMulticastTest(0, 20000, 6000, 1000);
    }

    @Test
    void testMulticast10000() throws IOException, InterruptedException
    {
        System.out.println("Multicast: 10 000 packets\n");
        parametrizedMulticastTest(0, 20000, 6000, 10000);
    }

    @Test
    void testMulticast100000() throws IOException, InterruptedException
    {
        System.out.println("Multicast: 100 000 packets\n");
        parametrizedMulticastTest(0, 20000, 6000, 100000);
    }

    @Test
    void testMulticast1000000() throws IOException, InterruptedException
    {
        System.out.println("Multicast: 1 000 000 packets\n");
        parametrizedMulticastTest(0, 20000, 6000, 1000000);
    }

    @Test
    void testMulticast10000000() throws IOException, InterruptedException
    {
        System.out.println("Multicast: 10 000 000 packets\n");
        parametrizedMulticastTest(0, 20000, 6000, 10000000);
    }

    private void parametrizedMulticastTest(int receiverMinWaitTime, int receiverMaxWaitTime, int waitTimeStep, int packets) throws IOException, InterruptedException
    {
        System.out.println("Number of packets: " + Integer.toString(packets));
        for(int t = receiverMinWaitTime; t <= receiverMaxWaitTime; t += waitTimeStep)
        {
            MulticastReceiver receiver = new MulticastReceiver(5000, "225.4.5.6", 2048);
            Thread receiverThread = new Thread(receiver, "Receiver");
            receiverThread.start();

            System.out.println("Sleeping for " + Integer.toString(t));
            Thread.sleep(t);

            MulticastSender sender = new MulticastSender(5000, "225.4.5.6", 2048);
            Thread senderThread = new Thread(sender, "Sender");
            for (int i = 0; i < packets; i++)
            {
                sender.addMessageToQueue(Integer.toString(i));
            }
            receiver.receivedMessages = new LinkedList<Message>();
            assertEquals(0, receiver.receivedMessages.size());
            senderThread.start();

            System.out.println("Successful delivery rate: " + (float) receiver.receivedMessages.size() / packets);
            System.out.println("Delivered " + Integer.toString(receiver.receivedMessages.size()) + " of " + packets + " packets");
            System.out.println("***************************\n");

            Thread.sleep(10000);

            senderThread.interrupt();
            receiverThread.interrupt();
            Thread.sleep(1000);
        }
    }

    @Test
    void interruptThreads() throws IOException, InterruptedException
    {
        MulticastReceiver receiver = new MulticastReceiver(5000, "225.5.5.6", 2048);
        Thread receiverThread = new Thread(receiver, "Receiver");
        receiverThread.start();

        MulticastSender sender = new MulticastSender(5000, "225.5.5.6", 64);
        Thread senderThread = new Thread(sender, "Sender");
        senderThread.start();

        Thread.sleep(5000);

        senderThread.interrupt();
        receiverThread.interrupt();

        Thread.sleep(5000);
    }
}