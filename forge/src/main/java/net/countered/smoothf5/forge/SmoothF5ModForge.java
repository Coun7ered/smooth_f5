package net.countered.smoothf5.forge;

import dev.architectury.platform.forge.EventBuses;
import eu.midnightdust.lib.config.MidnightConfig;
import net.countered.smoothf5.SmoothF5Mod;
import net.countered.smoothf5.config.forge.ConfigPlatformImpl;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SmoothF5Mod.MOD_ID)
public final class SmoothF5ModForge {
    public SmoothF5ModForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(SmoothF5Mod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        SmoothF5Mod.init();
        MidnightConfig.init(SmoothF5Mod.MOD_ID, ConfigPlatformImpl.class);
    }
}
