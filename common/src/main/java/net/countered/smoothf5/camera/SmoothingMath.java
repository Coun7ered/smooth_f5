package net.countered.smoothf5.camera;

import net.countered.smoothf5.config.TransitionStyle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SmoothingMath {

    private SmoothingMath() {
    }

    public static float expFactor(float rate, float dtSec) {
        if (dtSec <= 0.0f || rate <= 0.0f) {
            return 0.0f;
        }
        return Mth.clamp(1.0f - (float) Math.exp(-rate * dtSec), 0.0f, 1.0f);
    }

    public static Vec3 expLerpVec3(Vec3 current, Vec3 target, float rateH, float rateV, float dtSec) {
        double fx = expFactor(rateH, dtSec);
        double fy = expFactor(rateV, dtSec);
        return new Vec3(
                Mth.lerp(fx, current.x, target.x),
                Mth.lerp(fy, current.y, target.y),
                Mth.lerp(fx, current.z, target.z)
        );
    }

    public static float ease(float t, TransitionStyle style) {
        float clamped = Mth.clamp(t, 0.0f, 1.0f);
        return switch (style) {
            case ORBIT, EASE_OUT -> {
                float inv = 1.0f - clamped;
                yield 1.0f - inv * inv * inv;
            }
        };
    }
}
