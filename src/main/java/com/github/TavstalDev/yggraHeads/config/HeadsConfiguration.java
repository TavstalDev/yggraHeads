package com.github.TavstalDev.yggraHeads.config;

import com.github.TavstalDev.yggraHeads.YggraHeads;
import com.github.TavstalDev.yggraHeads.config.models.GeneralConfig;
import com.github.TavstalDev.yggraHeads.config.models.StorageConfig;
import com.github.TavstalDev.yggraLib.bootstrap.YggraConfig;

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
