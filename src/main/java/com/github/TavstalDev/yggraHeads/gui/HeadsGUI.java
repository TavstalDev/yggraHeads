package com.github.TavstalDev.yggraHeads.gui;

import com.cryptomorin.xseries.XMaterial;
import com.github.TavstalDev.yggraLib.bukkit.gui.IGuiService;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.GuiItem;
import com.github.TavstalDev.yggraHeads.YggraHeads;
import com.github.TavstalDev.yggraHeads.database.IHeadsDatabase;
import com.github.TavstalDev.yggraHeads.database.models.Favorite;
import com.github.TavstalDev.yggraHeads.managers.PlayerCacheManager;
import com.github.TavstalDev.yggraHeads.models.FavoriteEntry;
import com.github.TavstalDev.yggraHeads.models.HeadCategory;
import com.github.TavstalDev.yggraHeads.models.HeadData;
import com.github.TavstalDev.yggraHeads.models.PlayerCache;
import com.github.TavstalDev.yggraHeads.utils.EconomyUtils;
import com.github.TavstalDev.yggraHeads.utils.HeadUtils;
import com.github.TavstalDev.yggraLib.api.database.QueryCondition;
import com.github.TavstalDev.yggraLib.api.scheduler.IScheduler;
import com.github.TavstalDev.yggraLib.api.scheduler.YggraTask;
import com.github.TavstalDev.yggraLib.api.utils.ListUtil;
import com.github.TavstalDev.yggraLib.api.utils.MapUtil;
import com.github.TavstalDev.yggraLib.bukkit.gui.GuiBase;
import com.github.TavstalDev.yggraLib.bukkit.gui.GuiButton;
import com.github.TavstalDev.yggraLib.bukkit.gui.GuiService;
import com.github.TavstalDev.yggraLib.bukkit.items.metaAccessors.ISkullMetaAccessor;
import com.github.TavstalDev.yggraLib.bukkit.services.IChatService;
import com.github.TavstalDev.yggraLib.bukkit.utils.AdventureUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * GUI for browsing, searching, favoriting, and obtaining heads within a selected context.
 */
public class HeadsGUI extends GuiBase {

    /** Unique GUI identifier used with {@link GuiService}. */
    public static String ID = "heads";
    private final IGuiService gui;
    private final IChatService chatService;
    private final ISkullMetaAccessor skullMetaAccessor;
    private final IScheduler scheduler;
    private final IHeadsDatabase database;

    /**
     * Creates and configures the heads GUI layout and controls.
     */
    public HeadsGUI() {
        super(ID,
                YggraHeads.instance,
                "heads.yml",
                "gui.main-title",
                true,
                6,
                new LinkedHashMap<String, List<String>>() {{
                    put("head_slots", new ArrayList<String>() {{
                        add("0-44");
                    }});
                }},
                new HashSet<GuiButton>() {{
                    // Placeholder
                    add(new GuiButton(null,
                            "§r ",
                            false,
                            null,
                            false,
                            ListUtil.of("46", "47", "51", "52", "53"),
                            XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()
                    ));
                    // Back button
                    add(new GuiButton(
                            "CLOSE",
                            "gui.common.back",
                            true,
                            null,
                            false,
                            ListUtil.of("45"),
                            XMaterial.SPRUCE_DOOR.parseItem()
                    ));
                    // Previous button
                    add(new GuiButton(
                            "PREV_PAGE",
                            "gui.common.previous-page",
                            true,
                            null,
                            false,
                            ListUtil.of("48"),
                            XMaterial.ARROW.parseItem()
                    ));
                    // Page button, NOTE: should be updated on refresh
                    add(new GuiButton(
                            "CURRENT_PAGE",
                            "gui.common.current-page",
                            true,
                            null,
                            false,
                            ListUtil.of("49"),
                            XMaterial.PAPER.parseItem()
                    ));
                    // Next button
                    add(new GuiButton(
                            "NEXT_PAGE",
                            "gui.common.next-page",
                            true,
                            null,
                            false,
                            ListUtil.of("50"),
                            XMaterial.ARROW.parseItem()
                    ));
                }}
        );
        gui = YggraHeads.instance.getService(IGuiService.class);
        chatService = YggraHeads.instance.getService(IChatService.class);
        skullMetaAccessor = YggraHeads.instance.getService(ISkullMetaAccessor.class);
        scheduler = YggraHeads.instance.getService(IScheduler.class);
        database = YggraHeads.instance.getService(IHeadsDatabase.class);
    }

