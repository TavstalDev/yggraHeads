package com.github.TavstalDev.yggraHeads;

import com.github.TavstalDev.yggraHeads.commands.CommandHeads;
import com.github.TavstalDev.yggraHeads.config.HeadsConfiguration;
import com.github.TavstalDev.yggraHeads.config.models.RedisConfig;
import com.github.TavstalDev.yggraHeads.database.MySqlDatabase;
import com.github.TavstalDev.yggraHeads.database.PostgreDatabase;
import com.github.TavstalDev.yggraHeads.database.SqLiteDatabase;
import com.github.TavstalDev.yggraHeads.events.PlayerEventListener;
import com.github.TavstalDev.yggraHeads.gui.CategoryGUI;
import com.github.TavstalDev.yggraHeads.gui.HeadsGUI;
import com.github.TavstalDev.yggraHeads.database.IHeadsDatabase;
import com.github.TavstalDev.yggraHeads.utils.EconomyUtils;
import com.github.TavstalDev.yggraHeads.utils.HeadUtils;
import com.github.TavstalDev.yggraLib.api.cache.ICache;
import com.github.TavstalDev.yggraLib.api.cache.impl.MemoryCache;
import com.github.TavstalDev.yggraLib.api.cache.impl.RedisCache;
import com.github.TavstalDev.yggraLib.api.localization.ITranslationService;
import com.github.TavstalDev.yggraLib.bootstrap.YggraPlugin;
import com.github.TavstalDev.yggraLib.bootstrap.models.LibraryInfo;
import com.github.TavstalDev.yggraLib.bukkit.gui.GuiService;
import com.github.TavstalDev.yggraLib.bukkit.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

/**
 * Main plugin class for YggraHeads.
 * <p>
 * Handles plugin lifecycle, service registration (configuration, localization,
 * economy, cache, database, GUI service), command registration, and reload logic.
 */
public class YggraHeads extends YggraPlugin<HeadsConfiguration> {
    /** Global plugin instance. */
    public static YggraHeads instance;

    /**
     * Creates a new plugin instance and registers runtime libraries used by the plugin.
     */
    public YggraHeads() {
        super(27765,
                new String[]{
                "https://oss.sonatype.org/content/groups/public/",
                "https://repo.codemc.io/repository/maven-public/"
        }, new LibraryInfo[]{
            new LibraryInfo(
                    "com{}zaxxer",
                    "HikariCP",
                    "4.0.3",
                    null
                    /*"com{}zaxxer",
                    "io{}github{}}tavstaldev{}yggraHeads{}shadow{}hikari"*/
            ),
            new LibraryInfo(
                    "org{}projectlombok",
                    "lombok",
                    "1.18.46",
                    null
                    /*"org{}projectlombok",
                    "io{}github{}}tavstaldev{}yggraHeads{}shadow{}lombok"*/
            ),
            new LibraryInfo(
                    "com{}github{}cryptomorin",
                    "XSeries",
                    "14.0.0",
                    null
                    /*"com{}github{}cryptomorin",
                    "io{}github{}}tavstaldev{}yggraHeads{}shadow{}xseries"*/
            )
        });
    }

    /**
     * Performs early plugin initialization before the main enable phase.
     * <p>
     * Initializes the static instance reference and loads the plugin configuration.
     */
    @Override
    public void onPrePluginEnable() {
        instance = this;
        _config = new HeadsConfiguration();
        _config.load();
    }

