package me.cortex.nvidium.sodiumCompat;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.joml.Vector3d;

import me.cortex.nvidium.NvidiumWorldRenderer;

public interface ISodiumCalls {

    NvidiumWorldRenderer getWorldRenderer();

    boolean getAnimateOnlyVisibleTextures();

    boolean getUseFogOcclusion();

    boolean getUseOcclusionCulling();

    TerrainRenderPass getTranslucentPass();

    TerrainRenderPass getSolidPass();

    TerrainRenderPass getCutoutPass();

    void markSpriteActive(TextureAtlasSprite sprite);

    int getEffectiveRenderDistance();

    void setTexture(int textureId, int bindingPoint);

    Vector3d getCameraPosition();

    void setTranslucencySorting(boolean enabled);

    int getCpuRenderAheadLimit();

    boolean getUseBlockFaceCulling();

    void enableBlend();

    void disableBlend();

    void blendFuncSeperate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha);

    ChunkVertexType getChunkVertexType();

    RenderSectionManager getRenderSectionManager();

}
