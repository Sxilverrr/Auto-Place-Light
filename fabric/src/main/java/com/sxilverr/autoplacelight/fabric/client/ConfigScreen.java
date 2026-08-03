package com.sxilverr.autoplacelight.fabric.client;

import com.sxilverr.autoplacelight.AutoPlaceLight;
import com.sxilverr.autoplacelight.Settings;
import com.sxilverr.autoplacelight.fabric.FabricConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class ConfigScreen {
    private ConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(text("title"))
                .setSavingRunnable(FabricConfig::save);

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(text("category"));

        category.addEntry(entries.startBooleanToggle(text("checkWalls"), Settings.checkWalls)
                .setDefaultValue(true)
                .setTooltip(text("checkWalls.tooltip"))
                .setSaveConsumer(value -> Settings.checkWalls = value)
                .build());

        category.addEntry(entries.startIntSlider(text("horizontalRadius"), Settings.horizontalRadius, 1, 4)
                .setDefaultValue(4)
                .setTooltip(text("horizontalRadius.tooltip"))
                .setSaveConsumer(value -> Settings.horizontalRadius = value)
                .build());

        category.addEntry(entries.startIntSlider(text("verticalRadius"), Settings.verticalRadius, 0, 4)
                .setDefaultValue(2)
                .setTooltip(text("verticalRadius.tooltip"))
                .setSaveConsumer(value -> Settings.verticalRadius = value)
                .build());

        category.addEntry(entries.startIntSlider(text("lightLevel"), Settings.lightLevel, 0, 15)
                .setDefaultValue(0)
                .setTooltip(text("lightLevel.tooltip"))
                .setSaveConsumer(value -> Settings.lightLevel = value)
                .build());

        category.addEntry(entries.startBooleanToggle(text("ignoreSunlight"), Settings.ignoreSunlight)
                .setDefaultValue(true)
                .setTooltip(text("ignoreSunlight.tooltip"))
                .setSaveConsumer(value -> Settings.ignoreSunlight = value)
                .build());

        category.addEntry(entries.startBooleanToggle(text("dontPlaceInFluid"), Settings.dontPlaceInFluid)
                .setDefaultValue(true)
                .setTooltip(text("dontPlaceInFluid.tooltip"))
                .setSaveConsumer(value -> Settings.dontPlaceInFluid = value)
                .build());

        category.addEntry(entries.startIntSlider(text("placeIntervalTicks"), Settings.placeInterval, 1, 100)
                .setDefaultValue(5)
                .setTooltip(text("placeIntervalTicks.tooltip"))
                .setSaveConsumer(value -> Settings.placeInterval = value)
                .build());

        category.addEntry(entries.startStrList(text("placeItem"), new ArrayList<>(Settings.placeItemIds))
                .setDefaultValue(new ArrayList<>(Settings.DEFAULT_ITEMS))
                .setTooltip(text("placeItem.tooltip"))
                .setSaveConsumer(value -> Settings.placeItemIds = valid(value))
                .build());

        return builder.build();
    }

    private static Component text(String key) {
        return Component.translatable("config." + AutoPlaceLight.MOD_ID + "." + key);
    }

    private static List<String> valid(List<String> entries) {
        List<String> kept = new ArrayList<>();
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (Settings.isPlaceEntry(trimmed)) {
                kept.add(trimmed);
            }
        }
        return List.copyOf(kept);
    }
}
