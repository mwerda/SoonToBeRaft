package obsolete;

class ReceiverThreadPair
{
    Thread thread;
    MulticastReceiver receiver;

    ReceiverThreadPair(MulticastReceiver receiver, Thread thread)
    {
        this.thread = thread;
        this.receiver = receiver;
    }
}