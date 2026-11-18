package com.github.TavstalDev.yggraHeads.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.github.TavstalDev.yggraHeads.config.HeadsConfiguration;
import com.github.TavstalDev.yggraHeads.config.models.StorageConfig;
import com.github.TavstalDev.yggraHeads.database.models.Favorite;
import com.github.TavstalDev.yggraLib.api.IYggraPlugin;
import com.github.TavstalDev.yggraLib.api.database.repositories.IRepository;
import com.github.TavstalDev.yggraLib.api.database.repositories.impl.PostgreSqlRepository;
import com.github.TavstalDev.yggraLib.api.logger.YggraLogger;

import java.util.UUID;

/**
 * PostgreSQL implementation of the YggraHeads database.
 */
public class PostgreDatabase implements IHeadsDatabase {
    private final IYggraPlugin plugin;
    private final HeadsConfiguration config;
    private final YggraLogger logger;
    private HikariDataSource _dataSource;

    private PostgreSqlRepository<UUID, Favorite> _favorites;

    /**
     * Creates a PostgreSQL database handler.
     *
     * @param plugin the plugin instance providing configuration and logging
     */
    public PostgreDatabase(IYggraPlugin plugin) {

        this.plugin = plugin;
        config = (HeadsConfiguration) plugin.config();
        logger = this.plugin.logger().withModule(MySqlDatabase.class);
    }

    /**
     * Initializes the data source and repositories.
     */
    @Override
    public void load() {
        String tablePrefix = config.storage.getTablePrefix();
        _dataSource = createDataSource();
        _favorites = new PostgreSqlRepository<>(plugin, Favorite.class, _dataSource, 60, tablePrefix);
    }

    /**
     * Closes the database connection pool if it is open.
     */
    @Override
    public void unload() {
        if (_dataSource != null) {
            if (!_dataSource.isClosed())
                _dataSource.close();
        }
    }

    /**
     * Creates and configures the HikariCP data source for PostgreSQL.
     *
     * @return the configured data source, or {@code null} if creation fails
     */
    public HikariDataSource createDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            StorageConfig sc = this.config.storage;
            config.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/%s", sc.getHost(), sc.getPort(), sc.getDatabase()));
            config.setUsername(sc.getUsername());
            config.setPassword(sc.getPassword());
            config.setDriverClassName("org.postgresql.Driver");
            config.setMaximumPoolSize(sc.getPool());
            config.setMaxLifetime(sc.getMaxLifeTime());
            config.setConnectionTimeout(sc.getConnectionTimeout());
            return new HikariDataSource(config);
        } catch (Exception ex) {
            logger.error("Unknown error happened during the creation of database connection..", ex);
            return null;
        }
    }

    /**
     * Ensures the required database schema exists.
     */
    @Override
    public void checkSchema() {
        _favorites.checkSchema();
    }

    /** {@inheritDoc} */
    @Override
    public IRepository<UUID, Favorite> getFavorites() {
        return _favorites;
    }
}
