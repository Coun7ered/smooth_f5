package net.countered.smoothf5.config.fabric;

import eu.midnightdust.lib.config.MidnightConfig;
import net.countered.smoothf5.config.SmoothingMode;

public class ConfigPlatformImpl extends MidnightConfig {

    public static final String GENERAL = "general";
    public static final String CAMERA = "camera";

    @Entry(category = GENERAL)
    public static SmoothingMode smoothingMode = SmoothingMode.ALWAYS;

    public static SmoothingMode getSmoothingMode() {
        return smoothingMode;
    }

    @Entry(category = GENERAL)
    public static boolean useOldThirdToSecondTransition = false;

    public static boolean isUseOldThirdToSecondTransition() {
        return useOldThirdToSecondTransition;
    }

    @Entry(category = GENERAL)
    public static boolean enablePosSmoothing = true;

    public static boolean isEnablePosSmoothing() {
        return enablePosSmoothing;
    }

    @Entry(category = GENERAL)
    public static boolean enableRotSmoothing = true;

    public static boolean isEnableRotSmoothing() {
        return enableRotSmoothing;
    }

    @Entry(category = CAMERA, isSlider = true, min = 0.01f, max = 1f, precision = 100)
    public static float posStiffness = 0.3f;

    public static float getPosStiffness() {
        return posStiffness;
    }

    @Entry(category = CAMERA, isSlider = true, min = 0.01f, max = 1f, precision = 100)
    public static float rotStiffness = 0.7f;

    public static float getRotStiffness() {
        return rotStiffness;
    }

    @Entry(category = CAMERA,  isSlider = true, min = 1, max = 100, precision = 100)
    public static int returnDuration = 15;

    public static int getFPReturnDuration() {
        return returnDuration;
    }
}