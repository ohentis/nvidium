package me.cortex.nvidium.mixin.celeritas;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.ventooth.beddium.config.ModuleConfig;
import com.ventooth.beddium.modules.ConservativeAnimatedTextures.ext.TextureAtlasSpriteExt;
import com.ventooth.beddium.modules.TerrainRendering.ArchaicRenderPassConfigurationBuilder;
import org.embeddedt.embeddium.impl.render.chunk.ChunkRenderMatrices;
import org.embeddedt.embeddium.impl.render.chunk.ChunkUpdateType;
import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.chunk.lists.ChunkRenderList;
import org.embeddedt.embeddium.impl.render.chunk.lists.RenderListManager;
import org.embeddedt.embeddium.impl.render.chunk.occlusion.OcclusionNode;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegion;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegionManager;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.embeddedt.embeddium.impl.util.iterator.ByteIterator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.gtnewhorizons.angelica.AngelicaMod;
import com.gtnewhorizons.angelica.rendering.celeritas.AngelicaRenderPassConfiguration;
import com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer;
import com.gtnewhorizons.angelica.rendering.celeritas.SpriteExtension;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererGetter;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererSetter;
import me.cortex.nvidium.sodiumCompat.IRenderSectionExtension;

@Mixin(value = RenderSectionManager.class, remap = false, priority = 1500) // Ensure priority over Iris so it doesn't
                                                                           // hijack our ChunkVertexFormat
public abstract class MixinRenderSectionManager implements INvidiumWorldRendererGetter {

    @Shadow
    @Final
    private RenderRegionManager regions;
    @Shadow
    @Final
    private Long2ReferenceMap<RenderSection> sectionByPosition;
    @Shadow
    @Final
    private int renderDistance;

    @Shadow
    protected abstract RenderListManager getCurrentRenderListManager();

    @Unique
    public NvidiumWorldRenderer nvidium$renderer;
    @Unique
    private Viewport nvidium$viewport;

    @Unique
    public RenderRegionManager nvidium$getRegions() {
        return regions;
    }

    @Inject(method = "destroy", at = @At("TAIL"))
    private void nvidium$destroy(CallbackInfo ci) {
        if (Nvidium.IS_ENABLED) {
            if (nvidium$renderer == null) throw new IllegalStateException("Pipeline already destroyed");
            ((INvidiumWorldRendererSetter) regions).nvidium$setWorldRenderer(null);
            nvidium$renderer.delete();
            nvidium$renderer = null;
        }
    }

    @Redirect(
        method = "onSectionRemoved",
        at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/render/chunk/RenderSection;delete()V"))
    private void nvidium$deleteSection(RenderSection section) {
        if (Nvidium.IS_ENABLED) {
            if (Nvidium.config.region_keep_distance == 32
                || Nvidium.config.region_keep_distance <= (Nvidium.isWithAngelica()?CeleritasWorldRenderer.getInstance().getEffectiveRenderDistance(): com.ventooth.beddium.modules.TerrainRendering.CeleritasWorldRenderer.getEffectiveRenderDistance())) {
                nvidium$renderer.deleteSection(section);
            }
        }
        section.delete();
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void nvidium$trackViewport(Viewport positionedViewport, int frame, boolean spectator, CallbackInfo ci) {
        this.nvidium$viewport = positionedViewport;
    }

    @Inject(method = "renderLayer", at = @At("HEAD"), cancellable = true)
    public void nvidium$renderLayer(ChunkRenderMatrices matrices, TerrainRenderPass pass,
        CameraTransform occlusionCamera, CameraTransform camera, CallbackInfo ci) {
        if (Nvidium.IS_ENABLED) {
            ci.cancel();
            pass.startDrawing();
            if (pass == (Nvidium.isWithAngelica()?AngelicaRenderPassConfiguration.SOLID_PASS: ArchaicRenderPassConfigurationBuilder.SOLID_PASS)) {
                nvidium$renderer.renderFrame(nvidium$viewport, matrices, camera.x, camera.y, camera.z);
            } else if (pass == (Nvidium.isWithAngelica()?AngelicaRenderPassConfiguration.TRANSLUCENT_PASS: ArchaicRenderPassConfigurationBuilder.TRANSLUCENT_PASS)) {
                nvidium$renderer.renderTranslucent();
            }
            pass.endDrawing();
        }
    }

    @Inject(method = "getDebugStrings", at = @At("HEAD"), cancellable = true)
    private void nvidium$redirectDebug(CallbackInfoReturnable<Collection<String>> cir) {
        if (Nvidium.IS_ENABLED) {
            var debugStrings = new ArrayList<String>();
            nvidium$renderer.addDebugInfo(debugStrings);
            cir.setReturnValue(debugStrings);
            cir.cancel();
        }
    }

    @Override
    public NvidiumWorldRenderer nvidium$getRenderer() {
        return nvidium$renderer;
    }

    @Inject(method = "createTerrainRenderList", at = @At("HEAD"), cancellable = true)
    private void nvidium$redirectTerrainRenderList(Viewport viewport, int frame, boolean spectator, CallbackInfo ci) {
        if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
            // ci.cancel();
        }
    }

    @Redirect(
        method = "submitRebuildTasks(Lorg/embeddedt/embeddium/impl/render/chunk/compile/executor/ChunkJobCollector;Lorg/embeddedt/embeddium/impl/render/chunk/ChunkUpdateType;)V",
        at = @At(
            value = "INVOKE",
            target = "Lorg/embeddedt/embeddium/impl/render/chunk/RenderSection;setPendingUpdate(Lorg/embeddedt/embeddium/impl/render/chunk/ChunkUpdateType;)V"))
    private void nvidium$injectEnqueueFalse(RenderSection instance, ChunkUpdateType type) {
        instance.setPendingUpdate(type);
        if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
            // We need to reset the fact that its been submitted to the rebuild queue from the build queue
            ((IRenderSectionExtension) instance).nvidium$isSubmittedRebuild(false);
        }
    }

