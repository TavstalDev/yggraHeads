package io.github.tavstaldev.yggraHeads.config;

import io.github.tavstaldev.yggraHeads.YggraHeads;
import io.github.tavstaldev.yggraHeads.config.models.GeneralConfig;
import io.github.tavstaldev.yggraHeads.config.models.StorageConfig;
import io.github.tavstaldev.yggraLib.bootstrap.YggraConfig;

/**
 * Root configuration handler for YggraHeads.
 * <p>
 * Loads and exposes typed configuration sections used by the plugin.
 */
public class HeadsConfiguration extends YggraConfig {

    /**
     * Creates the configuration handler for {@code config.yml}.
     */
    public HeadsConfiguration() {
        super(YggraHeads.instance, "config.yml", null);
    }

    /** General plugin settings (locale, prefix, economy, updates, debug). */
    public GeneralConfig general;
    /** Storage settings for database and Redis cache. */
    public StorageConfig storage;

    /**
     * Loads typed configuration models from the underlying configuration values.
     */
    @Override
    protected void loadDefaults() {
        general = new GeneralConfig(this);
        storage = new StorageConfig(this);
    }
}
