package com.demonicrous.furusato.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/** Time-based position state used by the transformed vanilla hotbar selector. */
public final class HotbarAnimation {
    private static final ResourceLocation WIDGETS =
            new ResourceLocation("textures/gui/widgets.png");
    private static final Gui DRAWER = new Gui();
    private static final double SPEED = 18.0D;
    private static double displayedX = Double.NaN;
    private static long lastFrameNanos;
    private static int selectorX;
    private static int selectorY;
    private static boolean selectorPending;

    private HotbarAnimation() {
    }

    public static int adjustSelectorX(int targetX) {
        long now = System.nanoTime();
        if (Double.isNaN(displayedX) || lastFrameNanos == 0L
                || now - lastFrameNanos > 500000000L) {
            displayedX = targetX;
        } else {
            double seconds = Math.min(0.05D,
                    Math.max(0.0D, (now - lastFrameNanos) / 1.0E9D));
            double blend = 1.0D - Math.exp(-SPEED * seconds);
            displayedX += (targetX - displayedX) * blend;
            if (Math.abs(targetX - displayedX) < 0.05D) {
                displayedX = targetX;
            }
        }
        lastFrameNanos = now;
        return (int) Math.round(displayedX);
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
        DRAWER.drawTexturedModalRect(selectorX, selectorY, 0, 22, 24, 22);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
    }
}
