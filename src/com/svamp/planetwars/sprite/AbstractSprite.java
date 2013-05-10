package com.svamp.planetwars.sprite;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import com.svamp.planetwars.R;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.network.ByteSerializeable;
import com.svamp.planetwars.network.PackageHeader;

import javax.microedition.khronos.opengles.GL10;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/*
 * This abstract class will handle all the bounds modification.
 * Remember to call super at all times!
 */

public abstract class AbstractSprite implements Sprite,ByteSerializeable {
    //This sprite's position and size in the "world". This value is set only once if the sprite is not moving.
    protected final RectF bounds = new RectF(0,0,0,0);

    private static final String TAG = "com.svamp.sprite.AbstractSprite";

    //Serialization content: hash
    private int elementHash=-1;

    public void move(float dx, float dy) {
        this.bounds.offset(dx, dy);
        updateVertices();
    }
    public void setPos(float x, float y) {
        this.bounds.offsetTo(x,y);
        updateVertices();
    }

    public void setSize(float width, float height) {
        this.bounds.set(bounds.left, bounds.top, bounds.left+width, bounds.top+height);
        updateVertices();
    }

    /**
     * Preloads and caches the drawable requested in GL memory.
     * Must be called from GL thread.
     * @param drawable R.drawable requested, to be fetched.
     * @return Texture pointer to GL memory.
     */
    public int getGlTexPointer(int drawable) {
        return SpriteFactory.getInstance().getTextureId(drawable);
    }

    @Override
    public void update(float dt) {}

    @Override
    public boolean contains(Vector pos) {
        return bounds.contains(pos.x,pos.y);
    }

    @Override
    public RectF getBounds() { return bounds; }

    @Override
    public byte[] getSerialization() {
        //Make sure this element has a hash before serializing...
        if(elementHash==-1) throw new IllegalStateException("Tried to serialize an object with no hash set!");

        ByteBuffer buffer = ByteBuffer.allocate(20);
        //Insert hash
        buffer.putInt(elementHash);
        //Put coordinates. 4*4=16 bytes
        buffer.putFloat(bounds.left).putFloat(bounds.top).putFloat(bounds.right).putFloat(bounds.bottom);
        return buffer.array();
    }

    @Override
    public void updateFromSerialization(ByteBuffer buffer) {
        //Make sure pointer points at this object's hash!
        int hash = buffer.getInt();
        if(hash==-1) throw new IllegalArgumentException("Tried to update sprite having a -1 hash!");
        if(hash!=elementHash) throw new IllegalStateException("Bytebuffer with wrongly placed pointer passed to element!");
        //Set bounds.
        bounds.left=buffer.getFloat();
        bounds.top=buffer.getFloat();
        bounds.right=buffer.getFloat();
        bounds.bottom=buffer.getFloat();
        updateVertices();
    }

    public void setElementHash(int hash) {
        this.elementHash=hash;
    }
    public int getElementHash() { return elementHash; }

    @Override
    public int getSerializedSize() {
        return 20;
    }

    /*
     * OpenGL-specific stuff:
     */
    private static short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    private static float textureOrder[] = { 0,0,0,1,1,1,1,0 }; //Coordinates for texture.

    protected FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(12 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected ShortBuffer drawOrderBuffer = ByteBuffer.allocateDirect(12).order(ByteOrder.nativeOrder()).asShortBuffer().put(drawOrder);
    protected FloatBuffer textureBuffer = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(textureOrder);
    private void updateVertices() {
        drawOrderBuffer.position(0);
        textureBuffer.position(0);
        vertexBuffer
                .put(bounds.left).put(bounds.top).put(0)
                .put(bounds.left).put(bounds.bottom).put(0)
                .put(bounds.right).put(bounds.bottom).put(0)
                .put(bounds.right).put(bounds.top).put(0);
        vertexBuffer.position(0);
    }

    /**
     * Shader stuff.
     */

    /** This will be used to pass in the transformation matrix. */
    protected static int mMVPMatrixHandle;
    /** This will be used to pass in model position information. */
    protected static int mPositionHandle;
    protected static int mProgramHandle;
    protected static int mTexCoordinateHandle;

    /**
     * Creates a program with a vertex and a frag shader.
     * Method ONLY TO BE CALLED FROM GL THREAD!
     */
    public static void initShaders(Context c) {
        String fragShaderStr = c.getString(R.string.shader_tex_frag);
        String vertShaderStr = c.getString(R.string.shader_tex_vert);

        int vertexShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER,vertShaderStr);
        int fragmentShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER,fragShaderStr);
        // Create a program object and store the handle to it.
        mProgramHandle = GLES20.glCreateProgram();

        // Bind the vertex shader to the program.
        GLES20.glAttachShader(mProgramHandle, vertexShaderHandle);

        // Bind the fragment shader to the program.
        GLES20.glAttachShader(mProgramHandle, fragmentShaderHandle);

        // Link the two shaders together into a program.
        GLES20.glLinkProgram(mProgramHandle);

        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "position");
        mTexCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle,"texCoordinate");

        Log.d(TAG,"errors:"+GLES20.glGetProgramInfoLog(mProgramHandle)+GLES20.glGetShaderInfoLog(vertexShaderHandle)+GLES20.glGetShaderInfoLog(fragmentShaderHandle));
    }

    private static int loadShader(int shaderType, String source) {
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
