package io.github.tavstaldev.yggraHeads.models;

import io.github.tavstaldev.yggraHeads.database.IHeadsDatabase;
import io.github.tavstaldev.yggraHeads.database.models.Favorite;
import io.github.tavstaldev.yggraHeads.utils.HeadUtils;
import io.github.tavstaldev.yggraLib.api.database.QueryCondition;
import io.github.tavstaldev.yggraLib.api.scheduler.IScheduler;
import io.github.tavstaldev.yggraLib.api.scheduler.YggraTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerCache {
    private final Player _player;
    @Getter @Setter
    private int categoryPage;
    @Getter @Setter
    private int headsPage;
    @Getter @Setter
    private String search;
    @Getter @Setter
    private HeadCategory searchCategory;
    @Getter @Setter
    private boolean isFavorite;
    @Getter @Setter
    private boolean waitingForSearchInput;
    @Getter @Setter
    private List<Map.Entry<String, HeadData>> heads;
    @Getter @Setter
    private boolean asyncTaskActive;
    @Getter @Setter
    private Set<FavoriteEntry> favoriteCache;

    public PlayerCache(Player player) {
        _player = player;
        categoryPage = 0;
        headsPage = 0;
    }

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
