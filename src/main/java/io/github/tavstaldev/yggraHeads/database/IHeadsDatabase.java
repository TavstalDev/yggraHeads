package io.github.tavstaldev.yggraHeads.database;

import io.github.tavstaldev.yggraHeads.database.models.Favorite;
import io.github.tavstaldev.yggraLib.api.database.IDatabase;
import io.github.tavstaldev.yggraLib.api.database.repositories.IRepository;

import java.util.UUID;

public interface IHeadsDatabase extends IDatabase {
    IRepository<UUID, Favorite> getFavorites();
}
