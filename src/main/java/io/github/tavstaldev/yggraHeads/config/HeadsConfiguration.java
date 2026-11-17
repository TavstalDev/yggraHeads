package io.github.tavstaldev.yggraHeads.config;

import io.github.tavstaldev.yggraHeads.YggraHeads;
import io.github.tavstaldev.yggraHeads.config.models.GeneralConfig;
import io.github.tavstaldev.yggraHeads.config.models.StorageConfig;
import io.github.tavstaldev.yggraLib.bootstrap.YggraConfig;

public class HeadsConfiguration extends YggraConfig {

    public HeadsConfiguration() {
        super(YggraHeads.instance, "config.yml", null);
    }

    public GeneralConfig general;
    public StorageConfig storage;

    @Override
    protected void loadDefaults() {
        general = new GeneralConfig(this);
        storage = new StorageConfig(this);
    }
}
