package io.github.tavstaldev.yggraHeads.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.tavstaldev.yggraHeads.config.HeadsConfiguration;
import io.github.tavstaldev.yggraHeads.database.models.Favorite;
import io.github.tavstaldev.yggraLib.api.IYggraPlugin;
import io.github.tavstaldev.yggraLib.api.database.repositories.IRepository;
import io.github.tavstaldev.yggraLib.api.database.repositories.impl.SqLiteRepository;
import io.github.tavstaldev.yggraLib.api.logger.YggraLogger;

import java.util.UUID;

public class SqLiteDatabase implements IHeadsDatabase {
    private final IYggraPlugin plugin;
    private final HeadsConfiguration _config;
    private final YggraLogger logger;

    private SqLiteRepository<UUID, Favorite> _favorites;

    public SqLiteDatabase(IYggraPlugin plugin) throws ClassNotFoundException {
        this.plugin = plugin;
        logger = this.plugin.logger().withModule(SqLiteDatabase.class);
        _config = (HeadsConfiguration) plugin.config();

        Class.forName("org.sqlite.JDBC");
    }

    @Override
    public void load() {
        String tablePrefix = _config.storage.getTablePrefix();
        HikariDataSource _dataSource = createDataSource();
        _favorites = new SqLiteRepository<>(plugin, Favorite.class, _dataSource, 60, tablePrefix);
    }

    @Override
    public void unload() {}

    public HikariDataSource createDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(String.format("jdbc:sqlite:plugins/%s/%s.db", plugin.name(), plugin.name()));
            config.setMaximumPoolSize(4);

            // Disable features and validation checks unsupported by older SQLite JDBC drivers
            config.setConnectionTestQuery("SELECT 1");
            config.setInitializationFailTimeout(-1);
            config.validate();

            config.setConnectionInitSql(
                    "PRAGMA journal_mode=WAL; PRAGMA synchronous=NORMAL; PRAGMA busy_timeout=5000;"
            );

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
