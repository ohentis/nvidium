package me.cortex.nvidium.mixin.sodium;

import org.embeddedt.embeddium.impl.render.chunk.compile.executor.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ChunkBuilder.class, remap = false)
public class MixinChunkBuilder {
    // Not messing with this until it breaks stuff lol
    // @Redirect(method = "getTotalRemainingBudget", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    // private int moreSchedulingBudget(List<Thread> threads) {
    // int budget = threads.size();
    // if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
    // budget *= 3;
    // }
    // return budget;
    // }
}
