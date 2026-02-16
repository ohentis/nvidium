package me.cortex.nvidium.renderers;

import static me.cortex.nvidium.RenderPipeline.GL_DRAW_INDIRECT_ADDRESS_NV;
import static me.cortex.nvidium.gl.shader.ShaderType.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL33.glGenSamplers;
import static org.lwjgl.opengl.NVMeshShader.glMultiDrawMeshTasksIndirectNV;
import static org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glBufferAddressRangeNV;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.mixin.minecraft.EntityRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GL45C;

import com.gtnewhorizons.angelica.glsm.GLStateManager;

import me.cortex.nvidium.gl.shader.Shader;
import me.cortex.nvidium.sodiumCompat.ShaderLoader;
import me.cortex.nvidium.util.FrameTimeProfiler;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
public class PrimaryTerrainRasterizer extends Phase {

    private final int blockSampler = glGenSamplers();
    private final int lightSampler = glGenSamplers();
    private final Shader shader = Shader.make()
        .addSource(TASK, ShaderLoader.parse(new ResourceLocation("nvidium", "terrain/task.glsl")))
        .addSource(MESH, ShaderLoader.parse(new ResourceLocation("nvidium", "terrain/mesh.glsl")))
        .addSource(FRAGMENT, ShaderLoader.parse(new ResourceLocation("nvidium", "terrain/frag.frag")))
        .compile();

    public PrimaryTerrainRasterizer() {
        GL45C.glSamplerParameteri(blockSampler, GL45C.GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        GL45C.glSamplerParameteri(blockSampler, GL45C.GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        GL45C.glSamplerParameteri(blockSampler, GL45C.GL_TEXTURE_MIN_LOD, 0);
        GL45C.glSamplerParameteri(blockSampler, GL45C.GL_TEXTURE_MAX_LOD, 4);
        GL45C.glSamplerParameteri(lightSampler, GL_TEXTURE_WRAP_T, GL12C.GL_CLAMP_TO_EDGE);
        GL45C.glSamplerParameteri(lightSampler, GL_TEXTURE_WRAP_S, GL12C.GL_CLAMP_TO_EDGE);
        GL45C.glSamplerParameteri(lightSampler, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GL45C.glSamplerParameteri(lightSampler, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }

    private static void setTexture(int textureId, int bindingPoint) {
        if(Nvidium.isWithAngelica()) {
            GLStateManager.glActiveTexture(GL13.GL_TEXTURE0 + bindingPoint);
            GLStateManager.glBindTexture(GL_TEXTURE_2D, textureId);
        } else if (Nvidium.isWithBeddium()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + bindingPoint);
            GL13.glBindTexture(GL_TEXTURE_2D, textureId);
        }
    }

    public void raster(int regionCount, long commandAddr, FrameTimeProfiler frameTimeProfiler) {
        shader.bind();

        int blockId = Minecraft.getMinecraft()
            .getTextureManager()
            .getTexture(new ResourceLocation("minecraft", "textures/atlas/blocks.png"))
            .getGlTextureId();
        int lightId = ((EntityRendererAccessor) Minecraft.getMinecraft().entityRenderer).nvidium$getLightmapTexture()
            .getGlTextureId();

        GL45C.glBindSampler(0, blockSampler);
        GL45C.glBindSampler(1, lightSampler);
        setTexture(blockId, 0);
        setTexture(lightId, 1);

        glBufferAddressRangeNV(GL_DRAW_INDIRECT_ADDRESS_NV, 0, commandAddr, regionCount * 8L);// Bind the command buffer
        frameTimeProfiler.startQuery();
        glMultiDrawMeshTasksIndirectNV(0, regionCount, 0);
        frameTimeProfiler.endQuery();
        GL45C.glBindSampler(0, 0);
        GL45C.glBindSampler(1, 0);
    }

    public void delete() {
        GL45.glDeleteSamplers(blockSampler);
        GL45.glDeleteSamplers(lightSampler);
        shader.delete();
    }
}
