package me.cortex.nvidium.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.cortex.nvidium.gl.RenderDevice;
import me.cortex.nvidium.gl.buffers.Buffer;
import me.cortex.nvidium.gl.buffers.PersistentClientMappedBuffer;

import java.nio.ByteBuffer;

// Download stream from gpu to cpu
public class DownloadTaskStream {

    public interface IDownloadFinishedCallback {

        void accept(ByteBuffer buffer);
    }

    private record Download(long addr, IDownloadFinishedCallback callback) {}

    private final SegmentedManager allocator = new SegmentedManager();
    private final RenderDevice device;
    private PersistentClientMappedBuffer buffer;

    private int cidx;
    private final ObjectList<Download>[] allocations;

    public DownloadTaskStream(RenderDevice device, int frames, long size) {
        this.device = device;
        allocator.setLimit(size);
        buffer = device.createClientMappedBuffer(size);
        TickableManager.register(this);
        allocations = new ObjectList[frames];
        for (int i = 0; i < frames; i++) {
            allocations[i] = new ObjectArrayList<>();
        }
    }

    public void download(Buffer source, long offset, int size, IDownloadFinishedCallback callback) {
        long addr = allocator.alloc(size);
        device.copyBuffer(source, buffer, offset, addr, size);
        allocations[cidx].add(new Download(addr, callback));
    }

    void tick() {
        cidx = (cidx + 1) % allocations.length;
        for (var download : allocations[cidx]) {
            download.callback.accept(buffer.clientBuffer());
            allocator.free(download.addr);
        }
        allocations[cidx].clear();
    }

    public void delete() {
        TickableManager.remove(this);
        buffer.delete();
    }
}
