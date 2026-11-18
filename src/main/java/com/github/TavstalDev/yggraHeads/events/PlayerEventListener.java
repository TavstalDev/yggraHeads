package com.github.TavstalDev.yggraHeads.events;

import com.github.TavstalDev.yggraHeads.YggraHeads;
import com.github.TavstalDev.yggraHeads.database.IHeadsDatabase;
import com.github.TavstalDev.yggraHeads.database.models.Favorite;
import com.github.TavstalDev.yggraHeads.gui.HeadsGUI;
import com.github.TavstalDev.yggraHeads.managers.PlayerCacheManager;
import com.github.TavstalDev.yggraHeads.models.FavoriteEntry;
import com.github.TavstalDev.yggraHeads.models.PlayerCache;
import com.github.TavstalDev.yggraLib.api.database.QueryCondition;
import com.github.TavstalDev.yggraLib.api.logger.YggraLogger;
import com.github.TavstalDev.yggraLib.api.scheduler.IScheduler;
import com.github.TavstalDev.yggraLib.api.scheduler.YggraTask;
import com.github.TavstalDev.yggraLib.bukkit.gui.GuiService;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * Bukkit event listener handling player cache lifecycle and GUI search input flow.
 */
@Slf4j
@SuppressWarnings("unused")
public class PlayerEventListener implements Listener
{
    private static final YggraLogger _logger = YggraHeads.instance.logger().withModule(PlayerEventListener.class);

    /**
     * Registers this listener with the Bukkit plugin manager.
     */
    public static void init() {
        _logger.debug("Registering event listener...");
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(), YggraHeads.instance);
        _logger.debug("Event listener registered.");
    }

    /**
     * Initializes and caches player data on join, including asynchronous loading of favorites.
     *
     * @param event the player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerCache playerData = new PlayerCache(player);
        PlayerCacheManager.add(player.getUniqueId(), playerData);

        IHeadsDatabase database = YggraHeads.instance.getService(IHeadsDatabase.class);
        IScheduler scheduler = YggraHeads.instance.getService(IScheduler.class);
        if (database == null || scheduler == null)
            return;

        YggraLogger logger = YggraHeads.instance.logger();
        scheduler.runAsync(new YggraTask() {
            @Override
            public void run() {
                try {
                    playerData.setAsyncTaskActive(true);
                    List<Favorite> favorites = database.getFavorites().findByCriteria("favorites:" + player.getUniqueId(),
                            QueryCondition.eq("playerId", player.getUniqueId()));
                    if (favorites == null)
                        favorites = new ArrayList<>();
                    List<Favorite> finalFavorites = favorites;
                    scheduler.run(new YggraTask() {
                        @Override
                        public void run() {
                            Set<FavoriteEntry> entries = new HashSet<>();
                            for (Favorite fav : finalFavorites) {
                               entries.add(new FavoriteEntry(fav.getCategoryName(), fav.getHeadName()));
                            }
                            playerData.setFavoriteCache(entries);
                        }
                    });
                }
                finally {
                    playerData.setAsyncTaskActive(false);
                }
            }
        });
    }

    /**
     * Removes cached player data when the player leaves the server.
     *
     * @param event the player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerCacheManager.remove(player.getUniqueId());
    }

    /**
     * Captures chat input for GUI search prompts when a player is awaiting search text.
     *
     * @param event the async player chat event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        PlayerCache cache = PlayerCacheManager.get(player.getUniqueId());
        if (!cache.isWaitingForSearchInput())
            return;

        event.setCancelled(true);
        cache.setWaitingForSearchInput(false);

        String text = event.getMessage();
        if (Objects.equals(text, "cancel"))
            return;

        IScheduler scheduler = YggraHeads.instance.getService(IScheduler.class);
        if (scheduler == null)
            return;

        scheduler.run(new YggraTask() {
            @Override
            public void run() {
                GuiService guiService = YggraHeads.instance.getService(GuiService.class);
                if (guiService == null)
                    return;

                cache.setSearch(text);
                guiService.open(player, HeadsGUI.ID);
            }
        });
    }

    /**
     * Blocks command execution while the player is in search input mode to avoid accidental commands.
     *
     * @param event the player command preprocess event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        PlayerCache cache = PlayerCacheManager.get(player.getUniqueId());
        if (!cache.isWaitingForSearchInput())
            return;
        event.setCancelled(true);
    }
}
