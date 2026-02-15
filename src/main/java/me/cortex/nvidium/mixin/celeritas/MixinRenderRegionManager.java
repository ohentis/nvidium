package me.cortex.nvidium.mixin.celeritas;

import java.util.Collection;

import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkTaskOutput;
import org.embeddedt.embeddium.impl.render.chunk.compile.executor.ChunkJobResult;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererSetter;

@Mixin(value = RenderRegionManager.class, remap = false)
public abstract class MixinRenderRegionManager implements INvidiumWorldRendererSetter {

    @Unique
    private NvidiumWorldRenderer nvidium$renderer;

    @Inject(method = "uploadMeshes", at = @At("HEAD"), cancellable = true)
    private void nvidium$uploadMeshes(CommandList commandList,
        Collection<ChunkJobResult.Success<? extends ChunkTaskOutput>> results, Runnable graphUpdateTrigger,
        CallbackInfo ci) {
        if (Nvidium.IS_ENABLED) {
            for (ChunkJobResult.Success<? extends ChunkTaskOutput> result : results) {
                nvidium$renderer.uploadBuildResult(result.output());
            }
            ci.cancel();
        }
    }

    @Override
    public void nvidium$setWorldRenderer(NvidiumWorldRenderer renderer) {
        this.nvidium$renderer = renderer;
    }
}
