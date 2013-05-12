package com.svamp.planetwars.sprite.hud;

import android.graphics.Color;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.Log;
import com.svamp.planetwars.Fleet;
import com.svamp.planetwars.R;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.sprite.AbstractSquareSprite;
import com.svamp.planetwars.sprite.SpriteFactory;
import com.svamp.planetwars.sprite.StarSprite;

import javax.microedition.khronos.opengles.GL10;

/**
 * Slider used to select quantity of ships to use.
 */
public class SliderSprite extends AbstractSquareSprite {
    private int glTexId = -1;
    protected final StarSprite star;
    private final Hud.HudItem type;

    private Slider slider;


    public SliderSprite(StarSprite star, Hud.HudItem type) {
        this.star=star;
        this.type=type;
        int color = type == Hud.HudItem.BOMBER_SLIDER ? Color.BLUE : Color.RED;
        this.slider = new Slider(color);
    }

    @Override
    public void draw(GL10 glUnused, float[] mvpMatrix) {
        //Number of fighters/bombers might change. Make sure we're updated.
        Fleet homeFleet = star.getBattleField().getHomeFleet();
        int val = type == Hud.HudItem.BOMBER_SLIDER
                ? homeFleet.getBomberNum()
                : homeFleet.getFighterNum();
        slider.setMaxVal(val);
        //Draw slider first so it goes underneath the following overlay.
        slider.draw(glUnused,mvpMatrix);
        //Texture not loaded. Load it. this is a hack. TODO: Preload textures.
        if(glTexId == -1) {
            glTexId = SpriteFactory.getInstance()
                    .getTextureId(glUnused, R.drawable.planetwars_slider, GLES20.GL_CLAMP_TO_EDGE);
        }
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTexId);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTexCoordinateHandle, 0);

        GLES20.glEnableVertexAttribArray(mTexCoordinateHandle);

        GLES20.glVertexAttribPointer(mTexCoordinateHandle, 2, GLES20.GL_FLOAT, false,
                0, textureBuffer);
        //Draw vertices.
        super.draw(glUnused,mvpMatrix);

        GLES20.glDisableVertexAttribArray(mTexCoordinateHandle);
    }

    @Override
    public void updateVertices() {
        super.updateVertices();
        slider.setPos(bounds.left,bounds.top);
        slider.setSize(bounds.width(),bounds.height());
        slider.setMaxWidth(bounds.width());
    }

    public void setVal(short val) {
        slider.incrementValue(val-slider.curVal);
    }

    public void move(Vector amount) {
        //Increment slider accordingly
        slider.incrementValue(slider.maxVal*amount.x/bounds.width());
    }

    public short getVal() {
        return (short) Math.round(100*slider.curVal);
    }

    protected Slider getSlider() { return slider; }

    protected class Slider extends AbstractSquareSprite {
        private final float[] color;
        private float maxWidth;
        private int maxVal = 0;
        private float curVal = 0;

        public Slider(int color) {
            this.color = SpriteFactory.splitColor(color);
        }

        public void setMaxWidth(float maxWidth) {
            this.maxWidth = maxWidth;
        }

        public void setMaxVal(int maxVal) {
            this.maxVal = maxVal;
        }

        /**
         * Specify fill rate of the slider.
         * @param val How much to increment the slider with. Cannot exceed interval 0-1.
         */
        public void incrementValue(float val) {
            Log.d(SliderSprite.class.getCanonicalName(),"Incremented slider with "+val+" now it is"+curVal);
            curVal += val;
            curVal = Math.min(maxVal,Math.max(0,curVal));
            this.setSize(maxWidth*curVal/maxVal,bounds.height());
        }

        @Override
        public void draw(GL10 glUnused, float[] mvpMatrix) {
            //Apply color!
            GLES20.glUniform4f(mColorHandle, color[0], color[1], color[2], color[3]);
            // Draw vertices.
            super.draw(glUnused, mvpMatrix);
            //Set color to former value
            GLES20.glUniform4f(mColorHandle, 1, 1, 1, 1);
        }

    }
}
