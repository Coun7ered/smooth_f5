package net.countered.smoothf5.config;

public class ModConfig {

    public static boolean enableSmoothF5() {
        return ConfigPlatform.enableSmoothF5();
    }

    public static boolean enablePosSmoothing() {
        return ConfigPlatform.enablePosSmoothing();
    }
    public static boolean enableRotSmoothing() {
        return ConfigPlatform.enableRotSmoothing();
    }

    public static float posStiffness() {
        return ConfigPlatform.getPosStiffness();
    }

    public static float rotStiffness() {
        return ConfigPlatform.getRotStiffness();
    }
}