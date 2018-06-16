import org.junit.jupiter.api.Test;

import java.io.IOException;

class MainTest
{
    @Test
    void testMulticast1000() throws IOException
    {
        // Define
        int port = 3637;
        String group = "225.4.5.6";
        int ttl = 10;
        int bufferSize = 2048;

        MulticastReceiver receiver = new MulticastReceiver(port, group, bufferSize);
        Thread receiverThread = new Thread(receiver, "Receiver");
        receiverThread.start();

        MulticastSender sender = new MulticastSender(port, group, 10, bufferSize);

        for(int i = 0; i < 1000; i++)
        {
            sender.addMessageToQueue(Integer.toString(i));
        }
        sender.run();
        System.out.printf("");
    }

}