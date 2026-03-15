package me.cortex.nvidium.gl.buffers;

import static org.lwjgl.opengl.ARBDirectStateAccess.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL44.GL_CLIENT_STORAGE_BIT;
import static org.lwjgl.opengl.GL44.GL_MAP_PERSISTENT_BIT;
import static org.lwjgl.opengl.NVShaderBufferLoad.*;
import static org.lwjgl.system.MemoryUtil.memByteBufferSafe;

import me.cortex.nvidium.gl.GlObject;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.GL30C;

import java.nio.ByteBuffer;

@Lwjgl3Aware
public class PersistentClientMappedBuffer extends GlObject implements IClientMappedBuffer {

    public final long addr;
    public final long size;
    public final ByteBuffer buffer;

    public PersistentClientMappedBuffer(long size) {
        super(glCreateBuffers());
        this.size = size;
        glNamedBufferStorage(id, size, GL_MAP_PERSISTENT_BIT | (GL_CLIENT_STORAGE_BIT | GL_MAP_WRITE_BIT));
        addr = nglMapNamedBufferRange(
            id,
            0,
            size,
            GL_MAP_PERSISTENT_BIT | (GL_MAP_UNSYNCHRONIZED_BIT | GL_MAP_FLUSH_EXPLICIT_BIT | GL_MAP_WRITE_BIT));
        buffer = memByteBufferSafe(addr, (int)size);
        int err = GL30C.glGetError();
        if (err != 0) {
            throw new IllegalStateException("nglMapNamedBufferRange failed: " + err + " size=" + size);
        }
    }

    @Override
    public long clientAddress() {
        return addr;
    }
    @Override
    public ByteBuffer clientBuffer() {return buffer.duplicate();}

    @Override
    public void delete() {
        super.free0();
        glUnmapNamedBuffer(id);
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
