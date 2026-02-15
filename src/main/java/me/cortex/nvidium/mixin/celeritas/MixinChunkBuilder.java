package me.cortex.nvidium.mixin.celeritas;

import java.util.List;

import org.embeddedt.embeddium.impl.render.chunk.compile.executor.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import me.cortex.nvidium.Nvidium;

@Mixin(value = ChunkBuilder.class, remap = false)
public class MixinChunkBuilder {

    @Redirect(method = "getSchedulingBudget", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int nvidium$moreSchedulingBudget(List<Thread> threads) {
        int budget = threads.size();
        if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
            budget *= 3;
        }
        return budget;
    }
}
