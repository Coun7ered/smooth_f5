package net.countered.smoothf5.logic;

import net.countered.smoothf5.mixin.CameraAccessor;
import net.minecraft.world.phys.Vec3;

public class SmoothCameraState {

    public Vec3 smoothPos = Vec3.ZERO;
    public Vec3  smoothVel = Vec3.ZERO;
    public float smoothYaw, smoothPitch;
    public float yawVel, pitchVel;

    public boolean wasDetached, wasMirrored, initialized;
    public boolean shouldSnapNextTail;

    public boolean isTransitioning;
    public float transitionProgress;
    public Vec3 transStartPos;
    public float transStartYaw, transStartPitch;
    public float transTargetYaw, transPrevRawYaw;
    public boolean transDeltaReady;

    public SmoothCameraState() {}

    protected void resetStates(CameraAccessor acc) {
        smoothPos = acc.getPosition();
        smoothVel = Vec3.ZERO;
        smoothYaw = acc.getYRot();
        smoothPitch = acc.getXRot();
        yawVel = 0;
        pitchVel = 0;
    }
}

