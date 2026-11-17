package io.github.tavstaldev.yggraHeads.models;

import com.cryptomorin.xseries.XMaterial;
import io.github.tavstaldev.yggraHeads.YggraHeads;
import io.github.tavstaldev.yggraLib.api.localization.ITranslationService;
import io.github.tavstaldev.yggraLib.api.utils.MapUtil;
import io.github.tavstaldev.yggraLib.bukkit.items.IItemStackAccessor;
import io.github.tavstaldev.yggraLib.bukkit.items.metaAccessors.ISkullMetaAccessor;
import io.github.tavstaldev.yggraLib.bukkit.utils.AdventureUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HeadData {
    /**
     * The name of the head data.
     */
    public String Name;

    /**
     * The tags associated with the head data.
     */
    public String Tags;

    /**
     * The texture associated with the head data.
     */
    public String Texture;

    /**
     * Constructs a new HeadData object with the specified parameters.
     *
     * @param name The name of the head data.
     * @param tags The tags associated with the head data.
     * @param texture The texture associated with the head data.
     */
    public HeadData(String name, String tags, String texture) {
        Name = name;
        Tags = tags;
        Texture = texture;
    }

    /**
     * Splits the tags associated with the head data into an array of individual tags.
     *
     * @return an array of strings, each representing a tag
     */
    public String[] getTags() {
        return Tags.split(",");
    }

    /**
     * Creates an ItemStack representing the head icon for the player.
     *
     * @param player the player for whom the icon is being created
     * @param categoryDisplayNameKey the key for the category display name localization
     * @return the ItemStack representing the head icon
     */
    public ItemStack getIcon(Player player, String category, String categoryDisplayNameKey, boolean isFavorite,
                             ITranslationService translator, IItemStackAccessor itemStackAccessor, ISkullMetaAccessor skullMetaAccessor) {
        List<Component> loreList = new ArrayList<>();
        UUID playerId = player.getUniqueId();
        String favoriteTxt = translator.localize(playerId, isFavorite ? "gui.favorite.remove" : "gui.favorite.add");
        String categoryTxt = translator.localize(playerId, categoryDisplayNameKey);
        for (String lore : translator.localizeList(playerId, "gui.head.lore"))
            loreList.add(AdventureUtils.parse(lore
                    .replace("%category%", categoryTxt)
                    .replace("%favorite%", favoriteTxt),
                    true)
            );

        String favoriteText = translator.localize(playerId, isFavorite ? "gui.favorite.yes-text" : "gui.favorite.no-text");
        Component displayName = AdventureUtils.parse(translator.localize(playerId, "gui.head.name", MapUtil.of(
                "head", Name,
                "favorite", favoriteText
        )));

        try
        {
            ItemStack result;

            if (Texture == null)
                result = XMaterial.ZOMBIE_HEAD.parseItem();
            else
                result = XMaterial.PLAYER_HEAD.parseItem();

            itemStackAccessor.setDisplayName(result, displayName);
            itemStackAccessor.setLoreComp(result, loreList);

            assert result != null;
            skullMetaAccessor.setTexture(result, Texture);
            return result;
        }
        catch (Exception ex) {
            YggraHeads.instance.logger().error("Failed to get category icon.", ex);
            ItemStack fallback = XMaterial.ZOMBIE_HEAD.parseItem();
            itemStackAccessor.setDisplayName(fallback, displayName);
            itemStackAccessor.setLoreComp(fallback, loreList);
            return fallback;
        }
    }

    /**
     * Creates an ItemStack representing the head item for the player.
     *
     * @param player the player for whom the item is being created
     * @param categoryDisplayNameKey the key for the category display name localization
     * @return the ItemStack representing the head item
     */
    public ItemStack getItem(Player player, String categoryDisplayNameKey, ITranslationService translator,
                             IItemStackAccessor itemStackAccessor, ISkullMetaAccessor skullMetaAccessor) {
        UUID playerId = player.getUniqueId();
        List<Component> loreList = new ArrayList<Component>() {{
            add(AdventureUtils.parse(String.format("&8%s", translator.localize(playerId, categoryDisplayNameKey)), true));
        }};
        Component displayName = AdventureUtils.parse(translator.localize(playerId, "gui.head.name")
                .replace("%head%", Name)
                .replace("%favorite%", ""));

        try
        {
            if (Texture == null) {
                ItemStack fallback = XMaterial.ZOMBIE_HEAD.parseItem();
                itemStackAccessor.setDisplayName(fallback, displayName);
                itemStackAccessor.setLoreComp(fallback, loreList);
                return fallback;
            }
            ItemStack item = XMaterial.PLAYER_HEAD.parseItem();
            assert item != null;
            // Set display name
            itemStackAccessor.setDisplayName(item, displayName);
            // Set lore
            itemStackAccessor.setLoreComp(item, loreList);
            skullMetaAccessor.setTexture(item, Texture);
            return item;
        }
        catch (Exception ex) {
            YggraHeads.instance.logger().error("Failed to get category icon.", ex);
            ItemStack fallback = XMaterial.ZOMBIE_HEAD.parseItem();
            itemStackAccessor.setDisplayName(fallback, displayName);
            itemStackAccessor.setLoreComp(fallback, loreList);
            return fallback;
        }
    }
}
