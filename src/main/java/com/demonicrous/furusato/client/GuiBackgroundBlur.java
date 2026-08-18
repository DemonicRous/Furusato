package com.demonicrous.furusato.client;

import com.demonicrous.furusato.Furusato;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Owns Minecraft's post-processing blur only while a container is open. */
public final class GuiBackgroundBlur {
    private static final ResourceLocation BLUR = new ResourceLocation(
            Furusato.MOD_ID, "shaders/post/gui_blur.json");
    private static final String BLUR_NAME = BLUR.toString();

    private boolean owned;
    private boolean conflict;

    @SubscribeEvent
    public void onGuiOpened(GuiOpenEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.getGui() instanceof GuiContainer) {
            enable(minecraft);
        } else {
            disable(minecraft);
        }
    }

    private void enable(Minecraft minecraft) {
        if (owned && isOwnedGroup(minecraft.entityRenderer.getShaderGroup())) {
            return;
        }
        owned = false;
        ShaderGroup current = minecraft.entityRenderer.getShaderGroup();
        if (current != null) {
            conflict = true;
            return;
        }
        conflict = false;
        minecraft.entityRenderer.loadShader(BLUR);
        ShaderGroup loaded = minecraft.entityRenderer.getShaderGroup();
        owned = isOwnedGroup(loaded);
        if (!owned && Furusato.getLogger() != null) {
            Furusato.getLogger().warn(
                    "Could not activate the Furusato container blur shader");
        }
    }

    private void disable(Minecraft minecraft) {
        if (owned && isOwnedGroup(minecraft.entityRenderer.getShaderGroup())) {
            minecraft.entityRenderer.stopUseShader();
        }
        owned = false;
        conflict = false;
    }

    private boolean isOwnedGroup(ShaderGroup group) {
        if (group == null) {
            return false;
        }
        String name = group.getShaderGroupName();
        return BLUR_NAME.equals(name) || name != null && name.endsWith(
                "furusato/shaders/post/gui_blur.json");
    }

    public boolean isOwned() {
        return owned;
    }

    public boolean hasConflict() {
        return conflict;
    }
}
