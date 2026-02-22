package me.cortex.nvidium.sodiumCompat;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import com.ventooth.beddium.config.ModuleConfig;
import com.ventooth.beddium.modules.ConservativeAnimatedTextures.ext.TextureAtlasSpriteExt;
import com.ventooth.beddium.modules.TerrainRendering.ArchaicRenderPassConfigurationBuilder;
import com.ventooth.beddium.modules.TerrainRendering.CameraHelper;
import com.ventooth.beddium.modules.TerrainRendering.CeleritasWorldRenderer;
import com.ventooth.beddium.modules.TerrainRendering.vertex.CompatibleChunkVertex;

import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.mixin.beddium.ArchaicRenderSectionManagerAccessor;
import me.cortex.nvidium.mixin.minecraft.MinecraftAccessor;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
public class BeddiumCompat implements ISodiumCalls {

    @Override
    public NvidiumWorldRenderer getWorldRenderer() {
        return ((INvidiumWorldRendererGetter) CeleritasWorldRenderer.instance()).nvidium$getRenderer();
    }

    @Override
    public boolean getAnimateOnlyVisibleTextures() {
        return ModuleConfig.ConservativeAnimatedTextures;
    }

    @Override
    public boolean getUseFogOcclusion() {
        return ((ArchaicRenderSectionManagerAccessor) CeleritasWorldRenderer.instance()).nvidium$useFogOcclusion();
    }

    @Override
    public boolean getUseOcclusionCulling() {
        return true;
    }

    @Override
    public TerrainRenderPass getTranslucentPass() {
        return ArchaicRenderPassConfigurationBuilder.TRANSLUCENT_PASS;
    }

    @Override
    public TerrainRenderPass getSolidPass() {
        return ArchaicRenderPassConfigurationBuilder.SOLID_PASS;
    }

    @Override
    public TerrainRenderPass getCutoutPass() {
        return ArchaicRenderPassConfigurationBuilder.CUTOUT_MIPPED_PASS;
    }

    @Override
    public void markSpriteActive(TextureAtlasSprite sprite) {
        ((TextureAtlasSpriteExt) sprite).celeritas$markActive();
    }

    @Override
    public int getEffectiveRenderDistance() {
        return CeleritasWorldRenderer.getEffectiveRenderDistance();
    }

    @Override
    public void setTexture(int textureId, int bindingPoint) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + bindingPoint);
        GL13.glBindTexture(GL_TEXTURE_2D, textureId);
    }

    @Override
    public Vector3d getCameraPosition() {
        return CameraHelper.getCurrentCameraPosition(
            ((MinecraftAccessor) Minecraft.getMinecraft()).nvidium$getTimer().renderPartialTicks);
    }

    public static boolean DO_TERRAIN_TRANSLUCENT_SORTING = true;

    @Override
    public void setTranslucencySorting(boolean enabled) {
        DO_TERRAIN_TRANSLUCENT_SORTING = enabled;
    }

    @Override
    public int getCpuRenderAheadLimit() {
        return 0;
    }

    @Override
    public boolean getUseBlockFaceCulling() {
        return true;
    }

    @Override
    public void enableBlend() {
        GL13.glEnable(GL13.GL_BLEND);
    }

    @Override
    public void disableBlend() {
        GL13.glDisable(GL13.GL_BLEND);
    }

    @Override
    public void blendFuncSeperate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
        GL30.glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }

    @Override
    public ChunkVertexType getChunkVertexType() {
        return CompatibleChunkVertex.get();
    }

    @Override
    public RenderSectionManager getRenderSectionManager() {
        return CeleritasWorldRenderer.instance()
            .getRenderSectionManager();
    }
}
