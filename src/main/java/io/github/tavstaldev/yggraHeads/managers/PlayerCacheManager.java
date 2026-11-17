package io.github.tavstaldev.yggraHeads.managers;

import io.github.tavstaldev.yggraHeads.models.PlayerCache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerCacheManager {
    private static final Map<UUID, PlayerCache> _playerData = new HashMap<>();

    public static void add(UUID playerId, PlayerCache playerData) {
        _playerData.put(playerId, playerData);
    }
    public static void remove(UUID playerId) {
        _playerData.remove(playerId);
    }

    public static PlayerCache get(UUID playerId) {
        return _playerData.get(playerId);
    }
}
