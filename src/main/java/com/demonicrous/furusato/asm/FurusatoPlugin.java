package com.demonicrous.furusato.asm;

import java.io.File;
import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

/** Loads Furusato bytecode transformers before Minecraft classes are defined. */
@IFMLLoadingPlugin.Name("Furusato")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("com.demonicrous.furusato.asm")
public final class FurusatoPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        PatchDiagnostics.register("unicode_gui_scale");
        if (FurusatoEarlyConfig.isSafeModeEnabled()) {
            PatchDiagnostics.safeMode("unicode_gui_scale");
            return new String[0];
        }
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
        FurusatoEarlyConfig.load(gameDirectory instanceof File ? (File) gameDirectory : null);
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
