package com.github.TavstalDev.yggraHeads.models;

import com.github.TavstalDev.yggraHeads.database.IHeadsDatabase;
import com.github.TavstalDev.yggraHeads.database.models.Favorite;
import com.github.TavstalDev.yggraHeads.utils.HeadUtils;
import com.github.TavstalDev.yggraLib.api.database.QueryCondition;
import com.github.TavstalDev.yggraLib.api.scheduler.IScheduler;
import com.github.TavstalDev.yggraLib.api.scheduler.YggraTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Per-player runtime cache for GUI state, search/favorite filters, and loaded head entries.
 */
public class PlayerCache {
    private final Player _player;
    /** Current page index in the category GUI. */
    @Getter @Setter
    private int categoryPage;
    /** Current page index in the heads GUI. */
    @Getter @Setter
    private int headsPage;
    /** Current search query, or {@code null} when no search filter is active. */
    @Getter @Setter
    private String search;
    /** Selected category filter, or {@code null} when not filtering by category. */
    @Getter @Setter
    private HeadCategory searchCategory;
    /** Whether the favorites view/filter is active. */
    @Getter @Setter
    private boolean isFavorite;
    /** Whether the player is expected to provide chat input for a head search. */
    @Getter @Setter
    private boolean waitingForSearchInput;
    /** Currently loaded head entries for display in the heads GUI. */
    @Getter @Setter
    private List<Map.Entry<String, HeadData>> heads;
    /** Whether an asynchronous cache/database task is currently running for this player. */
    @Getter @Setter
    private boolean asyncTaskActive;
    /** Cached favorite head references for quick lookup in GUI rendering. */
    @Getter @Setter
    private Set<FavoriteEntry> favoriteCache;

    /**
     * Creates a new player cache for the given player.
     *
     * @param player the player associated with this cache
     */
    public PlayerCache(Player player) {
        _player = player;
        categoryPage = 0;
        headsPage = 0;
    }

    /**
     * Refreshes the cached head list based on the current filters (favorites, search, or category).
     * <p>
     * Data loading is performed asynchronously when needed, and the provided task is executed
     * on the scheduler after the in-memory head list is updated.
     *
     * @param scheduler scheduler used for async/sync task execution
     * @param database  database access for favorite lookups
     * @param task      callback task to run after refresh completes
     */
    public void refreshHeads(IScheduler scheduler, IHeadsDatabase database, YggraTask task) {
        if (asyncTaskActive)
            return;

        scheduler.runAsync(new YggraTask() {
            @Override
            public void run() {
                try
                {
                    asyncTaskActive = true;

                    List<Favorite> favorites = isFavorite ? database.getFavorites().findByCriteria("favorites:" + _player.getUniqueId(),
                            QueryCondition.eq("playerId", _player.getUniqueId())) : new ArrayList<>();

                    if (favorites == null)
                        favorites = new ArrayList<>();

                    List<Favorite> finalFavorites = favorites;
                    scheduler.run(new YggraTask() {
                        @Override
                        public void run() {
                            heads = new ArrayList<>();
                            for (Favorite favorite : finalFavorites) {
                                HeadData head = HeadUtils.getHead(favorite.getCategoryName(), favorite.getHeadName());
                                if (head == null)
                                    continue;

                                heads.add(new AbstractMap.SimpleEntry<>(favorite.getCategoryName(), head));
                            }
                            if (search != null && !search.isEmpty()) {
                                for (HeadCategory category : HeadUtils.getHeadCategories()) {
                                    for (HeadData head : category.getHeads()) {
                                        if (head.Name.toLowerCase().contains(search.toLowerCase())) {
                                            heads.add(new AbstractMap.SimpleEntry<>(category.Name, head));
                                            continue;
                                        }

                                        if (head.Tags.toLowerCase().contains(search.toLowerCase())) {
                                            heads.add(new AbstractMap.SimpleEntry<>(category.Name, head));
                                        }
                                    }
                                }
                            }
                            if (searchCategory != null) {
                                for (HeadData headData : searchCategory.getHeads()) {
                                    heads.add(new AbstractMap.SimpleEntry<>(searchCategory.Name, headData));
                                }
                            }
                            task.run();
                        }
                    });
                }
                finally {
                    asyncTaskActive = false;
                }
            }
        });
    }

    /**
     * Frees the heads data by setting the heads map to null.
     */
    public void freeHeads() {
        heads = null;
    }
}
