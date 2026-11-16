package net.countered.smoothf5.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class SmoothF5Config extends MidnightConfig {

    public static final String GENERAL = "general";
    public static final String CAMERA = "camera";

    @Entry(category = GENERAL)
    public static SmoothingMode smoothingMode = SmoothingMode.ALWAYS;

    @Entry(category = GENERAL)
    public static boolean enablePosSmoothing = true;

    @Entry(category = GENERAL)
    public static boolean enableRotSmoothing = true;

    @Entry(category = CAMERA, isSlider = true, min = 0.01f, max = 1f, precision = 100)
    public static float posStiffness = 0.3f;

    @Entry(category = CAMERA, isSlider = true, min = 0.01f, max = 1f, precision = 100)
    public static float rotStiffness = 0.7f;

    @Entry(category = CAMERA, isSlider = true, min = 1, max = 100, precision = 100)
    public static int returnDuration = 10;
}
