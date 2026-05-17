package net.countered.smoothf5.fabric;

import eu.midnightdust.lib.config.MidnightConfig;
import net.countered.smoothf5.SmoothF5Mod;
import net.countered.smoothf5.config.ConfigPlatform;
import net.countered.smoothf5.config.fabric.ConfigPlatformImpl;
import net.fabricmc.api.ModInitializer;

public final class SmoothF5ModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigPlatform.setProvider(new ConfigPlatform.Provider() {
            @Override
            public boolean isEnablePosSmoothing() {
                return ConfigPlatformImpl.isEnablePosSmoothing();
            }

            @Override
            public boolean isEnableRotSmoothing() {
                return ConfigPlatformImpl.isEnableRotSmoothing();
            }

            @Override
            public float getPosStiffness() {
                return ConfigPlatformImpl.getPosStiffness();
            }

            @Override
            public float getRotStiffness() {
                return ConfigPlatformImpl.getRotStiffness();
            }

            @Override
            public int getFPReturnDuration() {
                return ConfigPlatformImpl.getFPReturnDuration();
            }

            @Override
            public net.countered.smoothf5.config.SmoothingMode getSmoothingMode() {
                return ConfigPlatformImpl.getSmoothingMode();
            }
        });

        SmoothF5Mod.init();
        MidnightConfig.init(SmoothF5Mod.MOD_ID, ConfigPlatformImpl.class);
    }
}
