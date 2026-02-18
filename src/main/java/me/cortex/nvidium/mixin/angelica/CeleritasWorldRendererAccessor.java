package me.cortex.nvidium.mixin.angelica;

import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer;

@Mixin(value = CeleritasWorldRenderer.class, remap = false)
public interface CeleritasWorldRendererAccessor {

    @Invoker("chooseVertexType")
    public ChunkVertexType nvidium$chooseVertexType();
}
