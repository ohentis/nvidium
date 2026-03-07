package me.cortex.nvidium.gl;

import static org.lwjgl.opengl.ARBDirectStateAccess.glCopyNamedBufferSubData;
import static org.lwjgl.opengl.ARBDirectStateAccess.glFlushMappedNamedBufferRange;
import static org.lwjgl.opengl.GL42C.glMemoryBarrier;

import me.cortex.nvidium.gl.buffers.Buffer;
import me.cortex.nvidium.gl.buffers.IClientMappedBuffer;
import me.cortex.nvidium.gl.buffers.PersistentClientMappedBuffer;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
public class RenderDevice {

    public PersistentClientMappedBuffer createClientMappedBuffer(long size) {
        return new PersistentClientMappedBuffer(size);
    }

    public void flush(IClientMappedBuffer buffer, long offset, int size) {
        int id = ((GlObject) buffer).getId();
        glFlushMappedNamedBufferRange(id, offset, size);
    }

    public void barrier(int flags) {
        glMemoryBarrier(flags);
    }

    public void copyBuffer(Buffer src, Buffer dst, long srcOffset, long dstOffset, long size) {
        glCopyNamedBufferSubData(((GlObject) src).getId(), ((GlObject) dst).getId(), srcOffset, dstOffset, size);
    }

}
