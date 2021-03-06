package com.svamp.planetwars.opengl;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.util.SparseIntArray;

import javax.microedition.khronos.opengles.GL10;

/**
 * Singleton factory class to ask for images!
 * Must be initialized prior to use!
 */
public class TextureTool {
    private static TextureTool instance;
    private final Resources res;
    //Map from R.drawable resources to OpenGL texture reference.
    private final SparseIntArray cache = new SparseIntArray();

    private final static String TAG = TextureTool.class.getCanonicalName();

    public static TextureTool getInstance() {
        return instance;
    }
    public static void initialize(Resources res) {
        instance = new TextureTool(res);
    }

    private TextureTool(Resources res) {
        this.res = res;
    }

    /**
     * Fetches or imports the drawable needed, then loads it into GL memory.
     * @param glUnused Unused object to ensure method is called from GL thread.
     * @param id R.drawable id of image to import.
     * @param repeat Specified to allow the texture to repeat in both directions. Ex: GLES20.GL_REPEAT
     * @return OpenGL texture handle.
     */
    public int makeAndRegisterDrawable(GL10 glUnused, int id, int repeat) {
        //Fetch texture ID from cache if possible
        if(cache.indexOfKey(id)>=0) return cache.get(id);
        //Fetch from resources, bind/upload to GPU.
        Bitmap bitmap = BitmapFactory.decodeResource(res,id);
        int result = registerBitmapInGl(bitmap, repeat, GLES20.GL_NEAREST);
        // Delete bitmap from local memory.
        bitmap.recycle();
        cache.put(id,result);
        return result;
    }

    /**
     * Takes a text string, draws it onto a Bitmap, and loads it into OpenGL memory.
     * Please do not call this often, as it is expensive.
     * @param glUnused Unused object to ensure method is called from GL thread.
     * @param text Text to write to texture
     * @param style The style of the text
     * @param strokeStyle Style of the stroke.
     * @return gl Bound texture handle
     */
    public int makeAndRegisterText(GL10 glUnused, String text, Paint style, Paint strokeStyle) {
        //Measure text to see how big a bitmap we need.
        Rect size = new Rect();
        Rect size2 = new Rect();
        style.getTextBounds(text,0,text.length(),size);
        strokeStyle.getTextBounds(text,0,text.length(),size2);

        Bitmap bmp = Bitmap.createBitmap(size2.width(),size2.height(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawText(text,0,size2.height(),strokeStyle);
        c.drawText(text,0,size.height(),style);
        return registerBitmapInGl(bmp,GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_LINEAR);
    }

    /**
     * Deletes a texture from GL memory, given its texture handle
     * @param glUnused Unused GL object to ensure method is called from GL thread.
     * @param textureHandles OpenGL texture handles to the texture to be deleted.
     */
    public void deleteTextureFromGL(GL10 glUnused, int ... textureHandles) {
        GLES20.glDeleteTextures(0, textureHandles,0);
        for(int handle : textureHandles) {
            cache.delete(cache.indexOfValue(handle));
        }
    }

    /**
     * Converts an Android color to a 4-element RGBA
     * @param color integer representing a color (generated by the Color class)
     * @return 4-element float where every color is in the range 0-1.
     */
    public static float[] splitColor(int color) {
        float[] res = new float[4];
        res[0] = Color.red(color)/255f;
        res[1] = Color.green(color)/255f;
        res[2] = Color.blue(color)/255f;
        res[3] = Color.alpha(color)/255f;
        return res;
    }

    /**
     * Loads a bitmap in GPU memory. Recycles the bitmap afterwards.
     * @param bitmap Bitmap image.
     * @param filtering Min and mag filter. Ex: GLES20.GL_NEAREST or GLES20.GL_LINEAR
     * @return OpenGL texture reference the image is bound to.
     */
    private int registerBitmapInGl(Bitmap bitmap, int repeat,int filtering) {
        int[] texture = new int[1];
        // Make a texture ID.
        GLES20.glGenTextures(1, texture, 0);
        // Fail if not generated.
        if(texture[0]==0) throw new RuntimeException("Failed creating new texture pointer! Called from right thread?");
        // ...and bind it to our array
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        // create nearest filtered texture
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, filtering);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, filtering);
        // Repeating
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, repeat);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, repeat);

        // Use Android GLUtils to specify a two-dimensional texture image from our bitmap
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        // Clean up
        bitmap.recycle();
        Log.d(TAG, "Registered new texture in GL with pointer "+texture[0]);
        return texture[0];
    }
}
