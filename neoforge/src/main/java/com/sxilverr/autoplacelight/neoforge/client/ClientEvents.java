package com.sxilverr.autoplacelight.neoforge.client;

import com.sxilverr.autoplacelight.AutoPlaceLight;
import com.sxilverr.autoplacelight.LightPlacer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = AutoPlaceLight.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (KeyBindings.TOGGLE.consumeClick()) {
            LightPlacer.toggle();
        }

        LocalPlayer player = Minecraft.getInstance().player;
        LightPlacer.tick(player == null ? 0.0D : player.blockInteractionRange());
    }
}
