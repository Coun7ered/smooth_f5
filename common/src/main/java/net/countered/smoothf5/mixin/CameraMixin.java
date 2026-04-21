package net.countered.smoothf5.mixin;

import net.countered.smoothf5.config.ModConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
        if (!ModConfig.enableSmoothF5()) return;

        CameraAccessor acc = (CameraAccessor)(Object)this;

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
    private void onSetupTail(
            Level level,
            Entity entity,
            boolean detached,
            boolean mirror,
            float partialTickTime,
            CallbackInfo ci
    ) {
        if (!ModConfig.enableSmoothF5()) return;

        CameraAccessor acc = (CameraAccessor)(Object)this;

        if (!detached) {
            smooth_f5$wasDetached = false;
            smooth_f5$fpTransitionStartPos = null;
            return;
        }

        float dt = Minecraft.getInstance()
                .getDeltaTracker()
                .getGameTimeDeltaTicks();

        Vec3 targetPos = acc.getPosition();
        float targetYaw = acc.getYRot();
        float targetPitch = acc.getXRot();

        if (smooth_f5$pendingInit && smooth_f5$fpTransitionStartPos != null) {
            smooth_f5$smoothPos = smooth_f5$fpTransitionStartPos;
            smooth_f5$smoothVel = Vec3.ZERO;

            smooth_f5$smoothYaw = smooth_f5$fpTransitionYaw;
            smooth_f5$smoothPitch = smooth_f5$fpTransitionPitch;
            smooth_f5$yawVel = smooth_f5$pitchVel = 0f;

            acc.callSetPosition(smooth_f5$smoothPos);
            acc.callSetRotation(smooth_f5$smoothYaw, smooth_f5$smoothPitch);

            smooth_f5$fpTransitionStartPos = null;
            smooth_f5$pendingInit = false;
            return;
        }

        Vec3 diff = targetPos.subtract(smooth_f5$smoothPos);

        float stiffness = ModConfig.posStiffness();
        float damping = 2.0f * (float)Math.sqrt(stiffness);


        Vec3 accel = diff.scale(stiffness);

        smooth_f5$smoothVel = smooth_f5$smoothVel.add(accel.scale(dt));

        float dampingFactor = (float)Math.exp(-damping * dt);
        smooth_f5$smoothVel = smooth_f5$smoothVel.scale(dampingFactor);

        smooth_f5$smoothPos = smooth_f5$smoothPos.add(smooth_f5$smoothVel.scale(dt));

        float yawDiff = Mth.wrapDegrees(targetYaw - smooth_f5$smoothYaw);
        float pitchDiff = targetPitch - smooth_f5$smoothPitch;

        float rotStiffness = ModConfig.rotStiffness();
        float rotDamping = 2.0f * (float)Math.sqrt(rotStiffness);

        float yawAccel = yawDiff * rotStiffness;
        float pitchAccel = pitchDiff * rotStiffness;

        smooth_f5$yawVel += yawAccel * dt;
        smooth_f5$pitchVel += pitchAccel * dt;

        float rotDampingFactor = (float)Math.exp(-rotDamping * dt);
        smooth_f5$yawVel *= rotDampingFactor;
        smooth_f5$pitchVel *= rotDampingFactor;

        smooth_f5$smoothYaw += smooth_f5$yawVel * dt;
        smooth_f5$smoothPitch += smooth_f5$pitchVel * dt;

        if (ModConfig.enablePosSmoothing()) {
            acc.callSetPosition(smooth_f5$smoothPos);
        }
        if (ModConfig.enableRotSmoothing()) {
            acc.callSetRotation(smooth_f5$smoothYaw, smooth_f5$smoothPitch);
        }
    }
}