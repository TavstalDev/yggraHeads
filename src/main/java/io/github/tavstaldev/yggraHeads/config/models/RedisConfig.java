package io.github.tavstaldev.yggraHeads.config.models;

import io.github.tavstaldev.yggraLib.bootstrap.YggraConfig;
import lombok.Getter;

@Getter
public class RedisConfig {
    private final boolean enabled;
    private final int port;
    private final String host;
    private final String username;
    private final String password;

    public RedisConfig(YggraConfig parent) {
        enabled = parent.resolveGet("storage.redis.enable", false);
        host = parent.resolveGet("storage.redis.host", "localhost");
        port = parent.resolveGet("storage.redis.port", 6379);
        username = parent.resolveGet("storage.redis.username", "root");
        password = parent.resolveGet("storage.redis.password", "ascent");
    }

    public RedisConfig(boolean enabled, int port, String host, String username, String password) {
        this.enabled = enabled;
        this.port = port;
        this.host = host;
        this.username = username;
        this.password = password;
    }
}
