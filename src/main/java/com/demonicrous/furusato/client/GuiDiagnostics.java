package com.demonicrous.furusato.client;

import com.demonicrous.furusato.Furusato;
import com.demonicrous.furusato.asm.CompatibilityDiagnostics;
import com.demonicrous.furusato.asm.FurusatoEarlyConfig;
import com.demonicrous.furusato.asm.PatchDiagnostics;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeVersion;
import org.lwjgl.input.Mouse;

/** Read-only runtime diagnostics and a copyable support report. */
public final class GuiDiagnostics extends GuiFurusatoScreen {
    private static final int COPY_REPORT = 10;
    private static final int EXPORT_REPORT = 11;
    private static final int OPEN_REPORTS = 12;
    private static final int OVERVIEW_TAB = 20;
    private static final int RENDERING_TAB = 21;
    private static final int COMPATIBILITY_TAB = 22;
    private static final int ISSUES_TAB = 23;
    private static final int DONE = 200;
    private static final int PANEL_PADDING = 10;
    private static final String UNICODE_PATCH = "unicode_gui_scale";

    private final GuiScreen parentScreen;
    private final List<Row> rows = new ArrayList<Row>();
    private String report = "";
    private String copyLabelKey = "furusato.diagnostics.copy";
    private String exportLabelKey = "furusato.diagnostics.export";
    private String openLabelKey = "furusato.diagnostics.openFolder";
    private Category activeCategory = Category.OVERVIEW;
    private int scrollOffset;

