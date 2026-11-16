package net.countered.smoothf5.camera;

import net.countered.smoothf5.config.FollowSpace;
import net.countered.smoothf5.config.SmoothF5Config;
import net.countered.smoothf5.config.SmoothingMode;
import net.countered.smoothf5.config.TransitionStyle;
import net.countered.smoothf5.mixin.CameraAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SmoothCameraController {

    private static final float TELEPORT_SNAP_DISTANCE_SQ = 16.0f * 16.0f;

    private final CameraState state = new CameraState();

    public void onUpdateHead(Camera camera) {
        SmoothingMode mode = SmoothF5Config.smoothingMode;
        if (mode == SmoothingMode.NEVER) {
            state.isTransitioning = false;
            state.initMovementBaselineOnFirstStep = false;
            return;
        }

        CameraAccessor acc = (CameraAccessor) camera;
        Minecraft minecraft = Minecraft.getInstance();
        boolean detached = !minecraft.options.getCameraType().isFirstPerson();
        boolean mirrored = minecraft.options.getCameraType().isMirrored();

        if (!state.initialized) {
            state.wasDetached = detached;
            state.wasMirrored = mirrored;
            return;
        }

        if (state.wasDetached != detached || state.wasMirrored != mirrored) {
            boolean shouldTransition = (mode == SmoothingMode.TRANSITION || mode == SmoothingMode.ALWAYS)
                    && isPerspectiveTransitionAllowed(state.wasDetached, state.wasMirrored, detached, mirrored);

            if (shouldTransition) {
                state.transStartPos = state.renderPos;
                state.transStartYaw = Mth.wrapDegrees(state.renderYaw);
                state.transStartPitch = state.renderPitch;
                Vec3 refEye = state.lastEyePos != null ? state.lastEyePos : acc.getPosition();
                state.transStartDist = (float) state.renderPos.distanceTo(refEye);

                boolean isBackFrontSwitch = state.wasDetached && detached && (state.wasMirrored != mirrored);
                state.returningToFirstPerson = !detached;
                state.transFromDetached = state.wasDetached;
                state.orbitTransition = SmoothF5Config.transitionStyle == TransitionStyle.ORBIT && isBackFrontSwitch;
                state.startFromEyeOnFirstStep = SmoothF5Config.transitionStyle == TransitionStyle.EASE_OUT && isBackFrontSwitch;
                state.initMovementBaselineOnFirstStep = detached;

                if (state.orbitTransition) {
                    Vec3 startRef = computeCameraOffsetPos(refEye, state.transStartYaw, state.transStartPitch, state.transStartDist);
                    state.transStartOffset = state.transStartPos.subtract(startRef);
                } else {
                    state.transStartOffset = Vec3.ZERO;
                }

                int durationMs = state.returningToFirstPerson
                        ? SmoothF5Config.firstPersonReturnDurationMs
                        : SmoothF5Config.transitionDurationMs;
                state.transitionDurationSec = Math.max(0.02f, durationMs / 1000.0f);
                state.transDeltaReady = false;
                state.isTransitioning = true;
                state.transitionProgress = 0.0f;
            } else {
                state.isTransitioning = false;
                state.startFromEyeOnFirstStep = false;
                state.initMovementBaselineOnFirstStep = false;
                if (mode == SmoothingMode.MOVEMENT) {
                    state.shouldSnapNextTail = true;
                }
            }
        }

        state.wasDetached = detached;
        state.wasMirrored = mirrored;
    }

    public void onAlignWithEntityTail(Camera camera, Vec3 moveDelta) {
        SmoothingMode mode = SmoothF5Config.smoothingMode;
        if (mode == SmoothingMode.NEVER) {
            state.initialized = false;
            state.lastFrameTimeNs = 0L;
            return;
        }

        float dtSec = state.consumeDeltaSeconds();

        CameraAccessor acc = (CameraAccessor) camera;
        Vec3 targetPos = acc.getPosition();
        float targetYaw = acc.getYRot();
        float targetPitch = acc.getXRot();

        boolean detached = camera.isDetached();
        boolean mirrored = Minecraft.getInstance().options.getCameraType().isMirrored();
        Vec3 eyePos = detached ? targetPos.subtract(moveDelta) : targetPos;

        if (!state.initialized) {
            state.snapTo(targetPos, targetYaw, targetPitch, eyePos);
            state.initialized = true;
            return;
        }

        // Cancel transitions and reset speed history on teleports, respawns, or dimension changes
        if (state.lastEyePos != null && eyePos.distanceToSqr(state.lastEyePos) > (double) TELEPORT_SNAP_DISTANCE_SQ) {
            state.isTransitioning = false;
            state.startFromEyeOnFirstStep = false;
            state.initMovementBaselineOnFirstStep = false;
            state.shouldSnapNextTail = false;
            state.snapTo(targetPos, targetYaw, targetPitch, eyePos);
            return;
        }

        // Track player speed continuously so adaptive speed smoothing stays accurate across perspective switches
        if (dtSec > 0.0f && state.lastEyePos != null) {
            float instantSpeed = (float) (eyePos.distanceTo(state.lastEyePos) / dtSec);
            float speedAlpha = SmoothingMath.expFactor(10.0f, dtSec);
            state.smoothedPlayerSpeed = Mth.lerp(speedAlpha, state.smoothedPlayerSpeed, instantSpeed);
        }

        // First-person and not transitioning back: stay passive and keep state synced
        if (!detached && !state.isTransitioning) {
            state.snapPositionsAndAngles(targetPos, targetYaw, targetPitch, eyePos);
            return;
        }

        if (dtSec <= 0.0f) {
            applyToCamera(acc);
            return;
        }

        boolean viewAllowed = !detached || (mirrored ? SmoothF5Config.smoothFrontView : SmoothF5Config.smoothBackView);
        boolean shouldSmoothMovement = viewAllowed
                && (mode == SmoothingMode.ALWAYS || mode == SmoothingMode.MOVEMENT)
                && detached;

        if (state.isTransitioning) {
            if (!state.returningToFirstPerson && shouldSmoothMovement) {
                if (state.initMovementBaselineOnFirstStep) {
                    initMovementBaseline(eyePos, targetPos, targetYaw, targetPitch);
                }
                stepMovement(camera, eyePos, targetPos, targetYaw, targetPitch, dtSec);
                stepTransition(camera, eyePos, targetPos, state.smoothPos, state.smoothYaw, state.smoothPitch, dtSec, true);
            } else {
                state.initMovementBaselineOnFirstStep = false;
                stepTransition(camera, eyePos, targetPos, targetPos, targetYaw, targetPitch, dtSec, false);
            }
        } else if (shouldSmoothMovement) {
            if (state.shouldSnapNextTail) {
                state.snapPositionsAndAngles(targetPos, targetYaw, targetPitch, eyePos);
                state.shouldSnapNextTail = false;
            } else {
                stepMovement(camera, eyePos, targetPos, targetYaw, targetPitch, dtSec);
                state.renderPos = state.smoothPos;
                state.renderYaw = state.smoothYaw;
                state.renderPitch = state.smoothPitch;
            }
        } else {
            state.snapPositionsAndAngles(targetPos, targetYaw, targetPitch, eyePos);
        }

        applyToCamera(acc);

        state.lastTargetPos = targetPos;
        state.lastEyePos = eyePos;
        state.lastTargetDist = (float) targetPos.distanceTo(eyePos);
    }

    private void initMovementBaseline(Vec3 eyePos, Vec3 targetPos, float targetYaw, float targetPitch) {
        state.smoothPos = targetPos;
        if (!state.transFromDetached) {
            state.smoothPivotPos = state.lastEyePos != null ? state.lastEyePos : eyePos;
        }
        state.smoothRelativeOffset = targetPos.subtract(eyePos);
        state.smoothFollowDist = (float) targetPos.distanceTo(eyePos);
        state.smoothYaw = targetYaw;
        state.smoothPitch = targetPitch;
        state.rotTargetYaw = targetYaw;
        state.rotPrevRawYaw = targetYaw;
        state.rotDeltaReady = true;
        state.lastTargetDist = state.smoothFollowDist;
        state.initMovementBaselineOnFirstStep = false;
    }

    private void stepTransition(
            Camera camera,
            Vec3 eyePos,
            Vec3 targetPos,
            Vec3 endPos,
            float endYaw,
            float endPitch,
            float dtSec,
            boolean keepMovementState
    ) {
        if (state.startFromEyeOnFirstStep) {
            state.transStartPos = eyePos;
            state.transStartYaw = Mth.wrapDegrees(endYaw);
            state.transStartPitch = endPitch;
            state.transDeltaReady = false;
            state.startFromEyeOnFirstStep = false;
        }

        if (!state.transDeltaReady) {
            state.transTargetYaw = state.transStartYaw + Mth.wrapDegrees(endYaw - state.transStartYaw);
            state.transPrevRawYaw = endYaw;
            state.transDeltaReady = true;
        } else {
            float frameDelta = Mth.wrapDegrees(endYaw - state.transPrevRawYaw);
            state.transTargetYaw += frameDelta;
            state.transPrevRawYaw = endYaw;
        }

        state.transitionProgress = Math.min(1.0f, state.transitionProgress + dtSec / state.transitionDurationSec);
        float ease = SmoothingMath.ease(state.transitionProgress, SmoothF5Config.transitionStyle);

        state.renderYaw = Mth.wrapDegrees(state.transStartYaw + (state.transTargetYaw - state.transStartYaw) * ease);
        state.renderPitch = state.transStartPitch + (endPitch - state.transStartPitch) * ease;

        if (state.transitionProgress >= 1.0f) {
            state.isTransitioning = false;
            state.renderPos = endPos;
            state.renderYaw = endYaw;
            state.renderPitch = endPitch;
            if (!keepMovementState) {
                state.snapPositionsAndAngles(endPos, endYaw, endPitch, eyePos);
            }
            return;
        }

        if (state.orbitTransition) {
            float targetDist = (float) targetPos.distanceTo(eyePos);
            float dist = Mth.lerp(ease, state.transStartDist, targetDist);
            Vec3 orbitPos = computeCameraOffsetPos(eyePos, state.renderYaw, state.renderPitch, dist);
            Vec3 endOrbitRef = computeCameraOffsetPos(eyePos, endYaw, endPitch, targetDist);
            Vec3 desired = orbitPos
                    .add(state.transStartOffset.scale(1.0 - ease))
                    .add(endPos.subtract(endOrbitRef).scale(ease));
            state.renderPos = clipIfEnabled(camera, eyePos, desired);
        } else {
            Vec3 lerped = state.transStartPos.lerp(endPos, ease);
            state.renderPos = state.returningToFirstPerson ? lerped : clipIfEnabled(camera, eyePos, lerped);
        }
    }

    private void stepMovement(Camera camera, Vec3 eyePos, Vec3 targetPos, float targetYaw, float targetPitch, float dtSec) {
        // Step rotation first so position smoothing stays synchronized with smoothed camera angles
        if (SmoothF5Config.enableRotSmoothing) {
            stepRotation(targetYaw, targetPitch, dtSec);
        } else {
            state.smoothYaw = targetYaw;
            state.smoothPitch = targetPitch;
            state.rotTargetYaw = targetYaw;
            state.rotPrevRawYaw = targetYaw;
            state.rotDeltaReady = true;
        }

        Vec3 desiredTargetPos = targetPos;
        if (SmoothF5Config.enableRotSmoothing) {
            float targetDist = (float) targetPos.distanceTo(eyePos);
            desiredTargetPos = clipIfEnabled(
                    camera,
                    eyePos,
                    computeCameraOffsetPos(eyePos, state.smoothYaw, state.smoothPitch, targetDist)
            );
        }

        FollowSpace space = SmoothF5Config.followSpace;
        switch (space) {
            case PLAYER_RELATIVE -> stepRelativePosition(eyePos, desiredTargetPos, dtSec);
            case ROTATION_CENTERED -> stepRotationCenteredPosition(eyePos, desiredTargetPos, dtSec);
            case WORLD -> stepWorldPosition(eyePos, desiredTargetPos, dtSec);
        }
        state.smoothPos = clipIfEnabled(camera, eyePos, state.smoothPos);
    }

    private void stepRelativePosition(Vec3 eyePos, Vec3 targetPos, float dtSec) {
        Vec3 targetOffset = targetPos.subtract(eyePos);
        double targetDist = targetOffset.length();

        // Snap inward immediately on wall collision so the camera doesn't clip into blocks
        if (SmoothF5Config.smoothWallZoom && targetDist < state.smoothRelativeOffset.length() - 0.02) {
            double currentLen = state.smoothRelativeOffset.length();
            if (currentLen > 1.0e-4) {
                state.smoothRelativeOffset = state.smoothRelativeOffset.scale(targetDist / currentLen);
            }
        }

        float rateH = SmoothF5Config.posResponsiveness;
        float rateV = rateH * SmoothF5Config.verticalResponsivenessScale;

        state.smoothRelativeOffset = SmoothingMath.expLerpVec3(state.smoothRelativeOffset, targetOffset, rateH, rateV, dtSec);
        state.smoothPivotPos = eyePos;
        state.smoothFollowDist = (float) state.smoothRelativeOffset.length();
        state.smoothPos = eyePos.add(state.smoothRelativeOffset);
    }

    private void stepRotationCenteredPosition(Vec3 eyePos, Vec3 targetPos, float dtSec) {
        float rateH = effectiveHorizontalRate();
        float rateV = rateH * SmoothF5Config.verticalResponsivenessScale;

        state.smoothPivotPos = SmoothingMath.expLerpVec3(state.smoothPivotPos, eyePos, rateH, rateV, dtSec);

        Vec3 targetOffset = targetPos.subtract(eyePos);
        float targetDist = (float) targetOffset.length();

        if (SmoothF5Config.smoothWallZoom) {
            if (targetDist < state.smoothFollowDist - 0.02f) {
                state.smoothFollowDist = targetDist;
            } else {
                state.smoothFollowDist = Mth.lerp(SmoothingMath.expFactor(rateH, dtSec), state.smoothFollowDist, targetDist);
            }
        } else {
            state.smoothFollowDist = targetDist;
        }

        Vec3 orbitOffset;
        if (SmoothF5Config.enableRotSmoothing) {
            orbitOffset = computeCameraOffsetPos(Vec3.ZERO, state.smoothYaw, state.smoothPitch, state.smoothFollowDist);
        } else if (targetDist > 1.0e-4f) {
            orbitOffset = targetOffset.scale(state.smoothFollowDist / targetDist);
        } else {
            orbitOffset = targetOffset;
        }

        state.smoothPos = state.smoothPivotPos.add(orbitOffset);

        // Apply min-distance and max-lag safeguards, keeping smoothPivotPos consistent with any clamp
        Vec3 clampedPos = applyDistanceAndLagClamps(eyePos, targetPos, state.smoothPos);
        if (!clampedPos.equals(state.smoothPos)) {
            state.smoothPos = clampedPos;
            state.smoothPivotPos = clampedPos.subtract(orbitOffset);
        }

        state.smoothRelativeOffset = state.smoothPos.subtract(eyePos);
    }

    private void stepWorldPosition(Vec3 eyePos, Vec3 targetPos, float dtSec) {
        float rateH = effectiveHorizontalRate();
        float rateV = rateH * SmoothF5Config.verticalResponsivenessScale;
        state.smoothPos = SmoothingMath.expLerpVec3(state.smoothPos, targetPos, rateH, rateV, dtSec);

        double targetDist = targetPos.distanceTo(eyePos);

        // Immediate inward snap when a wall forces the target camera closer
        if (SmoothF5Config.smoothWallZoom && targetDist < state.lastTargetDist - 0.05f) {
            Vec3 fromEye = state.smoothPos.subtract(eyePos);
            double curDist = fromEye.length();
            if (curDist > targetDist && curDist > 1.0e-4) {
                state.smoothPos = eyePos.add(fromEye.scale(targetDist / curDist));
            }
        }

        state.smoothPos = applyDistanceAndLagClamps(eyePos, targetPos, state.smoothPos);
        state.smoothPivotPos = eyePos;
        state.smoothRelativeOffset = state.smoothPos.subtract(eyePos);
        state.smoothFollowDist = (float) state.smoothRelativeOffset.length();
    }

    private float effectiveHorizontalRate() {
        float rateH = SmoothF5Config.posResponsiveness;
        if (SmoothF5Config.adaptiveSpeed && state.lastEyePos != null) {
            float excessSpeed = Math.max(0.0f, state.smoothedPlayerSpeed - 4.0f);
            rateH *= 1.0f + excessSpeed * 0.18f * SmoothF5Config.adaptiveSpeedBoost;
        }
        return rateH;
    }

    private Vec3 applyDistanceAndLagClamps(Vec3 eyePos, Vec3 targetPos, Vec3 pos) {
        Vec3 toTarget = targetPos.subtract(eyePos);
        double targetDist = toTarget.length();

        // Prevent camera from coming too close to or crossing behind the player (e.g. sprinting toward front camera)
        if (targetDist > 0.05) {
            Vec3 dir = toTarget.scale(1.0 / targetDist);
            Vec3 offset = pos.subtract(eyePos);
            double axialDist = offset.dot(dir);
            double minAllowed = targetDist * SmoothF5Config.minPlayerDistanceRatio;

            if (axialDist < minAllowed) {
                pos = eyePos.add(offset.add(dir.scale(minAllowed - axialDist)));
            }
        }

        // Enforce maximum trailing lag so high-speed movement doesn't detach the camera
        Vec3 lag = pos.subtract(targetPos);
        double lagLen = lag.length();
        double maxLag = SmoothF5Config.maxLagDistance;
        if (lagLen > maxLag && lagLen > 1.0e-4) {
            Vec3 lagDir = lag.scale(1.0 / lagLen);
            pos = targetPos.add(lagDir.scale(maxLag));
        }

        return pos;
    }

    private void stepRotation(float targetYaw, float targetPitch, float dtSec) {
        if (!state.rotDeltaReady) {
            state.rotTargetYaw = state.smoothYaw + Mth.wrapDegrees(targetYaw - state.smoothYaw);
            state.rotPrevRawYaw = targetYaw;
            state.rotDeltaReady = true;
        } else {
            float frameDelta = Mth.wrapDegrees(targetYaw - state.rotPrevRawYaw);
            state.rotTargetYaw += frameDelta;
            state.rotPrevRawYaw = targetYaw;
        }

        float factor = SmoothingMath.expFactor(SmoothF5Config.rotResponsiveness, dtSec);
        state.smoothYaw = Mth.lerp(factor, state.smoothYaw, state.rotTargetYaw);
        state.smoothPitch = Mth.lerp(factor, state.smoothPitch, targetPitch);

        if (Math.abs(state.smoothYaw) > 3600.0f) {
            float shift = Math.round(state.smoothYaw / 360.0f) * 360.0f;
            state.smoothYaw -= shift;
            state.rotTargetYaw -= shift;
        }
    }

    private void applyToCamera(CameraAccessor acc) {
        acc.callSetPosition(state.renderPos);
        if (state.isTransitioning || SmoothF5Config.enableRotSmoothing) {
            acc.callSetRotation(Mth.wrapDegrees(state.renderYaw), state.renderPitch);
        }
    }

    private static boolean isPerspectiveTransitionAllowed(boolean prevDetached, boolean prevMirrored, boolean nextDetached, boolean nextMirrored) {
        if (!prevDetached && !nextDetached) {
            return false;
        }
        if (prevDetached && !nextDetached) {
            return prevMirrored ? SmoothF5Config.smoothFrontView : SmoothF5Config.smoothBackView;
        }
        if (!prevDetached) {
            return nextMirrored ? SmoothF5Config.smoothFrontView : SmoothF5Config.smoothBackView;
        }
        return SmoothF5Config.smoothBackView || SmoothF5Config.smoothFrontView;
    }

    private static Vec3 computeCameraOffsetPos(Vec3 eyePos, float yaw, float pitch, float dist) {
        Quaternionf rot = new Quaternionf().rotationYXZ(
                (float) Math.PI - yaw * Mth.DEG_TO_RAD,
                -pitch * Mth.DEG_TO_RAD,
                0.0f
        );
        Vector3f back = new Vector3f(0.0f, 0.0f, dist).rotate(rot);
        return eyePos.add(back.x, back.y, back.z);
    }

    private static Vec3 clipIfEnabled(Camera camera, Vec3 eyePos, Vec3 desiredPos) {
        if (!SmoothF5Config.preventWallClipping) {
            return desiredPos;
        }
        Level level = ((CameraAccessor) camera).getLevel();
        Entity entity = camera.entity();
        if (level == null || entity == null) {
            return desiredPos;
        }
        Vec3 delta = desiredPos.subtract(eyePos);
        double desiredDist = delta.length();
        if (desiredDist < 1.0e-4) {
            return desiredPos;
        }
        double minDist = desiredDist;
        for (int i = 0; i < 8; i++) {
            double ox = ((i & 1) * 2 - 1) * 0.1;
            double oy = (((i >> 1) & 1) * 2 - 1) * 0.1;
            double oz = (((i >> 2) & 1) * 2 - 1) * 0.1;
            Vec3 from = eyePos.add(ox, oy, oz);
            Vec3 to = from.add(delta);
            HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
            if (hit.getType() != HitResult.Type.MISS) {
                double hitDist = hit.getLocation().distanceTo(eyePos);
                if (hitDist < minDist) {
                    minDist = hitDist;
                }
            }
        }
        if (minDist < desiredDist) {
            return eyePos.add(delta.scale(Math.max(0.0, minDist) / desiredDist));
        }
        return desiredPos;
    }
}
