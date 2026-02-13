package me.cortex.nvidium.mixin.sodium;

import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.terrain.SimpleWorldRenderer;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererGetter;

@Mixin(value = SimpleWorldRenderer.class, remap = false)
public abstract class MixinSimpleWorldRenderer implements INvidiumWorldRendererGetter {

    @Shadow
    public abstract RenderSectionManager getRenderSectionManager();

    @Inject(
        method = "setupTerrain",
        at = @At(
            value = "INVOKE",
            target = "Lorg/embeddedt/embeddium/impl/render/chunk/RenderSectionManager;needsUpdate()Z",
            shift = At.Shift.BEFORE))
    private void injectTerrainSetup(Viewport viewport, SimpleWorldRenderer.CameraState cameraState, int frame,
        boolean spectator, boolean updateChunksImmediately, CallbackInfo ci) {
        if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
            ((INvidiumWorldRendererGetter) getRenderSectionManager()).nvidium$getRenderer()
                .update(cameraState, viewport, spectator);
        }

    }

    @Override
    public NvidiumWorldRenderer nvidium$getRenderer() {
        if (Nvidium.IS_ENABLED) {
            return ((INvidiumWorldRendererGetter) getRenderSectionManager()).nvidium$getRenderer();
        } else {
            return null;
        }
    }
}