    /**
     * Enables the plugin and initializes core systems.
     * <p>
     * This includes version checks, event registration, localization and economy setup,
     * cache/database initialization, GUI registration, command binding, and optional
     * update checks.
     */
    @Override
    public void onPluginEnable() {
        if (!VersionUtils.isAtLeast(1, 8)) {
            _logger.error("The plugin is not compatible with legacy versions of Minecraft. Please use a newer version of the game.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register Events
        PlayerEventListener.init();

        // Load Localizations
        ITranslationService translator = getService(ITranslationService.class);
        if (translator == null)
        {
            _logger.error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register Economy
        if (config().general.isUseEconomy()) {
            _logger.debug("Hooking into Vault...");
            if (EconomyUtils.setupEconomy())
                _logger.info("Economy plugin found and hooked into Vault.");
            else {
                _logger.warn("Economy plugin not found. Unloading...");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }

        // Create cache
        ICache cacheService;
        RedisConfig redis = config().storage.getRedis();
        if (redis.isEnabled()) {
            cacheService = new RedisCache(
                    redis.getHost(),
                    redis.getPort(),
                    redis.getUsername(),
                    redis.getPassword()
            );
        } else {
            cacheService = new MemoryCache(logger().withModule(MemoryCache.class));
        }
        _serviceCollection.put(ICache.class, cacheService);

        // Create Database
        IHeadsDatabase database;
        String databaseType = config().storage.getType();
        if (databaseType == null)
            databaseType = "sqlite";
        switch (databaseType.toLowerCase()) {
            case "mysql":
            case "mariadb": {
                try
                {
                    database = new MySqlDatabase(this);
                }
                catch (Exception ex) {
                    _logger.error("Failed to construct mysql/mariadb database. Unloading...", ex);
                    Bukkit.getPluginManager().disablePlugin(this);
                    return;
                }
                break;
            }
            case "postgre":
            case "postgres":
            case "postgresql": {
                try {
                    database = new PostgreDatabase(this);
                }
                catch (Exception ex) {
                    _logger.error("Failed to construct postgres database. Unloading...", ex);
                    Bukkit.getPluginManager().disablePlugin(this);
                    return;
                }
                break;
            }
            case "sqlite":
            default: {
                try {
                    database = new SqLiteDatabase(this);
                }
                catch (Exception ex) {
                    _logger.error("Failed to construct sqlite database. Unloading...", ex);
                    Bukkit.getPluginManager().disablePlugin(this);
                    return;
                }
                break;
            }
        }
        database.load();
        database.checkSchema();
        _serviceCollection.put(IHeadsDatabase.class, database);

        // Register Head Config
        _logger.debug("Loading config...");
        HeadUtils.load();

        GuiService guiService = new GuiService(this, cacheService);
        _serviceCollection.put(GuiService.class, guiService);

        // Register GUI
        guiService.register(new CategoryGUI());
        guiService.register(new HeadsGUI());

        // Register Commands
        _logger.debug("Registering commands...");
        PluginCommand command = getCommand("heads");
        if (command != null) {
            command.setExecutor(new CommandHeads());
        }

        _logger.ok("Successfully loaded.");
        if (config().general.isCheckForUpdates()) {
            isUpToDate().thenAccept(upToDate -> {
                if (upToDate == null)
                    return;

                if (upToDate) {
                    _logger.ok("Plugin is up to date!");
                    return;
                }

                _logger.warn("A new version of the plugin is available at: " + downloadUrl());
            }).exceptionally(e -> {
                _logger.error("Failed to determine update status: " + e.toString());
                return null;
            });
        }
    }

    /**
     * Disables the plugin and releases registered resources.
     * <p>
     * Unloads the database (if present), closes/unregisters GUIs, and clears GUI caches.
     */
    @Override
    public void onPluginDisable() {
        IHeadsDatabase database = getService(IHeadsDatabase.class);
        if (database != null)
            database.unload();

        // Unregister GUIs
        GuiService guiService = getService(GuiService.class);
        if (guiService != null) {
            guiService.closeAll();
            guiService.unregister(CategoryGUI.ID);
            guiService.unregister(HeadsGUI.ID);
            guiService.invalidateAllCaches();
        }

        _logger.info("Successfully unloaded.");
    }


    /**
     * Returns the public download URL for plugin updates.
     *
     * @return the GitHub releases URL for YggraHeads
     */
    @Override
    public String downloadUrl() {
        return "https://github.com/TavstalDev/yggraHeads/releases";
    }

    /**
     * Replaces plugin placeholders in a message.
     * <p>
     * In addition to base placeholders, this resolves currency placeholders using Vault
     * when available, falling back to localized currency names.
     *
     * @param message the input message containing placeholders
     * @return the message with placeholders replaced
     */
    @Override
    public @NotNull String replacePlaceholders(@NotNull String message) {
        String result = super.replacePlaceholders(message);
        ITranslationService translator = getService(ITranslationService.class);
        if (translator == null)
            return result;

        if (result.contains("%currency_singular%")) {
            String currencySingular = EconomyUtils.currencyNameSingular();
            result = result.replace("%currency_singular%",
                    currencySingular == null ? translator.localize("general.currency.singular") : currencySingular);
        }

        if (result.contains("%currency_plural%")) {
            String currencyPlural = EconomyUtils.currencyNamePlural();
            result = result.replace("%currency_plural%",
                    currencyPlural == null ? translator.localize("general.currency.plural") : currencyPlural);
        }
        return result;
    }

    /**
     * Reloads plugin runtime state.
     * <p>
     * Refreshes localizations and configuration, then closes and re-registers GUIs
     * so changes take effect without a server restart.
     */
    public void reload() {
        _logger.info("Reloading YggraHeads...");
        _logger.debug("Reloading localizations...");
        ITranslationService translator = getService(ITranslationService.class);
        if (translator != null)
            translator.refresh();
        _logger.debug("Localizations reloaded.");
        _logger.debug("Reloading configuration...");
        _config.load();
        _logger.debug("Configuration reloaded.");

        // Re-register GUIs
        GuiService guiService = getService(GuiService.class);
        if (guiService != null) {
            guiService.closeAll();
            guiService.unregister(CategoryGUI.ID);
            guiService.unregister(HeadsGUI.ID);
            guiService.invalidateAllCaches();

            guiService.register(new CategoryGUI());
            guiService.register(new HeadsGUI());
        }
    }
}
