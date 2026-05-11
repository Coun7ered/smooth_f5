package net.countered.smoothf5.neoforge;

import eu.midnightdust.lib.config.MidnightConfig;
import net.neoforged.fml.common.Mod;

import net.countered.smoothf5.SmoothF5Mod;

@Mod(SmoothF5Mod.MOD_ID)
public final class SmoothF5ModNeoForge {
    public SmoothF5ModNeoForge() {
        // Run our common setup.
        SmoothF5Mod.init();
        MidnightConfig.init(SmoothF5Mod.MOD_ID, ConfigPlatformImpl.class);
    }
}
