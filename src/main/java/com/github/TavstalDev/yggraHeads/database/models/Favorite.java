package com.github.TavstalDev.yggraHeads.database.models;

import com.github.TavstalDev.yggraLib.api.database.annotations.Column;
import com.github.TavstalDev.yggraLib.api.database.annotations.Table;
import com.github.TavstalDev.yggraLib.api.database.enums.EColumnKind;
import lombok.Getter;

import java.util.UUID;

/**
 * Database entity representing a player's favorited head.
 */
@Table("favorites")
@Getter
public class Favorite {
    /** Unique favorite record identifier. */
    @Column(value = "id", kind = EColumnKind.PRIMARY)
    private UUID id;

    /** UUID of the player who favorited the head. */
    @Column(value = "playerId", nullable = false)
    private UUID playerId;

    /** Category name/key of the favorited head. */
    @Column(value = "categoryName", nullable = false)
    private String categoryName;

    /** Name of the favorited head. */
    @Column(value = "headName", nullable = false)
    private String headName;

    /**
     * Creates an empty favorite entity (required by ORM/mapping frameworks).
     */
    public Favorite() {
    }

    /**
     * Creates a favorite entity with the provided values.
     *
     * @param id           unique favorite record identifier
     * @param playerId     UUID of the owning player
     * @param category     category name/key of the head
     * @param headName     name of the head
     */
    public Favorite(UUID id, UUID playerId, String category, String headName) {
        this.id = id;
        this.playerId = playerId;
        categoryName = category;
        this.headName = headName;
    }
}
