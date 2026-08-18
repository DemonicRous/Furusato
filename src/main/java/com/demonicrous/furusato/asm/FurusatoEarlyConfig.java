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
    private static volatile boolean activeUnicodeGuiScaleEnabled = true;
    private static volatile boolean diagnosticLoggingEnabled = true;
    private static volatile boolean hotbarAnimationEnabled = true;
    private static volatile boolean hotbarPulseEnabled = true;
    private static volatile int hotbarDurationMillis = 90;
    private static volatile boolean containerAnimationEnabled = true;
    private static volatile int containerDurationMillis = 180;
    private static volatile boolean containerBlurEnabled = true;
    private static volatile int blurRadius = 6;
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
            activeUnicodeGuiScaleEnabled = unicodeGuiScaleEnabled;
            diagnosticLoggingEnabled = readBoolean(properties, Option.DIAGNOSTIC_LOGGING);
            hotbarAnimationEnabled = readBoolean(properties, Option.HOTBAR_ANIMATION);
            hotbarPulseEnabled = readBoolean(properties, Option.HOTBAR_PULSE);
            containerAnimationEnabled = readBoolean(properties, Option.CONTAINER_ANIMATION);
            containerBlurEnabled = readBoolean(properties, Option.CONTAINER_BLUR);
            hotbarDurationMillis = readInt(properties, IntOption.HOTBAR_DURATION);
            containerDurationMillis = readInt(properties, IntOption.CONTAINER_DURATION);
            blurRadius = readInt(properties, IntOption.BLUR_RADIUS);
            configurationFile = file;
            configuration = properties;

            changed |= normalize(properties, Option.UNICODE_GUI_SCALE,
                    unicodeGuiScaleEnabled);
            changed |= normalize(properties, Option.DIAGNOSTIC_LOGGING,
                    diagnosticLoggingEnabled);
            changed |= normalize(properties, Option.HOTBAR_ANIMATION,
                    hotbarAnimationEnabled);
            changed |= normalize(properties, Option.HOTBAR_PULSE,
                    hotbarPulseEnabled);
            changed |= normalize(properties, Option.CONTAINER_ANIMATION,
                    containerAnimationEnabled);
            changed |= normalize(properties, Option.CONTAINER_BLUR,
                    containerBlurEnabled);
            changed |= normalize(properties, IntOption.HOTBAR_DURATION,
                    hotbarDurationMillis);
            changed |= normalize(properties, IntOption.CONTAINER_DURATION,
                    containerDurationMillis);
            changed |= normalize(properties, IntOption.BLUR_RADIUS, blurRadius);
            if (changed) {
                saveAtomic(file, properties, false);
            }

            LOGGER.info("Loaded early configuration from {}", file.getAbsolutePath());
        } catch (IOException error) {
            unicodeGuiScaleEnabled = true;
            activeUnicodeGuiScaleEnabled = true;
            diagnosticLoggingEnabled = true;
            hotbarAnimationEnabled = true;
            hotbarPulseEnabled = true;
            hotbarDurationMillis = 90;
            containerAnimationEnabled = true;
            containerDurationMillis = 180;
            containerBlurEnabled = true;
            blurRadius = 6;
            LOGGER.error("Could not load {}; using Furusato defaults", file, error);
        }
    }

    public static boolean isUnicodeGuiScaleEnabled() {
        return unicodeGuiScaleEnabled;
    }

    /** Value captured during CoreMod startup and currently represented by bytecode. */
    public static boolean isActiveUnicodeGuiScaleEnabled() {
        return activeUnicodeGuiScaleEnabled;
    }

    public static boolean isRestartPending() {
        return unicodeGuiScaleEnabled != activeUnicodeGuiScaleEnabled;
    }

    public static boolean isDiagnosticLoggingEnabled() {
        return diagnosticLoggingEnabled;
    }

    public static boolean isHotbarAnimationEnabled() { return hotbarAnimationEnabled; }
    public static boolean isHotbarPulseEnabled() { return hotbarPulseEnabled; }
    public static int getHotbarDurationMillis() { return hotbarDurationMillis; }
    public static boolean isContainerAnimationEnabled() { return containerAnimationEnabled; }
    public static int getContainerDurationMillis() { return containerDurationMillis; }
    public static boolean isContainerBlurEnabled() { return containerBlurEnabled; }
    public static int getBlurRadius() { return blurRadius; }

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

    public static synchronized boolean setHotbarAnimationEnabled(boolean enabled) {
        return set(Option.HOTBAR_ANIMATION, enabled);
    }

    public static synchronized boolean setHotbarPulseEnabled(boolean enabled) {
        return set(Option.HOTBAR_PULSE, enabled);
    }

    public static synchronized boolean setHotbarDurationMillis(int value) {
        return set(IntOption.HOTBAR_DURATION, value);
    }

    public static synchronized boolean setContainerAnimationEnabled(boolean enabled) {
        return set(Option.CONTAINER_ANIMATION, enabled);
    }

    public static synchronized boolean setContainerDurationMillis(int value) {
        return set(IntOption.CONTAINER_DURATION, value);
    }

    public static synchronized boolean setContainerBlurEnabled(boolean enabled) {
        return set(Option.CONTAINER_BLUR, enabled);
    }

    public static synchronized boolean setBlurRadius(int value) {
        return set(IntOption.BLUR_RADIUS, value);
    }

    public static synchronized boolean setEffectsSettings(
            boolean hotbar, boolean pulse, int hotbarMillis,
            boolean containers, int containerMillis, boolean blur, int radius) {
        if (configurationFile == null) {
            LOGGER.warn("Cannot save effect settings before configuration is loaded");
            return false;
        }
        int normalizedHotbar = IntOption.HOTBAR_DURATION.clamp(hotbarMillis);
        int normalizedContainer = IntOption.CONTAINER_DURATION.clamp(containerMillis);
        int normalizedRadius = IntOption.BLUR_RADIUS.clamp(radius);
        Properties updated = copyOf(configuration);
        updated.setProperty(Option.HOTBAR_ANIMATION.key, Boolean.toString(hotbar));
        updated.setProperty(Option.HOTBAR_PULSE.key, Boolean.toString(pulse));
        updated.setProperty(IntOption.HOTBAR_DURATION.key,
                Integer.toString(normalizedHotbar));
        updated.setProperty(Option.CONTAINER_ANIMATION.key,
                Boolean.toString(containers));
        updated.setProperty(IntOption.CONTAINER_DURATION.key,
                Integer.toString(normalizedContainer));
        updated.setProperty(Option.CONTAINER_BLUR.key, Boolean.toString(blur));
        updated.setProperty(IntOption.BLUR_RADIUS.key,
                Integer.toString(normalizedRadius));
        try {
            saveAtomic(configurationFile, updated, true);
            configuration = updated;
            hotbarAnimationEnabled = hotbar;
            hotbarPulseEnabled = pulse;
            hotbarDurationMillis = normalizedHotbar;
            containerAnimationEnabled = containers;
            containerDurationMillis = normalizedContainer;
            containerBlurEnabled = blur;
            blurRadius = normalizedRadius;
            return true;
        } catch (IOException error) {
            LOGGER.error("Could not save {}", configurationFile, error);
            return false;
        }
    }

    public static synchronized boolean revertPendingUnicodeGuiScaleChange() {
        return !isRestartPending()
                || set(Option.UNICODE_GUI_SCALE, activeUnicodeGuiScaleEnabled);
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
        for (IntOption option : IntOption.values()) {
            updated.setProperty(option.key, Integer.toString(option.defaultValue));
        }
        try {
            saveAtomic(configurationFile, updated, true);
            configuration = updated;
            unicodeGuiScaleEnabled = Option.UNICODE_GUI_SCALE.defaultValue;
            diagnosticLoggingEnabled = Option.DIAGNOSTIC_LOGGING.defaultValue;
            hotbarAnimationEnabled = Option.HOTBAR_ANIMATION.defaultValue;
            hotbarPulseEnabled = Option.HOTBAR_PULSE.defaultValue;
            containerAnimationEnabled = Option.CONTAINER_ANIMATION.defaultValue;
            containerBlurEnabled = Option.CONTAINER_BLUR.defaultValue;
            hotbarDurationMillis = IntOption.HOTBAR_DURATION.defaultValue;
            containerDurationMillis = IntOption.CONTAINER_DURATION.defaultValue;
            blurRadius = IntOption.BLUR_RADIUS.defaultValue;
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
            } else if (option == Option.HOTBAR_ANIMATION) {
                hotbarAnimationEnabled = enabled;
            } else if (option == Option.HOTBAR_PULSE) {
                hotbarPulseEnabled = enabled;
            } else if (option == Option.CONTAINER_ANIMATION) {
                containerAnimationEnabled = enabled;
            } else if (option == Option.CONTAINER_BLUR) {
                containerBlurEnabled = enabled;
            }
            return true;
        } catch (IOException error) {
            LOGGER.error("Could not save {}", configurationFile, error);
            return false;
        }
    }

    private static boolean set(IntOption option, int value) {
        if (configurationFile == null) {
            LOGGER.warn("Cannot save {} before configuration is loaded", option.key);
            return false;
        }
        int normalized = option.clamp(value);
        Properties updated = copyOf(configuration);
        updated.setProperty(option.key, Integer.toString(normalized));
        try {
            saveAtomic(configurationFile, updated, true);
            configuration = updated;
            if (option == IntOption.HOTBAR_DURATION) {
                hotbarDurationMillis = normalized;
            } else if (option == IntOption.CONTAINER_DURATION) {
                containerDurationMillis = normalized;
            } else if (option == IntOption.BLUR_RADIUS) {
                blurRadius = normalized;
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

    private static int readInt(Properties properties, IntOption option) {
        String value = properties.getProperty(option.key);
        if (value == null) {
            return option.defaultValue;
        }
        try {
            return option.clamp(Integer.parseInt(value));
        } catch (NumberFormatException error) {
            LOGGER.warn("Invalid integer value for {}: {}; using {}",
                    option.key, value, option.defaultValue);
            return option.defaultValue;
        }
    }

    private static boolean normalize(Properties properties, IntOption option, int value) {
        String normalized = Integer.toString(option.clamp(value));
        if (normalized.equals(properties.getProperty(option.key))) {
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
        activeUnicodeGuiScaleEnabled = true;
        diagnosticLoggingEnabled = true;
        hotbarAnimationEnabled = true;
        hotbarPulseEnabled = true;
        hotbarDurationMillis = 90;
        containerAnimationEnabled = true;
        containerDurationMillis = 180;
        containerBlurEnabled = true;
        blurRadius = 6;
        configurationFile = null;
        configuration = new Properties();
    }

    private enum Option {
        UNICODE_GUI_SCALE("patches.unicodeGuiScale", true, true),
        DIAGNOSTIC_LOGGING("diagnostics.logging", true, false),
        HOTBAR_ANIMATION("animations.hotbar", true, false),
        HOTBAR_PULSE("animations.hotbarPulse", true, false),
        CONTAINER_ANIMATION("animations.containers", true, false),
        CONTAINER_BLUR("effects.containerBlur", true, false);

        private final String key;
        private final boolean defaultValue;
        private final boolean requiresRestart;

        Option(String key, boolean defaultValue, boolean requiresRestart) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.requiresRestart = requiresRestart;
        }
    }

    private enum IntOption {
        HOTBAR_DURATION("animations.hotbarDurationMs", 90, 50, 180),
        CONTAINER_DURATION("animations.containerDurationMs", 180, 80, 400),
        BLUR_RADIUS("effects.blurRadius", 6, 2, 12);

        private final String key;
        private final int defaultValue;
        private final int minimum;
        private final int maximum;

        IntOption(String key, int defaultValue, int minimum, int maximum) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.minimum = minimum;
            this.maximum = maximum;
        }

        private int clamp(int value) {
            return Math.max(minimum, Math.min(maximum, value));
        }
    }
}
