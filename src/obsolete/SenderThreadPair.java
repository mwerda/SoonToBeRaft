package obsolete;

class SenderThreadPair
{
    Thread thread;
    MulticastSender sender;

    SenderThreadPair(MulticastSender sender, Thread thread)
    {
        this.thread = thread;
        this.sender = sender;
    }
}