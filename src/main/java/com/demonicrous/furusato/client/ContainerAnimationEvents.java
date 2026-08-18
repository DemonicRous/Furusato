package com.demonicrous.furusato.client;

import com.demonicrous.furusato.asm.FurusatoEarlyConfig;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Applies one consistent, short opening transition to every container screen. */
public final class ContainerAnimationEvents {
    private static final float START_OFFSET = 12.0F;

    private GuiContainer animatedScreen;
    private long openedAtNanos;
    private boolean matrixPushed;

    @SubscribeEvent
    public void onGuiOpened(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiContainer
                && FurusatoEarlyConfig.isContainerAnimationEnabled()) {
            animatedScreen = (GuiContainer) event.getGui();
            openedAtNanos = System.nanoTime();
        } else {
            animatedScreen = null;
            openedAtNanos = 0L;
        }
    }

    @SubscribeEvent
    public void afterBackground(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (event.getGui() != animatedScreen || matrixPushed) {
            return;
        }
        float offset = currentOffset();
        if (offset <= 0.0F) {
            animatedScreen = null;
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, offset, 0.0F);
        matrixPushed = true;
    }

    @SubscribeEvent
    public void afterDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (matrixPushed) {
            GlStateManager.popMatrix();
            matrixPushed = false;
        }
    }

    private float currentOffset() {
        double progress = Math.min(1.0D,
                Math.max(0.0D, (System.nanoTime() - openedAtNanos)
                        / (FurusatoEarlyConfig.getContainerDurationMillis()
                                * 1000000.0D)));
        double remaining = 1.0D - progress;
        return (float) (START_OFFSET * remaining * remaining * remaining);
    }
}
