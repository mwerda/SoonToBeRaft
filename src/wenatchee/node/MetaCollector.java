package wenatchee.node;

import java.util.LinkedList;

public class MetaCollector
{
    boolean[] draftReceipts;
    float mean;
    int counter;
    
    public MetaCollector(int size)
    {
        this.draftReceipts = new boolean[size];
        this.mean = 0;
        this.counter = 0;
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

    public float getMean()
    {
        return mean;
    }
}
