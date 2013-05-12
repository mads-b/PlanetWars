package com.svamp.planetwars.sprite;

import android.graphics.Color;
import android.opengl.GLES20;
import com.svamp.planetwars.math.Vector;

import javax.microedition.khronos.opengles.GL10;
import java.util.Collection;


public class BlobSprite extends AbstractLineSprite {
    private final float[] color;

    private static final String TAG = BlobSprite.class.getCanonicalName();

    public BlobSprite(Collection<Vector> line,int color) {
        super(line);
        this.color = SpriteFactory.splitColor(color);
    }

    @Override
    public void draw(GL10 glUnused, float[] mvpMatrix) {
        //Apply color!
        GLES20.glUniform4f(mColorHandle,color[0],color[1],color[2],color[3]);
        // Draw vertices.
        super.draw(glUnused, mvpMatrix);

    }
}
