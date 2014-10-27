package com.svamp.planetwars.opengl;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class ShaderProgram {
    private final static String TAG = ShaderProgram.class.getCanonicalName();

    private static Resources resources;
    private final int programHandle;
    private int vertShaderHandle = -1;
    private int fragShaderHandle = -1;

    public static void init(Resources res) {
        ShaderProgram.resources = res;
    }

    /**
     * Creates and links a GL program
     * @param glUnused Unused object to ensure this gets called from GL thread.
     */
    public ShaderProgram(GL10 glUnused) {
        if(resources==null) throw new IllegalStateException("Error! The shaderPrograms have no " +
                "resource link! Initialize them!");
        programHandle = GLES20.glCreateProgram();
    }

    /**
     * Compiles a shader from a shader source reference (Like R.string.shader)
     * Adding a shader of an already added type will replace that type.
     * @param glUnused Unused object to ensure this gets called from GL thread.
     * @param shaderType Type of shader. Try GLES20.GL_FRAGMENT_SHADER or GLES20.GL_VERTEX_SHADER.
     * @param sourceResource Source reference of shader
     */
    public void linkShaders(GL10 glUnused, int shaderType, int sourceResource) {
        String source = resources.getString(sourceResource);
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        GLES20.glAttachShader(programHandle, shader);

        // Link shaders in program
        GLES20.glLinkProgram(programHandle);
    }

    /**
     * Deletes this program and all attached shaders from GL memory. Do NOT try to use this
     * object afterwards!
     * @param glUnused Unused object to ensure this gets called from GL thread.
     */
    public void delete(GL10 glUnused) {
        GLES20.glDeleteShader(vertShaderHandle);
        GLES20.glDeleteShader(fragShaderHandle);
        GLES20.glDeleteProgram(programHandle);
    }

    public int getVertShaderHandle() { return vertShaderHandle; }
    public int getFragShaderHandle() { return fragShaderHandle; }
}
