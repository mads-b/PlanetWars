package com.svamp.planetwars.sprite;

import android.opengl.GLES20;
import android.util.Log;
import com.svamp.planetwars.R;
import com.svamp.planetwars.math.Vector;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Collection;

/**
 * Abstract implementation of a poly line sprite.
 */
public abstract class AbstractLineSprite extends AbstractSprite {
    private static final String TAG = AbstractLineSprite.class.getCanonicalName();
    private final int size;
    private final FloatBuffer vertexBuffer;
    private float[] color;

    AbstractLineSprite(Collection<Vector> line) {
        this.size = line.size();
        vertexBuffer = ByteBuffer.allocateDirect(size * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for(Vector v : line) {
            vertexBuffer.put(v.x).put(v.y);
        }
        vertexBuffer.rewind();
    }

    protected void setColor(float[] color) {
        this.color = color;
    }


    public void draw(GL10 glUnused, float[] mvpMatrix) {
        if(mProgramHandle == -1) throw new IllegalStateException("Error! Initialize the shaders for this class!");
        if(color != null) {
            GLES20.glUniform4f(mColorHandle, color[0], color[1], color[2], color[3]);
        }
        //Handle to line positions
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //Prepare line coordinate data
        GLES20.glVertexAttribPointer(0,2,GLES20.GL_FLOAT,false,0,vertexBuffer);
        //Draw lines:
        GLES20.glDrawArrays(GLES20.GL_LINES,0,size);
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        if(color != null) {
            GLES20.glUniform4f(mColorHandle, 1,1,1,1);
        }
    }


    /** This will be used to pass in the transformation matrix. */
    private static int mMVPMatrixHandle;
    /** This will be used to pass in model position information. */
    private static int mPositionHandle;
    private static int mProgramHandle = -1;
    private static int mColorHandle;

    /**
     * Creates a program with a vertex and a frag shader.
     */
    public static void initShaders(GL10 glUnused) {
        final ShaderTool shaderTool = ShaderTool.getInstance();
        final int vertexShaderHandle = shaderTool
                .loadShader(glUnused, GLES20.GL_VERTEX_SHADER, R.string.shader_line_vert);
        final int fragmentShaderHandle = shaderTool
                .loadShader(glUnused, GLES20.GL_FRAGMENT_SHADER, R.string.shader_line_frag);
        // Create a program object and store the handle to it.
        mProgramHandle = shaderTool
                .makeProgram(glUnused,vertexShaderHandle,fragmentShaderHandle);

        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "position");
        mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "color");

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

    @Override
    protected void updateVertices() {
        //TODO: This class is atomic as of now. Might change.
    }
}
