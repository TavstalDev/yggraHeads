package io.github.tavstaldev.yggraHeads.gui;

import com.cryptomorin.xseries.XMaterial;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.tavstaldev.yggraHeads.YggraHeads;
import io.github.tavstaldev.yggraHeads.database.IHeadsDatabase;
import io.github.tavstaldev.yggraHeads.managers.PlayerCacheManager;
import io.github.tavstaldev.yggraHeads.models.HeadCategory;
import io.github.tavstaldev.yggraHeads.models.PlayerCache;
import io.github.tavstaldev.yggraHeads.utils.HeadUtils;
import io.github.tavstaldev.yggraLib.api.scheduler.IScheduler;
import io.github.tavstaldev.yggraLib.api.scheduler.YggraTask;
import io.github.tavstaldev.yggraLib.api.utils.ListUtil;
import io.github.tavstaldev.yggraLib.api.utils.MapUtil;
import io.github.tavstaldev.yggraLib.bukkit.gui.GuiBase;
import io.github.tavstaldev.yggraLib.bukkit.gui.GuiButton;
import io.github.tavstaldev.yggraLib.bukkit.gui.GuiService;
import io.github.tavstaldev.yggraLib.bukkit.items.metaAccessors.ISkullMetaAccessor;
import io.github.tavstaldev.yggraLib.bukkit.services.IChatService;
import io.github.tavstaldev.yggraLib.bukkit.utils.AdventureUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * GUI for browsing head categories and navigating to heads, favorites, or search.
 */
public class CategoryGUI extends GuiBase {

    /** Unique GUI identifier used with {@link GuiService}. */
    public static String ID = "categories";
    private final GuiService gui;
    private final IChatService chatService;
    private final ISkullMetaAccessor skullMetaAccessor;
    private final IScheduler scheduler;
    private final IHeadsDatabase database;

