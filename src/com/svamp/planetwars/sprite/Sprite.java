package com.svamp.planetwars.sprite;

import android.graphics.Canvas;
import android.graphics.RectF;
import com.svamp.planetwars.math.Vector;

import javax.microedition.khronos.opengles.GL10;

/*
 * Composite pattern for sprites.
 * Supports multiple Sprites contained
 * within itself being treated as a single sprite.
 * "Sprite" in this case means drawable object.
 */

public interface Sprite {
    //Draw
    void draw (float[] mvpMatrix);
    void update (float dt);

    boolean contains(Vector v);

    RectF getBounds();
}