    /**
     * Prepares the heads GUI for a player and refreshes the displayed content.
     *
     * @param player the player opening the GUI
     * @param args   optional open arguments (unused)
     */
    @Override
    public void onOpen(@NotNull Player player, Object... args) {
        PlayerCache playerData = PlayerCacheManager.get(player.getUniqueId());
        playerData.setHeadsPage(1);

        BaseGui menu = gui.getMenu(player, ID);
        if (menu == null)
            return;
        playerData.refreshHeads(scheduler, database, new YggraTask() {
            @Override
            public void run() {
                refresh(player, menu);
            }
        });
    }

    /**
     * Handles GUI button actions such as pagination and returning to the category GUI.
     *
     * @param player the player interacting with the GUI
     * @param action the configured action identifier
     * @param event  the inventory click event
     */
    @Override
    public void executeCommand(Player player, String action, InventoryClickEvent event) {
        switch (action) {
            case "CLOSE": {
                gui.close(player, true);
                gui.open(player, CategoryGUI.ID);
                break;
            }
            case "PREV_PAGE": {
                UUID playerId = player.getUniqueId();
                BaseGui menu = gui.getMenu(player, ID);
                if (menu == null)
                    break;

                PlayerCache playerCache = PlayerCacheManager.get(playerId);

                if (playerCache.getHeadsPage() - 1 < 0)
                    break;

                playerCache.setHeadsPage(playerCache.getHeadsPage() - 1);
                refresh(player, menu);
                break;
            }
            case "NEXT_PAGE": {
                UUID playerId = player.getUniqueId();
                BaseGui menu = gui.getMenu(player, ID);
                if (menu == null)
                    break;

                PlayerCache playerCache = PlayerCacheManager.get(playerId);

                int maxPage = 1 + (playerCache.getHeads().size() /
                        dynamicSlots.getOrDefault("head_slots", new ArrayList<>()).size());
                if (playerCache.getHeadsPage() + 1 > maxPage)
                    break;

                playerCache.setHeadsPage(playerCache.getHeadsPage() + 1);
                refresh(player, menu);
                break;
            }
        }
    }

