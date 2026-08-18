package com.demonicrous.limecore;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = LimeCore.MOD_ID,
        name = LimeCore.NAME,
        version = LimeCore.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:forge@[14.23.5.2847,)",
        guiFactory = "com.demonicrous.limecore.client.LimeCoreGuiFactory",
        clientSideOnly = true
)
public final class LimeCore {
    public static final String MOD_ID = "limecore";
    public static final String NAME = "Lime Core";
    public static final String VERSION = "@VERSION@";

    private static Logger logger;

    public static Logger getLogger() {
        return logger;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("Lime Core {} initialized", VERSION);
    }
}
