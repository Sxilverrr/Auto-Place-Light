package com.sxilverr.autoplacelight.forge;

import com.mojang.blaze3d.platform.InputConstants;
import com.sxilverr.autoplacelight.AutoPlaceLight;
import com.sxilverr.autoplacelight.LightPlacer;
import com.sxilverr.autoplacelight.Settings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod(AutoPlaceLight.MOD_ID)
public final class AutoPlaceLightForge {
    private static final KeyMapping TOGGLE = new KeyMapping("key." + AutoPlaceLight.MOD_ID + ".toggle",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT,
            "key.categories." + AutoPlaceLight.MOD_ID);

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue CHECK_WALLS = BUILDER
            .comment("Require a line of sight to a location before placing.")
            .define("checkWalls", true);
    private static final ForgeConfigSpec.IntValue HORIZONTAL_RADIUS = BUILDER
            .comment("How far to search horizontally in blocks.")
            .defineInRange("horizontalRadius", 4, 1, 4);
    private static final ForgeConfigSpec.IntValue VERTICAL_RADIUS = BUILDER
            .comment("How far to search vertically in blocks.")
            .defineInRange("verticalRadius", 2, 0, 4);
    private static final ForgeConfigSpec.IntValue LIGHT_LEVEL = BUILDER
            .comment("Lights will be placed whenever the light level is at or below this value.")
            .defineInRange("lightLevel", 0, 0, 15);
    private static final ForgeConfigSpec.BooleanValue IGNORE_SUNLIGHT = BUILDER
            .comment("Do not count sunlight as lit")
            .define("ignoreSunlight", true);
    private static final ForgeConfigSpec.BooleanValue DONT_PLACE_IN_FLUID = BUILDER
            .comment("Don't Place In Fluid")
            .define("dontPlaceInFluid", true);
    private static final ForgeConfigSpec.IntValue PLACE_INTERVAL = BUILDER
            .comment("Client tick delay between placements.")
            .defineInRange("placeIntervalTicks", 5, 1, 100);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PLACE_ITEMS = BUILDER
            .comment("Item id's of the items to place. Accepts tags(#) and wild cards(*).")
            .defineList("placeItem", Settings.DEFAULT_ITEMS, Settings::isPlaceEntry);
    private static final ForgeConfigSpec SPEC = BUILDER.build();

    public AutoPlaceLightForge(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, SPEC);
        IEventBus modBus = context.getModEventBus();
        modBus.addListener(AutoPlaceLightForge::onConfigLoad);
        modBus.addListener(AutoPlaceLightForge::onConfigReload);
        modBus.addListener(AutoPlaceLightForge::onRegisterKeyMappings);
        MinecraftForge.EVENT_BUS.addListener(AutoPlaceLightForge::onClientTick);
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
        event.register(TOGGLE);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        while (TOGGLE.consumeClick()) {
            LightPlacer.toggle();
        }
        LocalPlayer player = Minecraft.getInstance().player;
        LightPlacer.tick(player == null ? 0.0D : player.getBlockReach());
    }
}
