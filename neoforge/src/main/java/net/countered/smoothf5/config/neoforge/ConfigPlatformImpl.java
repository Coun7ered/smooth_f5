package net.countered.smoothf5.config.neoforge;

public class ConfigPlatformImpl {

    public static boolean enableSmoothF5() {
        return NeoForgeModConfig.enableSmoothF5;
    }
    public static boolean enablePosSmoothing() {return NeoForgeModConfig.enablePosSmoothing;}
    public static boolean enableRotSmoothing() {return NeoForgeModConfig.enableRotSmoothing;}

    public static float getPosStiffness() {
        return NeoForgeModConfig.posStiffness;
    }

    public static float getRotStiffness() {
        return NeoForgeModConfig.rotStiffness;
    }
}