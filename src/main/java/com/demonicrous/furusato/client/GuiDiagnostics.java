package com.demonicrous.furusato.client;

import com.demonicrous.furusato.Furusato;
import com.demonicrous.furusato.asm.CompatibilityDiagnostics;
import com.demonicrous.furusato.asm.FurusatoEarlyConfig;
import com.demonicrous.furusato.asm.PatchDiagnostics;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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
    private static final int EXPORT_REPORT = 11;
    private static final int OVERVIEW_TAB = 20;
    private static final int RENDERING_TAB = 21;
    private static final int COMPATIBILITY_TAB = 22;
    private static final int DONE = 200;
    private static final int PANEL_PADDING = 10;
    private static final String UNICODE_PATCH = "unicode_gui_scale";

    private final GuiScreen parentScreen;
    private final List<Row> rows = new ArrayList<Row>();
    private String report = "";
    private String copyLabelKey = "furusato.diagnostics.copy";
    private String exportLabelKey = "furusato.diagnostics.export";
    private Category activeCategory = Category.OVERVIEW;

    public GuiDiagnostics(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        refreshDiagnostics();
        int center = width / 2;
        int contentWidth = Math.min(620, Math.max(280, width - 32));
        int contentLeft = (width - contentWidth) / 2;
        int panelHeight = 4 * 14 + PANEL_PADDING * 2;
        int groupHeight = panelHeight + 46;
        int groupTop = Math.max(8, (height - 55 - groupHeight) / 2);
        int tabTop = groupTop + 20;
        int gap = 4;
        int tabWidth = (contentWidth - gap * 2) / 3;
        addCategoryButton(OVERVIEW_TAB, Category.OVERVIEW,
                contentLeft, tabTop, tabWidth, "furusato.diagnostics.tab.overview");
        addCategoryButton(RENDERING_TAB, Category.RENDERING,
                contentLeft + tabWidth + gap, tabTop, tabWidth,
                "furusato.diagnostics.tab.rendering");
        addCategoryButton(COMPATIBILITY_TAB, Category.COMPATIBILITY,
                contentLeft + (tabWidth + gap) * 2, tabTop, tabWidth,
                "furusato.diagnostics.tab.compatibility");
        addButton(new GuiButton(COPY_REPORT, center - 100, height - 51, 98, 20,
                I18n.format(copyLabelKey)));
        addButton(new GuiButton(EXPORT_REPORT, center + 2, height - 51, 98, 20,
                I18n.format(exportLabelKey)));
        addButton(new GuiButton(DONE, center - 100, height - 27, 200, 20,
                I18n.format("gui.done")));
    }

    private void addCategoryButton(int id, Category category, int x, int y,
            int buttonWidth, String translationKey) {
        GuiResponsiveButton button = addButton(new GuiResponsiveButton(
                id, x, y, buttonWidth, 20, ""));
        button.setFullText(fontRenderer, I18n.format(translationKey));
        button.enabled = activeCategory != category;
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
        boolean safeMode = FurusatoEarlyConfig.isSafeModeEnabled();
        List<String> transformers = CompatibilityDiagnostics.transformerClassNames();
        List<String> thirdParty = CompatibilityDiagnostics.thirdPartyTransformerClassNames();
        String health = healthFor(patchStatus, unicodeResources, safeMode);

        addRow(Category.OVERVIEW,
                "furusato.diagnostics.version", Furusato.VERSION, 0xFFFFFF);
        addRow(Category.OVERVIEW, "furusato.diagnostics.environment",
                "Minecraft 1.12.2 / Forge " + ForgeVersion.getVersion(), 0xFFFFFF);
        addRow(Category.RENDERING,
                "furusato.diagnostics.patch", localizeStatus(patchStatus),
                statusColor(patchStatus));
        addRow(Category.COMPATIBILITY, "furusato.diagnostics.safeMode",
                I18n.format(safeMode ? "options.on" : "options.off"),
                safeMode ? 0xFFAA00 : 0xAAAAAA);
        addRow(Category.RENDERING, "furusato.diagnostics.unicodeFont",
                I18n.format(mc.gameSettings.forceUnicodeFont ? "options.on" : "options.off"),
                mc.gameSettings.forceUnicodeFont ? 0x55FF55 : 0xAAAAAA);
        addRow(Category.RENDERING, "furusato.diagnostics.guiScale",
                I18n.format("furusato.font.scale." + selectedScale)
                        + " [" + effectiveScale + "]", 0xFFFFFF);
        addRow(Category.RENDERING, "furusato.diagnostics.resources",
                I18n.format(unicodeResources
                        ? "furusato.diagnostics.available"
                        : "furusato.diagnostics.missing"),
                unicodeResources ? 0x55FF55 : 0xFF5555);
        addRow(Category.OVERVIEW, "furusato.diagnostics.config",
                config == null ? I18n.format("furusato.diagnostics.unavailable")
                        : configPath, config == null ? 0xFF5555 : 0xAAAAAA);
        addRow(Category.COMPATIBILITY, "furusato.diagnostics.transformers",
                Integer.toString(thirdParty.size()),
                thirdParty.isEmpty() ? 0x55FF55 : 0xFFAA00);
        addRow(Category.OVERVIEW, "furusato.diagnostics.health", I18n.format(health),
                "furusato.diagnostics.ok".equals(health) ? 0x55FF55 : 0xFFAA00);

        report = buildReport(patchStatus, patch, config, selectedScale,
                effectiveScale, unicodeResources, safeMode, health, configPath,
                transformers, thirdParty);
    }

    private void addRow(Category category, String labelKey, String value, int color) {
        rows.add(new Row(category, I18n.format(labelKey), value, color));
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

    private String healthFor(String patchStatus, boolean resourcesAvailable,
            boolean safeMode) {
        if (!resourcesAvailable || "FAILED".equals(patchStatus)
                || "SKIPPED".equals(patchStatus) || "UNKNOWN".equals(patchStatus)
                || safeMode) {
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
            boolean unicodeResources, boolean safeMode, String healthKey,
            String configPath, List<String> transformers, List<String> thirdParty) {
        String detail = patch == null ? "unavailable" : patch.getDetail();
        return "Furusato " + Furusato.VERSION + "\n"
                + "Minecraft 1.12.2 / Forge " + ForgeVersion.getVersion() + "\n"
                + "unicode_gui_scale: " + patchStatus + " (" + detail + ")\n"
                + "safeMode: " + safeMode + "\n"
                + "forceUnicodeFont: " + mc.gameSettings.forceUnicodeFont + "\n"
                + "guiScale: " + selectedScale + " (effective: " + effectiveScale + ")\n"
                + "unicodeResources: " + unicodeResources + "\n"
                + "config: " + (config == null ? "unavailable" : configPath) + "\n"
                + "transformers: " + join(transformers) + "\n"
                + "thirdPartyTransformers: " + join(thirdParty) + "\n"
                + "health: " + I18n.format(healthKey);
    }

    private String join(List<String> values) {
        if (values.isEmpty()) {
            return "none";
        }
        StringBuilder joined = new StringBuilder();
        for (String value : values) {
            if (joined.length() > 0) {
                joined.append(", ");
            }
            joined.append(value);
        }
        return joined.toString();
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
        } else if (button.id == EXPORT_REPORT) {
            if (exportReport()) {
                exportLabelKey = "furusato.diagnostics.exported";
            } else {
                exportLabelKey = "furusato.diagnostics.exportFailed";
            }
            button.displayString = I18n.format(exportLabelKey);
        } else if (button.id == OVERVIEW_TAB) {
            selectCategory(Category.OVERVIEW);
        } else if (button.id == RENDERING_TAB) {
            selectCategory(Category.RENDERING);
        } else if (button.id == COMPATIBILITY_TAB) {
            selectCategory(Category.COMPATIBILITY);
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    private void selectCategory(Category category) {
        activeCategory = category;
        initGui();
    }

    private boolean exportReport() {
        File configFile = FurusatoEarlyConfig.getConfigurationFile();
        File configDirectory = configFile == null ? null : configFile.getParentFile();
        File gameDirectory = configDirectory == null
                ? new File(".") : configDirectory.getParentFile();
        if (gameDirectory == null) {
            gameDirectory = new File(".");
        }
        File reportFile = new File(new File(gameDirectory, "logs"),
                "furusato-diagnostics.txt");
        File parent = reportFile.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            return false;
        }
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(reportFile), StandardCharsets.UTF_8)) {
            writer.write(report);
            writer.write(System.lineSeparator());
            return true;
        } catch (IOException error) {
            if (Furusato.getLogger() != null) {
                Furusato.getLogger().error("Could not export diagnostics to {}",
                        reportFile, error);
            }
            return false;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int contentWidth = Math.min(620, Math.max(280, width - 32));
        int contentLeft = (width - contentWidth) / 2;
        int contentRight = contentLeft + contentWidth;
        int rowHeight = 14;
        int panelHeight = 4 * rowHeight + PANEL_PADDING * 2;
        int groupHeight = panelHeight + 46;
        int groupTop = Math.max(8, (height - 55 - groupHeight) / 2);
        int panelTop = groupTop + 46;

        drawCenteredString(fontRenderer, I18n.format("furusato.diagnostics.title"),
                width / 2, groupTop, 0xFFFFFF);

        drawTooltipStylePanel(contentLeft + 6, panelTop + 6,
                contentWidth - 12, panelHeight - 12);

        List<Row> visibleRows = visibleRows();
        int labelWidth = 0;
        for (Row row : visibleRows) {
            labelWidth = Math.max(labelWidth,
                    fontRenderer.getStringWidth(row.label + ":"));
        }
        labelWidth = Math.min(labelWidth, contentWidth / 2 - 12);
        int labelX = contentLeft + PANEL_PADDING;
        int valueX = labelX + labelWidth + 18;
        int maxValueWidth = Math.max(60,
                contentRight - valueX - PANEL_PADDING);
        for (int index = 0; index < visibleRows.size(); index++) {
            Row row = visibleRows.get(index);
            int rowTop = panelTop + PANEL_PADDING + index * rowHeight;
            if ((index & 1) == 1) {
                drawRect(contentLeft + 2, rowTop, contentRight - 2,
                        rowTop + rowHeight, 0x10000000);
            }
            if (mouseX >= contentLeft && mouseX < contentRight
                    && mouseY >= rowTop && mouseY < rowTop + rowHeight) {
                drawRect(contentLeft + 2, rowTop, contentRight - 2,
                        rowTop + rowHeight, 0x22FFFFFF);
            }
            int textY = rowTop + (rowHeight - fontRenderer.FONT_HEIGHT) / 2 + 1;
            drawString(fontRenderer, row.label + ":", labelX, textY, 0xA0A0A0);
            drawString(fontRenderer, fitText(row.value, maxValueWidth),
                    valueX, textY, row.color);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private List<Row> visibleRows() {
        List<Row> visible = new ArrayList<Row>();
        for (Row row : rows) {
            if (row.category == activeCategory) {
                visible.add(row);
            }
        }
        return visible;
    }

    /** Same background, border colors and padding as Furusato's tooltips. */
    private void drawTooltipStylePanel(
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
        private final Category category;
        private final String label;
        private final String value;
        private final int color;

        private Row(Category category, String label, String value, int color) {
            this.category = category;
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private enum Category {
        OVERVIEW,
        RENDERING,
        COMPATIBILITY
    }
}
