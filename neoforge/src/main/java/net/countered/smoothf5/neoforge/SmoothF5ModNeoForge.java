package net.countered.smoothf5.neoforge;

import eu.midnightdust.lib.config.MidnightConfig;
import net.countered.smoothf5.SmoothF5Mod;
import net.countered.smoothf5.config.neoforge.ConfigPlatformImpl;
import net.neoforged.fml.common.Mod;

@Mod(SmoothF5Mod.MOD_ID)
public final class SmoothF5ModNeoForge {

    public SmoothF5ModNeoForge() {
        SmoothF5Mod.init();
        MidnightConfig.init(SmoothF5Mod.MOD_ID, ConfigPlatformImpl.class);
    }
}
