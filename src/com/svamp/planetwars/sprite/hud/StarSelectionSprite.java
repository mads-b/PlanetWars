package com.svamp.planetwars.sprite.hud;

import android.graphics.Bitmap;
import com.svamp.planetwars.sprite.StarSprite;

import javax.microedition.khronos.opengles.GL10;

/**
 * This class represents the "profile" image of a star, shown in the HUD.
 * It wraps a StarSprite, and uses it to draw.
 */
public class StarSelectionSprite extends HudSprite {
    private Bitmap star;

    private StarSprite realStar;
    private float subWidth;

    public StarSelectionSprite() {
    }

    public void loadStar(StarSprite s) {
        //this.star = s.getImage();
        realStar=s;
    }


    public void draw(GL10 glUnused, float[] mvcMatrix) {

    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width,height);
        subWidth=width*0.2f;
    }
}
