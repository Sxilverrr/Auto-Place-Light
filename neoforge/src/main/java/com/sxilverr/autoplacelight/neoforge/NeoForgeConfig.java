package com.sxilverr.autoplacelight.neoforge;

import com.sxilverr.autoplacelight.AutoPlaceLight;
import com.sxilverr.autoplacelight.Settings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AutoPlaceLight.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NeoForgeConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue CHECK_WALLS;
    private static final ModConfigSpec.IntValue HORIZONTAL_RADIUS;
    private static final ModConfigSpec.IntValue VERTICAL_RADIUS;
    private static final ModConfigSpec.IntValue LIGHT_LEVEL;
    private static final ModConfigSpec.BooleanValue IGNORE_SUNLIGHT;
    private static final ModConfigSpec.BooleanValue DONT_PLACE_IN_FLUID;
    private static final ModConfigSpec.IntValue PLACE_INTERVAL;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> PLACE_ITEMS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        CHECK_WALLS = builder
                .comment("Require a line of sight to a location before placing.")
                .define("checkWalls", true);

        HORIZONTAL_RADIUS = builder
                .comment("How far to search horizontally in blocks.")
                .defineInRange("horizontalRadius", 4, 1, 4);

        VERTICAL_RADIUS = builder
                .comment("How far to search vertically in blocks.")
                .defineInRange("verticalRadius", 2, 0, 4);

        LIGHT_LEVEL = builder
                .comment("Lights will be placed whenever the light level is at or below this value.")
                .defineInRange("lightLevel", 0, 0, 15);

        IGNORE_SUNLIGHT = builder
                .comment("Do not count sunlight as lit")
                .define("ignoreSunlight", true);

        DONT_PLACE_IN_FLUID = builder
                .comment("Don't Place In Fluid")
                .define("dontPlaceInFluid", true);

        PLACE_INTERVAL = builder
                .comment("Client tick delay between placements.")
                .defineInRange("placeIntervalTicks", 5, 1, 100);

        PLACE_ITEMS = builder
                .comment("Item id's of the items to place. Accepts tags(#) and wild cards(*).")
                .defineList("placeItem", Settings.DEFAULT_ITEMS, () -> "minecraft:torch", Settings::isPlaceEntry);

        SPEC = builder.build();
    }

    private NeoForgeConfig() {
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        apply();
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        apply();
    }

    private static void apply() {
        Settings.checkWalls = CHECK_WALLS.get();
        Settings.horizontalRadius = HORIZONTAL_RADIUS.get();
        Settings.verticalRadius = VERTICAL_RADIUS.get();
        Settings.lightLevel = LIGHT_LEVEL.get();
        Settings.ignoreSunlight = IGNORE_SUNLIGHT.get();
        Settings.dontPlaceInFluid = DONT_PLACE_IN_FLUID.get();
        Settings.placeInterval = PLACE_INTERVAL.get();
        Settings.placeItemIds = new ArrayList<>(PLACE_ITEMS.get());
    }
}
