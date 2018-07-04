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
    void testSelectors() throws IOException, InterruptedException
    {
        RaftNode node = new RaftNode((byte) 1, 30, 5000, "src/configuration");
        node.runNode();
        Scanner s = new Scanner(System.in);
        String cmd = s.nextLine();
        if(cmd == "s")
        {
            BlockingQueue<Draft> drafts = node.getReceivedDrafts();
            int i = 0;
            for(Draft d : drafts)
            {
                if(d.getTerm() != i)
                {
                    System.out.println("Expected " + i + " got " + d.getTerm());
                    i = d.getTerm();
                }
                else
                {
                    i++;
                }
            }
        }
    }
}