    public GuiDiagnostics(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        refreshDiagnostics();
        int center = width / 2;
        boolean compact = isCompactLayout();
        int contentWidth = responsiveContentWidth(280, compact ? 380 : 500, 12);
        int contentLeft = (width - contentWidth) / 2;
        int headerHeight = compact ? 82 : 58;
        int panelHeight = panelHeight(headerHeight);
        int groupHeight = panelHeight + headerHeight;
        int footerHeight = 52;
        int groupTop = Math.max(8, (height - groupHeight - footerHeight) / 2);
        int cardLeft = contentLeft + 8;
        int cardWidth = contentWidth - 16;
        int tabTop = groupTop + 26;
        int gap = 4;
        if (compact) {
            int tabWidth = (cardWidth - gap) / 2;
            addCategoryButton(OVERVIEW_TAB, Category.OVERVIEW,
                    cardLeft, tabTop, tabWidth, "furusato.diagnostics.tab.overview");
            addCategoryButton(RENDERING_TAB, Category.RENDERING,
                    cardLeft + tabWidth + gap, tabTop, tabWidth,
                    "furusato.diagnostics.tab.rendering");
            addCategoryButton(COMPATIBILITY_TAB, Category.COMPATIBILITY,
                    cardLeft, tabTop + 24, tabWidth,
                    "furusato.diagnostics.tab.compatibility");
            addCategoryButton(ISSUES_TAB, Category.ISSUES,
                    cardLeft + tabWidth + gap, tabTop + 24, tabWidth,
                    "furusato.diagnostics.tab.issues");
        } else {
            int tabWidth = (cardWidth - gap * 3) / 4;
            addCategoryButton(OVERVIEW_TAB, Category.OVERVIEW,
                    cardLeft, tabTop, tabWidth, "furusato.diagnostics.tab.overview");
            addCategoryButton(RENDERING_TAB, Category.RENDERING,
                    cardLeft + tabWidth + gap, tabTop, tabWidth,
                    "furusato.diagnostics.tab.rendering");
            addCategoryButton(COMPATIBILITY_TAB, Category.COMPATIBILITY,
                    cardLeft + (tabWidth + gap) * 2, tabTop, tabWidth,
                    "furusato.diagnostics.tab.compatibility");
            addCategoryButton(ISSUES_TAB, Category.ISSUES,
                    cardLeft + (tabWidth + gap) * 3, tabTop, tabWidth,
                    "furusato.diagnostics.tab.issues");
        }
        int actionTop = groupTop + groupHeight + 8;
        int actionWidth = Math.min(360, contentWidth);
        int actionLeft = center - actionWidth / 2;
        int actionGap = 4;
        int actionButtonWidth = (actionWidth - actionGap * 2) / 3;
        addButton(new GuiResponsiveButton(COPY_REPORT, actionLeft, actionTop,
                actionButtonWidth, 20,
                I18n.format(copyLabelKey)));
        addButton(new GuiResponsiveButton(EXPORT_REPORT,
                actionLeft + actionButtonWidth + actionGap, actionTop,
                actionButtonWidth, 20,
                I18n.format(exportLabelKey)));
        addButton(new GuiResponsiveButton(OPEN_REPORTS,
                actionLeft + (actionButtonWidth + actionGap) * 2, actionTop,
                actionButtonWidth, 20,
                I18n.format(openLabelKey)));
        addButton(new GuiButton(DONE, actionLeft, actionTop + 24, actionWidth, 20,
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
        boolean configuredPatch = FurusatoEarlyConfig.isUnicodeGuiScaleEnabled();
        boolean activePatch = FurusatoEarlyConfig.isActiveUnicodeGuiScaleEnabled();
        boolean restartPending = FurusatoEarlyConfig.isRestartPending();
        List<String> transformers = CompatibilityDiagnostics.transformerClassNames();
        List<String> thirdParty = CompatibilityDiagnostics.thirdPartyTransformerClassNames();
        List<DiagnosticAssessment.Finding> findings = DiagnosticAssessment.assess(
                patchStatus, unicodeResources, safeMode, thirdParty.size());
        String health = findings.isEmpty()
                ? "furusato.diagnostics.ok" : "furusato.diagnostics.warning";

        addRow(Category.OVERVIEW,
                "furusato.diagnostics.version", Furusato.VERSION, 0xFFFFFF);
        addRow(Category.OVERVIEW, "furusato.diagnostics.environment",
                "Minecraft 1.12.2 / Forge " + ForgeVersion.getVersion(), 0xFFFFFF);
        addRow(Category.RENDERING,
                "furusato.diagnostics.patch", localizeStatus(patchStatus),
                statusColor(patchStatus));
        addRow(Category.RENDERING, "furusato.diagnostics.patchConfigured",
                I18n.format(configuredPatch ? "options.on" : "options.off"), 0xFFFFFF);
        addRow(Category.RENDERING, "furusato.diagnostics.patchActive",
                I18n.format(activePatch ? "options.on" : "options.off"), 0xFFFFFF);
        addRow(Category.OVERVIEW, "furusato.diagnostics.restartPending",
                I18n.format(restartPending ? "options.on" : "options.off"),
                restartPending ? 0xFFAA00 : 0x55FF55);
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

        if (findings.isEmpty()) {
            addRow(Category.ISSUES, "furusato.diagnostics.health",
                    I18n.format("furusato.diagnostics.ok"), 0x55FF55);
        } else {
            for (DiagnosticAssessment.Finding finding : findings) {
                addRow(Category.ISSUES, finding.titleKey(),
                        I18n.format(finding.adviceKey()), 0xFFAA00);
            }
        }

        report = buildReport(patchStatus, patch, config, selectedScale,
                effectiveScale, unicodeResources, safeMode, health, configPath,
                transformers, thirdParty, findings, configuredPatch, activePatch,
                restartPending);
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
            String configPath, List<String> transformers, List<String> thirdParty,
            List<DiagnosticAssessment.Finding> findings, boolean configuredPatch,
            boolean activePatch, boolean restartPending) {
        String detail = patch == null ? "unavailable" : patch.getDetail();
        StringBuilder result = new StringBuilder("Furusato ").append(Furusato.VERSION).append('\n')
                .append("Minecraft 1.12.2 / Forge ").append(ForgeVersion.getVersion()).append('\n')
                .append("unicode_gui_scale: ").append(patchStatus).append(" (").append(detail).append(")\n")
                .append("safeMode: ").append(safeMode).append('\n')
                .append("unicodeGuiScaleConfigured: ").append(configuredPatch).append('\n')
                .append("unicodeGuiScaleActive: ").append(activePatch).append('\n')
                .append("restartPending: ").append(restartPending).append('\n')
                .append("forceUnicodeFont: ").append(mc.gameSettings.forceUnicodeFont).append('\n')
                .append("guiScale: ").append(selectedScale).append(" (effective: ").append(effectiveScale).append(")\n")
                .append("unicodeResources: ").append(unicodeResources).append('\n')
                .append("config: ").append(config == null ? "unavailable" : configPath).append('\n')
                .append("transformers: ").append(join(transformers)).append('\n')
                .append("thirdPartyTransformers: ").append(join(thirdParty)).append('\n')
                .append("health: ").append(I18n.format(healthKey));
        for (DiagnosticAssessment.Finding finding : findings) {
            result.append('\n').append("warning: ")
                    .append(I18n.format(finding.titleKey())).append(" — ")
                    .append(I18n.format(finding.adviceKey()));
        }
        return result.toString();
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
        } else if (button.id == OPEN_REPORTS) {
            if (!openReportsFolder()) {
                openLabelKey = "furusato.diagnostics.openFailed";
                button.displayString = I18n.format(openLabelKey);
            }
        } else if (button.id == OVERVIEW_TAB) {
            selectCategory(Category.OVERVIEW);
        } else if (button.id == RENDERING_TAB) {
            selectCategory(Category.RENDERING);
        } else if (button.id == COMPATIBILITY_TAB) {
            selectCategory(Category.COMPATIBILITY);
        } else if (button.id == ISSUES_TAB) {
            selectCategory(Category.ISSUES);
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    private void selectCategory(Category category) {
        activeCategory = category;
        scrollOffset = 0;
        initGui();
    }

    private boolean exportReport() {
        File reportFile = DiagnosticReportFiles.uniqueFile(reportsDirectory(),
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()));
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

    private File reportsDirectory() {
        File configFile = FurusatoEarlyConfig.getConfigurationFile();
        File configDirectory = configFile == null ? null : configFile.getParentFile();
        File gameDirectory = configDirectory == null
                ? new File(".") : configDirectory.getParentFile();
        return new File(gameDirectory == null ? new File(".") : gameDirectory,
                "furusato-reports");
    }

    private boolean openReportsFolder() {
        File directory = reportsDirectory();
        if (!directory.isDirectory() && !directory.mkdirs()) {
            return false;
        }
        try {
            if (!Desktop.isDesktopSupported()
                    || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                return false;
            }
            Desktop.getDesktop().open(directory);
            return true;
        } catch (IOException | RuntimeException error) {
            if (Furusato.getLogger() != null) {
                Furusato.getLogger().error("Could not open diagnostics directory {}",
                        directory, error);
            }
            return false;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        boolean compact = isCompactLayout();
        int contentWidth = responsiveContentWidth(280, compact ? 380 : 500, 12);
        int contentLeft = (width - contentWidth) / 2;
        int contentRight = contentLeft + contentWidth;
        int rowHeight = 14;
        int headerHeight = compact ? 82 : 58;
        int panelHeight = panelHeight(headerHeight);
        int groupHeight = panelHeight + headerHeight;
        int footerHeight = 52;
        int groupTop = Math.max(8, (height - groupHeight - footerHeight) / 2);
        int panelTop = groupTop + headerHeight;

        drawCenteredString(fontRenderer, I18n.format("furusato.diagnostics.title"),
                width / 2, groupTop, 0xFFFFFF);

        int cardTop = groupTop + 18;
        drawFurusatoPanel(contentLeft + 6, cardTop + 6,
                contentWidth - 12, groupHeight - 18 - 12);

        drawRect(contentLeft + 10, panelTop + 2,
                contentRight - 10, panelTop + panelHeight - 8, 0x70000000);
        drawRect(contentLeft + 10, panelTop + 2,
                contentRight - 10, panelTop + 3, 0x405000FF);

        List<Row> visibleRows = visibleRows();
        int capacity = FurusatoGuiLayout.visibleRows(
                panelHeight, PANEL_PADDING, rowHeight);
        scrollOffset = FurusatoGuiLayout.clampScroll(
                scrollOffset, visibleRows.size(), capacity);
        int lastRow = Math.min(visibleRows.size(), scrollOffset + capacity);
        List<Row> displayedRows = visibleRows.subList(scrollOffset, lastRow);
        int labelWidth = 0;
        for (Row row : displayedRows) {
            labelWidth = Math.max(labelWidth,
                    fontRenderer.getStringWidth(row.label + ":"));
        }
        labelWidth = Math.min(labelWidth, contentWidth / 2 - 12);
        int labelX = contentLeft + PANEL_PADDING;
        int valueX = labelX + labelWidth + 18;
        int maxValueWidth = Math.max(60,
                contentRight - valueX - PANEL_PADDING);
        Row hoveredRow = null;
        for (int index = 0; index < displayedRows.size(); index++) {
            Row row = displayedRows.get(index);
            int rowTop = panelTop + PANEL_PADDING + index * rowHeight;
            if ((index & 1) == 1) {
                drawRect(contentLeft + 2, rowTop, contentRight - 2,
                        rowTop + rowHeight, 0x10000000);
            }
            if (mouseX >= contentLeft && mouseX < contentRight
                    && mouseY >= rowTop && mouseY < rowTop + rowHeight) {
                drawRect(contentLeft + 2, rowTop, contentRight - 2,
                        rowTop + rowHeight, 0x22FFFFFF);
                hoveredRow = row;
            }
            int textY = rowTop + (rowHeight - fontRenderer.FONT_HEIGHT) / 2 + 1;
            drawString(fontRenderer, row.label + ":", labelX, textY, 0xA0A0A0);
            drawString(fontRenderer, fitText(row.value, maxValueWidth),
                    valueX, textY, row.color);
        }
        drawScrollBar(contentRight, panelTop, panelHeight,
                visibleRows.size(), capacity);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (hoveredRow != null
                && fontRenderer.getStringWidth(hoveredRow.value) > maxValueWidth) {
            drawHoveringText(fontRenderer.listFormattedStringToWidth(
                    hoveredRow.value, Math.min(320, Math.max(180, width - 40))),
                    mouseX, mouseY);
        }
    }

    private int panelHeight(int headerHeight) {
        int desired = Math.max(4, visibleRows().size()) * 14 + PANEL_PADDING * 2;
        int available = Math.max(34, height - 16 - 52 - headerHeight);
        return Math.min(desired, available);
    }

    private boolean isCompactLayout() {
        return FurusatoViewport.isCompact(width, height,
                mc.displayWidth, mc.displayHeight);
    }

    private void drawScrollBar(int contentRight, int panelTop, int panelHeight,
            int rowCount, int capacity) {
        if (rowCount <= capacity) {
            return;
        }
        int trackTop = panelTop + PANEL_PADDING;
        int trackBottom = panelTop + panelHeight - PANEL_PADDING;
        int trackHeight = trackBottom - trackTop;
        int thumbHeight = Math.max(8, trackHeight * capacity / rowCount);
        int maxOffset = rowCount - capacity;
        int thumbTop = trackTop + (trackHeight - thumbHeight) * scrollOffset / maxOffset;
        drawRect(contentRight - 5, trackTop, contentRight - 3, trackBottom, 0x44000000);
        drawRect(contentRight - 5, thumbTop, contentRight - 3,
                thumbTop + thumbHeight, 0xFFAAAAAA);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset += wheel < 0 ? 1 : -1;
            int headerHeight = isCompactLayout() ? 82 : 58;
            int capacity = FurusatoGuiLayout.visibleRows(
                    panelHeight(headerHeight), PANEL_PADDING, 14);
            scrollOffset = FurusatoGuiLayout.clampScroll(
                    scrollOffset, visibleRows().size(), capacity);
        }
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
        COMPATIBILITY,
        ISSUES
    }
}
