package me.cortex.nvidium.mixin.celeritas;

import java.util.concurrent.ConcurrentLinkedDeque;

import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkTaskOutput;
import org.embeddedt.embeddium.impl.render.chunk.compile.executor.ChunkJobResult;
import org.embeddedt.embeddium.impl.render.chunk.lists.RenderListManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RenderSectionManager.class, remap = false)
public interface RenderSectionManagerAccessor {

    @Accessor("buildResults")
    public ConcurrentLinkedDeque<ChunkJobResult<? extends ChunkTaskOutput>> nvidium$getBuildResults();

    @Invoker("getCurrentRenderListManager")
    public RenderListManager nvidium$getCurrentRenderListManager();
}
