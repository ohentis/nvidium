package me.cortex.nvidium.gl.buffers;

import static org.lwjgl.opengl.ARBSparseBuffer.GL_SPARSE_STORAGE_BIT_ARB;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL44.GL_DYNAMIC_STORAGE_BIT;
import static org.lwjgl.opengl.GL45.glCreateBuffers;
import static org.lwjgl.opengl.GL45.glNamedBufferStorage;

import org.lwjgl.opengl.ARBSparseBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import me.cortex.nvidium.gl.GlObject;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;

@Lwjgl3Aware
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

    private void allocatePages(int page, int pageCount) {
        doCommit(id, PAGE_SIZE * page, PAGE_SIZE * pageCount, true);
        for (int i = 0; i < pageCount; i++) {
            allocationCount.addTo(i + page, 1);
        }
    }

    private void deallocatePages(int page, int pageCount) {
        for (int i = 0; i < pageCount; i++) {
            int newCount = allocationCount.get(i + page) - 1;
            if (newCount != 0) {
                allocationCount.put(i + page, newCount);
            } else {
                allocationCount.remove(i + page);
                doCommit(id, PAGE_SIZE * (page + i), PAGE_SIZE, false);
            }
        }
    }

    public int getPagesCommitted() {
        return allocationCount.size();
    }

    public void ensureAllocated(long addr, long size) {
        int pstart = (int) (addr / PAGE_SIZE);
        int pend = (int) ((addr + size + PAGE_SIZE - 1) / PAGE_SIZE);
        allocatePages(pstart, pend - pstart);
    }

    public void deallocate(long addr, long size) {
        int pstart = (int) (addr / PAGE_SIZE);
        int pend = (int) ((addr + size + PAGE_SIZE - 1) / PAGE_SIZE);
        deallocatePages(pstart, pend - pstart);
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
