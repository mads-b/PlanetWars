package com.svamp.planetwars.sprite.hud;

import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import com.svamp.planetwars.Fleet;
import com.svamp.planetwars.R;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.sprite.SpriteFactory;
import com.svamp.planetwars.sprite.StarSprite;

import javax.microedition.khronos.opengles.GL10;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Slider used to select quantity of ships to use.
 */
public class SliderSprite extends HudSprite {
    //Remember old slider values for this class:
    private final static Map<Hud.HudItem,Short> oldValues = new HashMap<Hud.HudItem, Short>();
    //Ensures we only load this texture once.
    private int glTexId = -1;
    protected final StarSprite star;
    private final Hud.HudItem type;

    private final Slider slider;

    private final TextSprite text;

    public SliderSprite(StarSprite star, Hud.HudItem type) {
        this.star=star;
        this.type=type;
        this.slider = new Slider(type.getColor());
        // Set up text for this slider:
        Paint textStyle = new Paint();
        textStyle.setColor(Color.BLACK);
        textStyle.setTextSize(30);

        text = new TextSprite(textStyle, textStyle);
        setZVal(-.2f);
        text.setZVal(-.11f);
        slider.setZVal(-.1f);
    }

    @Override
    public void draw(GL10 glUnused, float[] mvpMatrix) {
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
        Fleet homeFleet = star.getBattleField().getHomeFleet();
        int val = type == Hud.HudItem.RED_SLIDER  ? homeFleet.getRedCrafts()
                : type == Hud.HudItem.BLUE_SLIDER ? homeFleet.getBlueCrafts()
                : homeFleet.getGreenCrafts();

        // Maximum value has changed!
        if(slider.maxVal != val) {
            slider.setMaxVal(val);
            // To be safe, set cur val to cached val.
            short oldVal = oldValues.containsKey(type) ? oldValues.get(type) : 0;
            slider.incrementValue(oldVal-slider.curVal);

            // Set new text.
            text.changeText((int)slider.curVal+"/"+slider.maxVal);
        }
    }

    @Override
    public void updateVertices() {
        super.updateVertices();
        slider.setPos(bounds.left,bounds.top);
        slider.setSize(bounds.width(),bounds.height());
        slider.setMaxWidth(bounds.width());

        // Put text to the left of this sprite's bounds.
        text.setPos(bounds.right,bounds.centerY()-bounds.height()/6);
        // Text doesn't care about width.
        text.setSize(1337,bounds.height()/3);
    }

    @Override
    public Collection<HudSprite> getSprites() {
        Collection<HudSprite> spriteList = super.getSprites();
        spriteList.addAll(text.getSprites());
        spriteList.addAll(slider.getSprites());
        return spriteList;
    }

    public void move(Vector amount) {
        //Increment slider accordingly
        int oldVal = getVal();
        slider.incrementValue(slider.maxVal*amount.x/bounds.width());
        if(oldVal - getVal() != 0) {
            // Set new text.
            text.changeText((int)slider.curVal+"/"+slider.maxVal);
            //Cache this new value:
            oldValues.put(type,getVal());
        }
    }

    public short getVal() {
        return (short) Math.round(slider.curVal);
    }

    private class Slider extends HudSprite {
        private float maxWidth;
        private int maxVal = 0;
        private float curVal = 0;

        public Slider(int color) {
            setColor(SpriteFactory.splitColor(color));
        }

        public void setMaxWidth(float maxWidth) {
            this.maxWidth = maxWidth;
        }

        public void setMaxVal(int maxVal) {
            this.maxVal = maxVal;
            this.curVal = Math.min(maxVal,curVal);
        }

        /**
         * Specify fill rate of the slider.
         * @param val How much to increment the slider with. Cannot exceed interval 0-maxVal
         */
        public void incrementValue(float val) {
            curVal += val;
            curVal = Math.min(maxVal,Math.max(0,curVal));
            this.setSize(maxWidth*Math.round(curVal)/maxVal,bounds.height());
        }
    }
}
