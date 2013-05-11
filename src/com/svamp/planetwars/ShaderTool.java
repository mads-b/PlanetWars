package com.svamp.planetwars;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

/**
 * Simplified tool used to import, compile and pack shaders and programs.
 */
public class ShaderTool {
    private static Resources resources;
    private final static String TAG = ShaderTool.class.getCanonicalName();

    public static void init(Resources res) {
        resources = res;
    }

    /**
     * Creates and links a GL program
     * @param glUnused Unused object to ensure this gets called from GL thread.
     * @param shaderHandles Array of shader handles to be linked into a program.
     * @return Program handle
     */
    public static int makeProgram(GL10 glUnused, int ... shaderHandles) {
        if(resources==null) throw new IllegalStateException("Error! The shaderTool has not been initialized!");
        int mProgramHandle = GLES20.glCreateProgram();

        for(int handle : shaderHandles) {
            GLES20.glAttachShader(mProgramHandle, handle);
        }
        // Link shaders in program
        GLES20.glLinkProgram(mProgramHandle);
        return mProgramHandle;
    }

    /**
     * Compiles a shader from a shader source reference (Like R.string.shader)
     * @param glUnused Unused object to ensure this gets called from GL thread.
     * @param shaderType Type of shader. Try GLES20.GL_FRAGMENT_SHADER or GLES20.GL_VERTEX_SHADER.
     * @param sourceResource Source reference of shader
     * @return Shader handle
     */
    public static int loadShader(GL10 glUnused, int shaderType, int sourceResource) {
        String source = resources.getString(sourceResource);
        if(resources==null) throw new IllegalStateException("Error! The shaderTool has not been initialized!");
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
        return shader;
    }
}
