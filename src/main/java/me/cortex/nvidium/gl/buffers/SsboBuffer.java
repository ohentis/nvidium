package me.cortex.nvidium.gl.buffers;

import me.cortex.nvidium.gl.GlObject;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;
import org.lwjgl.opengl.ARBDirectStateAccess;

import static org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferStorage;
import static org.lwjgl.opengl.GL30C.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL44.GL_MAP_PERSISTENT_BIT;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;


@Lwjgl3Aware
public class SsboBuffer extends GlObject implements Buffer {

    private final long size;

    public SsboBuffer(long size) {
        super(ARBDirectStateAccess.glCreateBuffers());
        this.size = size;
        glNamedBufferStorage(id, size, 0);
    }

    @Override
    public void delete() {
        super.free0();
        glDeleteBuffers(id);
    }

    @Override
    public void free() {
        this.delete();
    }

    @Override
    public long getSize() {
        return size;
    }
}
