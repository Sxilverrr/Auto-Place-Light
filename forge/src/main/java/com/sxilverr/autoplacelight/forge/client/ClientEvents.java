package com.sxilverr.autoplacelight.forge.client;

import com.sxilverr.autoplacelight.AutoPlaceLight;
import com.sxilverr.autoplacelight.LightPlacer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AutoPlaceLight.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        while (KeyBindings.TOGGLE.consumeClick()) {
            LightPlacer.toggle();
        }

        LocalPlayer player = Minecraft.getInstance().player;
        LightPlacer.tick(player == null ? 0.0D : player.getBlockReach());
    }
}
