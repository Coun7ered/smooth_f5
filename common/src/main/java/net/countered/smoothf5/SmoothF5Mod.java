package net.countered.smoothf5;

import eu.midnightdust.lib.config.MidnightConfig;
import net.countered.smoothf5.config.SmoothF5Config;

public final class SmoothF5Mod {

    public static final String MOD_ID = "smooth_f5";

    public static void init() {
        MidnightConfig.init(MOD_ID, SmoothF5Config.class);
    }
}
