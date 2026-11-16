package net.countered.smoothf5.mixin;

import net.countered.smoothf5.camera.SmoothCameraController;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Unique
    private final SmoothCameraController smooth_f5$controller = new SmoothCameraController();

    @Unique
    private Vec3 smooth_f5$preMovePos = Vec3.ZERO;

    @Unique
    private Vec3 smooth_f5$moveDelta = Vec3.ZERO;

    @Inject(method = "update", at = @At("HEAD"))
    private void smooth_f5$onUpdateHead(DeltaTracker deltaTracker, CallbackInfo ci) {
        smooth_f5$moveDelta = Vec3.ZERO;
        smooth_f5$controller.onUpdateHead((Camera) (Object) this);
    }

    @Inject(
            method = "alignWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;move(FFF)V",
                    ordinal = 0
            )
    )
    private void smooth_f5$beforeDetachedMove(float partialTicks, CallbackInfo ci) {
        smooth_f5$preMovePos = ((CameraAccessor) this).getPosition();
    }

    @Inject(
            method = "alignWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;move(FFF)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void smooth_f5$afterDetachedMove(float partialTicks, CallbackInfo ci) {
        smooth_f5$moveDelta = ((CameraAccessor) this).getPosition().subtract(smooth_f5$preMovePos);
    }

    @Inject(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;alignWithEntity(F)V",
                    shift = At.Shift.AFTER
            )
    )
    private void smooth_f5$onAlignWithEntityTail(DeltaTracker deltaTracker, CallbackInfo ci) {
        smooth_f5$controller.onAlignWithEntityTail((Camera) (Object) this, smooth_f5$moveDelta);
    }
}