    @Unique
    private boolean nvidium$isSectionVisibleBfs(OcclusionNode section) {
        // The reason why this is done is that since the bfs search is async it could be updating the frame counter with
        // the next frame
        // while some sections that arnt updated/ticked yet still have the old frame id
        int delta = Math.abs(section.getLastVisibleFrame() - nvidium$renderer.getAsyncFrameId());
        return delta <= 1;
    }

    @Inject(method = "isSectionVisible", at = @At("TAIL"), cancellable = true)
    private void nvidium$overrideIsSectionVisible(int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
            cir.setReturnValue(
                nvidium$isSectionVisibleBfs(
                    ((RenderListManagerAccessor) getCurrentRenderListManager()).nvidium$getOcclusionNode(x, y, z)));
        }
    }

    @Inject(method = "tickVisibleRenders", at = @At("HEAD"), cancellable = true)
    private void nvidium$redirectAnimatedSpriteUpdates(CallbackInfo ci) {

        if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs
            && (Nvidium.isWithAngelica()?AngelicaMod.options().performance.animateOnlyVisibleTextures: ModuleConfig.ConservativeAnimatedTextures)) {
            // ci.cancel();
            var sprites = nvidium$renderer.getAnimatedSpriteSet();
            if (sprites == null) {
                return;
            }
            for (var sprite : sprites) {
                if(Nvidium.isWithAngelica()) {
                    ((SpriteExtension) sprite).celeritas$markActive();
                } else {
                    ((TextureAtlasSpriteExt) sprite).celeritas$markActive();
                }

            }
        }

    }

    @Inject(
        method = "scheduleSectionForRebuild",
        at = @At(
            value = "INVOKE",
            target = "Lorg/embeddedt/embeddium/impl/render/chunk/RenderSection;requestUpdate(Lorg/embeddedt/embeddium/impl/render/chunk/ChunkUpdateType;)Z",
            shift = At.Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void instantReschedule(int x, int y, int z, boolean important, CallbackInfo ci, RenderSection section,
        ChunkUpdateType pendingUpdate) {
        // this might result in the section being enqueued multiple times, if this gets executed,
        // and the async search sees it at the exactly wrong moment
        // This is a problem when sodium translucency sorting is enabled since translucentData.getGeometryPlanes()
        // can be null on the second ChunkBuildOutput resulting in a NPE
        if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
            var queue = getCurrentRenderListManager().getRebuildLists()
                .byUpdateType()
                .get(pendingUpdate);
            if (nvidium$isSectionVisibleBfs(
                ((RenderListManagerAccessor) getCurrentRenderListManager()).nvidium$getOcclusionNode(x, y, z))
                && queue.size() < pendingUpdate.getMaximumQueueSize()
                && !queue.contains(section)) {
                // ((IRenderSectionExtension)section).isSubmittedRebuild(true);
                queue.add(section);
            }
        }
    }

    @Inject(
        method = "scheduleTranslucencyUpdates",
        at = @At(
            value = "INVOKE",
            target = "Lorg/embeddedt/embeddium/impl/render/chunk/RenderSection;setPendingUpdate(Lorg/embeddedt/embeddium/impl/render/chunk/ChunkUpdateType;)V"),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void promoteScheduleSort(int camSectionX, int camSectionY, int camSectionZ, CallbackInfo ci,
        RenderListManager renderListManager, Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists,
        ArrayDeque sortRebuildList, ArrayDeque importantSortRebuildList, boolean allowImportant,
        TerrainRenderPass translucentPass, Iterator it, ChunkRenderList entry, RenderRegion region,
        ByteIterator sectionIterator, RenderSection section, ChunkUpdateType update, double dx, double dy, double dz,
        double camDelta, boolean cameraChangedSection) {
        if (Nvidium.IS_ENABLED && section.getPendingUpdate() != null && update != section.getPendingUpdate()) {
            // The sorter promoted our task, we need to change the taskList
            rebuildLists.get(section.getPendingUpdate())
                .remove(section);
            rebuildLists.get(update)
                .add(section);
        }
    }

    @Inject(method = "getVisibleChunkCount", at = @At("HEAD"), cancellable = true)
    private void nvidium$injectVisibilityCount(CallbackInfoReturnable<Integer> cir) {
        if (Nvidium.IS_ENABLED && Nvidium.config.async_bfs) {
            cir.setReturnValue(this.nvidium$renderer.getAsyncBfsVisibilityCount());
        }
    }

    @Unique
    public Long2ReferenceMap<RenderSection> nvidium$getSectionByPosition() {
        return sectionByPosition;
    }

}
