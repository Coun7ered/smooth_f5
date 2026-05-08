package net.countered.smoothf5.fabric;

import eu.midnightdust.lib.config.MidnightConfig;
import net.countered.smoothf5.SmoothF5Mod;
import net.countered.smoothf5.config.fabric.FabricModConfig;
import net.fabricmc.api.ModInitializer;

public final class SmoothF5ModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        SmoothF5Mod.init();
        MidnightConfig.init(SmoothF5Mod.MOD_ID, FabricModConfig.class);
    }
}
