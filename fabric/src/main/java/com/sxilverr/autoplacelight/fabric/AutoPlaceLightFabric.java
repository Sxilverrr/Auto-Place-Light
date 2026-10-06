package com.sxilverr.autoplacelight.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import com.sxilverr.autoplacelight.AutoPlaceLight;
import com.sxilverr.autoplacelight.LightPlacer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
//? if >=1.21.11 {
/*import net.minecraft.resources.Identifier;
*///?}
import org.lwjgl.glfw.GLFW;

public final class AutoPlaceLightFabric implements ClientModInitializer {
    //? if >=1.21.11 {
    /*private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(AutoPlaceLight.MOD_ID, AutoPlaceLight.MOD_ID));
    *///?} else {
    private static final String CATEGORY = "key.categories." + AutoPlaceLight.MOD_ID;
    //?}
    private static final KeyMapping TOGGLE = new KeyMapping("key." + AutoPlaceLight.MOD_ID + ".toggle",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, CATEGORY);

    @Override
    public void onInitializeClient() {
        ConfigHolder<FabricConfig> config = AutoConfig.register(FabricConfig.class, Toml4jConfigSerializer::new);
        KeyBindingHelper.registerKeyBinding(TOGGLE);
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            config.getConfig().apply();
            while (TOGGLE.consumeClick()) {
                LightPlacer.toggle();
            }
            //? if >=1.20.5 {
            /*LightPlacer.tick(minecraft.player == null ? 0.0D : minecraft.player.blockInteractionRange());
            *///?} else {
            LightPlacer.tick(minecraft.gameMode == null ? 0.0D : minecraft.gameMode.getPickRange());
            //?}
        });
    }
}
