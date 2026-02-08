package me.cortex.nvidium.mixin.sodium;

import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.cortex.nvidium.sodiumCompat.IRepackagedResult;
import me.cortex.nvidium.sodiumCompat.RepackagedSectionOutput;

@Mixin(value = ChunkBuildOutput.class, remap = false)
public class MixinChunkBuildOutput implements IRepackagedResult {

    @Unique
    private RepackagedSectionOutput nvidium$repackagedSectionOutput;

    public RepackagedSectionOutput nvidium$getOutput() {
        return nvidium$repackagedSectionOutput;
    }

    public void nvidium$set(RepackagedSectionOutput output) {
        nvidium$repackagedSectionOutput = output;
    }

    @Inject(method = "delete", at = @At("HEAD"))
    private void nvidium$cleanup(CallbackInfo ci) {
        if (nvidium$repackagedSectionOutput != null) {
            nvidium$repackagedSectionOutput.delete();
        }
    }
}
