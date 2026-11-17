package io.github.tavstaldev.yggraHeads.database;

import io.github.tavstaldev.yggraHeads.database.models.Favorite;
import io.github.tavstaldev.yggraLib.api.database.IDatabase;
import io.github.tavstaldev.yggraLib.api.database.repositories.IRepository;

import java.util.UUID;

/**
 * Database contract for YggraHeads persistent storage operations.
 */
public interface IHeadsDatabase extends IDatabase {
    /**
     * Returns the repository used to store and query favorite head entries.
     *
     * @return the favorites repository
     */
    IRepository<UUID, Favorite> getFavorites();
}
