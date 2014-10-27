package com.svamp.planetwars.opengl;

import android.opengl.GLES20;

import javax.microedition.khronos.opengles.GL10;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Java representation of an OpenGL 3d model. This class will contains the GL references to
 * vertex/texture coordinates as well as any handles needed to render the object. Supports VBO
 * uploading. This implementation draws from client side buffers.
 */
public class GlModel {
    protected FloatBuffer drawBuffer;
    protected Buffer indices;
    protected int drawBufferHandle = -1;
    protected int texHandle = -1;
    protected ShaderProgram shaders;
    private static final int STRIDE = (3+2)*4;
    private final int INDEX_TYPE;

    public GlModel(FloatBuffer drawBuffer, Buffer indices, int texHandle, ShaderProgram shaders) {
        this.drawBuffer = drawBuffer;
        this.indices = indices;
        this.texHandle = texHandle;
        this.shaders = shaders;
        drawBuffer.rewind();
        indices.rewind();
        INDEX_TYPE = indices instanceof IntBuffer ? GLES20.GL_UNSIGNED_INT
                : indices instanceof ShortBuffer ? GLES20.GL_UNSIGNED_SHORT
                : GLES20.GL_UNSIGNED_BYTE;
        //TODO: Set TexHandle to a single pixel white texture to ensure it always works.
    }

    /**
     * Instructs OpenGL to use the texture contained within this object.
     * The reason this is an API method, is because if drawing multiple objects with the same
     * texture, it is wise to call this only prior to the first GlModel being drawn.
     * @param glUnused Unused object to ensure this gets called from GL thread.
     */
    public void useThisTexture(GL10 glUnused) {
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);
    }

    /**
     * Instructs OpenGL to draw the model this object represents.
     * @param glUnused
     */
    public void draw(GL10 glUnused) {
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(drawBufferHandle);

        //Prepare the texture coordinate data
        GLES20.glVertexAttribPointer(
                texCoordHandle, 2, GLES20.GL_FLOAT, false, STRIDE, drawBuffer);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                positionHandle, 3, GLES20.GL_FLOAT, false, STRIDE, drawBuffer);

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, INDEX_TYPE, indices);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);

    }



}

