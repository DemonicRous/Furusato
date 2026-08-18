package com.demonicrous.furusato.client;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

/** A vanilla-looking button that keeps long labels inside its bounds. */
final class GuiResponsiveButton extends GuiButton {
    private String fullText = "";
    private boolean trimmed;

    GuiResponsiveButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
        fullText = text;
    }

    void setFullText(FontRenderer renderer, String text) {
        fullText = text == null ? "" : text;
        int availableWidth = Math.max(0, width - 8);
        if (renderer.getStringWidth(fullText) <= availableWidth) {
            displayString = fullText;
            trimmed = false;
            return;
        }

        String ellipsis = "...";
        int textWidth = Math.max(0, availableWidth - renderer.getStringWidth(ellipsis));
        displayString = renderer.trimStringToWidth(fullText, textWidth) + ellipsis;
        trimmed = true;
    }

    boolean hasHiddenText() {
        return trimmed;
    }

    String getFullText() {
        return fullText;
    }

    boolean isMouseOver(int mouseX, int mouseY) {
        return visible && mouseX >= x && mouseY >= y
                && mouseX < x + width && mouseY < y + height;
    }
}
