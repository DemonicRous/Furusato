package com.demonicrous.furusato.client;

import com.demonicrous.furusato.Furusato;
import com.demonicrous.furusato.asm.FurusatoEarlyConfig;
import com.demonicrous.furusato.asm.PatchDiagnostics;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeVersion;

/** Read-only runtime diagnostics and a copyable support report. */
public final class GuiDiagnostics extends GuiScreen {
    private static final int COPY_REPORT = 10;
    private static final int DONE = 200;
    private static final String UNICODE_PATCH = "unicode_gui_scale";

    private final GuiScreen parentScreen;
    private final List<Row> rows = new ArrayList<Row>();
    private String report = "";
    private String copyLabelKey = "furusato.diagnostics.copy";

    public GuiDiagnostics(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        refreshDiagnostics();
        int center = width / 2;
        addButton(new GuiButton(COPY_REPORT, center - 100, height - 51, 200, 20,
                I18n.format(copyLabelKey)));
        addButton(new GuiButton(DONE, center - 100, height - 27, 200, 20,
                I18n.format("gui.done")));
    }

    private void refreshDiagnostics() {
        rows.clear();
        Map<String, PatchDiagnostics.Result> patches = PatchDiagnostics.snapshot();
        PatchDiagnostics.Result patch = patches.get(UNICODE_PATCH);
        String patchStatus = patch == null ? "UNKNOWN" : patch.getStatus();
        File config = FurusatoEarlyConfig.getConfigurationFile();
        String configPath = relativeConfigPath(config);
        int selectedScale = GuiScalePolicy.normalize(mc.gameSettings.guiScale);
        int effectiveScale = new ScaledResolution(mc).getScaleFactor();
        boolean unicodeResources = hasUnicodeResources();
        String health = healthFor(patchStatus, unicodeResources);

        addRow("furusato.diagnostics.version", Furusato.VERSION, 0xFFFFFF);
        addRow("furusato.diagnostics.environment",
                "Minecraft 1.12.2 / Forge " + ForgeVersion.getVersion(), 0xFFFFFF);
        addRow("furusato.diagnostics.patch", localizeStatus(patchStatus),
                statusColor(patchStatus));
        addRow("furusato.diagnostics.unicodeFont",
                I18n.format(mc.gameSettings.forceUnicodeFont ? "options.on" : "options.off"),
                mc.gameSettings.forceUnicodeFont ? 0x55FF55 : 0xAAAAAA);
        addRow("furusato.diagnostics.guiScale",
                I18n.format("furusato.font.scale." + selectedScale)
                        + " [" + effectiveScale + "]", 0xFFFFFF);
        addRow("furusato.diagnostics.resources",
                I18n.format(unicodeResources
                        ? "furusato.diagnostics.available"
                        : "furusato.diagnostics.missing"),
                unicodeResources ? 0x55FF55 : 0xFF5555);
        addRow("furusato.diagnostics.config",
                config == null ? I18n.format("furusato.diagnostics.unavailable")
                        : configPath, config == null ? 0xFF5555 : 0xAAAAAA);
        addRow("furusato.diagnostics.health", I18n.format(health),
                "furusato.diagnostics.ok".equals(health) ? 0x55FF55 : 0xFFAA00);

        report = buildReport(patchStatus, patch, config, selectedScale,
                effectiveScale, unicodeResources, health, configPath);
    }

    private void addRow(String labelKey, String value, int color) {
        rows.add(new Row(I18n.format(labelKey), value, color));
    }

