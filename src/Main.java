//TODO

import java.io.IOException;

public class Main
{
    private int port;
    private String receiverGroup;
    private int ttl;

    public static void main(String[] args) throws IOException
    {
        int port = 3637;
        String group = "225.4.5.6";
        int ttl = 10;
        int bufferSize = 2048;

        MulticastReceiver receiver = new MulticastReceiver(port, group, bufferSize);
        Thread receiverThread = new Thread(receiver, "Receiver");
        receiverThread.start();

        String text = "Hello";
        byte[] data = text.getBytes();
        MulticastSender sender = new MulticastSender(port, group, 10, bufferSize);

        try
        {
            sender.broadcastMessage(data);
        }
        catch(Exception e)
        {
            Reporter.reportError(e.toString());
        }


    }
}
