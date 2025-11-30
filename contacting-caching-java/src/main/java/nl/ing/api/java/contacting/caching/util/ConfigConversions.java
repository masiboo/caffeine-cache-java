package nl.ing.api.java.contacting.caching.util;

import com.typesafe.config.Config;

import java.time.Duration;

/**
 * Utility class for safe config value extraction with defaults.
 */
public final class ConfigConversions {

    private ConfigConversions() {
        // Utility class; prevent instantiation
    }

    // Helper methods for config access
    public static boolean getDefaultBoolean(Config config, String path, boolean defaultValue) {
        return config.hasPath(path) ? config.getBoolean(path) : defaultValue;
    }

    public static String getDefaultString(Config config, String path, String defaultValue) {
        return config.hasPath(path) ? config.getString(path) : defaultValue;
    }

    public static Duration getDefaultDuration(Config config, String path, Duration defaultValue) {
        return config.hasPath(path) ? config.getDuration(path) : defaultValue;
    }

}
