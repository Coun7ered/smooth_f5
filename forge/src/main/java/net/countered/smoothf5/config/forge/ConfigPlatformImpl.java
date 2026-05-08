package net.countered.smoothf5.config.forge;

public class ConfigPlatformImpl {

    public static boolean enableSmoothF5() {
        return ForgeModConfig.enableSmoothF5;
    }
    public static boolean enablePosSmoothing() {return ForgeModConfig.enablePosSmoothing;}
    public static boolean enableRotSmoothing() {return ForgeModConfig.enableRotSmoothing;}

    public static float getPosStiffness() {
        return ForgeModConfig.posStiffness;
    }

    public static float getRotStiffness() {
        return ForgeModConfig.rotStiffness;
    }
}