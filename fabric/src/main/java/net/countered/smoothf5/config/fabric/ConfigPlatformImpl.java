package net.countered.smoothf5.config.fabric;

public class ConfigPlatformImpl {

    public static boolean enableSmoothF5() {
        return FabricModConfig.enableSmoothF5;
    }
    public static boolean enablePosSmoothing() {return FabricModConfig.enablePosSmoothing;}
    public static boolean enableRotSmoothing() {return FabricModConfig.enableRotSmoothing;}

    public static float getPosStiffness() {
        return FabricModConfig.posStiffness;
    }

    public static float getRotStiffness() {
        return FabricModConfig.rotStiffness;
    }
}