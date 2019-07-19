import JaideepSinghHeer.forgemod.splan.common.ASM_BytecodeTransformer;
import JaideepSinghHeer.forgemod.splan.common.Context;
import JaideepSinghHeer.forgemod.splan.common.PropertyManagerClient;
import JaideepSinghHeer.forgemod.splan.common.modinfo;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.UserListWhitelist;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

import static JaideepSinghHeer.forgemod.splan.common.Context.LOGGER;

/**
 * We cannot use the {@link Mod} annotation as the mod is already instantiated
 * via the {@link IFMLLoadingPlugin} interface as a CoreMod.
 *
 * We need it to be a CoreMod so that we can Edit the ByteCode
 * using the {@link net.minecraft.launchwrapper.IClassTransformer} interface.
 *
 * We Edit the ByteCode of the {@link net.minecraft.util.HttpUtil} class to return our specified Port for LAN connections.
 * @see net.minecraft.util.HttpUtil for the getSuitableLanPort() method which returns a LAN port.
 */

@SideOnly(Side.CLIENT)
// We want our mod to load after the game is de-obfuscated into SRG format by forge to make the ASM work.
@IFMLLoadingPlugin.SortingIndex(1001)
public class ServerPropertiesLAN extends DummyModContainer implements IFMLLoadingPlugin
{
    public int port=0;
    private static boolean whiteListFirstRun;
    private static boolean firstRun;
    private static IntegratedServer server;

    // This Class manages all the File IO.
    private PropertyManagerClient ServerProperties = null;

    /**
     * We don't want to do a lot of work so we extend {@link DummyModContainer}.
     * As it contains a {@link ModMetadata} object,
     * it must be initialised.
     *
     */
    public ServerPropertiesLAN()
    {
        super(new ModMetadata());
        whiteListFirstRun = false;
        LOGGER.info("-=-=-=-=-=-=-=ServerPropertiesLAN-Constructed=-=-=-=-=-=-=-");
        // static instance to always get the correct object.
        instance = this;
        // Mod Metadata defined in DummyModContainer received by func. getMetadata()
        ModMetadata md = getMetadata();
        md.modId=modinfo.MODID;
        md.version=modinfo.version;
        md.name=modinfo.name;
        md.authorList = modinfo.authorList;
        md.description = modinfo.description;
        md.credits = modinfo.credits;
        md.logoFile = modinfo.logoFile;
        md.screenshots = modinfo.screenshots;
        md.url = modinfo.url;
    }

