package com.demonicrous.furusato.client;

import com.demonicrous.furusato.asm.FurusatoEarlyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/** Time-based position state used by the transformed vanilla hotbar selector. */
public final class HotbarAnimation {
    private static final ResourceLocation WIDGETS =
            new ResourceLocation("textures/gui/widgets.png");
    private static final Gui DRAWER = new Gui();
    private static final long EXTRA_SLOT_DURATION_NANOS = 20000000L;
    private static final long PULSE_DURATION_NANOS = 120000000L;
    private static final long STALE_STATE_NANOS = 500000000L;
    private static final int SLOT_SPACING = 20;
    private static final int MAX_GLIDE_SLOTS = 2;
    private static double displayedX = Double.NaN;
    private static double startX;
    private static int targetX;
    private static long transitionStartedNanos;
    private static long transitionDurationNanos;
    private static long pulseStartedNanos;
    private static long lastFrameNanos;
    private static int selectorX;
    private static int selectorY;
    private static boolean selectorPending;

    private HotbarAnimation() {
    }

    public static int adjustSelectorX(int targetX) {
        if (!FurusatoEarlyConfig.isHotbarAnimationEnabled()) {
            displayedX = targetX;
            HotbarAnimation.targetX = targetX;
            transitionStartedNanos = 0L;
            pulseStartedNanos = 0L;
            lastFrameNanos = System.nanoTime();
            return targetX;
        }
        long now = System.nanoTime();
        if (Double.isNaN(displayedX) || lastFrameNanos == 0L
                || now - lastFrameNanos > STALE_STATE_NANOS) {
            displayedX = targetX;
            startX = targetX;
            HotbarAnimation.targetX = targetX;
            transitionStartedNanos = 0L;
        } else if (targetX != HotbarAnimation.targetX) {
            int distance = Math.max(1, Math.round(
                    Math.abs(targetX - HotbarAnimation.targetX)
                            / (float) SLOT_SPACING));
            HotbarAnimation.targetX = targetX;
            if (distance > MAX_GLIDE_SLOTS) {
                displayedX = targetX;
                startX = targetX;
                transitionStartedNanos = 0L;
                pulseStartedNanos = now;
            } else {
                startX = displayedX;
                transitionStartedNanos = now;
                transitionDurationNanos = FurusatoEarlyConfig
                        .getHotbarDurationMillis() * 1000000L
                        + (distance - 1L) * EXTRA_SLOT_DURATION_NANOS;
            }
        }
        updatePosition(now);
        lastFrameNanos = now;
        return (int) Math.round(displayedX);
    }

    private static void updatePosition(long now) {
        if (transitionStartedNanos == 0L) {
            return;
        }
        double progress = Math.min(1.0D, Math.max(0.0D,
                (now - transitionStartedNanos) / (double) transitionDurationNanos));
        double remaining = 1.0D - progress;
        double eased = 1.0D - remaining * remaining * remaining;
        displayedX = startX + (targetX - startX) * eased;
        if (progress >= 1.0D) {
            displayedX = targetX;
            transitionStartedNanos = 0L;
        }
    }

    public static void deferSelector(Gui ignored, int x, int y,
            int textureX, int textureY, int width, int height) {
        selectorX = x;
        selectorY = y;
        selectorPending = true;
    }

    static void renderDeferred(Minecraft minecraft) {
        if (!selectorPending) {
            return;
        }
        selectorPending = false;
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(WIDGETS);
        float scale = pulseScale(System.nanoTime());
        if (scale != 1.0F) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(selectorX + 12.0F, selectorY + 11.0F, 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            GlStateManager.translate(-selectorX - 12.0F,
                    -selectorY - 11.0F, 0.0F);
        }
        DRAWER.drawTexturedModalRect(selectorX, selectorY, 0, 22, 24, 22);
        if (scale != 1.0F) {
            GlStateManager.popMatrix();
        }
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
    }

    private static float pulseScale(long now) {
        if (!FurusatoEarlyConfig.isHotbarPulseEnabled()
                || pulseStartedNanos == 0L) {
            return 1.0F;
        }
        double progress = Math.min(1.0D, Math.max(0.0D,
                (now - pulseStartedNanos) / (double) PULSE_DURATION_NANOS));
        if (progress >= 1.0D) {
            pulseStartedNanos = 0L;
            return 1.0F;
        }
        double remaining = 1.0D - progress;
        return (float) (1.0D + 0.08D
                * remaining * remaining * remaining);
    }
}
