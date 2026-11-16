package io.github.tavstaldev.yggraHeads.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.tavstaldev.yggraHeads.config.HeadsConfiguration;
import io.github.tavstaldev.yggraHeads.config.models.StorageConfig;
import io.github.tavstaldev.yggraHeads.database.models.Favorite;
import io.github.tavstaldev.yggraLib.api.IYggraPlugin;
import io.github.tavstaldev.yggraLib.api.database.repositories.IRepository;
import io.github.tavstaldev.yggraLib.api.database.repositories.impl.PostgreSqlRepository;
import io.github.tavstaldev.yggraLib.api.logger.YggraLogger;

import java.util.UUID;

public class PostgreDatabase implements IHeadsDatabase {
    private final IYggraPlugin plugin;
    private final HeadsConfiguration config;
    private final YggraLogger logger;
    private HikariDataSource _dataSource;

    private PostgreSqlRepository<UUID, Favorite> _favorites;

    public PostgreDatabase(IYggraPlugin plugin) {

        this.plugin = plugin;
        config = (HeadsConfiguration) plugin.config();
        logger = this.plugin.logger().withModule(MySqlDatabase.class);
    }

    @Override
    public void load() {
        String tablePrefix = config.storage.getTablePrefix();
        _dataSource = createDataSource();
        _favorites = new PostgreSqlRepository<>(plugin, Favorite.class, _dataSource, 60, tablePrefix);
    }

    @Override
    public void unload() {
        if (_dataSource != null) {
            if (!_dataSource.isClosed())
                _dataSource.close();
        }
    }

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

    @Override
    public void checkSchema() {
        _favorites.checkSchema();
    }

    @Override
    public IRepository<UUID, Favorite> getFavorites() {
        return _favorites;
    }
}
