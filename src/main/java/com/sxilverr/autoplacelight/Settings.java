package com.sxilverr.autoplacelight;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
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
        return value instanceof String entry && !entry.isEmpty() && ENTRY_FORMAT.matcher(entry).matches();
    }

    public static boolean isPlaceable(ItemStack stack) {
        for (Predicate<ItemStack> rule : rules()) {
            if (rule.test(stack)) {
                return true;
            }
        }
        return false;
    }

    private static List<Predicate<ItemStack>> rules() {
        if (!placeItemIds.equals(resolvedFrom)) {
            resolvedFrom = List.copyOf(placeItemIds);
            List<Predicate<ItemStack>> built = new ArrayList<>();
            for (String entry : placeItemIds) {
                Predicate<ItemStack> rule = compile(entry);
                if (rule != null) {
                    built.add(rule);
                }
            }
            rules = List.copyOf(built);
        }
        return rules;
    }

    private static Predicate<ItemStack> compile(String entry) {
        boolean tag = entry.startsWith("#");
        String id = (tag ? entry.substring(1) : entry).toLowerCase(Locale.ROOT);
        if (id.isEmpty()) {
            return null;
        }

        if (id.indexOf('*') >= 0) {
            Pattern pattern = Pattern.compile(wildcard(id));
            return tag
                    ? stack -> stack.getTags().anyMatch(key -> pattern.matcher(key.location().toString()).matches())
                    : stack -> pattern.matcher(idOf(stack).toString()).matches();
        }

        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null) {
            return null;
        }
        if (tag) {
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, key);
            return stack -> stack.is(tagKey);
        }
        return stack -> key.equals(idOf(stack));
    }

    private static String wildcard(String id) {
        StringBuilder regex = new StringBuilder();
        int start = 0;
        for (int star = id.indexOf('*'); star >= 0; star = id.indexOf('*', start)) {
            regex.append(Pattern.quote(id.substring(start, star))).append(".*");
            start = star + 1;
        }
        return regex.append(Pattern.quote(id.substring(start))).toString();
    }

    private static ResourceLocation idOf(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }
}
