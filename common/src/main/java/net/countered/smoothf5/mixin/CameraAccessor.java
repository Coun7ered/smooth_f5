package net.countered.smoothf5.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor("xRot") void setXRot(float value);
    @Accessor("yRot") void setYRot(float value);

    @Accessor("xRot") float getXRot();
    @Accessor("yRot") float getYRot();
    @Accessor("position") Vec3 getPosition();

    @Invoker("setPosition")
    void callSetPosition(Vec3 pos);
}