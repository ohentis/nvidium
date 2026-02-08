package me.cortex.nvidium.mixin.sodium;

import org.embeddedt.embeddium.impl.render.chunk.lists.RenderListManager;
import org.embeddedt.embeddium.impl.render.chunk.occlusion.OcclusionCuller;
import org.embeddedt.embeddium.impl.render.chunk.occlusion.OcclusionNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RenderListManager.class, remap = false)
public interface RenderListManagerAccessor {

    @Invoker("getOcclusionNode")
    OcclusionNode nvidium$getOcclusionNode(int x, int y, int z);

    @Accessor("occlusionCuller")
    OcclusionCuller getOcclusionCuller();
}
