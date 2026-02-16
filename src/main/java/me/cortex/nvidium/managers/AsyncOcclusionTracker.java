package me.cortex.nvidium.managers;

import static java.lang.Thread.MAX_PRIORITY;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import com.ventooth.beddium.config.ModuleConfig;
import me.cortex.nvidium.Nvidium;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;

import org.embeddedt.embeddium.impl.render.chunk.ChunkUpdateType;
import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionFlags;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkTaskOutput;
import org.embeddedt.embeddium.impl.render.chunk.compile.executor.ChunkJobResult;
import org.embeddedt.embeddium.impl.render.chunk.lists.RenderListManager;
import org.embeddedt.embeddium.impl.render.chunk.occlusion.OcclusionCuller;
import org.embeddedt.embeddium.impl.render.terrain.SimpleWorldRenderer;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.angelica.AngelicaMod;

import me.cortex.nvidium.RenderPipeline;
import me.cortex.nvidium.mixin.celeritas.RenderListManagerAccessor;
import me.cortex.nvidium.sodiumCompat.IRenderSectionExtension;

public class AsyncOcclusionTracker {

    private final OcclusionCuller occlusionCuller;
    private final Thread cullThread;
    private final World world;
    private final RenderListManager renderListManager;

    private volatile boolean running = true;
    private volatile int frame = 0;
    private volatile Viewport viewport = null;

    private final Semaphore framesAhead = new Semaphore(0);

    private final AtomicReference<List<RenderSection>> atomicBfsResult = new AtomicReference<>();
    private final AtomicReference<List<RenderSection>> blockEntitySectionsRef = new AtomicReference<>(
        new ArrayList<>());
    private final AtomicReference<TextureAtlasSprite[]> visibleAnimatedSpritesRef = new AtomicReference<>();

    private final ConcurrentLinkedDeque<ChunkJobResult<? extends ChunkTaskOutput>> outputRebuildQueue;

    private final float renderDistance;
    private volatile long iterationTimeMillis;
    private volatile boolean shouldUseOcclusionCulling = true;

    private volatile int chunkVisibilityCount = 0;

    public AsyncOcclusionTracker(int renderDistance, RenderListManager renderListManager, World world,
        ConcurrentLinkedDeque<ChunkJobResult<? extends ChunkTaskOutput>> outputRebuildQueue) {
        this.renderListManager = renderListManager;
        this.occlusionCuller = ((RenderListManagerAccessor) renderListManager).getOcclusionCuller();
        this.cullThread = new Thread(this::run);
        this.cullThread.setName("Cull thread");
        this.cullThread.setPriority(MAX_PRIORITY);
        this.cullThread.start();
        this.renderDistance = renderDistance * 16f;

        this.outputRebuildQueue = outputRebuildQueue;
        this.world = world;
    }

    private void run() {

        while (running) {
            framesAhead.acquireUninterruptibly();
            if (!running) break;
            long startTime = System.currentTimeMillis();
            final boolean animateVisibleSpritesOnly;
            if(Nvidium.isWithAngelica()){
                animateVisibleSpritesOnly = AngelicaMod.options().performance.animateOnlyVisibleTextures;
            } else if (Nvidium.isWithBeddium()) {
                animateVisibleSpritesOnly = ModuleConfig.ConservativeAnimatedTextures;
            } else {
                animateVisibleSpritesOnly = false;
            }
            // The reason for batching is so that ordering is strongly defined
            List<RenderSection> chunkUpdates = new ArrayList<>();
            List<RenderSection> blockEntitySections = new ArrayList<>();
            Set<TextureAtlasSprite> animatedSpriteSet = animateVisibleSpritesOnly ? new HashSet<>() : null;
            int[] visibleGeometryCounter = new int[1];
            final OcclusionCuller.Visitor visitor = (node, flag) -> {
                var section = node.getRenderSection();
                if (section.getPendingUpdate() != null && section.getBuildCancellationToken() == null) {
                    if ((!((IRenderSectionExtension) section).nvidium$isSubmittedRebuild())
                        && !((IRenderSectionExtension) section).nvidium$isSeen()) {// If it is in submission queue or
                                                                                   // seen dont
                        // enqueue
                        // Set that the section has been seen
                        ((IRenderSectionExtension) section).nvidium$isSeen(true);
                        chunkUpdates.add(section);
                    }
                }

                if ((section.getVisualsServiceFlags() & (1 << RenderSectionFlags.HAS_BLOCK_GEOMETRY)) != 0) {
                    visibleGeometryCounter[0]++;
                }

                if ((section.getVisualsServiceFlags() & (1 << RenderSectionFlags.HAS_BLOCK_ENTITIES)) != 0
                    && section.getSquaredDistance(
                        viewport.getChunkCoord()
                            .x(),
                        viewport.getChunkCoord()
                            .y(),
                        viewport.getChunkCoord()
                            .z())
                        < 1024) {
                    blockEntitySections.add(section);
                }
                // if (animateVisibleSpritesOnly && section.getSquaredDistance(
                // viewport.getChunkCoord()
                // .x(),
                // viewport.getChunkCoord()
                // .y(),
                // viewport.getChunkCoord()
                // .z())
                // < 1024) {// 32 rd max chunk distance (i.e. only animate sprites up to 32 chunks away)
                // BuiltRenderSectionData context = section.getBuiltContext();
                // if (context instanceof MinecraftBuiltRenderSectionData) {
                // MinecraftBuiltRenderSectionData<TextureAtlasSprite, ?> mcData =
                // (MinecraftBuiltRenderSectionData<TextureAtlasSprite, ?>) context;
                // Collection<TextureAtlasSprite> sprites = mcData.animatedSprites;
                //
                // animatedSpriteSet.addAll(sprites);
                // }
                // }

            };

            frame++;
            float searchDistance = this.getSearchDistance();
            boolean useOcclusionCulling = this.shouldUseOcclusionCulling;
            try {
                this.occlusionCuller.findVisible(visitor, viewport, searchDistance, useOcclusionCulling, frame);
            } catch (Throwable e) {
                System.err.println("Error doing traversal");
                e.printStackTrace();
            }

            if (!chunkUpdates.isEmpty()) {
                var previous = atomicBfsResult.getAndSet(chunkUpdates);
                if (previous != null) {
                    // We need to cleanup our state from a previous iteration
                    for (var section : previous) {
                        if (section.isDisposed()) continue;
                        // Reset that it hasnt been seen
                        ((IRenderSectionExtension) section).nvidium$isSeen(false);
                    }
                }
            }
            this.chunkVisibilityCount = visibleGeometryCounter[0];
            blockEntitySectionsRef.set(blockEntitySections);
            visibleAnimatedSpritesRef
                .set(animatedSpriteSet == null ? null : animatedSpriteSet.toArray(new TextureAtlasSprite[0]));
            iterationTimeMillis = System.currentTimeMillis() - startTime;
        }
    }

