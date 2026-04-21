package net.countered.smoothf5.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Unique
    private Vec3 smooth_f5$smoothPos = Vec3.ZERO;
    @Unique
    private Vec3 smooth_f5$smoothVel = Vec3.ZERO;
    @Unique
    private float smooth_f5$smoothYaw;
    @Unique
    private float smooth_f5$smoothPitch;
    @Unique
    private float smooth_f5$yawVel, smooth_f5$pitchVel;

    @Unique
    private static final float POS_STIFFNESS = 0.3f;
    @Unique
    private static final float POS_DAMPING   = 1f;
    @Unique
    private static final float ROT_STIFFNESS   = 0.7f;
    @Unique
    private static final float ROT_DAMPING   = 1.3f;

    @Unique
    private boolean smooth_f5$initialized = false;

    @Inject(method = "setup", at = @At("TAIL"))
    private void onSetup(BlockGetter level, Entity entity, boolean detached,
                         boolean thirdPersonReverse, float partialTick,
                         CallbackInfo ci) {
        Camera self = (Camera)(Object)this;
        CameraAccessor acc = (CameraAccessor)self;

        if (!smooth_f5$initialized) {
            smooth_f5$smoothPos = self.getPosition();
            smooth_f5$initialized = true;
            return;
        }

        if (!detached) {
            smooth_f5$smoothPos = acc.getPosition();
            smooth_f5$smoothYaw = acc.getYRot();
            smooth_f5$smoothPitch = acc.getXRot();
            smooth_f5$smoothVel = Vec3.ZERO;
            smooth_f5$yawVel = smooth_f5$pitchVel = 0f;
            return;
        }

        Vec3 targetPos = acc.getPosition();
        float targetYaw = acc.getYRot();
        float targetPitch = acc.getXRot();

        float dt = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();

        Vec3 diff = targetPos.subtract(smooth_f5$smoothPos);
        smooth_f5$smoothVel = smooth_f5$smoothVel.add(diff.scale(POS_STIFFNESS * dt));
        smooth_f5$smoothVel = smooth_f5$smoothVel.scale((float)Math.exp(-POS_DAMPING * dt));
        smooth_f5$smoothPos = smooth_f5$smoothPos.add(smooth_f5$smoothVel.multiply(dt, dt, dt));

        float yawDiff   = targetYaw - smooth_f5$smoothYaw;
        float pitchDiff = targetPitch - smooth_f5$smoothPitch;

        smooth_f5$yawVel += yawDiff * ROT_STIFFNESS * dt;
        smooth_f5$pitchVel += pitchDiff * ROT_STIFFNESS * dt;
        smooth_f5$yawVel *= (float)Math.exp(-ROT_DAMPING * dt);
        smooth_f5$pitchVel *= (float)Math.exp(-ROT_DAMPING * dt);

        smooth_f5$smoothYaw += smooth_f5$yawVel * dt;
        smooth_f5$smoothPitch += smooth_f5$pitchVel * dt;

        acc.callSetPosition(smooth_f5$smoothPos);
        acc.setYRot(smooth_f5$smoothYaw);
        acc.setXRot(smooth_f5$smoothPitch);

        Quaternionf q = new Quaternionf()
                .rotationYXZ((float)Math.PI - smooth_f5$smoothYaw * ((float)Math.PI / 180f),
                        -smooth_f5$smoothPitch * ((float)Math.PI / 180f),
                        0.0F);

        acc.callSetRotation(smooth_f5$smoothYaw, smooth_f5$smoothPitch);
    }
}