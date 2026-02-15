package me.cortex.nvidium.mixin.minecraft;

import net.minecraft.client.renderer.EntityRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import me.cortex.nvidium.Nvidium;

@Mixin(EntityRenderer.class)
public class MixinFogRenderer {

    @ModifyConstant(method = "setupFog", constant = @Constant(floatValue = 192.0F))
    private float changeFog(float fog) {
        if (Nvidium.IS_ENABLED) {
            return 9999999f;
        } else {
            return fog;
        }
    }
}
