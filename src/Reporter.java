//TODO manage timestamps, pass them
public class Reporter
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

    static void reportCriticalError(String text)
    {
        System.err.println("Critical error: " + text);
    }

    static void reportError(String text)
    {
        System.err.println("Error occurred: " + text);
    }

    static void reportInfo(String text)
    {
        System.out.println(text);
    }
}
