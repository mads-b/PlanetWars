package com.svamp.planetwars.sprite;

import android.opengl.GLES20;
import com.svamp.planetwars.math.Vector;

import javax.microedition.khronos.opengles.GL10;
import java.util.Collection;


public class BlobSprite extends AbstractLineSprite {

    public BlobSprite(Collection<Vector> line,int color) {
        super(line);
    }

    @Override
    public void draw(GL10 glUnused, float[] mvpMatrix) {
        //Apply color!
        GLES20.glUniform4f(mColorHandle,.7f,.5f,.5f,.5f);
        super.draw(glUnused, mvpMatrix);
        GLES20.glUniform4f(mColorHandle,.7f,.5f,.5f,.5f);
    }
}
