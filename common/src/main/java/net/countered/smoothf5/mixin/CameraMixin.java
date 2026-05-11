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

    @Unique private Vec3 smooth_f5$fpTransitionStartPos = null;
    @Unique private float smooth_f5$fpTransitionYaw;
    @Unique private float smooth_f5$fpTransitionPitch;

    @Unique private boolean smooth_f5$wasDetached = false;
    @Unique private boolean smooth_f5$wasMirrored = false;
    @Unique private boolean smooth_f5$pendingInit = false;

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

        if (!smooth_f5$wasDetached && detached) {
            Vec3 eye = entity.getEyePosition(partialTickTime);
            Vec3 forward = entity.getViewVector(partialTickTime);
            smooth_f5$fpTransitionStartPos = eye.subtract(forward.scale(0.4));

            smooth_f5$fpTransitionYaw = acc.getYRot();
            smooth_f5$fpTransitionPitch = acc.getXRot();

            smooth_f5$pendingInit = true;
        }

        if (smooth_f5$wasDetached && detached && !smooth_f5$wasMirrored && mirror) {
            Vec3 eye = entity.getEyePosition(partialTickTime);
            Vec3 forward = entity.getViewVector(partialTickTime);

            smooth_f5$fpTransitionStartPos = eye.add(forward.scale(0.4));

            smooth_f5$fpTransitionYaw = Mth.wrapDegrees(entity.getYRot() + 180f);
            smooth_f5$fpTransitionPitch = - entity.getXRot();

            smooth_f5$pendingInit = true;
        }

        smooth_f5$wasDetached = detached;
        smooth_f5$wasMirrored = mirror;
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void onSetupTail(Level level, Entity entity, boolean detached,
                             boolean mirror, float partialTickTime, CallbackInfo ci) {
        if (ConfigPlatform.getSmoothingMode().equals(SmoothingMode.NEVER)) return;

        CameraAccessor acc = (CameraAccessor) this;

        if (!detached) {
            smooth_f5$wasDetached = false;
            smooth_f5$fpTransitionStartPos = null;
            return;
        }

        if (smooth_f5$pendingInit && smooth_f5$fpTransitionStartPos != null) {
            smooth_f5$snapTo(smooth_f5$fpTransitionStartPos, smooth_f5$fpTransitionYaw,
                    smooth_f5$fpTransitionPitch, acc);
            smooth_f5$fpTransitionStartPos = null;
            smooth_f5$pendingInit = false;
            return;
        }

        float dt = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
        smooth_f5$stepPos(acc.getPosition(), dt);
        smooth_f5$stepRot(acc.getYRot(), acc.getXRot(), dt);
        smooth_f5$apply(acc);
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
    private void smooth_f5$stepPos(Vec3 target, float dt) {
        float stiffness = ConfigPlatform.getPosStiffness();
        float damping = 2.0f * (float)Math.sqrt(stiffness);
        float expF = (float)Math.exp(-damping * dt);
        Vec3 diff = target.subtract(smooth_f5$smoothPos);
        smooth_f5$smoothVel = smooth_f5$smoothVel.add(diff.scale(stiffness * dt)).scale(expF);
        smooth_f5$smoothPos = smooth_f5$smoothPos.add(smooth_f5$smoothVel.scale(dt))
                .add(diff.scale(1f - expF));
    }

    @Unique
    private void smooth_f5$stepRot(float targetYaw, float targetPitch, float dt) {
        float rotStiffness = ConfigPlatform.getRotStiffness();
        float expF = (float)Math.exp(-2.0f * (float)Math.sqrt(rotStiffness) * dt);
        float yawDiff   = Mth.wrapDegrees(targetYaw - smooth_f5$smoothYaw);
        float pitchDiff = targetPitch - smooth_f5$smoothPitch;
        smooth_f5$yawVel   = (smooth_f5$yawVel   + yawDiff   * rotStiffness * dt) * expF;
        smooth_f5$pitchVel = (smooth_f5$pitchVel + pitchDiff * rotStiffness * dt) * expF;
        smooth_f5$smoothYaw   += smooth_f5$yawVel * dt + yawDiff   * (1f - expF);
        smooth_f5$smoothPitch += smooth_f5$pitchVel * dt + pitchDiff * (1f - expF);
    }

    @Unique
    private void smooth_f5$apply(CameraAccessor acc) {
        if (ConfigPlatform.enablePosSmoothing()) acc.callSetPosition(smooth_f5$smoothPos);
        if (ConfigPlatform.enableRotSmoothing()) acc.callSetRotation(smooth_f5$smoothYaw, smooth_f5$smoothPitch);
    }
}