package com.svamp.planetwars.sprite.hud;

import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import com.svamp.planetwars.R;
import com.svamp.planetwars.sprite.SpriteFactory;
import com.svamp.planetwars.sprite.StarSprite;

import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public class ButtonSprite extends AbstractHudSprite {
    private final Paint buttonPaint = new Paint();
    private final String buttonText;
    private final TextSprite textSprite;
    private final Hud hud;
    private final StarSprite star;
    private int texId = -1;

    public ButtonSprite(String buttonText,Hud hud,StarSprite star) {
        this.buttonText = buttonText;
        this.hud = hud;
        this.star = star;
        buttonPaint.setColor(Color.BLACK);
        buttonPaint.setAntiAlias(true);
        buttonPaint.setTextSize(30);

        textSprite = new TextSprite(buttonPaint,buttonPaint);
        setZVal(-.12f);
        textSprite.setZVal(-.22f);
    }

    @Override
    public void draw(GL10 glUnused, float[] mvcMatrix) {
        // First-time preload
        if(texId == -1) {
            textSprite.changeText(glUnused,buttonText);
            // Text doesn't care about width.
            textSprite.setSize(1337,bounds.height()/2);
            // Center text on button.
            textSprite.setPos(
                    bounds.centerX()-textSprite.getBounds().width()/2,
                    bounds.centerY()-textSprite.getBounds().height()/2);

            texId = SpriteFactory.getInstance().makeAndRegisterDrawable(glUnused, R.drawable.planetwars_button, GLES20.GL_CLAMP_TO_EDGE);
            setTexture(texId);
        }
        super.draw(glUnused,mvcMatrix);
    }

    public void push() {
        hud.buttonPushed(star);
    }

    public void updateVertices() {
        super.updateVertices();

    }

    @Override
    public Collection<AbstractHudSprite> getSprites() {
        Collection<AbstractHudSprite> spriteList = super.getSprites();
        spriteList.addAll(textSprite.getSprites());
        return spriteList;
    }
}
