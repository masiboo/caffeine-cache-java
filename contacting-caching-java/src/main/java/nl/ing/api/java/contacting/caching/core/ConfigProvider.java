package nl.ing.api.java.contacting.caching.core;

import com.typesafe.config.Config;

/**
 * Provides access to a Typesafe Config instance.
 */
public interface ConfigProvider {
    Config config();
}

