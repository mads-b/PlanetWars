package com.svamp.planetwars.sprite.hud;

import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import com.svamp.planetwars.Fleet;
import com.svamp.planetwars.R;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.sprite.AbstractSquareSprite;
import com.svamp.planetwars.sprite.SpriteFactory;
import com.svamp.planetwars.sprite.StarSprite;

import javax.microedition.khronos.opengles.GL10;
import java.util.Collection;

/**
 * Slider used to select quantity of ships to use.
 */
public class SliderSprite extends AbstractHudSprite {
    private int glTexId = -1;
    protected final StarSprite star;
    private final Hud.HudItem type;

    private Slider slider;

    private Paint textStyle = new Paint();
    private final TextSprite text;
    // Toggle used to refresh text sprite when data has changed.
    private boolean textDirty = true;


    public SliderSprite(StarSprite star, Hud.HudItem type) {
        this.star=star;
        this.type=type;
        this.slider = new Slider(type.getColor());
        // Set up text for this slider:
        textStyle.setColor(Color.BLACK);
        textStyle.setAntiAlias(true);
        textStyle.setTextSize(30);

        text = new TextSprite(textStyle,textStyle);
        setZVal(-.2f);
        text.setZVal(-.11f);
        slider.setZVal(-.1f);
    }

    @Override
    public void draw(GL10 glUnused, float[] mvpMatrix) {
        // Remake text if info changed.
        if(textDirty) {
            text.changeText(null, (int)slider.curVal+"/"+slider.maxVal);
            textDirty = false;
        }

        //Texture not loaded. Load it. this is a hack. TODO: Preload textures.
        if(glTexId == -1) {
            glTexId = SpriteFactory.getInstance()
                    .makeAndRegisterDrawable(glUnused, R.drawable.planetwars_slider, GLES20.GL_CLAMP_TO_EDGE);
            super.setTexture(glTexId);
        }
        super.draw(glUnused,mvpMatrix);
    }

    @Override
    public void update(float dt) {
        //Number of fighters/bombers might change. Make sure we're updated.
        if(type == Hud.HudItem.BLUE_SLIDER || type == Hud.HudItem.RED_SLIDER) {
            Fleet homeFleet = star.getBattleField().getHomeFleet();
            int val = type == Hud.HudItem.BLUE_SLIDER
                    ? homeFleet.getBlueCraftNum()
                    : homeFleet.getRedCraftNum();

            // Maximum value has changed!
            if(slider.maxVal != val) {
                slider.setMaxVal(val);
                textDirty = true;
            }
        }
    }

    @Override
    public void updateVertices() {
        super.updateVertices();
        slider.setPos(bounds.left,bounds.top);
        slider.setSize(bounds.width(),bounds.height());
        slider.setMaxWidth(bounds.width());

        // Put text to the left of this sprite's bounds.
        text.setPos(bounds.right,bounds.top);
        // Text doesn't care about width.
        text.setSize(1337,bounds.height()/3);

    }

    @Override
    public Collection<AbstractHudSprite> getSprites() {
        Collection<AbstractHudSprite> spriteList = super.getSprites();
        spriteList.addAll(text.getSprites());
        spriteList.addAll(slider.getSprites());
        return spriteList;
    }

    public void setVal(short val) {
        slider.incrementValue(val - slider.curVal);
    }

    public void move(Vector amount) {
        //Increment slider accordingly
        int oldVal = getVal();
        slider.incrementValue(slider.maxVal*amount.x/bounds.width());
        if(oldVal - getVal() != 0)
            textDirty = true;
    }

    public short getVal() {
        return (short) Math.round(slider.curVal);
    }

    protected Slider getSlider() { return slider; }

    protected class Slider extends AbstractHudSprite {
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
            curVal += val;
            curVal = Math.min(maxVal,Math.max(0,curVal));
            this.setSize(maxWidth*Math.round(curVal)/maxVal,bounds.height());
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
