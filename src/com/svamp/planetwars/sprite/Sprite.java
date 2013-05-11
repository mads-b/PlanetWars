package com.svamp.planetwars.sprite;

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
    /**
     * Method called to draw the sprite object onto the Gl canvas.
     * @param glUnused Unused object to ensure draw only gets called from renderer
     * @param mvpMatrix Tranformation matrix to convert world coordinates to screen coordinates.
     */
    void draw (GL10 glUnused, float[] mvpMatrix);

    /**
     * Makes time pass for this sprite
     * @param dt Time passed since last call to this method. Milliseconds.
     */
    void update (float dt);

    /**
     * Check if the provided position is within the bounds of this sprite
     * @param v Vector to collision check
     * @return True if vector point is contained within sprite bounds.
     */
    boolean contains(Vector v);

    /**
     * Fetches the bounding box of this sprite. Please do not modify this object.
     * @return Sprite bounds.
     */
    RectF getBounds();
}
