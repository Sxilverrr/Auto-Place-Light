package com.sxilverr.autoplacelight.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sxilverr.autoplacelight.AutoPlaceLight;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
    public static final KeyMapping TOGGLE = new KeyMapping(
            "key." + AutoPlaceLight.MOD_ID + ".toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            "key.categories." + AutoPlaceLight.MOD_ID);

    private KeyBindings() {
    }

    public static void register() {
        KeyBindingHelper.registerKeyBinding(TOGGLE);
    }
}
