package net.countered.smoothf5.camera;

import net.minecraft.world.phys.Vec3;

public final class CameraState {

    public boolean initialized = false;
    public boolean wasDetached = false;
    public boolean wasMirrored = false;

    public long lastFrameTimeNs = 0L;

    public Vec3 smoothPos = Vec3.ZERO;
    public Vec3 smoothPivotPos = Vec3.ZERO;
    public Vec3 smoothRelativeOffset = Vec3.ZERO;
    public float smoothFollowDist = 4.0f;

    public float smoothYaw = 0.0f;
    public float smoothPitch = 0.0f;
    public float rotTargetYaw = 0.0f;
    public float rotPrevRawYaw = 0.0f;
    public boolean rotDeltaReady = false;

    public Vec3 renderPos = Vec3.ZERO;
    public float renderYaw = 0.0f;
    public float renderPitch = 0.0f;

    public Vec3 lastTargetPos = null;
    public Vec3 lastEyePos = null;
    public float lastTargetDist = 0.0f;
    public float smoothedPlayerSpeed = 0.0f;

    public boolean shouldSnapNextTail = false;

    public boolean isTransitioning = false;
    public boolean returningToFirstPerson = false;
    public boolean transFromDetached = false;
    public boolean orbitTransition = false;
    public boolean startFromEyeOnFirstStep = false;
    public boolean initMovementBaselineOnFirstStep = false;
    public float transitionProgress = 0.0f;
    public float transitionDurationSec = 0.22f;

    public Vec3 transStartPos = Vec3.ZERO;
    public Vec3 transStartOffset = Vec3.ZERO;
    public float transStartYaw = 0.0f;
    public float transStartPitch = 0.0f;
    public float transStartDist = 4.0f;

    public float transTargetYaw = 0.0f;
    public float transPrevRawYaw = 0.0f;
    public boolean transDeltaReady = false;

    public float consumeDeltaSeconds() {
        long nowNs = System.nanoTime();
        if (this.lastFrameTimeNs == 0L) {
            this.lastFrameTimeNs = nowNs;
            return 1.0f / 60.0f;
        }
        double elapsed = (nowNs - this.lastFrameTimeNs) * 1.0e-9;
        this.lastFrameTimeNs = nowNs;
        if (elapsed <= 0.0) {
            return 0.0f;
        }
        return (float) Math.min(0.1, elapsed);
    }

    public void snapTo(Vec3 pos, float yaw, float pitch, Vec3 eyePos) {
        snapPositionsAndAngles(pos, yaw, pitch, eyePos);
        this.smoothedPlayerSpeed = 0.0f;
    }

    public void snapPositionsAndAngles(Vec3 pos, float yaw, float pitch, Vec3 eyePos) {
        float dist = (float) pos.distanceTo(eyePos);
        this.smoothPos = pos;
        this.smoothPivotPos = eyePos;
        this.smoothRelativeOffset = pos.subtract(eyePos);
        this.smoothFollowDist = dist;
        this.smoothYaw = yaw;
        this.smoothPitch = pitch;
        this.rotTargetYaw = yaw;
        this.rotPrevRawYaw = yaw;
        this.rotDeltaReady = true;
        this.renderPos = pos;
        this.renderYaw = yaw;
        this.renderPitch = pitch;
        this.lastTargetPos = pos;
        this.lastEyePos = eyePos;
        this.lastTargetDist = dist;
    }
}
