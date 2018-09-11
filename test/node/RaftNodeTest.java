package node;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RaftNodeTest
{
    @Test
    void testElectionTimeout() throws IOException, InterruptedException
    {
        RaftNode node = new RaftNode();
        node.runNode(RaftNode.Mode.LISTENER);
        Thread.sleep(100000);
    }
}