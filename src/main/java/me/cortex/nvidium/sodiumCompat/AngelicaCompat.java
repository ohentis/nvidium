package me.cortex.nvidium.sodiumCompat;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL13;

import com.gtnewhorizons.angelica.AngelicaMod;
import com.gtnewhorizons.angelica.compat.mojang.Camera;
import com.gtnewhorizons.angelica.glsm.GLStateManager;
import com.gtnewhorizons.angelica.rendering.celeritas.AngelicaRenderPassConfiguration;
import com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer;
import com.gtnewhorizons.angelica.rendering.celeritas.SpriteExtension;

import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.mixin.angelica.CameraAccessor;
import me.cortex.nvidium.mixin.angelica.CeleritasWorldRendererAccessor;

public class AngelicaCompat implements ISodiumCalls {

    @Override
    public NvidiumWorldRenderer getWorldRenderer() {
        return ((INvidiumWorldRendererGetter) CeleritasWorldRenderer.getInstance()).nvidium$getRenderer();
    }

    @Override
    public boolean getAnimateOnlyVisibleTextures() {
        return AngelicaMod.options().performance.animateOnlyVisibleTextures;
    }

    @Override
    public boolean getUseFogOcclusion() {
        return AngelicaMod.options().performance.useFogOcclusion;
    }

    @Override
    public boolean getUseOcclusionCulling() {
        return AngelicaMod.options().performance.useOcclusionCulling;
    }

    @Override
    public TerrainRenderPass getTranslucentPass() {
        return AngelicaRenderPassConfiguration.TRANSLUCENT_PASS;
    }

    @Override
    public TerrainRenderPass getSolidPass() {
        return AngelicaRenderPassConfiguration.SOLID_PASS;
    }

    @Override
    public TerrainRenderPass getCutoutPass() {
        return AngelicaRenderPassConfiguration.CUTOUT_MIPPED_PASS;
    }

    @Override
    public void markSpriteActive(TextureAtlasSprite sprite) {
        ((SpriteExtension) sprite).celeritas$markActive();
    }

    @Override
    public int getEffectiveRenderDistance() {
        return CeleritasWorldRenderer.getInstance()
            .getEffectiveRenderDistance();
    }

    @Override
    public void setTexture(int textureId, int bindingPoint) {
        GLStateManager.glActiveTexture(GL13.GL_TEXTURE0 + bindingPoint);
        GLStateManager.glBindTexture(GL_TEXTURE_2D, textureId);
    }

    @Override
    public Vector3d getCameraPosition() {
        return ((CameraAccessor) Camera.INSTANCE).nvidium$getPos();
    }

    @Override
    public void setTranslucencySorting(boolean enabled) {
        AngelicaMod.options().performance.translucencySorting = enabled;
    }

    @Override
    public int getCpuRenderAheadLimit() {
        return CeleritasWorldRenderer.getInstance()
            .getEffectiveRenderDistance();
    }

    @Override
    public boolean getUseBlockFaceCulling() {
        return AngelicaMod.options().performance.useBlockFaceCulling;
    }

    @Override
    public void enableBlend() {
        GLStateManager.enableBlend();
    }

    @Override
    public void disableBlend() {
        GLStateManager.disableBlend();
    }

    @Override
    public void blendFuncSeperate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
        GLStateManager.tryBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }

    @Override
    public ChunkVertexType getChunkVertexType() {
        return ((CeleritasWorldRendererAccessor) com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer
            .getInstance()).nvidium$chooseVertexType();
    }

    @Override
    public RenderSectionManager getRenderSectionManager() {
        return CeleritasWorldRenderer.getInstance()
            .getRenderSectionManager();
    }
}
