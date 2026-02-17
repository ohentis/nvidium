package me.cortex.nvidium.sodiumCompat;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import com.gtnewhorizons.angelica.AngelicaMod;
import com.gtnewhorizons.angelica.compat.mojang.Camera;
import com.gtnewhorizons.angelica.glsm.GLStateManager;
import com.gtnewhorizons.angelica.rendering.celeritas.AngelicaRenderPassConfiguration;
import com.gtnewhorizons.angelica.rendering.celeritas.SpriteExtension;
import com.ventooth.beddium.config.ModuleConfig;
import com.ventooth.beddium.modules.ConservativeAnimatedTextures.ext.TextureAtlasSpriteExt;
import com.ventooth.beddium.modules.TerrainRendering.ArchaicRenderPassConfigurationBuilder;
import com.ventooth.beddium.modules.TerrainRendering.CameraHelper;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.mixin.angelica.CameraAccessor;
import me.cortex.nvidium.mixin.beddium.ArchaicRenderSectionManagerAccessor;
import me.cortex.nvidium.mixin.minecraft.MinecraftAccessor;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
public class BeddiumAngelicaCompat {

    public static NvidiumWorldRenderer getWorldRenderer() {
        if (Nvidium.isWithAngelica()) {
            return ((INvidiumWorldRendererGetter) com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer
                .getInstance()).nvidium$getRenderer();
        } else if (Nvidium.isWithBeddium()) {
            return ((INvidiumWorldRendererGetter) com.ventooth.beddium.modules.TerrainRendering.CeleritasWorldRenderer
                .instance()).nvidium$getRenderer();
        } else {
            return null;
        }
    }

    public static boolean getAnimateOnlyVisibleTextures() {
        if (Nvidium.isWithAngelica()) {
            return AngelicaMod.options().performance.animateOnlyVisibleTextures;
        } else if (Nvidium.isWithBeddium()) {
            return ModuleConfig.ConservativeAnimatedTextures;
        } else {
            return false;
        }
    }

    public static boolean getUseFogOcclusion() {
        if (Nvidium.isWithAngelica()) {
            return AngelicaMod.options().performance.useFogOcclusion;
        } else if (Nvidium.isWithBeddium()) {

            return ((ArchaicRenderSectionManagerAccessor) com.ventooth.beddium.modules.TerrainRendering.CeleritasWorldRenderer
                .instance()
                .getRenderSectionManager()).nvidium$useFogOcclusion();
        } else {
            return false;
        }
    }

    public static boolean getUseOcclusionCulling() {
        if (Nvidium.isWithAngelica()) {
            return AngelicaMod.options().performance.useOcclusionCulling;
        } else return Nvidium.isWithBeddium();
    }

    public static TerrainRenderPass getTranslucentPass() {
        if (Nvidium.isWithAngelica()) {
            return AngelicaRenderPassConfiguration.TRANSLUCENT_PASS;
        } else if (Nvidium.isWithBeddium()) {
            return ArchaicRenderPassConfigurationBuilder.TRANSLUCENT_PASS;
        } else {
            return null;
        }
    }

    public static TerrainRenderPass getSolidPass() {
        if (Nvidium.isWithAngelica()) {
            return AngelicaRenderPassConfiguration.SOLID_PASS;
        } else if (Nvidium.isWithBeddium()) {
            return ArchaicRenderPassConfigurationBuilder.SOLID_PASS;
        } else {
            return null;
        }
    }

    public static TerrainRenderPass getCutoutPass() {
        if (Nvidium.isWithAngelica()) {
            return AngelicaRenderPassConfiguration.CUTOUT_MIPPED_PASS;
        } else if (Nvidium.isWithBeddium()) {
            return ArchaicRenderPassConfigurationBuilder.CUTOUT_MIPPED_PASS;
        } else {
            return null;
        }
    }

    public static void markSpriteActive(TextureAtlasSprite sprite) {
        if (Nvidium.isWithAngelica()) {
            ((SpriteExtension) sprite).celeritas$markActive();
        } else if (Nvidium.isWithBeddium()) {
            ((TextureAtlasSpriteExt) sprite).celeritas$markActive();
        }
    }

    public static int getEffectiveRenderDistance() {
        if (Nvidium.isWithAngelica()) {
            return com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer.getInstance()
                .getEffectiveRenderDistance();
        } else if (Nvidium.isWithBeddium()) {
            return com.ventooth.beddium.modules.TerrainRendering.CeleritasWorldRenderer.getEffectiveRenderDistance();
        } else {
            return 0;
        }
    }

    public static void setTexture(int textureId, int bindingPoint) {
        if (Nvidium.isWithAngelica()) {
            GLStateManager.glActiveTexture(GL13.GL_TEXTURE0 + bindingPoint);
            GLStateManager.glBindTexture(GL_TEXTURE_2D, textureId);
        } else if (Nvidium.isWithBeddium()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + bindingPoint);
            GL13.glBindTexture(GL_TEXTURE_2D, textureId);
        }
    }

    public static Vector3d getCameraPosition() {
        if (Nvidium.isWithAngelica()) {
            return ((CameraAccessor) Camera.INSTANCE).nvidium$getPos();
        } else if (Nvidium.isWithBeddium()) {
            return CameraHelper.getCurrentCameraPosition(
                ((MinecraftAccessor) Minecraft.getMinecraft()).nvidium$getTimer().renderPartialTicks);
        }
        return new Vector3d();
    }

    public static void setTranslucencySorting(boolean enabled) {
        if (Nvidium.isWithAngelica()) {
            AngelicaMod.options().performance.translucencySorting = enabled;
        }
    }

    public static int getCpuRenderAheadLimit() {
        if (Nvidium.isWithAngelica()) {
            return AngelicaMod.options().performance.cpuRenderAheadLimit;
        } else {
            return 0;
        }
    }

    public static boolean getUseBlockFaceCulling() {
        if (Nvidium.isWithAngelica()) {
            return AngelicaMod.options().performance.useBlockFaceCulling;
        } else if (Nvidium.isWithBeddium()) {
            return true;
        } else {
            return false;
        }
    }

    public static void enableBlend() {
        if (Nvidium.isWithAngelica()) {
            GLStateManager.enableBlend();
        } else if (Nvidium.isWithBeddium()) {
            GL13.glEnable(GL13.GL_BLEND);
        }
    }

    public static void disableBlend() {
        if (Nvidium.isWithAngelica()) {
            GLStateManager.disableBlend();
        } else if (Nvidium.isWithBeddium()) {
            GL13.glDisable(GL13.GL_BLEND);
        }
    }

    public static void blendFuncSeperate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
        if (Nvidium.isWithAngelica()) {
            GLStateManager.tryBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
        } else if (Nvidium.isWithBeddium()) {
            GL30.glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
        }
    }

}
