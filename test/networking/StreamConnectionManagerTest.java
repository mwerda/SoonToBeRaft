package networking;

import jdk.nashorn.internal.ir.Block;
import node.RaftNode;
import org.junit.jupiter.api.Test;
import protocol.Draft;
import sun.security.provider.NativePRNG;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

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

        RaftNode node = new RaftNode((byte) 1, 30, 5000, "src/configuration", 100000);
        node.runNode();
        Scanner s = new Scanner(System.in);
        String cmd = s.nextLine();
    }
}