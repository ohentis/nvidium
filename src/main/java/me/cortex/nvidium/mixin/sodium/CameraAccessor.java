package me.cortex.nvidium.mixin.sodium;

import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.gtnewhorizons.angelica.compat.mojang.Camera;

@Mixin(value = Camera.class, remap = false)
public interface CameraAccessor {

    @Accessor("pos")
    Vector3d nvidium$getPos();
}
