package me.cortex.nvidium.mixin.celeritas;

import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.sodiumCompat.NvidiumCompactChunkVertex;

@Mixin(value = CeleritasWorldRenderer.class, remap = false)
public class MixinCeleritasWorldRenderer {

    @Inject(method = "chooseVertexType", at = @At("HEAD"), cancellable = true)
    private void nvidium$chooseVertexType(CallbackInfoReturnable<ChunkVertexType> cir) {
        Nvidium.updateNvidiumIsEnabled();
        if (Nvidium.IS_ENABLED && !Nvidium.config.use_sodium_vertex_format) {
            cir.setReturnValue(NvidiumCompactChunkVertex.INSTANCE);
            cir.cancel();
        }
    }
}
