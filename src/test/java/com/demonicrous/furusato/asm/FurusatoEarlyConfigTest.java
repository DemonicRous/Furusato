package com.demonicrous.furusato.asm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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
