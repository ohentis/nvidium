package me.cortex.nvidium.gl;

import static org.lwjgl.opengl.EXTMeshShader.glDrawMeshTasksEXT;
import static org.lwjgl.opengl.EXTMeshShader.glMultiDrawMeshTasksIndirectEXT;
import static org.lwjgl.opengl.NVMeshShader.glDrawMeshTasksNV;
import static org.lwjgl.opengl.NVMeshShader.glMultiDrawMeshTasksIndirectNV;

import org.lwjgl.opengl.GL;

import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
public class MeshShaderDispatcher {

    public static final MeshShaderDispatcher INSTANCE = new MeshShaderDispatcher();
    private final boolean isEXT;

    public MeshShaderDispatcher() {
        GL.getCapabilities();
        this.isEXT = GL.getCapabilities().GL_EXT_mesh_shader;
    }

    public void drawMeshTasks(int first, int count) {
        if (isEXT) {
            glDrawMeshTasksEXT(count, 1, 1);
        } else {
            glDrawMeshTasksNV(first, count);
        }
    }

    public void multiDrawMeshTasksIndirect(long indirectOffset, int drawCount, int stride) {
        if (isEXT) {
            glMultiDrawMeshTasksIndirectEXT(indirectOffset, drawCount, stride);
        } else {
            glMultiDrawMeshTasksIndirectNV(indirectOffset, drawCount, stride);
        }
    }
}
