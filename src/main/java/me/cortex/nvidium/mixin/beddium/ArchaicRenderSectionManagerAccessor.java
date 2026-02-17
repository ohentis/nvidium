package me.cortex.nvidium.mixin.beddium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.ventooth.beddium.modules.TerrainRendering.ArchaicRenderSectionManager;

@Mixin(value = ArchaicRenderSectionManager.class, remap = false)
public interface ArchaicRenderSectionManagerAccessor {

    @Invoker("useFogOcclusion")
    public boolean nvidium$useFogOcclusion();
}
