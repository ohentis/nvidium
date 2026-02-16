package me.cortex.nvidium.mixin.beddium;

import com.ventooth.beddium.api.task.SimpleChunkBuilderMeshingTask;
import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.sodiumCompat.IRepackagedResult;
import me.cortex.nvidium.sodiumCompat.SodiumResultCompatibility;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildOutput;
import org.embeddedt.embeddium.impl.util.task.CancellationToken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value= SimpleChunkBuilderMeshingTask.class, remap = false)
public class MixinSimpleChunkBuilderMeshingTask {
    @Inject(
        method = "execute(Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildContext;Lorg/embeddedt/embeddium/impl/util/task/CancellationToken;)Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildOutput;",
        at = @At("TAIL"))
    private void nvidium$repackageResults(ChunkBuildContext buildContext, CancellationToken cancellationToken,
                                          CallbackInfoReturnable<ChunkBuildOutput> cir) {
        if (Nvidium.IS_ENABLED) {
            var result = cir.getReturnValue();
            if (result != null) {
                ((IRepackagedResult) result).nvidium$set(SodiumResultCompatibility.repackage(result));
            }
        }
    }

}
