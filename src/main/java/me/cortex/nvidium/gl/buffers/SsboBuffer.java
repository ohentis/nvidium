package me.cortex.nvidium.gl.buffers;

import static org.lwjgl.opengl.ARBDirectStateAccess.glNamedBufferStorage;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;

import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.GL30C;

import me.cortex.nvidium.gl.GlObject;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
public class SsboBuffer extends GlObject implements Buffer {

    private final long size;

    public SsboBuffer(long size) {
        super(ARBDirectStateAccess.glCreateBuffers());
        this.size = size;
        glNamedBufferStorage(id, size, 0);
        int err = GL30C.glGetError();
        if (err != 0) {
            throw new IllegalStateException("glNamedBufferStorage failed: " + err + " size=" + size);
        }
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
