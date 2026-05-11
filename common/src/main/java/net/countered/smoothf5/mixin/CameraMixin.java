package net.countered.smoothf5.mixin;

import net.countered.smoothf5.config.ConfigPlatform;
import net.countered.smoothf5.config.SmoothingMode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Unique private Vec3 smooth_f5$smoothPos = Vec3.ZERO;
    @Unique private Vec3 smooth_f5$smoothVel = Vec3.ZERO;

    @Unique private float smooth_f5$smoothYaw;
    @Unique private float smooth_f5$smoothPitch;
    @Unique private float smooth_f5$yawVel, smooth_f5$pitchVel;

    @Unique private boolean smooth_f5$wasDetached = false;
    @Unique private boolean smooth_f5$wasMirrored = false;
    @Unique private boolean smooth_f5$initialized = false;

    @Unique private boolean smooth_f5$isTransitioningToFP = false;
    @Unique private float smooth_f5$returnProgress = 0f;

    @Inject(method = "setup", at = @At("HEAD"))
    private void onSetupHead(
            Level level,
            Entity entity,
            boolean detached,
            boolean mirror,
            float partialTickTime,
            CallbackInfo ci
    ) {
        if (ConfigPlatform.getSmoothingMode().equals(SmoothingMode.NEVER)) return;
        CameraAccessor acc = (CameraAccessor) this;

        if (smooth_f5$wasDetached != detached || smooth_f5$wasMirrored != mirror) {
            if (!smooth_f5$initialized) {
                smooth_f5$snapTo(acc.getPosition(), acc.getYRot(), acc.getXRot(), acc);
                smooth_f5$initialized = true;
            }
            if (!smooth_f5$wasDetached && detached) {
                smooth_f5$applyOffsetInit(entity, partialTickTime, acc, -0.4);
                smooth_f5$isTransitioningToFP = false;
            }
            else if (smooth_f5$wasDetached && detached && !smooth_f5$wasMirrored && mirror && ConfigPlatform.isUseOldThirdToSecondTransition()) {
                smooth_f5$applyOffsetInit(entity, partialTickTime, acc, 0.4);
                smooth_f5$isTransitioningToFP = false;
            }
            else if (smooth_f5$wasDetached && !detached) {
                smooth_f5$isTransitioningToFP = true;
                smooth_f5$returnProgress = 0f;
            }
        }
        smooth_f5$wasDetached = detached;
        smooth_f5$wasMirrored = mirror;
    }

    @Unique
    private void smooth_f5$applyOffsetInit(Entity entity, float partialTickTime, CameraAccessor acc, double offsetScale) {
        Vec3 eye = entity.getEyePosition(partialTickTime);
        Vec3 forward = entity.getViewVector(partialTickTime);
        smooth_f5$smoothPos = eye.add(forward.scale(offsetScale));

        if (offsetScale > 0) {
            smooth_f5$smoothYaw = Mth.wrapDegrees(entity.getYRot() + 180f);
            smooth_f5$smoothPitch = -entity.getXRot();
        } else {
            smooth_f5$smoothYaw = acc.getYRot();
            smooth_f5$smoothPitch = acc.getXRot();
        }

        smooth_f5$smoothVel = Vec3.ZERO;
        smooth_f5$yawVel = 0;
        smooth_f5$pitchVel = 0;
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void onSetupTail(
            Level level,
            Entity entity,
            boolean detached,
            boolean mirror,
            float partialTickTime,
            CallbackInfo ci
    ) {
        if (ConfigPlatform.getSmoothingMode().equals(SmoothingMode.NEVER)) return;
        CameraAccessor acc = (CameraAccessor) this;

        if (!detached && !smooth_f5$isTransitioningToFP) {
            smooth_f5$smoothPos = acc.getPosition();
            smooth_f5$smoothYaw = acc.getYRot();
            smooth_f5$smoothPitch = acc.getXRot();
            smooth_f5$smoothVel = Vec3.ZERO;
            smooth_f5$yawVel = 0;
            smooth_f5$pitchVel = 0;
            return;
        }

        float dt = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();

        Vec3 targetPos = acc.getPosition();
        float targetYaw = acc.getYRot();
        float targetPitch = acc.getXRot();

        if (smooth_f5$isTransitioningToFP) {
            smooth_f5$returnProgress += dt / Math.max(1.0f, ConfigPlatform.getFPReturnDuration()* 10);
            if (smooth_f5$returnProgress >= 1.0f) {
                smooth_f5$isTransitioningToFP = false;
                return;
            }
            float invT = 1.0f - smooth_f5$returnProgress;
            float ease = 1.0f - (invT * invT * invT); // Cubic Ease-Out
            smooth_f5$smoothPos = smooth_f5$smoothPos.lerp(targetPos, ease);
            float yawDiff = Mth.wrapDegrees(targetYaw - smooth_f5$smoothYaw);
            smooth_f5$smoothYaw = Mth.wrapDegrees(smooth_f5$smoothYaw + yawDiff * ease);
            smooth_f5$smoothPitch = Mth.lerp(ease, smooth_f5$smoothPitch, targetPitch);
        } else {
            smooth_f5$stepPos(targetPos, dt);
            smooth_f5$stepRot(targetYaw, targetPitch, dt);
        }

        smooth_f5$apply(acc);
    }

    @Unique
    private void smooth_f5$stepPos(Vec3 target, float dt) {
        float stiffness = ConfigPlatform.getPosStiffness();
        float damping = 2.0f * (float)Math.sqrt(stiffness);

        float maxSubStep = 0.25f;
        while (dt > 0) {
            float step = Math.min(dt, maxSubStep);

            Vec3 diff = target.subtract(smooth_f5$smoothPos);
            Vec3 acceleration = diff.scale(stiffness).subtract(smooth_f5$smoothVel.scale(damping));

            smooth_f5$smoothVel = smooth_f5$smoothVel.add(acceleration.scale(step));
            smooth_f5$smoothPos = smooth_f5$smoothPos.add(smooth_f5$smoothVel.scale(step));

            dt -= step;
        }
    }

    @Unique
    private void smooth_f5$stepRot(float targetYaw, float targetPitch, float dt) {
        float rotStiffness = ConfigPlatform.getRotStiffness();
        float rotDamping = 2.0f * (float)Math.sqrt(rotStiffness);

        float maxSubStep = 0.1f;

        while (dt > 0) {
            float step = Math.min(dt, maxSubStep);

            float yawDiff = Mth.wrapDegrees(targetYaw - smooth_f5$smoothYaw);
            float pitchDiff = targetPitch - smooth_f5$smoothPitch;

            float yawAcc = (yawDiff * rotStiffness) - (smooth_f5$yawVel * rotDamping);
            float pitchAcc = (pitchDiff * rotStiffness) - (smooth_f5$pitchVel * rotDamping);

            smooth_f5$yawVel += yawAcc * step;
            smooth_f5$pitchVel += pitchAcc * step;

            smooth_f5$smoothYaw += smooth_f5$yawVel * step;
            smooth_f5$smoothPitch += smooth_f5$pitchVel * step;

            dt -= step;
        }
        smooth_f5$smoothYaw = Mth.wrapDegrees(smooth_f5$smoothYaw);
    }

    @Unique
    private void smooth_f5$snapTo(Vec3 pos, float yaw, float pitch, CameraAccessor acc) {
        smooth_f5$smoothPos = pos;
        smooth_f5$smoothVel = Vec3.ZERO;
        smooth_f5$smoothYaw = yaw;
        smooth_f5$smoothPitch = pitch;
        smooth_f5$yawVel = smooth_f5$pitchVel = 0f;
        acc.callSetPosition(pos);
        acc.callSetRotation(yaw, pitch);
    }

    @Unique
    private void smooth_f5$apply(CameraAccessor acc) {
        if (ConfigPlatform.isEnablePosSmoothing()) acc.callSetPosition(smooth_f5$smoothPos);
        if (ConfigPlatform.isEnableRotSmoothing()) acc.callSetRotation(smooth_f5$smoothYaw, smooth_f5$smoothPitch);
    }
}