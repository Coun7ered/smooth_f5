package net.countered.smoothf5.neoforge;

import eu.midnightdust.lib.config.MidnightConfig;
import net.countered.smoothf5.config.ConfigPlatform;
import net.countered.smoothf5.config.neoforge.ConfigPlatformImpl;
import net.neoforged.fml.common.Mod;

import net.countered.smoothf5.SmoothF5Mod;

@Mod(SmoothF5Mod.MOD_ID)
public final class SmoothF5ModNeoForge {
    public SmoothF5ModNeoForge() {
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
