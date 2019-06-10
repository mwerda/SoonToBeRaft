package wenatchee.networking;

import wenatchee.node.RaftNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Scanner;

class StreamConnectionManagerTest
{

    @Test
    void test100kMessages() throws IOException, InterruptedException
    {
        // Results
        // 74 s for 90 0000 Drafts
        // 1131.6947 mean Draft size
        // 1375.541 kB per s
        // 100% delivered

        RaftNode node = new RaftNode(30, 5000, "src/configuration", 100000);
        node.runNode();
        Scanner s = new Scanner(System.in);
        String cmd = s.nextLine();
    }
}