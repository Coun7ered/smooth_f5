package net.countered.smoothf5.logic;

import net.countered.smoothf5.config.ConfigPlatform;
import net.countered.smoothf5.mixin.CameraAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class CameraSmoother {
    
    private final SmoothCameraState state;

    public CameraSmoother() {
        this.state = new SmoothCameraState();
    }

    /**
     * This method is called at the head of the camera setup method.
     * It can be used to check the camera position before vanilla would move it.
     */
    public void onSetupHead(CameraAccessor acc, boolean detached, boolean thirdPersonReverse) {
        SmoothingMode mode = ConfigPlatform.getSmoothingMode();
        if (mode == SmoothingMode.NEVER) return;

        // Camera mode changed
        if (state.wasDetached != detached || state.wasMirrored != thirdPersonReverse) {
            // First time initialization
            if (!state.initialized) {
                snapTo(acc.getPosition(), acc.getYRot(), acc.getXRot());
                state.initialized = true;
            }
            // Start transition (transition or FPReturn)
            if (mode == SmoothingMode.TRANSITION || (mode == SmoothingMode.ALWAYS && !detached && state.wasDetached)) {
                state.transStartPos   = state.smoothPos;
                state.transStartYaw   = state.smoothYaw;
                state.transStartPitch = state.smoothPitch;
                state.transDeltaReady    = false;
                state.isTransitioning = true;
                state.transitionProgress = 0f;
            }
            // No transition
            else {
                state.isTransitioning = false;
                if (mode == SmoothingMode.MOVEMENT) {
                    state.shouldSnapNextTail = true;
                }
            }
        }
        state.wasDetached = detached;
        state.wasMirrored = thirdPersonReverse;
    }

    /**
     * This method is called at the end of the camera setup method.
     * It can be used to modify the position that the camera will end up in the world.
     */
    public void onSetupTail(CameraAccessor acc, boolean detached, float dt) {
        SmoothingMode mode = ConfigPlatform.getSmoothingMode();
        if (mode == SmoothingMode.NEVER) return;

        // No smoothing in first person
        if (!detached && !state.isTransitioning) {
            state.resetStates(acc);
            return;
        }

        Vec3 targetPos = acc.getPosition();
        float targetYaw = acc.getYRot();
        float targetPitch = acc.getXRot();

        boolean shouldSmooth = false;

        switch (mode) {
            case ALWAYS -> shouldSmooth = true;
            case TRANSITION -> shouldSmooth = state.isTransitioning;
            case MOVEMENT -> shouldSmooth = detached && !state.isTransitioning;
            case null, default -> shouldSmooth = false;
        }

        if (shouldSmooth) {
            if (state.isTransitioning) {
                applyTransitionLogic(targetPos, targetYaw, targetPitch, dt);
            } else if (state.shouldSnapNextTail) {
                snapTo(targetPos, targetYaw, targetPitch);
                state.shouldSnapNextTail = false;
            } else {
                stepPos(targetPos, dt);
                stepRot(targetYaw, targetPitch, dt);
            }
        } else {
            snapTo(targetPos, targetYaw, targetPitch);
            if (!detached) state.isTransitioning = false;
        }

        apply(acc);
    }

    private void applyTransitionLogic(Vec3 targetPos, float targetYaw, float targetPitch, float dt) {
        if (!state.transDeltaReady) {
            state.transTargetYaw  = state.transStartYaw + Mth.wrapDegrees(targetYaw - state.transStartYaw);
            state.transPrevRawYaw = targetYaw;
            state.transDeltaReady = true;
        } else {
            float frameDelta = Mth.wrapDegrees(targetYaw - state.transPrevRawYaw);
            state.transTargetYaw += frameDelta;
            state.transPrevRawYaw = targetYaw;
        }

        float duration = Math.max(1.0f, ConfigPlatform.getFPReturnDuration());
        state.transitionProgress = Math.min(1.0f,
                state.transitionProgress + dt / duration);

        float t    = state.transitionProgress;
        float ease = 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t);

        state.smoothPos   = state.transStartPos.lerp(targetPos, ease);
        state.smoothYaw   = Mth.wrapDegrees(
                state.transStartYaw + (state.transTargetYaw - state.transStartYaw) * ease);
        state.smoothPitch = state.transStartPitch
                + (targetPitch - state.transStartPitch) * ease;

        if (state.transitionProgress >= 1.0f) {
            state.isTransitioning = false;
            state.smoothPos   = targetPos;
            state.smoothYaw   = targetYaw;
            state.smoothPitch = targetPitch;
            state.smoothVel   = Vec3.ZERO;
            state.yawVel      = 0f;
            state.pitchVel    = 0f;
        }
    }

    private void stepPos(Vec3 target, float dt) {
        float stiffness = ConfigPlatform.getPosStiffness();
        float damping = 2.0f * (float)Math.sqrt(stiffness);

        float maxSubStep = 0.25f;
        while (dt > 0) {
            float step = Math.min(dt, maxSubStep);

            Vec3 diff = target.subtract(state.smoothPos);
            Vec3 acceleration = diff.scale(stiffness).subtract(state.smoothVel.scale(damping));

            state.smoothVel = state.smoothVel.add(acceleration.scale(step));
            state.smoothPos = state.smoothPos.add(state.smoothVel.scale(step));

            dt -= step;
        }
    }

    private void stepRot(float targetYaw, float targetPitch, float dt) {
        float rotStiffness = ConfigPlatform.getRotStiffness();
        float rotDamping = 2.0f * (float)Math.sqrt(rotStiffness);

        float maxSubStep = 0.1f;

        while (dt > 0) {
            float step = Math.min(dt, maxSubStep);

            float yawDiff = Mth.wrapDegrees(targetYaw - state.smoothYaw);
            float pitchDiff = targetPitch - state.smoothPitch;

            float yawAcc = (yawDiff * rotStiffness) - (state.yawVel * rotDamping);
            float pitchAcc = (pitchDiff * rotStiffness) - (state.pitchVel * rotDamping);

            state.yawVel += yawAcc * step;
            state.pitchVel += pitchAcc * step;

            state.smoothYaw += state.yawVel * step;
            state.smoothPitch += state.pitchVel * step;

            dt -= step;
        }
        state.smoothYaw = Mth.wrapDegrees(state.smoothYaw);
    }

    private void snapTo(Vec3 pos, float yaw, float pitch) {
        state.smoothPos = pos;
        state.smoothVel = Vec3.ZERO;
        state.smoothYaw = yaw;
        state.smoothPitch = pitch;
        state.yawVel = 0f;
        state.pitchVel = 0f;
    }

    private void apply(CameraAccessor acc) {
        if (ConfigPlatform.isEnablePosSmoothing()) acc.callSetPosition(state.smoothPos);
        if (ConfigPlatform.isEnableRotSmoothing()) acc.callSetRotation(state.smoothYaw, state.smoothPitch);
    }
}
