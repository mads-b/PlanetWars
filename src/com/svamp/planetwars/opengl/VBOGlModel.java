package com.svamp.planetwars.opengl;


import android.opengl.GLES20;

import javax.microedition.khronos.opengles.GL10;
import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 * VBO implementation of the GlModel.
 */
class VBOGlModel extends GlModel {
    protected static final int BYTES_PER_FLOAT = 4;

    public VBOGlModel(FloatBuffer drawBuffer, Buffer indices, int texHandle) {
        super(drawBuffer, indices, texHandle);
    }

    void bufferInGl(GL10 glUnused) {
        // First, generate as many buffers as we need.
        // This will give us the OpenGL handles for these buffers.
        final int buffers[] = new int[1];
        GLES20.glGenBuffers(1, buffers, 0);

        // Bind to the buffer. Future commands will affect this buffer specifically.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);

        // Transfer data from client memory to the buffer.
        // We can release the client memory after this call.
        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                drawBuffer.capacity() * BYTES_PER_FLOAT,
                drawBuffer,
                GLES20.GL_STATIC_DRAW);

        // IMPORTANT: Unbind from the buffer when we're done with it.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        drawBufferHandle = buffers[0];
        drawBuffer = null;
    }

    /**
     * Deletes this VBO from GL memory. Should be called by GL thread to clean up this object.
     *
     * @param glUnused Unused GL object to enforce bein called on correct thread.
     */
    public void deleteBuffers(GL10 glUnused) {
        GLES20.glDeleteBuffers(0, new int[] {drawBufferHandle},0);
        drawBufferHandle = -1;
    }

}
