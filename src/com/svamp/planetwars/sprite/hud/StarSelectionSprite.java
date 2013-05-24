package com.svamp.planetwars.sprite.hud;

import android.graphics.Bitmap;
import com.svamp.planetwars.sprite.StarSprite;

import javax.microedition.khronos.opengles.GL10;

/**
 * This class represents the "profile" image of a star, shown in the HUD.
 * It wraps a StarSprite, and uses it to draw.
 */
public class StarSelectionSprite extends HudSprite {
    private StarSprite realStar;
    private boolean textLeft;
    //Cached values from the star (ordered red-green-blue)
    private int[] thisShips = new int[3];
    private int[] enemyShips = new int[3];


    /**
     * Constructor for the StarSelectionSprite
     * @param s StarSprite this Hud item represents.
     * @param textLeft Whether the text should be to the left or the right of the image frame.
     *                 True sets text to the left.
     */
    public StarSelectionSprite(StarSprite s, boolean textLeft) {
        this.realStar = s;
        this.textLeft = textLeft;
    }

    public void draw(GL10 glUnused, float[] mvcMatrix) {
        //TODO: Some cool visualization of the star here.
    }
}