    private boolean hasUnicodeResources() {
        try {
            mc.getResourceManager().getResource(
                    new ResourceLocation("minecraft", "font/glyph_sizes.bin"));
            mc.getResourceManager().getResource(
                    new ResourceLocation("minecraft", "textures/font/unicode_page_00.png"));
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private String healthFor(String patchStatus, boolean resourcesAvailable) {
        if (!resourcesAvailable || "FAILED".equals(patchStatus)
                || "SKIPPED".equals(patchStatus) || "UNKNOWN".equals(patchStatus)) {
            return "furusato.diagnostics.warning";
        }
        return "furusato.diagnostics.ok";
    }

    private String localizeStatus(String status) {
        String key = "furusato.diagnostics.status." + status.toLowerCase();
        return I18n.hasKey(key) ? I18n.format(key) : status;
    }

    private int statusColor(String status) {
        if ("APPLIED".equals(status)) {
            return 0x55FF55;
        }
        if ("FAILED".equals(status) || "SKIPPED".equals(status)) {
            return 0xFF5555;
        }
        if ("DISABLED".equals(status)) {
            return 0xAAAAAA;
        }
        return 0xFFAA00;
    }

    private String buildReport(String patchStatus, PatchDiagnostics.Result patch,
            File config, int selectedScale, int effectiveScale,
            boolean unicodeResources, String healthKey, String configPath) {
        String detail = patch == null ? "unavailable" : patch.getDetail();
        return "Furusato " + Furusato.VERSION + "\n"
                + "Minecraft 1.12.2 / Forge " + ForgeVersion.getVersion() + "\n"
                + "unicode_gui_scale: " + patchStatus + " (" + detail + ")\n"
                + "forceUnicodeFont: " + mc.gameSettings.forceUnicodeFont + "\n"
                + "guiScale: " + selectedScale + " (effective: " + effectiveScale + ")\n"
                + "unicodeResources: " + unicodeResources + "\n"
                + "config: " + (config == null ? "unavailable" : configPath) + "\n"
                + "health: " + I18n.format(healthKey);
    }

    private String relativeConfigPath(File config) {
        if (config == null) {
            return "unavailable";
        }
        File parent = config.getParentFile();
        return (parent == null ? "config" : parent.getName()) + "/" + config.getName();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }
        if (button.id == COPY_REPORT) {
            setClipboardString(report);
            copyLabelKey = "furusato.diagnostics.copied";
            button.displayString = I18n.format(copyLabelKey);
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int contentWidth = Math.min(620, Math.max(280, width - 32));
        int contentLeft = (width - contentWidth) / 2;
        int contentRight = contentLeft + contentWidth;
        int rowHeight = 16;
        int panelHeight = rows.size() * rowHeight + 16;
        int groupHeight = panelHeight + 25;
        int groupTop = Math.max(10, (height - 60 - groupHeight) / 2);
        int panelTop = groupTop + 25;

        drawCenteredString(fontRenderer, I18n.format("furusato.diagnostics.title"),
                width / 2, groupTop, 0xFFFFFF);

        drawRect(contentLeft - 2, panelTop - 2, contentRight + 2,
                panelTop + panelHeight + 2, 0x88000000);
        drawRect(contentLeft, panelTop, contentRight,
                panelTop + panelHeight, 0x66000000);

        int labelWidth = 0;
        for (Row row : rows) {
            labelWidth = Math.max(labelWidth,
                    fontRenderer.getStringWidth(row.label + ":"));
        }
        labelWidth = Math.min(labelWidth, contentWidth / 2 - 12);
        int labelX = contentLeft + 10;
        int valueX = labelX + labelWidth + 18;
        int maxValueWidth = Math.max(60, contentRight - valueX - 10);

        for (int index = 0; index < rows.size(); index++) {
            Row row = rows.get(index);
            int rowTop = panelTop + 8 + index * rowHeight;
            if (mouseX >= contentLeft && mouseX < contentRight
                    && mouseY >= rowTop - 3 && mouseY < rowTop + rowHeight - 3) {
                drawRect(contentLeft + 2, rowTop - 3, contentRight - 2,
                        rowTop + rowHeight - 3, 0x22FFFFFF);
            } else if ((index & 1) == 1) {
                drawRect(contentLeft + 2, rowTop - 3, contentRight - 2,
                        rowTop + rowHeight - 3, 0x10000000);
            }
            drawString(fontRenderer, row.label + ":", labelX, rowTop, 0xA0A0A0);
            drawString(fontRenderer, fitText(row.value, maxValueWidth),
                    valueX, rowTop, row.color);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String fitText(String text, int maxWidth) {
        if (fontRenderer.getStringWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int available = Math.max(0, maxWidth - fontRenderer.getStringWidth(ellipsis));
        return fontRenderer.trimStringToWidth(text, available) + ellipsis;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parentScreen);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static final class Row {
        private final String label;
        private final String value;
        private final int color;

        private Row(String label, String value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }
}
