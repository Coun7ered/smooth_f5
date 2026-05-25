package net.countered.smoothf5.mixin;

import net.countered.smoothf5.logic.CameraSmoother;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Unique
    private final CameraSmoother smooth_f5$smoother = new CameraSmoother();

    @Inject(method = "setup", at = @At("HEAD"))
    private void onSetupHead(
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean thirdPersonReverse,
            float partialTick,
            CallbackInfo ci
    ) {
        smooth_f5$smoother.onSetupHead(
                (CameraAccessor) this,
                detached,
                thirdPersonReverse
        );
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void onSetup(
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean thirdPersonReverse,
            float partialTick,
            CallbackInfo ci
    ) {
       smooth_f5$smoother.onSetupTail(
               (CameraAccessor) this,
               detached,
               Minecraft.getInstance().getTimer().getGameTimeDeltaTicks()
       );
    }
}