package io.github.tavstaldev.yggraHeads.models;

import lombok.Getter;

/**
 * Lightweight in-memory representation of a favorited head reference.
 */
@Getter
public class FavoriteEntry {

    /** Category name/key of the favorited head. */
    private String categoryName;

    /** Name of the favorited head. */
    private String headName;

    /**
     * Creates a favorite entry reference.
     *
     * @param categoryName category name/key of the head
     * @param headName     name of the head
     */
    public FavoriteEntry(String categoryName, String headName) {
        this.categoryName = categoryName;
        this.headName = headName;
    }
}
