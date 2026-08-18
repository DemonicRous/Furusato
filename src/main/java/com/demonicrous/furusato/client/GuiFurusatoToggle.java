package com.demonicrous.furusato.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Standalone toggle using the same visual language as Furusato sliders. */
final class GuiFurusatoToggle extends GuiButton {
    private static final int TRACK_WIDTH = 34;
    private boolean value;

    GuiFurusatoToggle(int id, int x, int y, int width, boolean initialValue) {
        super(id, x, y, width, 20, "");
        value = initialValue;
    }

    boolean getValue() {
        return value;
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (!super.mousePressed(minecraft, mouseX, mouseY)) {
            return false;
        }
        value = !value;
        return true;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY,
            float partialTicks) {
        if (!visible) {
            return;
        }
        hovered = mouseX >= x && mouseY >= y
                && mouseX < x + width && mouseY < y + height;
        int trackLeft = x + width - TRACK_WIDTH - 2;
        int trackTop = y + height / 2 - 3;
        int trackRight = trackLeft + TRACK_WIDTH;
        drawRect(trackLeft, trackTop, trackRight, trackTop + 6, 0xFF202020);
        drawRect(trackLeft + 1, trackTop + 1, trackRight - 1, trackTop + 5,
                value ? 0xFF7070D8 : 0xFF606060);
        int handleCenter = value ? trackRight - 5 : trackLeft + 5;
        int handleColor = hovered ? 0xFFFFFFFF : 0xFFD0D0D0;
        drawRect(handleCenter - 4, trackTop - 5,
                handleCenter + 5, trackTop + 11, 0xFF000000);
        drawRect(handleCenter - 3, trackTop - 4,
                handleCenter + 4, trackTop + 10, handleColor);
        String label = displayString;
        int labelWidth = Math.max(0, trackLeft - x - 8);
        if (minecraft.fontRenderer.getStringWidth(label) > labelWidth) {
            String ellipsis = "...";
            label = minecraft.fontRenderer.trimStringToWidth(label,
                    Math.max(0, labelWidth
                            - minecraft.fontRenderer.getStringWidth(ellipsis)))
                    + ellipsis;
        }
        minecraft.fontRenderer.drawStringWithShadow(label,
                x + 2, y + 6, enabled ? 0xFFFFFF : 0xA0A0A0);
    }
}
