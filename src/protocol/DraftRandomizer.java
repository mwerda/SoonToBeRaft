package protocol; /**
 * Both min and max bounds are INCLUSIVE
 */

import protocol.Draft;
import protocol.RaftEntry;
import java.util.Random;

public class DraftRandomizer
{
    private final static String CHARSET = "abcdefghijklmnopqrstuvwxyz";

    private static int DEFAULT_MIN_STRING_LENGTH = 2;

    private static int DEFAULT_MAX_STRING_LENGTH = 5;

    private static byte DEFAULT_MIN_ID = 1;

    private static byte DEFAULT_MAX_ID = 100;

    private static int DEFAULT_MIN_ENTRY_COUNT = 0;

    private static int DEFAULT_MAX_ENTRY_COUNT = 500;

    private static int DEFAULT_MIN_TERM_NUMBER = 0;

    private static int DEFAULT_MAX_TERM_NUMBER = Integer.MAX_VALUE;

    private static Random random = new Random();

    public static Draft generateDraft()
    {
        // 1 added as type numbering starts from 1 instead of 0
        Draft.DraftType draftType = Draft.DraftType.fromByte((byte) (random.nextInt(Draft.DraftType.values().length) + 1));
        byte id = (byte) (random.nextInt(DEFAULT_MAX_ID + 1) + DEFAULT_MIN_ID);

        // +1 not used for Integer max value overflow occurence
        int term = random.nextInt(DEFAULT_MAX_TERM_NUMBER) + DEFAULT_MIN_TERM_NUMBER;
        RaftEntry[] raftEntries;

        if(draftType == Draft.DraftType.HEARTBEAT)
        {
            int entriesCount = random.nextInt(DEFAULT_MAX_ENTRY_COUNT + 1) + DEFAULT_MIN_ENTRY_COUNT;
            raftEntries = new RaftEntry[entriesCount];
            for(int i = 0; i < entriesCount; i++)
            {
                raftEntries[i] = generateRaftEntry();
            }
        }
        else
        {
            raftEntries = new RaftEntry[0];
        }

        return new Draft(draftType, term, id, raftEntries);
    }

    public static RaftEntry generateRaftEntry()
    {
        // 1 added as type numbering starts from 1 instead of 0
        RaftEntry.OperationType operationType = RaftEntry.OperationType.fromByte(
                (byte) (random.nextInt(RaftEntry.OperationType.values().length) + 1)
        );
        int value = operationType == RaftEntry.OperationType.SET ? random.nextInt() : 0;
        String key = generateString();

        return new RaftEntry(operationType, value, key);
    }


    public static String generateString()
    {
        StringBuilder builder = new StringBuilder();

        int stringLength = random.nextInt(DEFAULT_MAX_STRING_LENGTH + 1) + DEFAULT_MIN_STRING_LENGTH;
        for(int i = 0; i < stringLength; i++)
        {
            builder.append(CHARSET.toCharArray()[random.nextInt(CHARSET.length())]);
        }
        return new String(builder);
    }
}
