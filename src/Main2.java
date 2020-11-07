import wenatchee.networking.RemoteClient;
import wenatchee.networking.RemoteServer;

import java.io.IOException;

public class Main2
{
    public static void main(String[] args) throws IOException, InterruptedException {
//        int port = 5100;
//        RemoteServer rs = new RemoteServer();
//        rs.startServer(port);

        RemoteClient client = new RemoteClient();
        try {
            client.start2();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
