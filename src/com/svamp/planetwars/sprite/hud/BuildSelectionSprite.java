package com.svamp.planetwars.sprite.hud;

import android.graphics.Color;
import com.svamp.planetwars.Hud;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.sprite.StarSprite;

import javax.microedition.khronos.opengles.GL10;

/**
 * TODO: Javadoc!
 */
public class BuildSelectionSprite extends SliderSprite {
    private Hud callback;

    public BuildSelectionSprite(StarSprite star, Hud.HudItem type) {
        super(star, type);
        maxVal=1;
        sliderPaint.setColor(Color.GREEN);
    }

    @Override
    public void draw(GL10 glUnused, float[] mvcMatrix) {
        /*c.drawRect(bounds,framePaint);
        c.drawRect(bounds.left+bounds.width()*Math.round(curVal)/2,
                bounds.top,
                bounds.right-bounds.width()*Math.round(1 - curVal)/2,
                bounds.bottom,sliderPaint);*/
    }

    @Override
    public void move(Vector amount) {
        super.move(amount);
        //Inform star if state changed.
        if(star.getBuildType()!=Math.round(curVal)) {
            star.setBuildType(Math.round(curVal));
            callback.buildSelectionChanged(star);
        }
    }

    public void setCallback(Hud hud) {
        callback=hud;
    }
}
