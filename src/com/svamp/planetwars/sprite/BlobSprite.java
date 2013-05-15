package com.svamp.planetwars.sprite;

import android.graphics.Color;
import android.opengl.GLES20;
import com.svamp.planetwars.math.Vector;

import javax.microedition.khronos.opengles.GL10;
import java.util.Collection;


public class BlobSprite extends AbstractLineSprite {
    private static final String TAG = BlobSprite.class.getCanonicalName();

    public BlobSprite(Collection<Vector> line,int color) {
        super(line);
        setColor(SpriteFactory.splitColor(color));
    }
}
