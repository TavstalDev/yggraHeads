package com.github.TavstalDev.yggraHeads.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.github.TavstalDev.yggraHeads.config.HeadsConfiguration;
import com.github.TavstalDev.yggraHeads.config.models.StorageConfig;
import com.github.TavstalDev.yggraHeads.database.models.Favorite;
import com.github.TavstalDev.yggraLib.api.IYggraPlugin;
import com.github.TavstalDev.yggraLib.api.database.DatabaseHelper;
import com.github.TavstalDev.yggraLib.api.database.repositories.IRepository;
import com.github.TavstalDev.yggraLib.api.database.repositories.impl.MySqlRepository;
import com.github.TavstalDev.yggraLib.api.logger.YggraLogger;
import java.util.UUID;

/**
 * MySQL/MariaDB implementation of the YggraHeads database.
 */
public class MySqlDatabase implements IHeadsDatabase {
    private final IYggraPlugin plugin;
    private final HeadsConfiguration config;
    private final YggraLogger logger;
    private HikariDataSource dataSource;
    private MySqlRepository<UUID, Favorite> favorites;

    /**
     * Creates a MySQL/MariaDB database handler.
     *
     * @param plugin the plugin instance providing configuration and logging
     */
    public MySqlDatabase(IYggraPlugin plugin) {

        this.plugin = plugin;
        this.config = (HeadsConfiguration)plugin.config();
        logger = plugin.logger().withModule(MySqlDatabase.class);
    }
    
    /**
     * Initializes the data source and repositories.
     */
    @Override
    public void load() {
        String tablePrefix = this.config.storage.getTablePrefix();
        dataSource = createDataSource();
        boolean isMariaDb = DatabaseHelper.isMariaDB(dataSource);
        favorites = new MySqlRepository<>(plugin, Favorite.class, dataSource, isMariaDb, 60, tablePrefix);
    }
    
    /**
     * Closes the database connection pool if it is open.
     */
    @Override
    public void unload() {
        if (dataSource != null) {
            if (!dataSource.isClosed())
                dataSource.close();
        }
    }

    /**
     * Ensures the required database schema exists.
     */
    @Override
    public void checkSchema() {
        favorites.checkSchema();
    }

    /** {@inheritDoc} */
    @Override
    public IRepository<UUID, Favorite> getFavorites() {
        return favorites;
    }

    /**
     * Creates and configures the HikariCP data source for MySQL/MariaDB.
     *
     * @return the configured data source, or {@code null} if creation fails
     */
    public HikariDataSource createDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            StorageConfig sc = this.config.storage;
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s", config, sc.getPort(), sc.getDatabase()));
            config.setUsername(sc.getUsername());
            config.setPassword(sc.getPassword());
            config.setMaximumPoolSize(sc.getPool());
            config.setMaxLifetime(sc.getMaxLifeTime());
            config.setConnectionTimeout(sc.getConnectionTimeout());
            return new HikariDataSource(config);
        } catch (Exception ex) {
            logger.error("Unknown error happened during the creation of database connection..", ex);
            return null;
        }
    }
}