    /**
     * Refreshes the heads list, title, and pagination state for the current player view.
     *
     * @param player the player viewing the GUI
     * @param menu   the menu instance to update
     */
    @Override
    public void refresh(@NotNull Player player, @NotNull BaseGui menu) {
        UUID playerId = player.getUniqueId();
        PlayerCache playerCache = PlayerCacheManager.get(player.getUniqueId());

        //#region Title
        if (isTitleTranslated) {
            String menuName;
            String playerSearch = playerCache.getSearch();
            HeadCategory headCategory = playerCache.getSearchCategory();
            if (playerCache.isFavorite()) {
                menuName = translator.localize(playerId, "gui.favorite-title");
            } else if (playerSearch != null && !playerSearch.isEmpty()) {
                menuName = translator.localize(playerId, "gui.search-title",
                        MapUtil.of("search", playerSearch)
                );
            } else if (headCategory != null) {
                String category = translator.localize(playerId, headCategory.DisplayNameKey);
                menuName = translator.localize(playerId, "gui.category-title",
                        MapUtil.of("category", category)
                );
            }
            else
                menuName = menuTitle;

            menu.updateTitle(AdventureUtils.parse(menuName, true));
        }
        //#endregion

        //#region Page Indicator
        Component pageComp = AdventureUtils.parse(translator.localize(playerId,  "gui.common.page", MapUtil.of(
                "page", String.valueOf(playerCache.getHeadsPage())
        )));
        for (GuiButton btn : buttons) {
            if (btn.getTitle() == null || !btn.getTitle().equalsIgnoreCase("{PAGE}"))
                continue;

            for (Integer slot : btn.getSlots()) {
                GuiItem guiItem = menu.getGuiItem(slot);
                if (guiItem == null)
                    continue;

                ItemStack icon = guiItem.getItemStack();
                itemStackAccessor.setDisplayName(icon, pageComp);

                guiItem.setItemStack(icon);
            }
        }
        //#endregion

        //#region Dynamic slots

        List<Integer> dynamicSlots = this.dynamicSlots.getOrDefault("head_slots", new ArrayList<>());
        int page = playerCache.getHeadsPage();
        List<Map.Entry<String, HeadData>> heads = playerCache.getHeads();
        for (int i = 0; i < dynamicSlots.size(); i++) {
            int index = i + (page - 1) * dynamicSlots.size();
            int slot = dynamicSlots.get(i);

            if (index >= heads.size()) {
                menu.removeItem(slot);
                continue;
            }

            Map.Entry<String, HeadData> head = heads.get(index);
            HeadCategory category = HeadUtils.getCategory(head.getKey());
            if (category == null) {
                logger.warn("Failed to find category for head data.");
                continue;
            }

            boolean isFavorite = playerCache.getFavoriteCache().stream()
                    .anyMatch(e -> Objects.equals(e.getHeadName(), head.getValue().Name)
                            && Objects.equals(e.getCategoryName(), category.Name));
            ItemStack icon = head.getValue().getIcon(player, category.Name, category.DisplayNameKey,  isFavorite,
                    translator, itemStackAccessor, skullMetaAccessor);
            menu.setItem(slot, new GuiItem(icon, event ->
            {
                if (playerCache.isAsyncTaskActive())
                    return;

                // Handle left-click events: Buy or receive the head
                if (event.isLeftClick()) {
                    double price = category.Price;
                    HeadData headValue = head.getValue();

                    // Check if the player has enough money to buy the head
                    if (price > 0 && EconomyUtils.isEnabled()) {
                        if (!EconomyUtils.has(player, price)) {
                            chatService.sendLocalizedMsg(player, "general.not-enough-money");
                            return;
                        }

                        // Deduct the price and add the head to the player's inventory
                        EconomyUtils.withdraw(player, price);
                        player.getInventory().addItem(headValue.getItem(player, category.DisplayNameKey,
                                translator, itemStackAccessor, skullMetaAccessor));
                        chatService.sendLocalizedMsg(player, "general.bought-head", MapUtil.of(
                                "price", String.format("%.2f", price), // Format the price to two decimal places
                                "head", headValue.Name
                        ));
                        return;
                    }

                    // Add the head to the player's inventory for free
                    player.getInventory().addItem(headValue.getItem(player, category.DisplayNameKey,
                            translator, itemStackAccessor, skullMetaAccessor));
                    chatService.sendLocalizedMsg(player, "general.received-head", MapUtil.of(
                            "head", headValue.Name
                    ));
                    return;
                }

                // Handle right-click events: Add or remove the head from favorites
                if (event.isRightClick()) {
                    HeadData headValue = head.getValue();
                    String categoryKey = head.getKey();

                    scheduler.runAsync(new YggraTask() {
                        @Override
                        public void run() {
                            try {
                                playerCache.setAsyncTaskActive(true);
                                String cacheKey = "favorites:" + playerId + ":" + categoryKey + ":" + headValue.Name;
                                List<Favorite> favorites = database.getFavorites().findByCriteria(cacheKey,
                                        QueryCondition.eq("playerId", playerId),
                                        QueryCondition.eq("categoryName", categoryKey),
                                        QueryCondition.eq("headName", headValue.Name));
                                if (favorites == null || favorites.isEmpty()) {
                                    database.getFavorites().add(new Favorite(UUID.randomUUID(), playerId, categoryKey, headValue.Name));
                                    playerCache.getFavoriteCache().add(new FavoriteEntry(categoryKey, headValue.Name));
                                }
                                else {
                                    database.getFavorites().deleteById(favorites.get(0).getId());
                                    playerCache.getFavoriteCache().remove(new FavoriteEntry(categoryKey, headValue.Name));
                                }

                                scheduler.run(new YggraTask() {
                                    @Override
                                    public void run() {
                                        // Refresh the slot to reflect the changes
                                        refreshSlot(player, slot);
                                    }
                                });
                            }
                            finally {
                                playerCache.setAsyncTaskActive(false);
                            }
                        }
                    });
                }
            }));
        }

        //#endregion

        menu.update();
    }

    /**
     * Clears temporary head view state when the GUI is closed.
     *
     * @param player the player closing the GUI
     */
    @Override
    public void onClose(@NotNull Player player) {
        PlayerCache playerData = PlayerCacheManager.get(player.getUniqueId());
        playerData.freeHeads();
        playerData.setSearchCategory(null);
    }

