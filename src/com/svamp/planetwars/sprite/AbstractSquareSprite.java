package com.svamp.planetwars.sprite;

import android.opengl.GLES20;
import android.util.Log;
import com.svamp.planetwars.R;
import com.svamp.planetwars.ShaderTool;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * A slightly more implemented version of the AbstractSprite.
 * This class only accepts sprites using one texture mapped on a quad.
 */
public abstract class AbstractSquareSprite extends AbstractSprite {

    private static final String TAG = AbstractSquareSprite.class.getCanonicalName();

    /**
     * Draws the vertices. Remember to call glUseProgram prior to calling this,
     * and also initializing eventual textures and other info.
     */
    public void draw(GL10 glUnused, float[] mvpMatrix) {
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                3*4, vertexBuffer);

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6,
                GLES20.GL_UNSIGNED_SHORT, drawOrderBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    /*
     * OpenGL-specific stuff:
     */
    private static final short[] drawOrder = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    private static final float[] textureOrder = { 0,0,0,1,1,1,1,0 }; //Coordinates for texture.

    private final FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(12 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private final ShortBuffer drawOrderBuffer = ByteBuffer.allocateDirect(12).order(ByteOrder.nativeOrder()).asShortBuffer().put(drawOrder);
    protected final FloatBuffer textureBuffer = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(textureOrder);
    protected void updateVertices() {
        drawOrderBuffer.rewind();
        textureBuffer.rewind();
        vertexBuffer.rewind();
        vertexBuffer
                .put(bounds.left).put(bounds.bottom).put(0)
                .put(bounds.left).put(bounds.top).put(0)
                .put(bounds.right).put(bounds.top).put(0)
                .put(bounds.right).put(bounds.bottom).put(0);
        vertexBuffer.rewind();
    }


    /** This will be used to pass in the transformation matrix. */
    private static int mMVPMatrixHandle;
    /** This will be used to pass in model position information. */
    private static int mPositionHandle;
    protected static int mProgramHandle = -1;
    protected static int mColorHandle;
    protected static int mTexCoordinateHandle;

    /**
     * Creates a program with a vertex and a frag shader.
     */
    public static void initShaders(GL10 glUnused) {
        int vertexShaderHandle = ShaderTool.loadShader(glUnused, GLES20.GL_VERTEX_SHADER, R.string.shader_tex_vert);
        int fragmentShaderHandle = ShaderTool.loadShader(glUnused, GLES20.GL_FRAGMENT_SHADER, R.string.shader_tex_frag);
        // Create a program object and store the handle to it.
        mProgramHandle = ShaderTool.makeProgram(glUnused,vertexShaderHandle,fragmentShaderHandle);

        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "MVPMatrix");
        mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "color");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "position");
        mTexCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle,"texCoordinate");

        //Set default color to white to ensure unset color draws uncolorized texture.
        GLES20.glUseProgram(mProgramHandle);
        GLES20.glUniform4f(mColorHandle,1,1,1,1);

        Log.d(TAG, "errors:" + GLES20.glGetProgramInfoLog(mProgramHandle) + GLES20.glGetShaderInfoLog(vertexShaderHandle) + GLES20.glGetShaderInfoLog(fragmentShaderHandle));
    }

    /**
     * Fetches the program handle for this sprite
     * @return The shader program handle for this class of sprite.
     * @throws IllegalStateException if the program handle is attempted accessed prior to it being created.
     */
    public static int getProgramHandle() {
        if(mProgramHandle == -1) throw new IllegalStateException("Error! Shaders and program not initialized!");
        return mProgramHandle;
    }
}
