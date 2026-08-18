package com.demonicrous.limecore.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Configuration available before normal Forge mod initialization. */
public final class LimeCoreEarlyConfig {
    private static final Logger LOGGER = LogManager.getLogger("Lime Core/Config");
    private static final String UNICODE_GUI_SCALE = "patches.unicodeGuiScale";
    private static final String DIAGNOSTIC_LOGGING = "diagnostics.logging";

    private static volatile boolean unicodeGuiScaleEnabled = true;
    private static volatile boolean diagnosticLoggingEnabled = true;

    private LimeCoreEarlyConfig() {
    }

    public static void load(File gameDirectory) {
        if (gameDirectory == null) {
            LOGGER.warn("Game directory is unavailable; using Lime Core defaults");
            return;
        }

        File file = new File(new File(gameDirectory, "config"), "limecore.properties");
        Properties properties = new Properties();
        boolean changed = false;

        try {
            if (file.isFile()) {
                try (Reader reader = new InputStreamReader(
                        new FileInputStream(file), StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }
            }

            unicodeGuiScaleEnabled = readBoolean(properties, UNICODE_GUI_SCALE, true);
            diagnosticLoggingEnabled = readBoolean(properties, DIAGNOSTIC_LOGGING, true);

            changed |= putDefault(properties, UNICODE_GUI_SCALE, unicodeGuiScaleEnabled);
            changed |= putDefault(properties, DIAGNOSTIC_LOGGING, diagnosticLoggingEnabled);
            if (changed) {
                save(file, properties);
            }

            LOGGER.info("Loaded early configuration from {}", file.getAbsolutePath());
        } catch (IOException error) {
            unicodeGuiScaleEnabled = true;
            diagnosticLoggingEnabled = true;
            LOGGER.error("Could not load {}; using Lime Core defaults", file, error);
        }
    }

    public static boolean isUnicodeGuiScaleEnabled() {
        return unicodeGuiScaleEnabled;
    }

    public static boolean isDiagnosticLoggingEnabled() {
        return diagnosticLoggingEnabled;
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        if (value == null) {
            return fallback;
        }
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        LOGGER.warn("Invalid boolean value for {}: {}; using {}", key, value, fallback);
        return fallback;
    }

    private static boolean putDefault(Properties properties, String key, boolean value) {
        if (properties.containsKey(key)) {
            return false;
        }
        properties.setProperty(key, Boolean.toString(value));
        return true;
    }

    private static void save(File file, Properties properties) throws IOException {
        File parent = file.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create configuration directory: " + parent);
        }
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            properties.store(writer, "Lime Core early configuration");
        }
    }

    static void setUnicodeGuiScaleEnabledForTests(boolean enabled) {
        unicodeGuiScaleEnabled = enabled;
    }

    static void resetForTests() {
        unicodeGuiScaleEnabled = true;
        diagnosticLoggingEnabled = true;
    }
}
