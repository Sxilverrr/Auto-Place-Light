package com.sxilverr.autoplacelight;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class Settings {
    public static final List<String> DEFAULT_ITEMS = List.of(
            "*:torch",
            "*:*_torch*",
            "*:torch_*",
            "*:candle",
            "*:*_candle*",
            "*:candle_*",
            "*:lantern",
            "*:*_lantern*",
            "*:lantern_*",
            "*:lamp",
            "*:*_lamp*",
            "*:lamp_*",
            "*:light",
            "*:*_light*",
            "*:glowstone",
            "*:*_glowstone*",
            "*:glowstone_*",
            "minecraft:shroomlight",
            "minecraft:*_froglight");

    private static final Pattern ENTRY_FORMAT = Pattern.compile("#?[a-zA-Z0-9_.*/-]+(:[a-zA-Z0-9_.*/-]+)?");

    public static boolean checkWalls = true;
    public static int horizontalRadius = 4;
    public static int verticalRadius = 2;
    public static int lightLevel = 0;
    public static boolean ignoreSunlight = true;
    public static boolean dontPlaceInFluid = true;
    public static int placeInterval = 5;
    public static List<String> placeItemIds = DEFAULT_ITEMS;

    private static List<String> resolvedFrom;
    private static List<Predicate<ItemStack>> rules = List.of();

    private Settings() {
    }

    public static boolean isPlaceEntry(Object value) {
        return value instanceof String entry && ENTRY_FORMAT.matcher(entry).matches();
    }

    public static boolean isPlaceable(ItemStack stack) {
        if (!placeItemIds.equals(resolvedFrom)) {
            resolvedFrom = List.copyOf(placeItemIds);
            rules = resolvedFrom.stream().filter(Settings::isPlaceEntry).map(Settings::compile).toList();
        }
        return rules.stream().anyMatch(rule -> rule.test(stack));
    }

    private static Predicate<ItemStack> compile(String entry) {
        boolean tag = entry.startsWith("#");
        String id = (tag ? entry.substring(1) : entry).toLowerCase(Locale.ROOT);
        if (id.indexOf(':') < 0 && id.indexOf('*') < 0) {
            id = "minecraft:" + id;
        }
        Pattern pattern = Pattern.compile(id.replace(".", "\\.").replace("*", ".*"));
        return tag
                ? stack -> stack.getTags().anyMatch(key -> pattern.matcher(key.location().toString()).matches())
                : stack -> pattern.matcher(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()).matches();
    }
}
