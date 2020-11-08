package wenatchee.node;

import java.util.HashMap;
import java.util.LinkedList;

public class MetaCollector
{
    boolean[] draftReceipts;
    float mean;
    int counter;

    int incomingDrafts;
    int outgoingDrafts;

    HashMap<Integer, Integer> outgoingMessagesCtr;
    HashMap<Integer, Integer> incomingMessagesCtr;
    
    public MetaCollector(int size)
    {
        this.draftReceipts = new boolean[size];
        this.mean = 0;
        this.counter = 0;

        this.incomingDrafts = 0;
        this.outgoingDrafts = 0;

        incomingMessagesCtr = new HashMap<Integer, Integer>()
        {{
           put(0, 0);
           put(1, 0);
           put(2, 0);
        }};
        outgoingMessagesCtr = new HashMap<Integer, Integer>()
        {{
           put(0, 0);
           put(1, 0);
           put(2, 0);
        }};
    }

    public void markDraftReceived(int number)
    {
        draftReceipts[number] = true;
    }
    
    public boolean[] getDraftReceipts()
    {
        return draftReceipts;
    }

    public Integer[] getFalseIndexes()
    {
        LinkedList<Integer> missedIndexes = new LinkedList<>();
        for(int i = 0; i < draftReceipts.length; i++)
        {
            if(!draftReceipts[i])
            {
                missedIndexes.add(i);
            }
        }
        return missedIndexes.toArray(new Integer[missedIndexes.size()]);
    }

    public void tickMeanValue(int value)
    {
        mean = (mean * counter + value) / (counter + 1);
        counter++;
    }

    public void tickOutgoingMessages(int id)
    {
        this.outgoingMessagesCtr.put(id, outgoingMessagesCtr.get(id) + 1);
    }

    public void tickIncomingMessages(int id)
    {
        this.incomingMessagesCtr.put(id, incomingMessagesCtr.get(id) + 1);
    }

    public void tickOutgoingDrafts()
    {
        this.outgoingDrafts++;
    }

    public void tickIncomingDrafts()
    {
        this.incomingDrafts++;
    }

    public float getMean()
    {
        return mean;
    }
}
