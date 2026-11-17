package io.github.tavstaldev.yggraHeads.models;

import com.cryptomorin.xseries.XMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.tavstaldev.yggraHeads.YggraHeads;
import io.github.tavstaldev.yggraHeads.utils.EconomyUtils;
import io.github.tavstaldev.yggraLib.api.localization.ITranslationService;
import io.github.tavstaldev.yggraLib.api.logger.YggraLogger;
import io.github.tavstaldev.yggraLib.bukkit.items.IItemStackAccessor;
import io.github.tavstaldev.yggraLib.bukkit.items.metaAccessors.ISkullMetaAccessor;
import io.github.tavstaldev.yggraLib.bukkit.utils.AdventureUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HeadCategory {
    /**
     * The name of the head category.
     */
    public final String Name;

    /**
     * The key for the display name of the head category.
     */
    public final String DisplayNameKey;

    /**
     * The key for the description of the head category.
     */
    public final String DescriptionKey;

    /**
     * Indicates if permission is required to access the head category.
     */
    public final boolean RequirePermission;

    /**
     * The permission string required to access the head category.
     */
    public final String Permission;

    /**
     * The price of the head category.
     */
    public final Double Price;

    /**
     * The file associated with the head category.
     */
    private final String _file;

    /**
     * The texture associated with the head category.
     */
    public final String Texture;

    /**
     * A list of head data associated with the head category.
     */
    private List<HeadData> _heads;

    /**
     * Gets the list of head data associated with the head category.
     *
     * @return A list of HeadData objects.
     */
    public List<HeadData> getHeads() {
        return _heads;
    }

    /**
     * The logger for the head category.
     */
    private final YggraLogger _logger;

    /**
     * The icon representing the head category.
     */
    private ItemStack _icon;

    /**
     * Constructs a new HeadCategory object with the specified parameters.
     *
     * @param name The name of the head category.
     * @param displayNameKey The key for the display name of the head category.
     * @param descriptionKey The key for the description of the head category.
     * @param requirePermission Indicates if permission is required to access the head category.
     * @param permission The permission string required to access the head category.
     * @param price The price of the head category.
     * @param file The file associated with the head category.
     * @param texture The texture associated with the head category.
     */
    public HeadCategory(String name, String displayNameKey, String descriptionKey, boolean requirePermission, String permission, Double price, String file, String texture) {
        Name = name;
        DisplayNameKey = displayNameKey;
        DescriptionKey = descriptionKey;
        RequirePermission = requirePermission;
        Permission = permission;
        Price = price;
        _file = file;
        Texture = texture;
        _heads = new ArrayList<>();
        _logger = YggraHeads.instance.logger().withModule(String.format("%s Category", Name));
    }

    /**
     * Copies the head data file from the plugin's resources to the plugin's data folder.
     *
     * @return true if the file was successfully copied or already exists, false otherwise
     */
    public boolean copyFromResource() {
        InputStream inputStream;
        Path dirPath = Paths.get(YggraHeads.instance.getDataFolder().getPath(), "heads");
        Path filePath = Paths.get(dirPath.toString(), _file);
        if (!Files.exists(dirPath)) {
            try {
                _logger.debug("Creating heads directory...");
                Files.createDirectory(dirPath);
            } catch (IOException ex) {
                _logger.warn("Failed to create heads directory.");
                _logger.error(ex.getMessage());
                return false;
            }
        }

        if (!Files.exists(filePath)) {
            _logger.debug(String.format("Copying %s head file...", Name));
            try {
                inputStream = YggraHeads.instance.getResource("heads/" + _file);
                if (inputStream == null) {
                    _logger.debug(String.format("Failed to get head data file for category '%s'.", Name));
                } else
                    Files.copy(inputStream, filePath);
            } catch (IOException ex) {
                _logger.warn(String.format("Failed to copy head data file for category '%s'.", Name));
                _logger.error(ex.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * Loads the head data from the file associated with the head category.
     *
     * @return true if the data was successfully loaded, false otherwise
     */
    public boolean load() {
        Path dirPath = Paths.get(YggraHeads.instance.getDataFolder().getPath(), "heads");
        Path filePath = Paths.get(dirPath.toString(), _file);
        if (!Files.exists(filePath))
            return false;

        _logger.debug(String.format("Reading %s head file...", Name));
        Gson gson = new Gson();
        try (FileReader fileReader = new FileReader(filePath.toFile())) {
            _heads = gson.fromJson(fileReader, new TypeToken<List<HeadData>>() {}.getType());
        } catch (IOException ex) {
            _logger.error(String.format("Failed to read or parse the file. Path: %s", filePath));
            _logger.error(ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Creates or returns the cached icon representing this head category for the specified player.
     *
     * @param player             the player for whom the icon is being created
     * @param translator         translation service used for localized text
     * @param itemStackAccessor  item stack accessor used to set display name and lore
     * @param skullMetaAccessor  skull meta accessor used to apply the category texture
     * @return the item stack representing the category icon
     */
    public ItemStack getIcon(Player player, ITranslationService translator, IItemStackAccessor itemStackAccessor,
                             ISkullMetaAccessor skullMetaAccessor) {
        if (_icon != null)
            return _icon;

        UUID playerId = player.getUniqueId();
        
        List<Component> loreList = new ArrayList<>();
        Component displayName = AdventureUtils.parse(translator.localize(playerId, "gui.category.name")
                .replace("%category%", translator.localize(playerId, DisplayNameKey)));
        String description = translator.localize(playerId, DescriptionKey);
        String freeText = translator.localize(playerId, "gui.common.free");
        for (String rawLore : translator.localizeList(playerId, "gui.category.lore")) {
            String lore = rawLore
                    .replace("%price%", Price == 0 ? freeText : String.format("%.2f", Price))
                    .replace("%description%", description)
                    .replace("%count%", String.valueOf(_heads.size()));

            if (lore.contains("%currency_singular%")) {
                String currencySingular = EconomyUtils.currencyNameSingular();
                lore = lore.replace("%currency_singular%", Price == 0 ? "" : currencySingular == null ? translator.localize(playerId, "general.currency.singular") : currencySingular);
            }
            if (lore.contains("%currency_plural%")) {
                String currencyPlural = EconomyUtils.currencyNamePlural();
                lore = lore.replace("%currency_plural%", Price == 0 ? "" : currencyPlural == null ? translator.localize(playerId, "general.currency.plural") : currencyPlural);
            }
            loreList.add(AdventureUtils.parse(lore, true));
        }

        try
        {
            ItemStack result;

            if (Texture == null)
                result = XMaterial.ZOMBIE_HEAD.parseItem();
            else
                result = XMaterial.PLAYER_HEAD.parseItem();

            itemStackAccessor.setDisplayName(result,  displayName);
            itemStackAccessor.setLoreComp(result, loreList);

            assert result != null;
            skullMetaAccessor.setTexture(result, Texture);
            _icon = result;
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
}
