package networking;

import node.RaftNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class StreamConnectionManagerTest
{
    @Test
    void testSelectors() throws IOException, InterruptedException
    {
        RaftNode node = new RaftNode((byte) 1, 30, 5000, "src/configuration");
        node.runNode();
        for(int i = 0; i < 1000; i++)
        {
            Thread.sleep(20000);
            System.out.println();
        }
    }
}