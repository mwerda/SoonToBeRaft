package node;

import java.util.LinkedList;

public class MetaCollector
{
    boolean[] draftReceipts;
    
    public MetaCollector(int size)
    {
        this.draftReceipts = new boolean[size];
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
}
