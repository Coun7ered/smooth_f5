package net.countered.smoothf5.config;

public final class ConfigPlatform {
    private static Provider provider;

    private ConfigPlatform() {}

    public static void setProvider(Provider provider) {
        ConfigPlatform.provider = provider;
    }

    private static Provider getProvider() {
        if (provider == null) {
            throw new IllegalStateException("ConfigPlatform provider not initialized");
        }

        return provider;
    }

    public static boolean isEnablePosSmoothing() {
        return getProvider().isEnablePosSmoothing();
    }

    public static boolean isEnableRotSmoothing() {
        return getProvider().isEnableRotSmoothing();
    }

    public static float getPosStiffness() {
        return getProvider().getPosStiffness();
    }

    public static float getRotStiffness() {
        return getProvider().getRotStiffness();
    }

    public static int getFPReturnDuration() {
        return getProvider().getFPReturnDuration();
    }

    public static SmoothingMode getSmoothingMode() {
        return getProvider().getSmoothingMode();
    }

    public interface Provider {
        boolean isEnablePosSmoothing();
        boolean isEnableRotSmoothing();
        float getPosStiffness();
        float getRotStiffness();
        int getFPReturnDuration();
        SmoothingMode getSmoothingMode();
    }
}
