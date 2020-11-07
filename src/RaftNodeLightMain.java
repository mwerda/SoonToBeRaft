import wenatchee.node.Node;

import java.io.IOException;

public class RaftNodeLightMain
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        Node node = new Node(0, 5000, 6000);
    }
}
