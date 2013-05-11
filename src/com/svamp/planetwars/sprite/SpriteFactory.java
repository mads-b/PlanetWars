package com.svamp.planetwars.sprite;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.util.SparseArray;

import javax.microedition.khronos.opengles.GL10;

/**
 * Singleton factory class to ask for images!
 * Must be initialized prior to use!
 */
public class SpriteFactory {
    //Eager singleton init.
    private static final SpriteFactory instance = new SpriteFactory();
    private Resources res;
    //Map from R.drawable resources to OpenGL texture reference.
    private final SparseArray<Integer> cache = new SparseArray<Integer>();

    private final static String TAG = SpriteFactory.class.getCanonicalName();

    public static SpriteFactory getInstance() {
        return instance;
    }
    public void initialize(Resources res) {
        this.res=res;
    }

    /**
     * Loads and prepares a sprite sheet from phone memory.
     * @param glUnused Unused object to ensure method is called from GL thread.
     * @param sheet Type of sheet required. @see SpriteSheetType
     * @param sprite Sprite to register as "owner" of spritesheet.
     * @return Spritesheet object ready for use.
     */
    public SpriteSheet makeSpriteSheet(GL10 glUnused, SpriteSheetType sheet,Sprite sprite) {
        return new SpriteSheet(sprite,sheet.getAnimNum(),sheet.getRotNum(),getTextureId(glUnused, sheet.getId()));
    }


    /**
     * Fetches or imports the drawable needed, then loads it into GL memory.
     * @param glUnused Unused object to ensure method is called from GL thread.
     * @param id R.drawable id of image to import.
     * @return OpenGL texture handle.
     */
    public int getTextureId(GL10 glUnused, int id) {
        //Fetch texture ID from cache if possible
        if(cache.indexOfKey(id)>=0) return cache.get(id);
        //Fetch from resources, bind/upload to GPU.
        int result = registerBitmapInGl(id);
        cache.put(id,result);
        return result;
    }

    /**
     * Loads a bitmap in GPU memory. Recycles the bitmap afterwards.
     * @param systemReference R.drawable reference to image.
     * @return OpenGL texture reference the image is bound to.
     */
    private int registerBitmapInGl(int systemReference) {
        Bitmap bitmap = BitmapFactory.decodeResource(res,systemReference);
        int[] texture = new int[1];
        // Make a texture ID.
        GLES20.glGenTextures(1, texture, 0);
        // Fail if not generated.
        if(texture[0]==0) throw new RuntimeException("Failed creating new texture pointer! Called from right thread?");
        // ...and bind it to our array
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        // create nearest filtered texture
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        // Use Android GLUtils to specify a two-dimensional texture image from our bitmap
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        // Clean up
        bitmap.recycle();
        Log.d(TAG, "Registered new texture in GL with pointer "+texture[0]);
        return texture[0];
    }
}
