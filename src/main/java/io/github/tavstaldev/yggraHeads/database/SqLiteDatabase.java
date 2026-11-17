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

/**
 * SQLite implementation of the YggraHeads database.
 */
public class SqLiteDatabase implements IHeadsDatabase {
    private final IYggraPlugin plugin;
    private final HeadsConfiguration _config;
    private final YggraLogger logger;

    private SqLiteRepository<UUID, Favorite> _favorites;

    /**
     * Creates a SQLite database handler.
     *
     * @param plugin the plugin instance providing configuration and logging
     * @throws ClassNotFoundException if the SQLite JDBC driver class cannot be found
     */
    public SqLiteDatabase(IYggraPlugin plugin) throws ClassNotFoundException {
        this.plugin = plugin;
        logger = this.plugin.logger().withModule(SqLiteDatabase.class);
        _config = (HeadsConfiguration) plugin.config();

        Class.forName("org.sqlite.JDBC");
    }

    /**
     * Initializes the data source and repositories.
     */
    @Override
    public void load() {
        String tablePrefix = _config.storage.getTablePrefix();
        HikariDataSource _dataSource = createDataSource();
        _favorites = new SqLiteRepository<>(plugin, Favorite.class, _dataSource, 60, tablePrefix);
    }

    /**
     * Unloads database resources.
     * <p>
     * SQLite repository lifecycle is managed by the implementation; no explicit shutdown
     * is performed here.
     */
    @Override
    public void unload() {}

    /**
     * Creates and configures the HikariCP data source for the plugin's SQLite file.
     *
     * @return the configured data source, or {@code null} if creation fails
     */
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
