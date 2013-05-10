package com.svamp.planetwars.sprite.hud;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.svamp.planetwars.Fleet;
import com.svamp.planetwars.Hud;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.sprite.AbstractSprite;
import com.svamp.planetwars.sprite.StarSprite;

/**
 */
public class SliderSprite extends AbstractSprite {
    protected float curVal=0;
    protected int maxVal=0;
    protected final Paint framePaint = new Paint();
    protected final Paint sliderPaint = new Paint();
    protected final StarSprite star;

    private final Hud.HudItem type;

    public SliderSprite(StarSprite star, Hud.HudItem type) {
        this.star=star;
        framePaint.setColor(Color.GREEN);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setTextSize(16);
        sliderPaint.setStyle(Paint.Style.FILL);
        if(type == Hud.HudItem.BOMBER_SLIDER)
            sliderPaint.setColor(Color.BLUE);
        if(type == Hud.HudItem.FIGHTER_SLIDER)
            sliderPaint.setColor(Color.RED);
        this.type=type;
    }

    @Override
    public void draw(float[] mvcMatrix) {
        //c.drawRect(bounds,framePaint);
        //Number of fighters/bombers might change. Make sure we're updated.
        Fleet homeFleet = star.getBattleField().getHomeFleet();

        maxVal = type== Hud.HudItem.BOMBER_SLIDER ? homeFleet.getBomberNum() : homeFleet.getFighterNum();
        curVal = curVal<=maxVal ? curVal : maxVal;
/*
        c.drawText("/" + maxVal, bounds.right + 5, bounds.bottom, framePaint);
        c.drawRect(
                bounds.left+1,
                bounds.top+1,
                (float) (bounds.left+(bounds.right-bounds.left)*((int)curVal/Math.max(maxVal,0.01)))-2,
                bounds.bottom-1,
                sliderPaint);*/
    }

    public void setVal(short val) { this.curVal=val; }

    public void move(Vector amount) {
        curVal+= maxVal*amount.x/bounds.width();
        //Enforce max/min constraints.
        curVal = Math.min(Math.max(0,curVal),maxVal);
    }

    public short getVal() {
        return (short) Math.round(curVal);
    }
}
