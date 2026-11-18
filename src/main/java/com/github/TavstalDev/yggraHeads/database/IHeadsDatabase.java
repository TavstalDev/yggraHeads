package com.github.TavstalDev.yggraHeads.database;

import com.github.TavstalDev.yggraHeads.database.models.Favorite;
import com.github.TavstalDev.yggraLib.api.database.IDatabase;
import com.github.TavstalDev.yggraLib.api.database.repositories.IRepository;

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
