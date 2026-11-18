package com.github.TavstalDev.yggraHeads.managers;

import com.github.TavstalDev.yggraHeads.models.PlayerCache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Static in-memory manager for per-player {@link PlayerCache} instances.
 */
public class PlayerCacheManager {
    private static final Map<UUID, PlayerCache> _playerData = new HashMap<>();

    /**
     * Stores or replaces the cache entry for a player.
     *
     * @param playerId   the player's UUID
     * @param playerData the cache instance to store
     */
    public static void add(UUID playerId, PlayerCache playerData) {
        _playerData.put(playerId, playerData);
    }

    /**
     * Removes the cache entry for a player.
     *
     * @param playerId the player's UUID
     */
    public static void remove(UUID playerId) {
        _playerData.remove(playerId);
    }

    /**
     * Retrieves the cached data for a player.
     *
     * @param playerId the player's UUID
     * @return the cached player data, or {@code null} if none exists
     */
    public static PlayerCache get(UUID playerId) {
        return _playerData.get(playerId);
    }
}
