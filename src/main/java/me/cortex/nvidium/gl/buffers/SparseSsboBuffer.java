package me.cortex.nvidium.gl.buffers;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import me.cortex.nvidium.gl.GlObject;
import org.lwjgl.opengl.ARBSparseBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;

import static org.lwjgl.opengl.ARBSparseBuffer.GL_SPARSE_STORAGE_BIT_ARB;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL44.GL_DYNAMIC_STORAGE_BIT;
import static org.lwjgl.opengl.GL45.glCreateBuffers;
import static org.lwjgl.opengl.GL45.glNamedBufferStorage;

public class SparseSsboBuffer extends GlObject implements Buffer {

    public static long alignUp(long number, long alignment) {
        long delta = number % alignment;
        return delta == 0 ? number : number + (alignment - delta);
    }

    public final long size;
    public static final long PAGE_SIZE = 1 << 20;

    private final Int2IntOpenHashMap allocationCount = new Int2IntOpenHashMap();

    public SparseSsboBuffer(long size) {
        super(glCreateBuffers());
        this.size = alignUp(size, PAGE_SIZE);
        glNamedBufferStorage(id, size, GL_DYNAMIC_STORAGE_BIT | GL_SPARSE_STORAGE_BIT_ARB);
    }

    private static void doCommit(int buffer, long offset, long size, boolean commit) {
        GL21.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
        ARBSparseBuffer.glBufferPageCommitmentARB(GL15.GL_ARRAY_BUFFER, offset, size, commit);
    }

    // ... rest of allocatePages/deallocatePages/ensureAllocated/deallocate identical to original ...

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
