package net.countered.smoothf5.fabric;

import eu.midnightdust.lib.config.MidnightConfig;
import net.countered.smoothf5.SmoothF5Mod;
import net.countered.smoothf5.config.fabric.ConfigPlatformImpl;
import net.fabricmc.api.ModInitializer;

public final class SmoothF5ModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        SmoothF5Mod.init();
        MidnightConfig.init(SmoothF5Mod.MOD_ID, ConfigPlatformImpl.class);
    }
}
