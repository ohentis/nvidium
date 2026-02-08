package me.cortex.nvidium.mixin.sodium;

import org.embeddedt.embeddium.impl.render.chunk.lists.RenderListManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = RenderListManager.class, remap = false)
public class RenderListManagerMixin {

    // @Unique
    // private boolean isSectionVisibleBfs(OcclusionNode node) {
    // // The reason why this is done is that since the bfs search is async it could be updating the frame counter with
    // // the next frame
    // // while some sections that arnt updated/ticked yet still have the old frame id
    // int delta = Math.abs(node.getLastVisibleFrame() - renderer.getAsyncFrameId());
    // return delta <= 1;
    // }
    //
    // @Inject(
    // method = "isSectionVisible",
    // at = @At(
    // value = "INVOKE",
    // target = "Lorg/embeddedt/embeddium/impl/render/chunk/occlusion/OcclusionNode;getLastVisibleFrame()I",
    // shift = At.Shift.BEFORE),
    // cancellable = true,
    // locals = LocalCapture.CAPTURE_FAILHARD)
    // private void redirectIsSectionVisible(int x, int y, int z, CallbackInfoReturnable<Boolean> cir,
    // OcclusionNode node) {
    // if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
    // cir.setReturnValue(isSectionVisibleBfs(node));
    // }
    // }
}
