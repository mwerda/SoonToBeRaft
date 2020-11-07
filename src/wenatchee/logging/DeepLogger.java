package wenatchee.logging;

import java.util.HashMap;
import java.util.logging.Logger;

public class DeepLogger
{
    Logger logger;
    HashMap<String, String> moduleToModuleTreeMap;
    public DeepLogger(String className)
    {
        this.logger = Logger.getLogger(className);
        this.moduleToModuleTreeMap = new HashMap<String, String>();
    }
    public void appendToHashMap(String module, String parent)
    {
        this.moduleToModuleTreeMap.put(module, parent + ":" + module);
    }
    public void info(String module, String msg)
    {
        this.logger.info(this.moduleToModuleTreeMap.get(module) + ": " + msg);
    }
    public void severe(String module, String msg)
    {
        this.logger.severe(this.moduleToModuleTreeMap.get(module) + ": " + msg);
    }
    public void fine(String module, String msg)
    {
        this.logger.fine(this.moduleToModuleTreeMap.get(module) + ": " + msg);
    }
    public void finer(String module, String msg)
    {
        this.logger.finer(this.moduleToModuleTreeMap.get(module) + ": " + msg);
    }
    public void finest(String module, String msg)
    {
        this.logger.info(this.moduleToModuleTreeMap.get(module) + ": " + msg);
    }
}
