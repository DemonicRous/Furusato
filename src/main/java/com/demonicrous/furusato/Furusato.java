package com.demonicrous.furusato;

import com.demonicrous.furusato.client.ClientGuiEvents;
import com.demonicrous.furusato.client.ContainerAnimationEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Furusato.MOD_ID,
        name = Furusato.NAME,
        version = Furusato.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:forge@[14.23.5.2847,)",
        guiFactory = "com.demonicrous.furusato.client.FurusatoGuiFactory",
        clientSideOnly = true
)
public final class Furusato {
    public static final String MOD_ID = "furusato";
    public static final String NAME = "Furusato";
    public static final String VERSION = "@VERSION@";

    private static Logger logger;

    public static Logger getLogger() {
        return logger;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        MinecraftForge.EVENT_BUS.register(new ClientGuiEvents());
        MinecraftForge.EVENT_BUS.register(new ContainerAnimationEvents());
        logger.info("Furusato {} initialized", VERSION);
    }
}
