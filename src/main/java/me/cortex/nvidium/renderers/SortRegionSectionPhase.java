package me.cortex.nvidium.renderers;

import static me.cortex.nvidium.gl.shader.ShaderType.*;
import static org.lwjgl.opengl.GL43C.glDispatchCompute;

import net.minecraft.util.ResourceLocation;

import me.cortex.nvidium.gl.shader.Shader;
import me.cortex.nvidium.sodiumCompat.ShaderLoader;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
public class SortRegionSectionPhase extends Phase {

    private final Shader shader = Shader.make()
        .addSource(COMPUTE, ShaderLoader.parse(new ResourceLocation("nvidium", "sorting/region_section_sorter.comp")))
        .compile();

    public SortRegionSectionPhase() {}

    public void dispatch(int sortingRegionCount) {
        shader.bind();
        glDispatchCompute(sortingRegionCount, 1, 1);
    }

    public void delete() {
        shader.delete();
    }
}
