package com.svamp.planetwars.sprite.hud;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.svamp.planetwars.Hud;
import com.svamp.planetwars.sprite.AbstractSprite;
import com.svamp.planetwars.sprite.StarSprite;

/**
 */
public class ButtonSprite extends AbstractSprite {
    private final Paint buttonPaint = new Paint();
    private final String buttonText;
    private final Hud hud;
    private StarSprite star;

    public ButtonSprite(String buttonText,Hud hud,StarSprite star) {
        this.buttonText=buttonText;
        this.hud = hud;
        this.star=star;
        buttonPaint.setAntiAlias(true);
        buttonPaint.setColor(Color.GREEN);
        buttonPaint.setStrokeWidth(1f);
        buttonPaint.setStyle(Paint.Style.STROKE);

    }

    @Override
    public void draw(float[] mvcMatrix) {
        //c.drawRect(bounds,buttonPaint);
        //c.drawText(buttonText,bounds.left,bounds.centerY(),buttonPaint);
    }

    public void push() {
        hud.buttonPushed(star);
    }
}
