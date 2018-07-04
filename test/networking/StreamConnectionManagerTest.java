package networking;

import node.RaftNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class StreamConnectionManagerTest
{
    @Test
    void testSelectors() throws IOException
    {
        RaftNode node = new RaftNode((byte) 1, 30, 5000, "src/configuration");
        node.runStreamConnectionManager();
    }
}