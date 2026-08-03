package com.sxilverr.autoplacelight.fabric;

import com.sxilverr.autoplacelight.fabric.client.ClientEvents;
import com.sxilverr.autoplacelight.fabric.client.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class AutoPlaceLightFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricConfig.load();
        KeyBindings.register();
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onClientTick);
    }
}
