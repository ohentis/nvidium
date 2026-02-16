package me.cortex.nvidium.mixin.beddium;

import com.ventooth.beddium.modules.TerrainRendering.ArchaicRenderSectionManager;
import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.managers.AsyncOcclusionTracker;
import me.cortex.nvidium.mixin.celeritas.MixinRenderSectionManager;
import me.cortex.nvidium.mixin.celeritas.RenderSectionManagerAccessor;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererSetter;
import net.minecraft.client.multiplayer.WorldClient;
import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.render.chunk.RenderPassConfiguration;
import org.embeddedt.embeddium.impl.render.chunk.lists.RenderListManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ArchaicRenderSectionManager.class, remap = false)
public abstract class MixinArchaicRenderSectionManager extends MixinRenderSectionManager {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void nvidium$init(RenderPassConfiguration<?> configuration, WorldClient world, int renderDistance, CommandList commandList, int minSection, int maxSection, int requestedThreads, boolean alwaysDeferChunkUpdates, CallbackInfo ci) {
        Nvidium.updateNvidiumIsEnabled();
        if (Nvidium.IS_ENABLED) {
            if (nvidium$renderer != null) throw new IllegalStateException("Cannot have multiple world renderers");
            nvidium$renderer = new NvidiumWorldRenderer(
                Nvidium.config.async_bfs
                    ? new AsyncOcclusionTracker(
                    renderDistance,
                    ((RenderSectionManagerAccessor) this).nvidium$getCurrentRenderListManager(),
                    world,
                    ((RenderSectionManagerAccessor) this).nvidium$getBuildResults())
                    : null);
            ((INvidiumWorldRendererSetter) nvidium$getRegions()).nvidium$setWorldRenderer(nvidium$renderer);
        }
    }
}
