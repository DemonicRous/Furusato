package com.demonicrous.furusato.client;

import com.demonicrous.furusato.asm.FurusatoEarlyConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

/** Font and GUI scale settings inspired by modern Minecraft versions. */
public final class GuiFontSettings extends GuiScreen {
    private static final int NEXT_SCALE = 11;
    private static final int FORCE_UNICODE = 12;
    private static final int PRESERVE_ODD_SCALE = 13;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;
    private GuiResponsiveButton scaleButton;
    private GuiResponsiveButton unicodeButton;
    private GuiResponsiveButton patchButton;
    private List<List<String>> scaleHelp = Collections.emptyList();
    private List<List<String>> unicodeHelp = Collections.emptyList();
    private List<List<String>> patchHelp = Collections.emptyList();

    public GuiFontSettings(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int top = 42;

        scaleButton = addButton(new GuiResponsiveButton(
                NEXT_SCALE, center - 100, top, 200, 20, ""));
        unicodeButton = addButton(new GuiResponsiveButton(
                FORCE_UNICODE, center - 100, top + 30, 200, 20, ""));
        patchButton = addButton(new GuiResponsiveButton(
                PRESERVE_ODD_SCALE, center - 100, top + 60, 200, 20, ""));
        addButton(new GuiButton(DONE, center - 100, height - 27, 200, 20,
                I18n.format("gui.done")));
        refreshLabels();
        int helpWidth = Math.max(80, Math.min(260, width - 30));
        scaleHelp = createHelp(helpWidth,
                "furusato.font.help.scale.1", "furusato.font.help.scale.2");
        unicodeHelp = createHelp(helpWidth,
                "furusato.font.help.unicode.1", "furusato.font.help.unicode.2");
        patchHelp = createHelp(helpWidth,
                "furusato.font.help.oddScale.1", "furusato.font.help.oddScale.2");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }

