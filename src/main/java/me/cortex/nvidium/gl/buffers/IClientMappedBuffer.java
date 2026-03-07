package me.cortex.nvidium.gl.buffers;

import java.nio.ByteBuffer;

public interface IClientMappedBuffer extends Buffer {

    ByteBuffer clientBuffer();
}
