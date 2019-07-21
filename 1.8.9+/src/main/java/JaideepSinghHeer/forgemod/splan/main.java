package JaideepSinghHeer.forgemod.splan;

import JaideepSinghHeer.forgemod.splan.common.Context;
import JaideepSinghHeer.forgemod.splan.common.PropertyManagerClient;
import JaideepSinghHeer.forgemod.splan.common.modinfo;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.File;

import static JaideepSinghHeer.forgemod.splan.common.Context.LOGGER;

@Mod(modid = modinfo.MODID, clientSideOnly = true)
public class main {
    static {
        Context.ModContainerClassName = main.class.getCanonicalName();
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent e)
    {
        LOGGER = e.getModLog();
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        File worldrootdir = event.getServer().getEntityWorld().getSaveHandler().getWorldDirectory();
        PropertyManagerClient toApply = Context.getPropertiesToApply(worldrootdir, new File(Context.mcLocation.getPath(),"config"));
        LOGGER.info("Selected properties file: "+toApply);
    }
}