    /**
     * Creates and configures the category selection GUI layout and controls.
     */
    public CategoryGUI() {
        super(ID,
                YggraHeads.instance,
                "categories.yml",
                "gui.main-title",
                true,
                6,
                new LinkedHashMap<String, List<String>>() {{
                    put("category_slots", new ArrayList<String>() {{
                        add("10-16");
                        add("19-25");
                        add("28-34");
                        add("37-43");
                    }});
                }},
                new HashSet<GuiButton>() {{
                    // Placeholder
                    add(new GuiButton(null,
                            "§r ",
                            false,
                            null,
                            false,
                        ListUtil.of("0-9", "17-18", "26-27", "35-36", "44", "46-47", "51"),
                            XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()
                    ));
                    // Back button
                    add(new GuiButton(
                            "CLOSE",
                            "gui.common.close",
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
                            "PAGE",
                            "{PAGE}",
                            false,
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
                    // Favorites button
                    add(new GuiButton(
                            "FAVORITES",
                            "gui.common.favorites",
                            true,
                            null,
                            false,
                            ListUtil.of("52"),
                            XMaterial.NETHER_STAR.parseItem()
                    ));
                    // Search button
                    add(new GuiButton(
                            "SEARCH",
                            "gui.common.search",
                            true,
                            null,
                            false,
                            ListUtil.of("53"),
                            XMaterial.COMPASS.parseItem()
                    ));
                }}
        );
        gui = YggraHeads.instance.getService(GuiService.class);
        chatService = YggraHeads.instance.getService(IChatService.class);
        skullMetaAccessor = YggraHeads.instance.getService(ISkullMetaAccessor.class);
        scheduler = YggraHeads.instance.getService(IScheduler.class);
        database = YggraHeads.instance.getService(IHeadsDatabase.class);
    }

    /**
     * Prepares the category GUI for a player and refreshes the displayed content.
     *
     * @param player the player opening the GUI
     * @param args   optional open arguments (unused)
     */
    @Override
    public void onOpen(@NotNull Player player, Object... args) {
        PlayerCache playerCache = PlayerCacheManager.get(player.getUniqueId());
        playerCache.setCategoryPage(1);

        BaseGui menu = gui.getMenu(player, ID);
        if (menu == null)
            return;
        playerCache.refreshHeads(scheduler, database, new YggraTask() {
            @Override
            public void run() {
                refresh(player, menu);
            }
        });
    }

    /**
     * Handles GUI button actions such as pagination, close, favorites, and search.
     *
     * @param player the player interacting with the GUI
     * @param action the configured action identifier
     * @param event  the inventory click event
     */
    @Override
    public void executeCommand(Player player, String action, InventoryClickEvent event) {
        switch (action) {
            case "CLOSE": {
                gui.close(player, false);
                break;
            }
            case "PREV_PAGE": {
                UUID playerId = player.getUniqueId();
                BaseGui menu = gui.getMenu(player, ID);
                if (menu == null)
                    break;

                PlayerCache playerCache = PlayerCacheManager.get(playerId);
                if (playerCache.getCategoryPage() - 1 < 0)
                    break;

                playerCache.setCategoryPage(playerCache.getCategoryPage() - 1);
                refresh(player, menu);
                break;
            }
            case "NEXT_PAGE": {
                UUID playerId = player.getUniqueId();
                BaseGui menu = gui.getMenu(player, ID);
                if (menu == null)
                    break;

                PlayerCache playerCache = PlayerCacheManager.get(playerId);

                int maxPage = 1 + (HeadUtils.getHeadCategories().size() /
                        dynamicSlots.getOrDefault("category_slots", new ArrayList<>()).size());
                if (playerCache.getCategoryPage() + 1 > maxPage)
                    break;

                playerCache.setCategoryPage(playerCache.getCategoryPage() + 1);
                refresh(player, menu);
                break;
            }
            case "FAVORITES": {
                PlayerCache playerCache = PlayerCacheManager.get(player.getUniqueId());
                playerCache.setHeadsPage(1);
                playerCache.setSearchCategory(null);
                playerCache.setFavorite(true);
                playerCache.setSearch(null);
                gui.open(player, HeadsGUI.ID);
                break;
            }
            case "SEARCH": {
                PlayerCache playerCache = PlayerCacheManager.get(player.getUniqueId());
                gui.close(player, false);
                playerCache.setHeadsPage(1);
                playerCache.setSearchCategory(null);
                playerCache.setFavorite(false);
                playerCache.setSearch(null);
                playerCache.setWaitingForSearchInput(true);
                chatService.sendLocalizedMsg(player, "gui.search-start");
                break;
            }
        }
    }

    /**
     * Refreshes the category list and page indicator for the current player state.
     *
     * @param player the player viewing the GUI
     * @param menu   the menu instance to update
     */
    @Override
    public void refresh(@NotNull Player player, @NotNull BaseGui menu) {
        UUID playerId = player.getUniqueId();
        PlayerCache playerCache = PlayerCacheManager.get(player.getUniqueId());

        //#region Page Indicator
        Component pageComp = AdventureUtils.parse(translator.localize(playerId,  "gui.common.page", MapUtil.of(
                "page", String.valueOf(playerCache.getCategoryPage())
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

        // 3. Handle dynamic slots
        List<Integer> dynamicSlots = this.dynamicSlots.getOrDefault("category_slots", new ArrayList<>());
        int page = playerCache.getCategoryPage();
        List<HeadCategory> heads = HeadUtils.getHeadCategories();
        for (int i = 0; i < dynamicSlots.size(); i++) {
            int index = i + (page - 1) * dynamicSlots.size();
            int slot = dynamicSlots.get(i);

            if (index >= heads.size()) {
                menu.removeItem(slot);
                continue;
            }

            HeadCategory category = heads.get(index);
            if (category == null) {
                logger.warn("Failed to find category.");
                continue;
            }

            menu.setItem(slot, new GuiItem(category.getIcon(player, translator, itemStackAccessor, skullMetaAccessor), event ->
            {
                PlayerCache data = PlayerCacheManager.get(player.getUniqueId());
                data.setHeadsPage(1);
                data.setSearchCategory(category);
                data.setFavorite(false);
                data.setSearch(null);
                gui.open(player, HeadsGUI.ID);
            }));
        }
        menu.update();
    }
}
