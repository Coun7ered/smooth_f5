package net.countered.smoothf5.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {

    @Accessor("xRot")
    float getXRot();

    @Accessor("yRot")
    float getYRot();

    @Accessor("position")
    Vec3 getPosition();

    @Invoker("setPosition")
    void callSetPosition(Vec3 pos);

    @Invoker("setRotation")
    void callSetRotation(float yRot, float xRot);
}
