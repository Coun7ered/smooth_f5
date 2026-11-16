package net.countered.smoothf5.config;

import eu.midnightdust.lib.config.MidnightConfig;

@SuppressWarnings("unused")
public class SmoothF5Config extends MidnightConfig {

    public static final String GENERAL = "general";
    public static final String POSITION = "position";
    public static final String ROTATION = "rotation";
    public static final String TRANSITIONS = "transitions";

    // ==================== General ====================

    @Comment(category = GENERAL, centered = true)
    public static Comment generalHeader;

    @Entry(category = GENERAL)
    public static SmoothingMode smoothingMode = SmoothingMode.ALWAYS;

    @Entry(category = GENERAL)
    public static FollowSpace followSpace = FollowSpace.ROTATION_CENTERED;

    @Comment(category = GENERAL, centered = true)
    public static Comment viewsHeader;

    @Entry(category = GENERAL)
    public static boolean smoothBackView = true;

    @Entry(category = GENERAL)
    public static boolean smoothFrontView = true;

    // ==================== Position & Distance ====================

    @Comment(category = POSITION, centered = true)
    public static Comment positionHeader;

    @Entry(category = POSITION, isSlider = true, min = 1.0f, max = 10.0f, precision = 100)
    public static float posResponsiveness = 4.0f;

    @Entry(category = POSITION, isSlider = true, min = 0.1f, max = 2.0f, precision = 20)
    public static float verticalResponsivenessScale = 0.75f;

    @Condition(requiredOption = "followSpace", requiredValue = {"WORLD", "ROTATION_CENTERED"}, visibleButLocked = true)
    @Entry(category = POSITION)
    public static boolean adaptiveSpeed = false;

    @Condition(requiredOption = "followSpace", requiredValue = {"WORLD", "ROTATION_CENTERED"}, visibleButLocked = true)
    @Condition(requiredOption = "adaptiveSpeed", visibleButLocked = true)
    @Entry(category = POSITION, isSlider = true, min = 0.0f, max = 2.0f, precision = 20)
    public static float adaptiveSpeedBoost = 1.0f;

    @Comment(category = POSITION, centered = true)
    public static Comment safetyHeader;

    @Condition(requiredOption = "followSpace", requiredValue = {"WORLD", "ROTATION_CENTERED"}, visibleButLocked = true)
    @Entry(category = POSITION, isSlider = true, min = 0.2f, max = 2f, precision = 20)
    public static float minPlayerDistanceRatio = 1f;

    @Condition(requiredOption = "followSpace", requiredValue = {"WORLD", "ROTATION_CENTERED"}, visibleButLocked = true)
    @Entry(category = POSITION, isSlider = true, min = 0.25f, max = 10.0f, precision = 20)
    public static float maxLagDistance = 2.0f;

    @Entry(category = POSITION)
    public static boolean preventWallClipping = true;

    @Condition(requiredOption = "preventWallClipping", visibleButLocked = true)
    @Entry(category = POSITION)
    public static boolean smoothWallZoom = true;

    // ==================== Rotation ====================

    @Comment(category = ROTATION, centered = true)
    public static Comment rotationHeader;

    @Entry(category = ROTATION)
    public static boolean enableRotSmoothing = false;

    @Condition(requiredOption = "enableRotSmoothing", visibleButLocked = true)
    @Entry(category = ROTATION, isSlider = true, min = 1.0f, max = 100.0f, precision = 100)
    public static float rotResponsiveness = 10.0f;

    // ==================== Transitions ====================

    @Comment(category = TRANSITIONS, centered = true)
    public static Comment transitionsHeader;

    @Entry(category = TRANSITIONS)
    public static TransitionStyle transitionStyle = TransitionStyle.ORBIT;

    @Entry(category = TRANSITIONS, isSlider = true, min = 50, max = 1000, precision = 1)
    public static int transitionDurationMs = 300;

    @Entry(category = TRANSITIONS, isSlider = true, min = 50, max = 1000, precision = 1)
    public static int firstPersonReturnDurationMs = 200;
}
