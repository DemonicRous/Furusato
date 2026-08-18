package com.demonicrous.furusato.client;

import com.demonicrous.furusato.Furusato;
import com.demonicrous.furusato.asm.FurusatoEarlyConfig;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Owns Minecraft's post-processing blur only while a container is open. */
public final class GuiBackgroundBlur {
    private static final ResourceLocation BLUR = new ResourceLocation(
            Furusato.MOD_ID, "shaders/post/gui_blur.json");
    private static final String BLUR_NAME = BLUR.toString();
    private static final Field SHADER_PASSES = findShaderPasses();
    private static volatile String status = "idle";

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
        if (!FurusatoEarlyConfig.isContainerBlurEnabled()) {
            status = "disabled";
            disable(minecraft);
            return;
        }
        if (owned && isOwnedGroup(minecraft.entityRenderer.getShaderGroup())) {
            return;
        }
        owned = false;
        ShaderGroup current = minecraft.entityRenderer.getShaderGroup();
        if (current != null) {
            conflict = true;
            status = "conflict";
            return;
        }
        conflict = false;
        minecraft.entityRenderer.loadShader(BLUR);
        ShaderGroup loaded = minecraft.entityRenderer.getShaderGroup();
        owned = isOwnedGroup(loaded);
        if (owned) {
            applyRadius(loaded, FurusatoEarlyConfig.getBlurRadius());
            status = "active";
        }
        if (!owned && Furusato.getLogger() != null) {
            status = "failed";
            Furusato.getLogger().warn(
                    "Could not activate the Furusato container blur shader");
        }
    }

    private void disable(Minecraft minecraft) {
        if (owned && isOwnedGroup(minecraft.entityRenderer.getShaderGroup())) {
            minecraft.entityRenderer.stopUseShader();
            status = "idle";
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

    public static String getStatus() {
        return status;
    }

    @SuppressWarnings("unchecked")
    private static void applyRadius(ShaderGroup group, int radius) {
        try {
            List<Shader> passes = (List<Shader>) SHADER_PASSES.get(group);
            for (Shader pass : passes) {
                ShaderUniform uniform = pass.getShaderManager()
                        .getShaderUniform("Radius");
                if (uniform != null) {
                    uniform.set((float) radius);
                }
            }
        } catch (IllegalAccessException | RuntimeException error) {
            if (Furusato.getLogger() != null) {
                Furusato.getLogger().warn(
                        "Could not apply the configured container blur radius", error);
            }
        }
    }

    private static Field findShaderPasses() {
        return ReflectionHelper.findField(
                ShaderGroup.class, "listShaders", "field_148031_d");
    }
}