    /**
     * Refreshes a single head slot after state changes (e.g. favorite toggles).
     *
     * @param player the player viewing the GUI
     * @param slot   the inventory slot to refresh
     */
    private void refreshSlot(@NotNull Player player, int slot) {
        try {
            // Retrieve the player's data
            PlayerCache playerCache = PlayerCacheManager.get(player.getUniqueId());
            int page = playerCache.getHeadsPage();

            // Calculate the index of the head based on the current page and slot
            int index = slot + (page - 1) * dynamicSlots.getOrDefault("head_slots", new ArrayList<>()).size();
            List<Map.Entry<String, HeadData>> heads = playerCache.getHeads();

            BaseGui menu = gui.getMenu(player, ID);
            if (menu == null)
                return;

            // If the index is out of bounds, remove the button from the slot
            if (index >= heads.size()) {
                menu.removeItem(slot);
                return;
            }

            // Retrieve the head data and its category
            Map.Entry<String, HeadData> head = heads.get(index);
            HeadCategory category = HeadUtils.getCategory(head.getKey());

            // Log a warning if the category is not found and exit
            if (category == null) {
                logger.warn("Failed to find category for head data.");
                return;
            }

            // Set a button in the specified slot with the head's icon and listener
            boolean isFavorite = playerCache.getFavoriteCache().stream()
                    .anyMatch(e -> Objects.equals(e.getHeadName(), head.getValue().Name)
                            && Objects.equals(e.getCategoryName(), category.Name));
            ItemStack icon = head.getValue().getIcon(player, category.Name, category.DisplayNameKey, isFavorite,
                    translator, itemStackAccessor, skullMetaAccessor);
            menu.setItem(slot, new GuiItem(icon, event ->
                    {
                        if (playerCache.isAsyncTaskActive())
                            return;

                        // Handle left-click events: Buy or receive the head
                        if (event.isLeftClick()) {
                            double price = category.Price;
                            HeadData headValue = head.getValue();

                            // Check if the player has enough money to buy the head
                            if (price > 0 && EconomyUtils.isEnabled()) {
                                if (!EconomyUtils.has(player, price)) {
                                    chatService.sendLocalizedMsg(player, "general.not-enough-money");
                                    return;
                                }

                                // Deduct the price and add the head to the player's inventory
                                EconomyUtils.withdraw(player, price);
                                player.getInventory().addItem(headValue.getItem(player, category.DisplayNameKey,
                                        translator, itemStackAccessor, skullMetaAccessor));
                                chatService.sendLocalizedMsg(player, "general.bought-head", MapUtil.of(
                                        "price", String.format("%.2f", price),
                                        "head", headValue.Name
                                ));
                                return;
                            }

                            // Add the head to the player's inventory for free
                            player.getInventory().addItem(headValue.getItem(player, category.DisplayNameKey,
                                    translator, itemStackAccessor, skullMetaAccessor));
                            chatService.sendLocalizedMsg(player, "general.received-head", MapUtil.of(
                                    "head", headValue.Name
                            ));
                            return;
                        }

                        // Handle right-click events: Add or remove the head from favorites
                        if (event.isRightClick()) {
                            HeadData headValue = head.getValue();
                            String categoryKey = head.getKey();
                            UUID playerId = player.getUniqueId();

                            scheduler.runAsync(new YggraTask() {
                                @Override
                                public void run() {
                                    try {
                                        playerCache.setAsyncTaskActive(true);
                                        String cacheKey = "favorites:" + playerId + ":" + categoryKey + ":" + headValue.Name;
                                        List<Favorite> favorites = database.getFavorites().findByCriteria(cacheKey,
                                                QueryCondition.eq("playerId", playerId),
                                                QueryCondition.eq("categoryName", categoryKey),
                                                QueryCondition.eq("headName", headValue.Name));
                                        if (favorites == null || favorites.isEmpty()) {
                                            database.getFavorites().add(new Favorite(UUID.randomUUID(), playerId, categoryKey, headValue.Name));
                                            playerCache.getFavoriteCache().add(new FavoriteEntry(categoryKey, headValue.Name));
                                        }
                                        else {
                                            database.getFavorites().deleteById(favorites.get(0).getId());
                                            playerCache.getFavoriteCache().remove(new FavoriteEntry(categoryKey, headValue.Name));
                                        }

                                        scheduler.run(new YggraTask() {
                                            @Override
                                            public void run() {
                                                // Refresh the slot to reflect the changes
                                                refreshSlot(player, slot);
                                            }
                                        });
                                    }
                                    finally {
                                        playerCache.setAsyncTaskActive(false);
                                    }
                                }
                            });
                        }
                    })
            );

            menu.update();
        } catch (Exception ex) {
            logger.error("An error occurred while refreshing one of the slots of the heads GUI.", ex);
        }
    }
}
