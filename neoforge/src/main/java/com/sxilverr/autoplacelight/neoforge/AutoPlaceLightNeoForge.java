package com.sxilverr.autoplacelight.neoforge;

import com.sxilverr.autoplacelight.AutoPlaceLight;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(value = AutoPlaceLight.MOD_ID, dist = Dist.CLIENT)
public final class AutoPlaceLightNeoForge {
    public AutoPlaceLightNeoForge(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, NeoForgeConfig.SPEC);
    }
}
