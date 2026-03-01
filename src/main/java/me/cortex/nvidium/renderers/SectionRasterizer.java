package me.cortex.nvidium.renderers;

import static me.cortex.nvidium.gl.shader.ShaderType.*;
import static org.lwjgl.opengl.NVMeshShader.glDrawMeshTasksNV;

import me.cortex.nvidium.gl.MeshShaderDispatcher;
import net.minecraft.util.ResourceLocation;

import me.cortex.nvidium.gl.shader.Shader;
import me.cortex.nvidium.sodiumCompat.ShaderLoader;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
public class SectionRasterizer extends Phase {

    private final Shader shader = Shader.make()
        .addSource(TASK, ShaderLoader.parse(new ResourceLocation("nvidium", "occlusion/section_raster/task.glsl")))
        .addSource(MESH, ShaderLoader.parse(new ResourceLocation("nvidium", "occlusion/section_raster/mesh.glsl")))
        .addSource(
            FRAGMENT,
            ShaderLoader.parse(new ResourceLocation("nvidium", "occlusion/section_raster/fragment.glsl")))
        .compile();

    public void raster(int regionCount) {
        shader.bind();
        MeshShaderDispatcher.INSTANCE.drawMeshTasks(0,regionCount);
    }

    public void delete() {
        shader.delete();
    }
}
