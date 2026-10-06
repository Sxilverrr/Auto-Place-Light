package com.sxilverr.autoplacelight.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import com.sxilverr.autoplacelight.AutoPlaceLight;
import com.sxilverr.autoplacelight.LightPlacer;
import com.sxilverr.autoplacelight.Settings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
//? if >=1.21.11 {
/*import net.minecraft.resources.Identifier;
*///?}
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod(value = AutoPlaceLight.MOD_ID, dist = Dist.CLIENT)
public final class AutoPlaceLightNeoForge {
    //? if >=1.21.11 {
    /*private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(AutoPlaceLight.MOD_ID, AutoPlaceLight.MOD_ID));
    *///?} else {
    private static final String CATEGORY = "key.categories." + AutoPlaceLight.MOD_ID;
    //?}
    private static final KeyMapping TOGGLE = new KeyMapping("key." + AutoPlaceLight.MOD_ID + ".toggle",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, CATEGORY);

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.BooleanValue CHECK_WALLS = BUILDER
            .comment("Require a line of sight to a location before placing.")
            .define("checkWalls", true);
    private static final ModConfigSpec.IntValue HORIZONTAL_RADIUS = BUILDER
            .comment("How far to search horizontally in blocks.")
            .defineInRange("horizontalRadius", 4, 1, 4);
    private static final ModConfigSpec.IntValue VERTICAL_RADIUS = BUILDER
            .comment("How far to search vertically in blocks.")
            .defineInRange("verticalRadius", 2, 0, 4);
    private static final ModConfigSpec.IntValue LIGHT_LEVEL = BUILDER
            .comment("Lights will be placed whenever the light level is at or below this value.")
            .defineInRange("lightLevel", 0, 0, 15);
    private static final ModConfigSpec.BooleanValue IGNORE_SUNLIGHT = BUILDER
            .comment("Do not count sunlight as lit")
            .define("ignoreSunlight", true);
    private static final ModConfigSpec.BooleanValue DONT_PLACE_IN_FLUID = BUILDER
            .comment("Don't Place In Fluid")
            .define("dontPlaceInFluid", true);
    private static final ModConfigSpec.IntValue PLACE_INTERVAL = BUILDER
            .comment("Client tick delay between placements.")
            .defineInRange("placeIntervalTicks", 5, 1, 100);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> PLACE_ITEMS = BUILDER
            .comment("Item id's of the items to place. Accepts tags(#) and wild cards(*).")
            .defineList("placeItem", Settings.DEFAULT_ITEMS, () -> "minecraft:torch", Settings::isPlaceEntry);
    private static final ModConfigSpec SPEC = BUILDER.build();

    public AutoPlaceLightNeoForge(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        modBus.addListener(AutoPlaceLightNeoForge::onConfigLoad);
        modBus.addListener(AutoPlaceLightNeoForge::onConfigReload);
        modBus.addListener(AutoPlaceLightNeoForge::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(AutoPlaceLightNeoForge::onClientTick);
    }

    private static void onConfigLoad(ModConfigEvent.Loading event) {
        applyConfig();
    }

    private static void onConfigReload(ModConfigEvent.Reloading event) {
        applyConfig();
    }

    private static void applyConfig() {
        Settings.checkWalls = CHECK_WALLS.get();
        Settings.horizontalRadius = HORIZONTAL_RADIUS.get();
        Settings.verticalRadius = VERTICAL_RADIUS.get();
        Settings.lightLevel = LIGHT_LEVEL.get();
        Settings.ignoreSunlight = IGNORE_SUNLIGHT.get();
        Settings.dontPlaceInFluid = DONT_PLACE_IN_FLUID.get();
        Settings.placeInterval = PLACE_INTERVAL.get();
        Settings.placeItemIds = List.copyOf(PLACE_ITEMS.get());
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        //? if >=1.21.11 {
        /*event.registerCategory(CATEGORY);
        *///?}
        event.register(TOGGLE);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        while (TOGGLE.consumeClick()) {
            LightPlacer.toggle();
        }
        LocalPlayer player = Minecraft.getInstance().player;
        LightPlacer.tick(player == null ? 0.0D : player.blockInteractionRange());
    }
}
