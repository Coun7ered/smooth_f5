package net.countered.smoothf5.config;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.countered.smoothf5.logic.SmoothingMode;

public class ConfigPlatform {

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

    @ExpectPlatform
    public static SmoothingMode getSmoothingMode() {
        throw new AssertionError();
    }
}