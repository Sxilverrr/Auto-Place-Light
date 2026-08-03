package com.sxilverr.autoplacelight.forge;

import com.sxilverr.autoplacelight.AutoPlaceLight;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(AutoPlaceLight.MOD_ID)
public final class AutoPlaceLightForge {
    public AutoPlaceLightForge(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            context.registerConfig(ModConfig.Type.CLIENT, ForgeConfig.SPEC);
        }
    }
}
