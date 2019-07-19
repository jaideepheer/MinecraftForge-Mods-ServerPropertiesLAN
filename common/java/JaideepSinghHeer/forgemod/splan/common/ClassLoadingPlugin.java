package JaideepSinghHeer.forgemod.splan.common;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE)   // load after FML to get de-obfuscated srg names in bytecode
public class ClassLoadingPlugin implements IFMLLoadingPlugin {
    // FML injects some data before calling.
    private static Map<String, Object> fmlInjectedData;
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{ASM_BytecodeTransformer.class.getCanonicalName()};
    }
    @Override
    public String getModContainerClass() { return Context.ModContainerClassName; }

    @Nullable @Override
    public String getSetupClass() { return PreLoadingSetup.class.getCanonicalName(); }

    @Override
    public void injectData(Map<String, Object> data) {
        Context.mcLocation = (File) data.get("mcLocation");
        Context.runtimeDeobfuscationEnabled = (Boolean) data.get("runtimeDeobfuscationEnabled");
        Context.thisCoreModLocation = (File) data.get("coremodLocation");
    }

    @Override
    public String getAccessTransformerClass() { return null; }
}
