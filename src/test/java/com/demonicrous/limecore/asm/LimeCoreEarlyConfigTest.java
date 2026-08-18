package com.demonicrous.limecore.asm;

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

public final class LimeCoreEarlyConfigTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @After
    public void resetConfiguration() {
        LimeCoreEarlyConfig.resetForTests();
    }

    @Test
    public void createsDefaultConfiguration() throws Exception {
        File gameDirectory = temporaryFolder.newFolder("defaults");
        LimeCoreEarlyConfig.load(gameDirectory);

        assertTrue(LimeCoreEarlyConfig.isUnicodeGuiScaleEnabled());
        assertTrue(LimeCoreEarlyConfig.isDiagnosticLoggingEnabled());
        assertTrue(new File(gameDirectory, "config/limecore.properties").isFile());
    }

    @Test
    public void readsPatchAndDiagnosticTogglesBeforeModInitialization() throws Exception {
        File gameDirectory = temporaryFolder.newFolder("custom");
        File configDirectory = new File(gameDirectory, "config");
        assertTrue(configDirectory.mkdirs());
        File file = new File(configDirectory, "limecore.properties");

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write("patches.unicodeGuiScale=false\n");
            writer.write("diagnostics.logging=false\n");
        }

        LimeCoreEarlyConfig.load(gameDirectory);
        assertFalse(LimeCoreEarlyConfig.isUnicodeGuiScaleEnabled());
        assertFalse(LimeCoreEarlyConfig.isDiagnosticLoggingEnabled());
    }

    @Test
    public void persistsUnicodePatchSettingFromGui() throws Exception {
        File gameDirectory = temporaryFolder.newFolder("save");
        LimeCoreEarlyConfig.load(gameDirectory);

        assertTrue(LimeCoreEarlyConfig.setUnicodeGuiScaleEnabled(false));
        LimeCoreEarlyConfig.resetForTests();
        LimeCoreEarlyConfig.load(gameDirectory);

        assertFalse(LimeCoreEarlyConfig.isUnicodeGuiScaleEnabled());
    }
}
