package com.demonicrous.furusato.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

/** Shared vanilla-style rendering primitives for Furusato configuration screens. */
abstract class GuiFurusatoScreen extends GuiScreen {
    protected final int responsiveContentWidth(int minimum, int maximum, int margin) {
        return FurusatoGuiLayout.contentWidth(width, minimum, maximum, margin);
    }

    protected final void beginScissor(int left, int top, int right, int bottom) {
        int scale = new ScaledResolution(mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(left * scale, mc.displayHeight - bottom * scale,
                Math.max(0, right - left) * scale,
                Math.max(0, bottom - top) * scale);
    }

    protected final void endScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    /** Matches the background, border colors and padding of vanilla tooltips. */
    protected final void drawFurusatoPanel(
            int left, int top, int panelWidth, int panelHeight) {
        int background = 0xF0100010;
        int borderTop = 0x505000FF;
        int borderBottom = (borderTop & 0xFEFEFE) >> 1
                | borderTop & 0xFF000000;
        int right = left + panelWidth;
        int bottom = top + panelHeight;
        int padding = 2;
        int boxLeft = left - 4 - padding;
        int boxTop = top - 4 - padding;
        int boxRight = right + 4 + padding;
        int boxBottom = bottom + 4 + padding;

        drawGradientRect(boxLeft + 1, boxTop, boxRight - 1, boxTop + 1,
                background, background);
        drawGradientRect(boxLeft + 1, boxBottom - 1, boxRight - 1, boxBottom,
                background, background);
        drawGradientRect(boxLeft + 1, boxTop + 1, boxRight - 1, boxBottom - 1,
                background, background);
        drawGradientRect(boxLeft, boxTop + 1, boxLeft + 1, boxBottom - 1,
                background, background);
        drawGradientRect(boxRight - 1, boxTop + 1, boxRight, boxBottom - 1,
                background, background);
        drawGradientRect(boxLeft + 1, boxTop + 2, boxLeft + 2, boxBottom - 2,
                borderTop, borderBottom);
        drawGradientRect(boxRight - 2, boxTop + 2, boxRight - 1, boxBottom - 2,
                borderTop, borderBottom);
        drawGradientRect(boxLeft + 1, boxTop + 1, boxRight - 1, boxTop + 2,
                borderTop, borderTop);
        drawGradientRect(boxLeft + 1, boxBottom - 2, boxRight - 1, boxBottom - 1,
                borderBottom, borderBottom);
    }
}
