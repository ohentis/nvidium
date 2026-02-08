package me.cortex.nvidium.mixin.sodium;

import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererGetter;

@Mixin(value = CeleritasWorldRenderer.class, remap = false)
public abstract class MixinSodiumWorldRenderer implements INvidiumWorldRendererGetter {

    @Shadow
    public abstract RenderSectionManager getRenderSectionManager();

    // @Shadow
    // protected static void renderBlockEntity(PoseStack matrices, RenderBuffers bufferBuilders,
    // Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta,
    // MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher dispatcher,
    // BlockEntity entity, LocalPlayer player, LocalBooleanRef isGlowing) {}

    // @Inject(method = "setupTerrain", at = @At(value = "INVOKE", target =
    // "Lcom/gtnewhorizons/angelica/rendering/celeritas/AngelicaRenderSectionManager;needsUpdate()Z", shift =
    // At.Shift.BEFORE))
    // private void injectTerrainSetup(Viewport viewport, SimpleWorldRenderer.CameraState cameraState, int frame,
    // boolean spectator, boolean updateChunksImmediately, CallbackInfo ci) {
    // if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
    // ((INvidiumWorldRendererGetter)getRenderSectionManager()).getRenderer().update(cameraState, viewport, spectator);
    // }
    //
    // }

    // @Inject(method =
    // "renderBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;Lnet/minecraft/client/player/LocalPlayer;Lcom/llamalad7/mixinextras/sugar/ref/LocalBooleanRef;)V",
    // at = @At("HEAD"), cancellable = true, remap = true)
    // private void overrideEntityRenderer(PoseStack matrices, RenderBuffers bufferBuilders,
    // Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta,
    // MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher
    // blockEntityRenderer, LocalPlayer player, LocalBooleanRef isGlowing, CallbackInfo ci) {
    // if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
    // ci.cancel();
    // var sectionsWithEntities =
    // ((INvidiumWorldRendererGetter)renderSectionManager).getRenderer().getSectionsWithEntities();
    // for (var section : sectionsWithEntities) {
    // if (section.isDisposed() || section.getCulledBlockEntities() == null)
    // continue;
    // for (var entity : section.getCulledBlockEntities()) {
    // renderBlockEntity(matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate, x, y, z,
    // blockEntityRenderer, entity, player, isGlowing);
    // }
    // }
    // }
    // }

    @Override
    public NvidiumWorldRenderer nvidium$getRenderer() {
        if (Nvidium.IS_ENABLED) {
            return ((INvidiumWorldRendererGetter) getRenderSectionManager()).nvidium$getRenderer();
        } else {
            return null;
        }
    }
}
