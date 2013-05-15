package com.svamp.planetwars.sprite.hud;

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
        getSlider().setMaxVal(1);
    }

    @Override
    public void draw(GL10 glUnused, float[] mvcMatrix) {
        super.draw(glUnused, mvcMatrix);
    }

    @Override
    public void move(Vector amount) {
        super.move(amount);
        //Inform star if state changed.
        //if(star.getBuildType()!=Math.round(0)) {
        //    star.setBuildType(Math.round(0));
        //    callback.buildSelectionChanged(star);
        //}
    }

    public void setCallback(Hud hud) {
        callback=hud;
    }
}
