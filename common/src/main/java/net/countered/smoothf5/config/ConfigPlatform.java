package net.countered.smoothf5.config;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class ConfigPlatform {

    @ExpectPlatform
    public static SmoothingMode getSmoothingMode() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isEnablePosSmoothing() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isEnableRotSmoothing() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static float getPosStiffness() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static float getRotStiffness() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getFPReturnDuration() {
        throw new AssertionError();
    }
}
