package com.svamp.planetwars.sprite.hud;

import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.util.Log;
import com.svamp.planetwars.R;
import com.svamp.planetwars.sprite.SpriteFactory;

import javax.microedition.khronos.opengles.GL10;
import java.util.Collection;

/**
 */
public class ButtonSprite extends HudSprite {
    private final String buttonText;
    private final TextSprite textSprite;
    private final Hud hud;

    private int glTexId = -1;

    /**
     * Constructor for a button with text.
     * @param buttonText Text to write on button
     * @param hud Hud to call when button is pressed.
     */
    public ButtonSprite(String buttonText,Hud hud) {
        this.buttonText = buttonText;
        this.hud = hud;
        Paint buttonPaint = new Paint();
        buttonPaint.setColor(Color.BLACK);
        buttonPaint.setAntiAlias(true);
        buttonPaint.setTextSize(30);

        textSprite = new TextSprite(buttonPaint, buttonPaint);
        textSprite.changeText(buttonText);
        setZVal(-.12f);
        textSprite.setZVal(-.22f);
    }

    @Override
    protected void updateVertices() {
        super.updateVertices();
        // Text doesn't care about width.
        textSprite.setSize(1337,bounds.height()/2);
        // Center text on button.
        textSprite.setPos(
                bounds.centerX()-textSprite.getBounds().width()/2,
                bounds.centerY()-textSprite.getBounds().height()/2);
    }

    @Override
    public void draw(GL10 glUnused, float[] mvcMatrix) {
        // First-time preload
        if(glTexId == -1) {
            glTexId = SpriteFactory.getInstance().makeAndRegisterDrawable(glUnused, R.drawable.planetwars_button, GLES20.GL_CLAMP_TO_EDGE);
            setTexture(glTexId);
        }
        super.draw(glUnused,mvcMatrix);
    }

    /**
     * Base method just triggers Hud.attackOrTransfer().
     */
    public void push() {
        hud.attackOrTransfer();
    }

    @Override
    public Collection<HudSprite> getSprites() {
        Collection<HudSprite> spriteList = super.getSprites();
        spriteList.addAll(textSprite.getSprites());
        return spriteList;
    }
}
