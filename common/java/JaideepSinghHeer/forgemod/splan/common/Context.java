package JaideepSinghHeer.forgemod.splan.common;

import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

public final class Context {
    // Log manager
    public static Logger LOGGER;
    // state info
    public static int port;
    public static PropertyManagerClient globalProperties;
    public static PropertyManagerClient localProperties = null;

    // STATIC CONTEXT
    // metadata
    public static File mcLocation;
    public static String ModContainerClassName;

    // defaults
    public static final Map<String, Object> DEFAULTS = new Hashtable<String, Object>();
    static {
        DEFAULTS.put("port",0);
        DEFAULTS.put("online-mode",true);
        DEFAULTS.put("spawn-animals",true);
        DEFAULTS.put("spawn-npcs",true);
        DEFAULTS.put("pvp",true);
        DEFAULTS.put("allow-flight",false);
        DEFAULTS.put("resource-pack-sha1","");
        DEFAULTS.put("motd","A LAN hosted server...!");
        DEFAULTS.put("player-idle-timeout",0);
        DEFAULTS.put("max-build-height",255);
        DEFAULTS.put("max-players",10);
        DEFAULTS.put("max-view-distance",0);
        DEFAULTS.put("white-list",false);
    }

    private static void setDefault(PropertyManagerClient prop) {
        DEFAULTS.forEach(prop::setProperty);
    }
    public static PropertyManagerClient getPropertiesToApply(File worldPath, File configPath)
    {
        File global = new File(configPath,"serverGlobalConfig.properties"),
                local = new File(worldPath,"server.properties");
        LOGGER.info("Global Properties file: "+global);
        LOGGER.info("Local Properties file: "+local);
        if(!global.exists()) {
            globalProperties = new PropertyManagerClient(global);
            Context.setDefault(globalProperties);
            globalProperties.saveProperties();
        }
        else {
            globalProperties = new PropertyManagerClient(global);
            globalProperties.loadProperties();
        }
        if(!local.exists()) {
            localProperties = new PropertyManagerClient(local);
            Context.setDefault(localProperties);
            localProperties.setProperty("overrideGlobalDefaults",false);
            localProperties.saveProperties();
        }
        else {
            localProperties = new PropertyManagerClient(local);
            localProperties.loadProperties();
        }
        if(localProperties.getBooleanProperty("overrideGlobalDefaults", false)) {
            port = localProperties.getIntProperty("port", 0);
            return localProperties;
        }
        else {
            port = globalProperties.getIntProperty("port", 0);
            return globalProperties;
        }
    }
}
