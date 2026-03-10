package me.cortex.nvidium.gl.shader;

import static org.lwjgl.opengl.EXTMeshShader.GL_MESH_SHADER_EXT;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;
import static org.lwjgl.opengl.NVMeshShader.GL_MESH_SHADER_NV;
import static org.lwjgl.opengl.NVMeshShader.GL_TASK_SHADER_NV;

import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
public enum ShaderType {

    VERTEX(GL_VERTEX_SHADER),
    FRAGMENT(GL_FRAGMENT_SHADER),
    COMPUTE(GL_COMPUTE_SHADER),
    MESH_NV(GL_MESH_SHADER_NV),
    MESH_EXT(GL_MESH_SHADER_EXT),
    TASK_NV(GL_TASK_SHADER_NV),
    TASK_EXT(0x955A);

    public final int gl;

    ShaderType(int glEnum) {
        gl = glEnum;
    }
}
