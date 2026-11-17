package io.github.tavstaldev.yggraHeads.config.models;

import io.github.tavstaldev.yggraLib.bootstrap.YggraConfig;
import lombok.Getter;

@Getter
public class StorageConfig {
    private final String type;
    private final String context;
    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;

    private final int port;
    private final int autoClean;
    private final int pool;
    private final int maxLifeTime;
    private final int connectionTimeout;

    private final RedisConfig redis;

    public StorageConfig(YggraConfig parent) {
        type = parent.resolveGet("storage.db.type", "sqlite");
        context = parent.resolveGet("storage.db.context", "survival");
        host = parent.resolveGet("storage.db.host", "localhost");
        port = parent.resolveGet("storage.db.port", 3306);
        database = parent.resolveGet("storage.db.database", "minecraft");
        username = parent.resolveGet("storage.db.username", "root");
        password = parent.resolveGet("storage.db.password", "ascent");
        tablePrefix = parent.resolveGet("storage.db.table-prefix", "yh_");
        autoClean = parent.resolveGet("storage.db.auto-clean-interval", 3600);
        pool = parent.resolveGet("storage.db.pool-settings.maximum-pool-size", 10);
        maxLifeTime = parent.resolveGet("storage.db.pool-settings.maximum-lifetime", 1800000);
        connectionTimeout = parent.resolveGet("storage.db.pool-settings.connection-timeout", 5000);
        redis = new RedisConfig(parent);
    }

    public StorageConfig(String type, String context, String host, String database, String username, String password, String tablePrefix, int port, int autoClean, int pool, int maxLifeTime, int connectionTimeout, RedisConfig redis) {
        this.type = type;
        this.context = context;
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix;
        this.port = port;
        this.autoClean = autoClean;
        this.pool = pool;
        this.maxLifeTime = maxLifeTime;
        this.connectionTimeout = connectionTimeout;
        this.redis = redis;
    }
}
