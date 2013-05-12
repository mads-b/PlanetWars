package com.svamp.planetwars.sprite;

import android.opengl.GLES20;
import android.util.Log;
import com.svamp.planetwars.R;
import com.svamp.planetwars.ShaderTool;
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

    AbstractLineSprite(Collection<Vector> line) {
        this.size = line.size();
        vertexBuffer = ByteBuffer.allocateDirect(size * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for(Vector v : line) {
            vertexBuffer.put(v.x).put(v.y);
        }
        vertexBuffer.rewind();
    }


    public void draw(GL10 glUnused, float[] mvpMatrix) {
        if(mProgramHandle == -1) throw new IllegalStateException("Error! Initialize the shaders for this class!");
        //Handle to line positions
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //Prepare line coordinate data
        GLES20.glVertexAttribPointer(0,2,GLES20.GL_FLOAT,false,0,vertexBuffer);
        //Draw lines:
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP,0,size);
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }


    /** This will be used to pass in the transformation matrix. */
    private static int mMVPMatrixHandle;
    /** This will be used to pass in model position information. */
    private static int mPositionHandle;
    static int mProgramHandle = -1;
    static int mColorHandle;

    /**
     * Creates a program with a vertex and a frag shader.
     */
    public static void initShaders(GL10 glUnused) {
        int vertexShaderHandle = ShaderTool.loadShader(glUnused, GLES20.GL_VERTEX_SHADER, R.string.shader_line_vert);
        int fragmentShaderHandle = ShaderTool.loadShader(glUnused, GLES20.GL_FRAGMENT_SHADER, R.string.shader_line_frag);
        // Create a program object and store the handle to it.
        mProgramHandle = ShaderTool.makeProgram(glUnused,vertexShaderHandle,fragmentShaderHandle);

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