        GameSettings settings = mc.gameSettings;
        if (button.id == NEXT_SCALE) {
            settings.guiScale = GuiScalePolicy.next(settings.guiScale);
            applyScale();
        } else if (button.id == FORCE_UNICODE) {
            settings.forceUnicodeFont = !settings.forceUnicodeFont;
            mc.fontRenderer.setUnicodeFlag(mc.getLanguageManager().isCurrentLocaleUnicode()
                    || settings.forceUnicodeFont);
            settings.saveOptions();
            refreshLabels();
        } else if (button.id == PRESERVE_ODD_SCALE) {
            FurusatoEarlyConfig.setUnicodeGuiScaleEnabled(
                    !FurusatoEarlyConfig.isUnicodeGuiScaleEnabled());
            refreshLabels();
        } else if (button.id == DONE) {
            settings.saveOptions();
            mc.displayGuiScreen(parentScreen);
        }
    }

    private void applyScale() {
        mc.gameSettings.saveOptions();
        ScaledResolution resolution = new ScaledResolution(mc);
        setWorldAndResolution(mc, resolution.getScaledWidth(), resolution.getScaledHeight());
    }

    private void refreshLabels() {
        if (scaleButton == null || unicodeButton == null || patchButton == null) {
            return;
        }
        int selected = GuiScalePolicy.normalize(mc.gameSettings.guiScale);
        String selectedText = I18n.format("furusato.font.scale." + selected);
        scaleButton.setFullText(fontRenderer, I18n.format(
                "furusato.font.guiScale.short", selectedText));
        unicodeButton.setFullText(fontRenderer, I18n.format(
                "furusato.font.forceUnicode.short",
                I18n.format(mc.gameSettings.forceUnicodeFont ? "options.on" : "options.off")));
        patchButton.setFullText(fontRenderer, I18n.format(
                "furusato.font.preserveOddScale.short",
                I18n.format(FurusatoEarlyConfig.isUnicodeGuiScaleEnabled()
                        ? "options.on" : "options.off")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("furusato.font.title"),
                width / 2, 18, 0xFFFFFF);

        int previewTop = 145;
        drawRect(width / 2 - 100, previewTop, width / 2 + 100,
                previewTop + 38, 0x88000000);
        drawCenteredString(fontRenderer, I18n.format(
                        "furusato.font.previewMode",
                        I18n.format(mc.gameSettings.forceUnicodeFont
                                ? "furusato.font.mode.unicode"
                                : "furusato.font.mode.default")),
                width / 2, previewTop + 7, 0xA0A0A0);
        drawCenteredString(fontRenderer, I18n.format("furusato.font.sample"),
                width / 2, previewTop + 21, 0xFFFFFF);

        drawCenteredLines(fontRenderer.listFormattedStringToWidth(
                        I18n.format("furusato.font.restartHint"),
                        Math.max(40, width - 30)),
                193, 0x808080);
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawHelpTooltip(mouseX, mouseY);
    }

    private void drawCenteredLines(String text, int top, int color) {
        drawCenteredLines(fontRenderer.listFormattedStringToWidth(
                text, Math.max(40, width - 30)), top, color);
    }

    private void drawCenteredLines(List<String> lines, int top, int color) {
        for (int index = 0; index < lines.size(); index++) {
            drawCenteredString(fontRenderer, lines.get(index),
                    width / 2, top + index * fontRenderer.FONT_HEIGHT, color);
        }
    }

    private void drawHelpTooltip(int mouseX, int mouseY) {
        if (scaleButton != null && scaleButton.isMouseOver(mouseX, mouseY)) {
            drawParagraphTooltip(scaleHelp, mouseX, mouseY);
        } else if (unicodeButton != null && unicodeButton.isMouseOver(mouseX, mouseY)) {
            drawParagraphTooltip(unicodeHelp, mouseX, mouseY);
        } else if (patchButton != null && patchButton.isMouseOver(mouseX, mouseY)) {
            drawParagraphTooltip(patchHelp, mouseX, mouseY);
        }
    }

    private List<List<String>> createHelp(int lineWidth, String... translationKeys) {
        List<List<String>> paragraphs = new ArrayList<>();
        for (String key : translationKeys) {
            // Minecraft's formatter preserves color and style codes on wrapped lines.
            // Each translation key is a separate paragraph, so formatting cannot leak.
            paragraphs.add(fontRenderer.listFormattedStringToWidth(
                    I18n.format(key), lineWidth));
        }
        return paragraphs;
    }

    private void drawParagraphTooltip(
            List<List<String>> paragraphs, int mouseX, int mouseY) {
        if (paragraphs.isEmpty()) {
            return;
        }

        int tooltipWidth = 0;
        int lineCount = 0;
        for (List<String> paragraph : paragraphs) {
            lineCount += paragraph.size();
            for (String line : paragraph) {
                tooltipWidth = Math.max(tooltipWidth, fontRenderer.getStringWidth(line));
            }
        }
        int paragraphGap = 4;
        int tooltipHeight = lineCount * fontRenderer.FONT_HEIGHT
                + Math.max(0, paragraphs.size() - 1) * paragraphGap;
        int left = mouseX + 12;
        int top = mouseY - 12;
        if (left + tooltipWidth + 12 > width) {
            left = mouseX - 12 - tooltipWidth;
        }
        left = Math.max(7, left);
        top = Math.max(7, Math.min(top, height - tooltipHeight - 7));

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        float previousZLevel = zLevel;
        zLevel = 300.0F;
        drawVanillaTooltipBackground(left, top, tooltipWidth, tooltipHeight);

        int textY = top;
        for (int paragraphIndex = 0; paragraphIndex < paragraphs.size(); paragraphIndex++) {
            for (String line : paragraphs.get(paragraphIndex)) {
                fontRenderer.drawStringWithShadow(line, left, textY, 0xFFFFFF);
                textY += fontRenderer.FONT_HEIGHT;
            }
            if (paragraphIndex + 1 < paragraphs.size()) {
                textY += paragraphGap;
            }
        }
        zLevel = previousZLevel;
        GlStateManager.enableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** Matches the background and border used by GuiScreen.drawHoveringText. */
    private void drawVanillaTooltipBackground(
            int left, int top, int tooltipWidth, int tooltipHeight) {
        int background = 0xF0100010;
        int borderTop = 0x505000FF;
        int borderBottom = (borderTop & 0xFEFEFE) >> 1
                | borderTop & 0xFF000000;
        int right = left + tooltipWidth;
        int bottom = top + tooltipHeight;
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

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.gameSettings.saveOptions();
            mc.displayGuiScreen(parentScreen);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        mc.gameSettings.saveOptions();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
