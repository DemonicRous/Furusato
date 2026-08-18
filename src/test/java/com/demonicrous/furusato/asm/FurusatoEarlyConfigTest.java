package com.demonicrous.furusato.asm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class FurusatoEarlyConfigTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @After
    public void resetConfiguration() {
        System.clearProperty("furusato.safeMode");
        FurusatoEarlyConfig.resetForTests();
    }

    @Test
    public void readsSafeModeBeforeConfigurationIsLoaded() {
        assertFalse(FurusatoEarlyConfig.isSafeModeEnabled());
        System.setProperty("furusato.safeMode", "true");
        assertTrue(FurusatoEarlyConfig.isSafeModeEnabled());
    }

    @Test
    public void createsDefaultConfiguration() throws Exception {
        File gameDirectory = temporaryFolder.newFolder("defaults");
        FurusatoEarlyConfig.load(gameDirectory);

        assertTrue(FurusatoEarlyConfig.isUnicodeGuiScaleEnabled());
        assertTrue(FurusatoEarlyConfig.isDiagnosticLoggingEnabled());
        assertTrue(new File(gameDirectory, "config/furusato.properties").isFile());
    }

    @Test
    public void readsPatchAndDiagnosticTogglesBeforeModInitialization() throws Exception {
        File gameDirectory = temporaryFolder.newFolder("custom");
        File configDirectory = new File(gameDirectory, "config");
        assertTrue(configDirectory.mkdirs());
        File file = new File(configDirectory, "furusato.properties");

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write("patches.unicodeGuiScale=false\n");
            writer.write("diagnostics.logging=false\n");
        }

        FurusatoEarlyConfig.load(gameDirectory);
        assertFalse(FurusatoEarlyConfig.isUnicodeGuiScaleEnabled());
        assertFalse(FurusatoEarlyConfig.isDiagnosticLoggingEnabled());
    }

    @Test
    public void persistsUnicodePatchSettingFromGui() throws Exception {
        File gameDirectory = temporaryFolder.newFolder("save");
        FurusatoEarlyConfig.load(gameDirectory);

        assertTrue(FurusatoEarlyConfig.setUnicodeGuiScaleEnabled(false));
        FurusatoEarlyConfig.resetForTests();
        FurusatoEarlyConfig.load(gameDirectory);

        assertFalse(FurusatoEarlyConfig.isUnicodeGuiScaleEnabled());
    }

    @Test
    public void tracksAndRevertsRestartRequiredChange() throws Exception {
        File gameDirectory = temporaryFolder.newFolder("pending-restart");
        FurusatoEarlyConfig.load(gameDirectory);

        assertTrue(FurusatoEarlyConfig.isActiveUnicodeGuiScaleEnabled());
        assertFalse(FurusatoEarlyConfig.isRestartPending());
        assertTrue(FurusatoEarlyConfig.setUnicodeGuiScaleEnabled(false));
        assertFalse(FurusatoEarlyConfig.isUnicodeGuiScaleEnabled());
        assertTrue(FurusatoEarlyConfig.isActiveUnicodeGuiScaleEnabled());
        assertTrue(FurusatoEarlyConfig.isRestartPending());

        assertTrue(FurusatoEarlyConfig.revertPendingUnicodeGuiScaleChange());
        assertTrue(FurusatoEarlyConfig.isUnicodeGuiScaleEnabled());
        assertFalse(FurusatoEarlyConfig.isRestartPending());
    }

    @Test
    public void atomicallySavesDiagnosticsAndPreservesUnknownProperties() throws Exception {
        File gameDirectory = temporaryFolder.newFolder("atomic");
        File configDirectory = new File(gameDirectory, "config");
        assertTrue(configDirectory.mkdirs());
        File file = new File(configDirectory, "furusato.properties");
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write("patches.unicodeGuiScale=true\n");
            writer.write("diagnostics.logging=false\n");
            writer.write("future.setting=preserved\n");
        }

        FurusatoEarlyConfig.load(gameDirectory);
        assertTrue(FurusatoEarlyConfig.setDiagnosticLoggingEnabled(true));

        String saved = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        String backup = new String(Files.readAllBytes(
                new File(configDirectory, "furusato.properties.bak").toPath()),
                StandardCharsets.UTF_8);
        assertTrue(saved.contains("diagnostics.logging=true"));
        assertTrue(saved.contains("future.setting=preserved"));
        assertTrue(backup.contains("diagnostics.logging=false"));
        assertFalse(new File(configDirectory, "furusato.properties.tmp").exists());
    }

    @Test
    public void resetRestoresKnownDefaultsWithoutRemovingFutureSettings() throws Exception {
        File gameDirectory = temporaryFolder.newFolder("reset");
        File configDirectory = new File(gameDirectory, "config");
        assertTrue(configDirectory.mkdirs());
        File file = new File(configDirectory, "furusato.properties");
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write("patches.unicodeGuiScale=false\n");
            writer.write("diagnostics.logging=false\n");
            writer.write("future.setting=preserved\n");
        }
        FurusatoEarlyConfig.load(gameDirectory);

        assertTrue(FurusatoEarlyConfig.resetDefaults());

        assertTrue(FurusatoEarlyConfig.isUnicodeGuiScaleEnabled());
        assertTrue(FurusatoEarlyConfig.isDiagnosticLoggingEnabled());
        String saved = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        assertTrue(saved.contains("future.setting=preserved"));
    }

    @Test
    public void migratesLegacyLimeCoreConfiguration() throws Exception {
        File gameDirectory = temporaryFolder.newFolder("legacy");
        File configDirectory = new File(gameDirectory, "config");
        assertTrue(configDirectory.mkdirs());
        File legacyFile = new File(configDirectory, "limecore.properties");

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(legacyFile), StandardCharsets.UTF_8)) {
            writer.write("patches.unicodeGuiScale=false\n");
            writer.write("diagnostics.logging=true\n");
        }

        FurusatoEarlyConfig.load(gameDirectory);

        assertFalse(FurusatoEarlyConfig.isUnicodeGuiScaleEnabled());
        assertTrue(new File(configDirectory, "furusato.properties").isFile());
        assertTrue(legacyFile.isFile());
    }
}
