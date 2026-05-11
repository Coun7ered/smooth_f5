package net.countered.smoothf5.config;
import dev.architectury.injectables.annotations.ExpectPlatform;

public class ConfigPlatform {

    @ExpectPlatform
    public static boolean enablePosSmoothing() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean enableRotSmoothing() {
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
    public static SmoothingMode getSmoothingMode() {
        throw new AssertionError();
    }
}