package me.cortex.nvidium.mixin.sodium;

import net.minecraft.client.multiplayer.WorldClient;

import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.render.chunk.RenderPassConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.angelica.rendering.celeritas.AngelicaRenderSectionManager;
import com.gtnewhorizons.angelica.rendering.celeritas.threading.ChunkTaskProvider;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.managers.AsyncOcclusionTracker;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererSetter;
import me.cortex.nvidium.sodiumCompat.IrisCheck;

@Mixin(value = AngelicaRenderSectionManager.class, remap = false)
public abstract class MixinAngelicaRenderSectionManager extends MixinRenderSectionManager {

    @Unique
    private static void nvidium$updateNvidiumIsEnabled() {
        Nvidium.IS_ENABLED = (!Nvidium.FORCE_DISABLE) && Nvidium.IS_COMPATIBLE && IrisCheck.checkIrisShouldDisable();

    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nvidium$init(RenderPassConfiguration<?> configuration, WorldClient world, int renderDistance,
        CommandList commandList, int minSection, int maxSection, int requestedThreads, ChunkTaskProvider taskProvider,
        CallbackInfo ci) {
        nvidium$updateNvidiumIsEnabled();
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
