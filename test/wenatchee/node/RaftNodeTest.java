package wenatchee.node;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class RaftNodeTest
{
    @Test
    void testElectionTimeout() throws IOException, InterruptedException
    {
        RaftNode node = new RaftNode(0);
        node.runNode(RaftNode.Mode.LISTENER);
        Thread.sleep(100000);
    }
}