    public final void update(Viewport viewport, SimpleWorldRenderer.CameraState camera, boolean spectator) {
        this.shouldUseOcclusionCulling = this.shouldUseOcclusionCulling(camera, spectator);

        this.viewport = viewport;

        if (framesAhead.availablePermits() < 5) {// This stops a runaway when the traversal time is greater than
                                                 // frametime
            framesAhead.release();
        }

        var bfsResult = atomicBfsResult.getAndSet(null);
        if (bfsResult != null) {
            for (var section : bfsResult) {
                if (section.isDisposed()) continue;
                var type = section.getPendingUpdate();
                if (type != null && section.getBuildCancellationToken() == null) {
                    var queue = renderListManager.getRebuildLists()
                        .byUpdateType()
                        .get(type);
                    if (queue.size() < type.getMaximumQueueSize()) {
                        ((IRenderSectionExtension) section).nvidium$isSubmittedRebuild(true);
                        queue.add(section);
                    }
                }
                // Reset that the section has not been seen (whether its been submitted to the queue or not)
                ((IRenderSectionExtension) section).nvidium$isSeen(false);
            }
        }
    }

    public void delete() {
        running = false;
        framesAhead.release(1000);
        try {
            cullThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private float getSearchDistance() {
        return renderDistance;
    }

    private float getSearchDistance2() {
        float distance;
        if (AngelicaMod.options().performance.useFogOcclusion) {
            distance = this.getEffectiveRenderDistance();
        } else {
            distance = this.getRenderDistance();
        }

        return distance;
    }

    private boolean shouldUseOcclusionCulling(SimpleWorldRenderer.CameraState camera, boolean spectator) {

        boolean useOcclusionCulling;
        if (spectator && this.world.getBlock((int) camera.x(), (int) camera.y(), (int) camera.z())
            .isOpaqueCube()) {
            useOcclusionCulling = false;
        } else {
            useOcclusionCulling = AngelicaMod.options().performance.useOcclusionCulling;
        }

        return useOcclusionCulling;
    }

    private float getEffectiveRenderDistance() {

        float[] color = RenderPipeline.renderSystem.getShaderFogColor();
        float distance = RenderPipeline.renderSystem.getShaderFogEnd();
        float renderDistance = this.getRenderDistance();
        return !(color[3] == 1.0F) ? renderDistance : Math.min(renderDistance, distance + 0.5F);
    }

    private float getRenderDistance() {
        return (float) this.renderDistance;
    }

    public int getFrame() {
        return frame;
    }

    public List<RenderSection> getLatestSectionsWithEntities() {
        return blockEntitySectionsRef.get();
    }

    @Nullable
    public TextureAtlasSprite[] getVisibleAnimatedSprites() {
        return visibleAnimatedSpritesRef.get();
    }

    public long getIterationTime() {
        return this.iterationTimeMillis;
    }

    public int[] getBuildQueueSizes() {
        var ret = new int[ChunkUpdateType.values().length];
        for (var type : ChunkUpdateType.values()) {
            ret[type.ordinal()] = this.renderListManager.getRebuildLists()
                .byUpdateType()
                .get(type)
                .size();
        }
        return ret;
    }

    public int getLastVisibilityCount() {
        return this.chunkVisibilityCount;
    }
}
