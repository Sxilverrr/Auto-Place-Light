package com.sxilverr.autoplacelight.fabric.client;

import com.sxilverr.autoplacelight.LightPlacer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class ClientEvents {
    private ClientEvents() {
    }

    public static void onClientTick(Minecraft minecraft) {
        while (KeyBindings.TOGGLE.consumeClick()) {
            LightPlacer.toggle();
        }

        LocalPlayer player = minecraft.player;
        LightPlacer.tick(player == null ? 0.0D : blockReach(minecraft, player));
    }

    private static double blockReach(Minecraft minecraft, LocalPlayer player) {
        //? if >=1.20.5 {
        /*return player.blockInteractionRange();
        *///?} else {
        return minecraft.gameMode == null ? 0.0D : minecraft.gameMode.getPickRange();
        //?}
    }
}
