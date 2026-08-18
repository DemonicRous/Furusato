package com.demonicrous.limecore.asm;

import java.io.File;
import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

/** Loads Lime Core bytecode transformers before Minecraft classes are defined. */
@IFMLLoadingPlugin.Name("Lime Core")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("com.demonicrous.limecore.asm")
public final class LimeCorePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        PatchDiagnostics.register("unicode_gui_scale");
        return new String[] {
                UnicodeGuiScaleTransformer.class.getName()
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        Object gameDirectory = data.get("mcLocation");
        LimeCoreEarlyConfig.load(gameDirectory instanceof File ? (File) gameDirectory : null);
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
