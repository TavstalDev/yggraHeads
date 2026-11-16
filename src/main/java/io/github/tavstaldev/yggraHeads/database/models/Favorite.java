package io.github.tavstaldev.yggraHeads.database.models;

import io.github.tavstaldev.yggraLib.api.database.annotations.Column;
import io.github.tavstaldev.yggraLib.api.database.annotations.Table;
import io.github.tavstaldev.yggraLib.api.database.enums.EColumnKind;
import lombok.Getter;

import java.util.UUID;

@Table("favorites")
@Getter
public class Favorite {
    @Column(value = "id", kind = EColumnKind.PRIMARY)
    private UUID id;

    @Column(value = "playerId", nullable = false)
    private UUID playerId;

    @Column(value = "categoryName", nullable = false)
    private String categoryName;

    @Column(value = "headName", nullable = false)
    private String headName;

    public Favorite() {
    }

    public Favorite(UUID id, UUID playerId, String category, String headName) {
        this.id = id;
        this.playerId = playerId;
        categoryName = category;
        this.headName = headName;
    }
}
