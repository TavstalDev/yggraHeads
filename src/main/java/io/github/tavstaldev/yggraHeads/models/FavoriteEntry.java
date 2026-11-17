package io.github.tavstaldev.yggraHeads.models;

import lombok.Getter;

@Getter
public class FavoriteEntry {

    private String categoryName;

    private String headName;

    public FavoriteEntry(String categoryName, String headName) {
        this.categoryName = categoryName;
        this.headName = headName;
    }
}
