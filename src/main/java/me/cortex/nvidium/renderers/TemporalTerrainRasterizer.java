package me.cortex.nvidium.renderers;

import static me.cortex.nvidium.gl.shader.ShaderType.*;
import static org.lwjgl.opengl.GL.getCapabilities;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL33.glGenSamplers;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GL45C;

import me.cortex.nvidium.gl.MeshShaderDispatcher;
import me.cortex.nvidium.gl.shader.Shader;
import me.cortex.nvidium.mixin.minecraft.EntityRendererAccessor;
import me.cortex.nvidium.sodiumCompat.ShaderLoader;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
public class TemporalTerrainRasterizer extends Phase {

    private final int blockSampler = glGenSamplers();
    private final int lightSampler = glGenSamplers();
    private final Shader shader = Shader.make()
        .addSource(getCapabilities().GL_EXT_mesh_shader ? TASK_EXT : TASK_NV, ShaderLoader.parse(new ResourceLocation("nvidium", "terrain/temporal_task.glsl")))
        .addSource(getCapabilities().GL_EXT_mesh_shader ? MESH_EXT : MESH_NV, ShaderLoader.parse(new ResourceLocation("nvidium", "terrain/mesh.glsl")))
        .addSource(FRAGMENT, ShaderLoader.parse(new ResourceLocation("nvidium", "terrain/frag.frag")))
        .compile();

    public TemporalTerrainRasterizer() {
        GL45C.glSamplerParameteri(blockSampler, GL45C.GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        GL45C.glSamplerParameteri(blockSampler, GL45C.GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        GL45C.glSamplerParameteri(blockSampler, GL45C.GL_TEXTURE_MIN_LOD, 0);
        GL45C.glSamplerParameteri(blockSampler, GL45C.GL_TEXTURE_MAX_LOD, 4);
        GL45C.glSamplerParameteri(lightSampler, GL_TEXTURE_WRAP_T, GL12C.GL_CLAMP_TO_EDGE);
        GL45C.glSamplerParameteri(lightSampler, GL_TEXTURE_WRAP_S, GL12C.GL_CLAMP_TO_EDGE);
        GL45C.glSamplerParameteri(lightSampler, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GL45C.glSamplerParameteri(lightSampler, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }

    public void raster(int regionCount, int commandBufferId) {
        shader.bind();

        int blockId = Minecraft.getMinecraft()
            .getTextureManager()
            .getTexture(new ResourceLocation("minecraft", "textures/atlas/blocks.png"))
            .getGlTextureId();
        int lightId = ((EntityRendererAccessor) Minecraft.getMinecraft().entityRenderer).nvidium$getLightmapTexture()
            .getGlTextureId();

        GL45C.glBindTextureUnit(0, blockId);
        GL45C.glBindSampler(0, blockSampler);

        GL45C.glBindTextureUnit(1, lightId);
        GL45C.glBindSampler(1, lightSampler);

        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, commandBufferId);
        MeshShaderDispatcher.INSTANCE.multiDrawMeshTasksIndirect(0, regionCount, 16);
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
