class Reporter
{
    enum OutputType
    {
        INFO,
        ERROR,
        CRIT_ERROR;
    }

    static void report(String text, OutputType type)
    {
        if(type == OutputType.CRIT_ERROR)
        {
            reportCriticalError(text);
        }

        if(type == OutputType.ERROR)
        {
            reportError(text);
        }

        if(type == OutputType.INFO)
        {
            reportInfo(text);
        }
    }

    private static void reportCriticalError(String text)
    {
        System.err.println("Critical error: " + text);
    }

    private static void reportError(String text)
    {
        System.err.println("Error occurred: " + text);
    }

    private static void reportInfo(String text)
    {
        System.out.println(text);
    }
}
