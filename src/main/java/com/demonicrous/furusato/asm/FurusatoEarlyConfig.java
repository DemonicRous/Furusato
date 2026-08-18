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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Configuration available before normal Forge mod initialization. */
public final class FurusatoEarlyConfig {
    private static final Logger LOGGER = LogManager.getLogger("Furusato/Config");
    private static final String SAFE_MODE_PROPERTY = "furusato.safeMode";

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

            unicodeGuiScaleEnabled = readBoolean(properties, Option.UNICODE_GUI_SCALE);
            diagnosticLoggingEnabled = readBoolean(properties, Option.DIAGNOSTIC_LOGGING);
            configurationFile = file;
            configuration = properties;

            changed |= normalize(properties, Option.UNICODE_GUI_SCALE,
                    unicodeGuiScaleEnabled);
            changed |= normalize(properties, Option.DIAGNOSTIC_LOGGING,
                    diagnosticLoggingEnabled);
            if (changed) {
                saveAtomic(file, properties, false);
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

    public static boolean unicodeGuiScaleRequiresRestart() {
        return Option.UNICODE_GUI_SCALE.requiresRestart;
    }

    /** Safe mode is a JVM-only switch because it must be available before config injection. */
    public static boolean isSafeModeEnabled() {
        try {
            return Boolean.parseBoolean(System.getProperty(SAFE_MODE_PROPERTY, "false"));
        } catch (SecurityException error) {
            LOGGER.warn("Cannot read -D{}; safe mode remains disabled", SAFE_MODE_PROPERTY);
            return false;
        }
    }

    public static synchronized File getConfigurationFile() {
        return configurationFile;
    }

    public static synchronized boolean setUnicodeGuiScaleEnabled(boolean enabled) {
        return set(Option.UNICODE_GUI_SCALE, enabled);
    }

    public static synchronized boolean setDiagnosticLoggingEnabled(boolean enabled) {
        return set(Option.DIAGNOSTIC_LOGGING, enabled);
    }

    public static synchronized boolean resetDefaults() {
        if (configurationFile == null) {
            LOGGER.warn("Cannot reset Furusato configuration before it is loaded");
            return false;
        }
        Properties updated = copyOf(configuration);
        for (Option option : Option.values()) {
            updated.setProperty(option.key, Boolean.toString(option.defaultValue));
        }
        try {
            saveAtomic(configurationFile, updated, true);
            configuration = updated;
            unicodeGuiScaleEnabled = Option.UNICODE_GUI_SCALE.defaultValue;
            diagnosticLoggingEnabled = Option.DIAGNOSTIC_LOGGING.defaultValue;
            return true;
        } catch (IOException error) {
            LOGGER.error("Could not reset {}", configurationFile, error);
            return false;
        }
    }

    private static boolean set(Option option, boolean enabled) {
        if (configurationFile == null) {
            LOGGER.warn("Cannot save {} before configuration is loaded", option.key);
            return false;
        }
        Properties updated = copyOf(configuration);
        updated.setProperty(option.key, Boolean.toString(enabled));
        try {
            saveAtomic(configurationFile, updated, true);
            configuration = updated;
            if (option == Option.UNICODE_GUI_SCALE) {
                unicodeGuiScaleEnabled = enabled;
            } else if (option == Option.DIAGNOSTIC_LOGGING) {
                diagnosticLoggingEnabled = enabled;
            }
            return true;
        } catch (IOException error) {
            LOGGER.error("Could not save {}", configurationFile, error);
            return false;
        }
    }

    private static boolean readBoolean(Properties properties, Option option) {
        String value = properties.getProperty(option.key);
        if (value == null) {
            return option.defaultValue;
        }
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        LOGGER.warn("Invalid boolean value for {}: {}; using {}",
                option.key, value, option.defaultValue);
        return option.defaultValue;
    }

    private static boolean normalize(Properties properties, Option option, boolean value) {
        String normalized = Boolean.toString(value);
        if (normalized.equalsIgnoreCase(properties.getProperty(option.key))) {
            return false;
        }
        properties.setProperty(option.key, normalized);
        return true;
    }

    private static Properties copyOf(Properties source) {
        Properties copy = new Properties();
        copy.putAll(source);
        return copy;
    }

    private static void saveAtomic(File file, Properties properties, boolean backup)
            throws IOException {
        File parent = file.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create configuration directory: " + parent);
        }
        File temporary = new File(parent, file.getName() + ".tmp");
        try {
            try (Writer writer = new OutputStreamWriter(
                    new FileOutputStream(temporary), StandardCharsets.UTF_8)) {
                properties.store(writer, "Furusato early configuration");
            }
            if (backup && file.isFile()) {
                Files.copy(file.toPath(),
                        new File(parent, file.getName() + ".bak").toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES);
            }
            try {
                Files.move(temporary.toPath(), file.toPath(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary.toPath());
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

    private enum Option {
        UNICODE_GUI_SCALE("patches.unicodeGuiScale", true, true),
        DIAGNOSTIC_LOGGING("diagnostics.logging", true, false);

        private final String key;
        private final boolean defaultValue;
        private final boolean requiresRestart;

        Option(String key, boolean defaultValue, boolean requiresRestart) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.requiresRestart = requiresRestart;
        }
    }
}
