package com.sxilverr.autoplacelight.fabric;

import com.mojang.logging.LogUtils;
import com.sxilverr.autoplacelight.AutoPlaceLight;
import com.sxilverr.autoplacelight.Settings;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FabricConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private FabricConfig() {
    }

    public static void load() {
        Map<String, String> values = read();
        Settings.checkWalls = bool(values.get("checkWalls"), true);
        Settings.horizontalRadius = integer(values.get("horizontalRadius"), 4, 1, 4);
        Settings.verticalRadius = integer(values.get("verticalRadius"), 2, 0, 4);
        Settings.lightLevel = integer(values.get("lightLevel"), 0, 0, 15);
        Settings.ignoreSunlight = bool(values.get("ignoreSunlight"), true);
        Settings.dontPlaceInFluid = bool(values.get("dontPlaceInFluid"), true);
        Settings.placeInterval = integer(values.get("placeIntervalTicks"), 5, 1, 100);
        Settings.placeItemIds = strings(values.get("placeItem"), Settings.DEFAULT_ITEMS);
        save();
    }

    public static void save() {
        StringBuilder toml = new StringBuilder();
        writeValue(toml, "Require a line of sight to a location before placing.",
                "checkWalls", Settings.checkWalls);
        writeRanged(toml, "How far to search horizontally in blocks.",
                "horizontalRadius", Settings.horizontalRadius, 1, 4);
        writeRanged(toml, "How far to search vertically in blocks.",
                "verticalRadius", Settings.verticalRadius, 0, 4);
        writeRanged(toml, "Lights will be placed whenever the light level is at or below this value.",
                "lightLevel", Settings.lightLevel, 0, 15);
        writeValue(toml, "Do not count sunlight as lit",
                "ignoreSunlight", Settings.ignoreSunlight);
        writeValue(toml, "Don't Place In Fluid",
                "dontPlaceInFluid", Settings.dontPlaceInFluid);
        writeRanged(toml, "Client tick delay between placements.",
                "placeIntervalTicks", Settings.placeInterval, 1, 100);
        writeList(toml, "Item id's of the items to place. Accepts tags(#) and wild cards(*).",
                "placeItem", Settings.placeItemIds);

        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, toml.toString(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            LOGGER.warn("Failed to write {}", path, exception);
        }
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(AutoPlaceLight.MOD_ID + "-client.toml");
    }

    private static void writeValue(StringBuilder out, String comment, String key, Object value) {
        out.append('#').append(comment).append('\n');
        out.append(key).append(" = ").append(value).append('\n');
    }

    private static void writeRanged(StringBuilder out, String comment, String key, int value, int min, int max) {
        out.append('#').append(comment).append('\n');
        out.append("#Range: ").append(min).append(" ~ ").append(max).append('\n');
        out.append(key).append(" = ").append(value).append('\n');
    }

    private static void writeList(StringBuilder out, String comment, String key, List<String> values) {
        out.append('#').append(comment).append('\n');
        out.append(key).append(" = [");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append('"').append(values.get(i).replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        out.append("]\n");
    }

    private static Map<String, String> read() {
        Path path = path();
        if (!Files.isRegularFile(path)) {
            return Map.of();
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            LOGGER.warn("Failed to read {}, falling back to defaults", path, exception);
            return Map.of();
        }

        Map<String, String> values = new HashMap<>();
        String openKey = null;
        StringBuilder openValue = null;

        for (String raw : lines) {
            String line = raw.trim();

            if (openValue != null) {
                openValue.append(' ').append(line);
                if (line.indexOf(']') >= 0) {
                    values.put(openKey, openValue.toString());
                    openKey = null;
                    openValue = null;
                }
                continue;
            }

            if (line.isEmpty() || line.startsWith("#") || line.startsWith("[")) {
                continue;
            }

            int split = line.indexOf('=');
            if (split <= 0) {
                continue;
            }

            String key = line.substring(0, split).trim();
            String value = line.substring(split + 1).trim();
            if (value.startsWith("[") && value.indexOf(']') < 0) {
                openKey = key;
                openValue = new StringBuilder(value);
                continue;
            }
            values.put(key, value);
        }

        return values;
    }

    private static boolean bool(String value, boolean fallback) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return fallback;
    }

    private static int integer(String value, int fallback, int min, int max) {
        if (value == null) {
            return fallback;
        }
        try {
            return Math.min(max, Math.max(min, Integer.parseInt(value)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static List<String> strings(String value, List<String> fallback) {
        if (value == null) {
            return fallback;
        }
        if (!value.startsWith("[") || !value.endsWith("]")) {
            LOGGER.warn("Ignoring malformed placeItem list, falling back to defaults");
            return fallback;
        }

        List<String> parsed = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        char quote = 0;

        for (int i = 1; i < value.length() - 1; i++) {
            char c = value.charAt(i);
            if (quote != 0) {
                if (c == quote) {
                    quote = 0;
                    accept(parsed, token);
                } else {
                    token.append(c);
                }
            } else if (c == '"' || c == '\'') {
                quote = c;
            } else if (c == ',') {
                accept(parsed, token);
            } else if (!Character.isWhitespace(c)) {
                token.append(c);
            }
        }
        accept(parsed, token);

        return List.copyOf(parsed);
    }

    private static void accept(List<String> out, StringBuilder token) {
        String entry = token.toString();
        token.setLength(0);
        if (entry.isEmpty()) {
            return;
        }
        if (Settings.isPlaceEntry(entry)) {
            out.add(entry);
        } else {
            LOGGER.warn("Ignoring invalid placeItem entry '{}'", entry);
        }
    }
}
