package com.sxilverr.autoplacelight.fabric;

import com.sxilverr.autoplacelight.Settings;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

@Config(name = "autoplacelight-client")
public final class FabricConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean checkWalls = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 4)
    public int horizontalRadius = 4;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    public int verticalRadius = 2;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 15)
    public int lightLevel = 0;

    @ConfigEntry.Gui.Tooltip
    public boolean ignoreSunlight = true;

    @ConfigEntry.Gui.Tooltip
    public boolean dontPlaceInFluid = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int placeIntervalTicks = 5;

    @ConfigEntry.Gui.Tooltip
    public List<String> placeItem = new ArrayList<>(Settings.DEFAULT_ITEMS);

    @Override
    public void validatePostLoad() {
        horizontalRadius = Mth.clamp(horizontalRadius, 1, 4);
        verticalRadius = Mth.clamp(verticalRadius, 0, 4);
        lightLevel = Mth.clamp(lightLevel, 0, 15);
        placeIntervalTicks = Mth.clamp(placeIntervalTicks, 1, 100);
    }

    void apply() {
        Settings.checkWalls = checkWalls;
        Settings.horizontalRadius = horizontalRadius;
        Settings.verticalRadius = verticalRadius;
        Settings.lightLevel = lightLevel;
        Settings.ignoreSunlight = ignoreSunlight;
        Settings.dontPlaceInFluid = dontPlaceInFluid;
        Settings.placeInterval = placeIntervalTicks;
        Settings.placeItemIds = placeItem;
    }
}
