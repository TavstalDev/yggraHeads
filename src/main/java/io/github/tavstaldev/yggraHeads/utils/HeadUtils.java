package io.github.tavstaldev.yggraHeads.utils;

import io.github.tavstaldev.yggraHeads.YggraHeads;
import io.github.tavstaldev.yggraHeads.models.HeadCategory;
import io.github.tavstaldev.yggraHeads.models.HeadData;
import io.github.tavstaldev.yggraLib.api.logger.YggraLogger;
import io.github.tavstaldev.yggraLib.shadow.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Regex Save
// Tags:\s*"([^"]*)"\s*"([^"]*)"
// Texture:\s*"([^"]*)"\s*"([^"]*)"\s*"([^"]*)"

/**
 * Utility class for handling head categories and YAML configuration.
 */
public class HeadUtils {
    private static List<HeadCategory> _headCategories;
    /**
     * Gets the list of head categories.
     *
     * @return the list of head categories
     */
    public static List<HeadCategory> getHeadCategories() {
        return _headCategories;
    }

    /**
     * Loads the head categories from the YAML configuration file.
     */
    public static void load() {
        YggraLogger logger = YggraHeads.instance.logger().withModule(HeadUtils.class);
        boolean isFirstLaunch = false;
        _headCategories = new ArrayList<>();

        Path dirPath = Paths.get(YggraHeads.instance.getDataFolder().getPath());
        Path filePath = Paths.get(dirPath.toString(), "categories.yml");
        File headsDir = Paths.get(dirPath.toString(), "heads").toFile();
        if (!Files.exists(filePath)) {
            try {
                InputStream inputStream = YggraHeads.instance.getResource("categories.yml");
                if (inputStream == null) {
                    logger.debug("Failed to get categories file from resources.");
                    return;
                }
                Files.copy(inputStream, filePath);
                isFirstLaunch = true;
            } catch (IOException ex) {
                logger.warn("Failed to create categories file.");
                logger.error(ex.getMessage());
                return;
            }
        }
        else
            isFirstLaunch = !headsDir.exists() || Objects.requireNonNull(headsDir.listFiles()).length == 0;

        try {
            logger.debug("Loading yaml file...");
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(filePath.toFile());
            List<Object> tempList = (List<Object>)yaml.getList("data");

            _headCategories = new ArrayList<>();
            for (Object obj : tempList) {
                if (obj instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) obj;
                    HeadCategory category = new HeadCategory(
                            (String) data.get("Name"),
                            (String) data.get("DisplayNameKey"),
                            (String) data.get("DescriptionKey"),
                            (Boolean) data.get("RequirePermission"),
                            (String) data.get("Permission"),
                            data.get("Price") instanceof Number ? ((Number) data.get("Price")).doubleValue() : 0.0,
                            (String) data.get("File"),
                            (String) data.get("Texture")
                    );
                    _headCategories.add(category);
                } else {
                    logger.warn("Element in yamlObject list is not a Map<String, Object>.");
                }
            }

            for (HeadCategory category : _headCategories) {
                if (isFirstLaunch) {
                    if (!category.copyFromResource()) {
                        logger.warn(String.format("Failed to copy head data file for category '%s'.", category.Name));
                        continue;
                    }
                }

                if (!category.load())
                    logger.warn(String.format("Failed to load head data file for category '%s'.", category.Name));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retrieves a head category by its name.
     *
     * @param categoryName the name of the category to search for
     * @return the HeadCategory object if found, null otherwise
     */
    public static HeadCategory getCategory(String categoryName) {
        for (HeadCategory cat : _headCategories) {
            if (cat.Name.equals(categoryName)) {
                return cat;
            }
        }
        return null;
    }

    /**
     * Retrieves the head data for a specific head within a specified category.
     *
     * @param categoryName the name of the category to search in
     * @param headName the name of the head to retrieve
     * @return the HeadData object if found, null otherwise
     */
    public static HeadData getHead(String categoryName, String headName) {
        for (HeadCategory cat : _headCategories) {
            if (!cat.Name.equals(categoryName)) {
                continue;
            }

            for (HeadData head : cat.getHeads()) {
                if (head.Name.equals(headName)) {
                    return head;
                }
            }

            break;
        }
        return null;
    }

    /**
     * Calculates the total number of heads across all categories.
     *
     * @return the total count of heads
     */
    public static int getHeadCount() {
        int count = 0;
        for (HeadCategory cat : _headCategories) {
            count += cat.getHeads().size();
        }
        return count;
    }
}
