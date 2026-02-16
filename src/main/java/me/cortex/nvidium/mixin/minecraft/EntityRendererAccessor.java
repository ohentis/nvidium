package me.cortex.nvidium.mixin.minecraft;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = EntityRenderer.class)
public interface EntityRendererAccessor {
    @Accessor("lightmapTexture")
    public DynamicTexture nvidium$getLightmapTexture();

}
