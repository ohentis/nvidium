package me.cortex.nvidium.renderers;

import static me.cortex.nvidium.RenderPipeline.GL_DRAW_INDIRECT_ADDRESS_NV;
import static me.cortex.nvidium.gl.shader.ShaderType.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL33.glGenSamplers;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.NVMeshShader.glMultiDrawMeshTasksIndirectNV;
import static org.lwjgl.opengl.NVVertexBufferUnifiedMemory.glBufferAddressRangeNV;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GL45C;

import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.gl.shader.Shader;
import me.cortex.nvidium.mixin.minecraft.EntityRendererAccessor;
import me.cortex.nvidium.sodiumCompat.ShaderLoader;
import me.cortex.nvidium.util.FrameTimeProfiler;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
public class TranslucentTerrainRasterizer extends Phase {

    private final int blockSampler = glGenSamplers();
    private final int lightSampler = glGenSamplers();

    private final Shader shader = Shader.make()
        .addSource(TASK, ShaderLoader.parse(new ResourceLocation("nvidium", "terrain/translucent/task.glsl")))
        .addSource(MESH, ShaderLoader.parse(new ResourceLocation("nvidium", "terrain/translucent/mesh.glsl")))
        .addSource(
            FRAGMENT,
            ShaderLoader.parse(
                new ResourceLocation("nvidium", "terrain/frag.frag"),
                builder -> { builder.add("TRANSLUCENT_PASS"); }))
        .compile();

    public TranslucentTerrainRasterizer() {
        GL45C.glSamplerParameteri(blockSampler, GL45C.GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        GL45C.glSamplerParameteri(blockSampler, GL45C.GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        GL45C.glSamplerParameteri(blockSampler, GL45C.GL_TEXTURE_MIN_LOD, 0);
        GL45C.glSamplerParameteri(blockSampler, GL45C.GL_TEXTURE_MAX_LOD, 4);
        GL45C.glSamplerParameteri(lightSampler, GL_TEXTURE_WRAP_T, GL12C.GL_CLAMP_TO_EDGE);
        GL45C.glSamplerParameteri(lightSampler, GL_TEXTURE_WRAP_S, GL12C.GL_CLAMP_TO_EDGE);
        GL45C.glSamplerParameteri(lightSampler, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GL45C.glSamplerParameteri(lightSampler, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }

    // Translucency is rendered in a very cursed and incorrect way
    // it hijacks the unassigned indirect command dispatch and uses that to dispatch the translucent chunks as well
    public void raster(int regionCount, int commandBufferId, FrameTimeProfiler frameTimeProfiler) {
        shader.bind();

        int blockId = Minecraft.getMinecraft()
            .getTextureManager()
            .getTexture(new ResourceLocation("minecraft", "textures/atlas/blocks.png"))
            .getGlTextureId();
        int lightId = ((EntityRendererAccessor) Minecraft.getMinecraft().entityRenderer).nvidium$getLightmapTexture()
            .getGlTextureId();

        GL45C.glBindSampler(0, blockSampler);
        GL45C.glBindSampler(1, lightSampler);
        Nvidium.Compat.setTexture(blockId, 0);
        Nvidium.Compat.setTexture(lightId, 1);

        // the +8*6 is to offset to the unassigned dispatch
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, commandBufferId);
        frameTimeProfiler.startQuery();
        int err;
        if ((err = GL30C.glGetError()) != 0) {
            throw new IllegalStateException("GLERROR: " + err);
        }
        glMultiDrawMeshTasksIndirectNV(0, regionCount, 0);
        if ((err = GL30C.glGetError()) != 0) {
            throw new IllegalStateException("GLERROR: " + err);
        }
        frameTimeProfiler.endQuery();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);
        GL45C.glBindSampler(0, 0);
        GL45C.glBindSampler(1, 0);
    }

    public void delete() {
        GL45.glDeleteSamplers(blockSampler);
        GL45.glDeleteSamplers(lightSampler);
        shader.delete();
    }
}