    /**
     * We cannot use {@link net.minecraftforge.fml.common.Mod.EventHandler} as that is a part of the {@link Mod} annotation
     * and hence requires a Class to be annotated with the {@link Mod} annotation which cannot be done for CoreMods.<See Above>
     *
     * Therefore we must register this class to the {@link EventBus} provided to it for being the {@link ModContainer}.
     *
     */
    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        //System.out.println("-=-=-=-=-=-=-=EventBusRegistered=-=-=-=-=-=-=-=-");
        bus.register(this);
        return true;
    }

    // get logger
    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent e)
    {
        LOGGER = e.getModLog();
    }

    /**
     * The static instance of this Class to be accessed as a Mod.
     * Forge automatically instantiates an Object for us
     * and we assign that to this object(called instance) in the constructor.
     */
    public static ServerPropertiesLAN instance;

    /**
     * This function is subscribed to the {@link EventBus} via the {@link Subscribe} annotation.
     * The type of event({@link net.minecraftforge.fml.common.eventhandler.Event}) to be subscribed is judged from the prototype.
     * This function gets the {@link net.minecraft.server.MinecraftServer} from the event
     * and gets the world save directory using the {@link DimensionManager}.
     *
     * It then uses the {@link PropertyManagerClient} Class to save/load data from the server.properties file
     * and sets the attributes of the {@link net.minecraft.server.MinecraftServer} via its functions.
     *
     */
    @Subscribe
    public void onServerStarting(FMLServerStartingEvent event) {
        LOGGER.info("Server Starting !");

        // Get the current world directory.
        String worldrootdir = DimensionManager.getCurrentSaveRootDirectory()+File.separator;

        // Define the config files.
        File local = new File(worldrootdir,"server.properties");
        File global = new File(Minecraft.getMinecraft().mcDataDir+File.separator+"config"+File.separator+"serverGlobalConfig.properties");

        // Use the appropriate config file.
        if(!global.exists())
        {
            // Probably first run. Generate global config to use.
            firstRun = true;
            ServerProperties = new PropertyManagerClient(global);
        }
        else if(local.exists()) {
            ServerProperties = new PropertyManagerClient(local);
            if(!ServerProperties.getBooleanProperty("overrideGlobalDefaults", true)) {
                ServerProperties.setPropertiesFile(global);
                LOGGER.info("Using Global Server Context !");
            }
        }
        else{
            // Local properties file doesn't exist.
            try {
                // Copy the global config to local world and use it.
                Files.copy(global,local);
                ServerProperties = new PropertyManagerClient(local);
                ServerProperties.comment += System.getProperty("line.separator")+"overrideGlobalDefaults :"+System.getProperty("line.separator")+"\tspecify weather to use this file to override the global settings in the file \""+global.getAbsolutePath()+"\"";
                ServerProperties.getBooleanProperty("overrideGlobalDefaults", false);
                ServerProperties.saveProperties();
            } catch (IOException e) {
                LOGGER.log(Level.WARN,"Could not create local server config file. Using the global one.");
                e.printStackTrace();
                ServerProperties = new PropertyManagerClient(global);
            }
        }

        LOGGER.info("Using file : "+(ServerProperties.getBooleanProperty("overrideGlobalDefaults", true)?local.getPath():global.getPath()));
        // Get the server instance.
        server = (IntegratedServer) event.getServer();

        // Write comments to file
        ServerProperties.comment = "Minecraft Server Context for LAN."
         + System.getProperty("line.separator")+"For default behaviour :-"
         + System.getProperty("line.separator")+"set max-view-distance=0"
         + System.getProperty("line.separator")+"set port=0"
         + System.getProperty("line.separator")+"You can also delete this(or any properties) file to get it regenerated with default values.";

        // Read data from the config file and set the server config.
        Context.port = ServerProperties.getIntProperty("port", 0);
        // ** Abandoned **
        //maxTickTime = ServerProperties.getLongProperty("maxTickTime",-1);
        server.setOnlineMode(ServerProperties.getBooleanProperty("online-mode", true));
        server.setCanSpawnAnimals(ServerProperties.getBooleanProperty("spawn-animals", true));
        server.setCanSpawnNPCs(ServerProperties.getBooleanProperty("spawn-npcs", true));
        server.setAllowPvp(ServerProperties.getBooleanProperty("pvp", true));
        server.setAllowFlight(ServerProperties.getBooleanProperty("allow-flight", false));
        server.setResourcePack(ServerProperties.getStringProperty("resource-pack-sha1", ""), this.loadResourcePackSHA());
        server.setMOTD(ServerProperties.getStringProperty("motd", "<! "+server.getServerOwner() + "'s " + server.worlds[0].getWorldInfo().getWorldName()+" ON LAN !>"));
        server.setPlayerIdleTimeout(ServerProperties.getIntProperty("player-idle-timeout", 0));
        server.setBuildLimit(ServerProperties.getIntProperty("max-build-height", 256));

        // Print data to the console
        LOGGER.info("Server Data :- ");
        LOGGER.info("online-mode = "+server.isServerInOnlineMode());
        // ** Abandoned **
        //LOGGER.info("maxTickTime = "+maxTickTime);
        LOGGER.info("spawn-animals = "+server.getCanSpawnAnimals());
        LOGGER.info("spawn-npcs = "+server.getCanSpawnNPCs());
        LOGGER.info("pvp = "+server.isPVPEnabled());
        LOGGER.info("allow-flight = "+server.isFlightAllowed());
        LOGGER.info("player-idle-timeout = "+server.getMaxPlayerIdleMinutes());
        LOGGER.info("max-build-height = "+server.getBuildLimit());
        LOGGER.info("resource-pack-sha1 = "+server.getResourcePackHash());
        LOGGER.info("motd = "+server.getMOTD());

        // Get the PlayerList Settings Object for whitelist features.
        PlayerList customPlayerList =  server.getPlayerList();

        // REFLECTION !!
        // NOTE : We need to make sure it works after obfuscation and so we use the ReflectionHelper class
        // which basically lets us specify many possible names for the field ...!
        // This block hacks into game to set MaxPlayers, MaxViewDistance and whitelist settings.
        try {
            // Set MaxPlayers
            Field field = ReflectionHelper.findField(PlayerList.class,"maxPlayers","field_72405_c");
            field.setAccessible(true);
            field.set(customPlayerList, ServerProperties.getIntProperty("max-players", 10));
            LOGGER.info("Max Players = "+customPlayerList.getMaxPlayers());

            // Set MaxViewDistance
            Field dist = ReflectionHelper.findField(PlayerList.class,"viewDistance","field_72402_d");
            dist.setAccessible(true);
            int d = ServerProperties.getIntProperty("max-view-distance", 0);
            if(d>0)
            {
                dist.set(customPlayerList, d);
                LOGGER.info("Max view distance = "+d);
            }
            else LOGGER.info("max-view-distance is set <= 0. Using default view distance algorithm.");

            // Set the server whitelist.
            if (ServerProperties.getBooleanProperty("white-list", false))
            {
                LOGGER.warn("=====>>WARNING whitelisting enabled...! Make sure at least one user entry is in the whitelist.json file !");
                File whitelistjson = new File(worldrootdir+"whitelist.json");
                UserListWhitelist whitelist = new UserListWhitelist(whitelistjson);
                if(!whitelistjson.exists()) {
                    whitelistjson.createNewFile();
                    whitelist.writeChanges();
                    whiteListFirstRun = true;
                    // Set WhiteList
                    field = ReflectionHelper.findField(PlayerList.class,"whiteListedPlayers","field_72411_j");
                    field.setAccessible(true);
                    field.set(customPlayerList, whitelist);
                }
                else {
                    // Not In First Run.
                    customPlayerList.setWhiteListEnabled(true);
                }
            }
            server.setPlayerList(customPlayerList);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if(firstRun)
        {
            try {
                Files.copy(global,local);
                ServerProperties.setPropertiesFile(local);
                ServerProperties.comment += System.getProperty("line.separator")+"overrideGlobalDefaults :"+System.getProperty("line.separator")+"\tspecify weather to use this file to override the global settings in the file \""+global.getAbsolutePath()+"\"";
                ServerProperties.getBooleanProperty("overrideGlobalDefaults", false);
                ServerProperties.saveProperties();
            } catch (IOException e) {
                LOGGER.error("Oops..! Couldn't copy to local server config file. Please manually copy the global server config file to your world save directory.");
                e.printStackTrace();
            }
        }
    }

    /**
     * These functions are a part of the {@link IFMLLoadingPlugin} interface.
     * @see IFMLLoadingPlugin for details.
     */
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                ASM_BytecodeTransformer.class.getCanonicalName()
        };
    }

    @Override
    public String getModContainerClass() {
        return ServerPropertiesLAN.class.getCanonicalName();
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data){
        ;
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public Object getMod()
    {
        return instance;
    }


    /**
     * This function checks the current ResoursePackSHA's validity
     * and returns the final ResoursePackSHA values of the server.
     */
    private String loadResourcePackSHA()
    {
        if (ServerProperties.hasProperty("resource-pack-hash"))
        {
            if (ServerProperties.hasProperty("resource-pack-sha1"))
            {
                LOGGER.warn("resource-pack-hash is deprecated and found along side resource-pack-sha1. resource-pack-hash will be ignored.");
            }
            else
            {
                LOGGER.warn("resource-pack-hash is deprecated. Please use resource-pack-sha1 instead.");
                ServerProperties.getStringProperty("resource-pack-sha1", ServerProperties.getStringProperty("resource-pack-hash", ""));
                ServerProperties.removeProperty("resource-pack-hash");
            }
        }

        String s = ServerProperties.getStringProperty("resource-pack-sha1", "");

        if (!s.isEmpty() && !Pattern.compile("^[a-fA-F0-9]{40}$").matcher(s).matches())
        {
            LOGGER.warn("Invalid sha1 for ressource-pack-sha1");
        }

        if (!ServerProperties.getStringProperty("resource-pack", "").isEmpty() && s.isEmpty())
        {
            LOGGER.warn("You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack.");
        }

        return s;
    }
    @Override
    public URL getUpdateUrl()
    {
        try{
        return new URL(modinfo.updateJSON);}
        catch (Exception e){e.printStackTrace();}
        return null;
    }
}