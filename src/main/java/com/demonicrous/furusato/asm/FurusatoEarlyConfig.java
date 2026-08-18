package com.demonicrous.furusato.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Configuration available before normal Forge mod initialization. */
public final class FurusatoEarlyConfig {
    private static final Logger LOGGER = LogManager.getLogger("Furusato/Config");
    private static final String UNICODE_GUI_SCALE = "patches.unicodeGuiScale";
    private static final String DIAGNOSTIC_LOGGING = "diagnostics.logging";

    private static volatile boolean unicodeGuiScaleEnabled = true;
    private static volatile boolean diagnosticLoggingEnabled = true;
    private static File configurationFile;
    private static Properties configuration = new Properties();

    private FurusatoEarlyConfig() {
    }

    public static synchronized void load(File gameDirectory) {
        if (gameDirectory == null) {
            LOGGER.warn("Game directory is unavailable; using Furusato defaults");
            return;
        }

        File configDirectory = new File(gameDirectory, "config");
        File file = new File(configDirectory, "furusato.properties");
        File legacyFile = new File(configDirectory, "limecore.properties");
        Properties properties = new Properties();
        boolean changed = false;

        try {
            if (!file.isFile() && legacyFile.isFile()) {
                if (!configDirectory.isDirectory() && !configDirectory.mkdirs()) {
                    throw new IOException("Could not create configuration directory: "
                            + configDirectory);
                }
                Files.copy(legacyFile.toPath(), file.toPath(),
                        StandardCopyOption.COPY_ATTRIBUTES);
                LOGGER.info("Migrated legacy configuration from {} to {}",
                        legacyFile.getName(), file.getName());
            }
            if (file.isFile()) {
                try (Reader reader = new InputStreamReader(
                        new FileInputStream(file), StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }
            }

            unicodeGuiScaleEnabled = readBoolean(properties, UNICODE_GUI_SCALE, true);
            diagnosticLoggingEnabled = readBoolean(properties, DIAGNOSTIC_LOGGING, true);
            configurationFile = file;
            configuration = properties;

            changed |= putDefault(properties, UNICODE_GUI_SCALE, unicodeGuiScaleEnabled);
            changed |= putDefault(properties, DIAGNOSTIC_LOGGING, diagnosticLoggingEnabled);
            if (changed) {
                save(file, properties);
            }

            LOGGER.info("Loaded early configuration from {}", file.getAbsolutePath());
        } catch (IOException error) {
            unicodeGuiScaleEnabled = true;
            diagnosticLoggingEnabled = true;
            LOGGER.error("Could not load {}; using Furusato defaults", file, error);
        }
    }

    public static boolean isUnicodeGuiScaleEnabled() {
        return unicodeGuiScaleEnabled;
    }

    public static boolean isDiagnosticLoggingEnabled() {
        return diagnosticLoggingEnabled;
    }

    public static synchronized File getConfigurationFile() {
        return configurationFile;
    }

    public static synchronized boolean setUnicodeGuiScaleEnabled(boolean enabled) {
        unicodeGuiScaleEnabled = enabled;
        configuration.setProperty(UNICODE_GUI_SCALE, Boolean.toString(enabled));
        if (configurationFile == null) {
            LOGGER.warn("Cannot save Unicode GUI-scale setting before configuration is loaded");
            return false;
        }
        try {
            save(configurationFile, configuration);
            return true;
        } catch (IOException error) {
            LOGGER.error("Could not save {}", configurationFile, error);
            return false;
        }
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
            properties.store(writer, "Furusato early configuration");
        }
    }

    static void setUnicodeGuiScaleEnabledForTests(boolean enabled) {
        unicodeGuiScaleEnabled = enabled;
    }

    static void resetForTests() {
        unicodeGuiScaleEnabled = true;
        diagnosticLoggingEnabled = true;
        configurationFile = null;
        configuration = new Properties();
    }
}
