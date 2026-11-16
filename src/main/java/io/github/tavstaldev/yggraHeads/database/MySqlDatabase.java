package io.github.tavstaldev.yggraHeads.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.tavstaldev.yggraHeads.config.HeadsConfiguration;
import io.github.tavstaldev.yggraHeads.config.models.StorageConfig;
import io.github.tavstaldev.yggraHeads.database.models.Favorite;
import io.github.tavstaldev.yggraLib.api.IYggraPlugin;
import io.github.tavstaldev.yggraLib.api.database.DatabaseHelper;
import io.github.tavstaldev.yggraLib.api.database.repositories.IRepository;
import io.github.tavstaldev.yggraLib.api.database.repositories.impl.MySqlRepository;
import io.github.tavstaldev.yggraLib.api.logger.YggraLogger;
import java.util.UUID;

public class MySqlDatabase implements IHeadsDatabase {
    private final IYggraPlugin plugin;
    private final HeadsConfiguration config;
    private final YggraLogger logger;
    private HikariDataSource dataSource;
    private MySqlRepository<UUID, Favorite> favorites;

    public MySqlDatabase(IYggraPlugin plugin) {

        this.plugin = plugin;
        this.config = (HeadsConfiguration)plugin.config();
        logger = plugin.logger().withModule(MySqlDatabase.class);
    }
    
    @Override
    public void load() {
        String tablePrefix = this.config.storage.getTablePrefix();
        dataSource = createDataSource();
        boolean isMariaDb = DatabaseHelper.isMariaDB(dataSource);
        favorites = new MySqlRepository<>(plugin, Favorite.class, dataSource, isMariaDb, 60, tablePrefix);
    }
    
    @Override
    public void unload() {
        if (dataSource != null) {
            if (!dataSource.isClosed())
                dataSource.close();
        }
    }

    @Override
    public void checkSchema() {
        favorites.checkSchema();
    }

    @Override
    public IRepository<UUID, Favorite> getFavorites() {
        return favorites;
    }

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
