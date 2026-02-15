package me.cortex.nvidium.mixin.celeritas;

import org.embeddedt.embeddium.impl.render.chunk.vertex.format.impl.CompactChunkVertex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CompactChunkVertex.class, remap = false)
public interface CompactChunkVertexAccessor {

    @Accessor("TEXTURE_MAX_VALUE")
    static int nvidium$getTextureMaxValue() {
        throw new UnsupportedOperationException();
    }
}
