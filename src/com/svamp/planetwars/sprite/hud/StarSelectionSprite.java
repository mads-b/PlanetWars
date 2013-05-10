package com.svamp.planetwars.sprite.hud;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.svamp.planetwars.Fleet;
import com.svamp.planetwars.sprite.AbstractSprite;
import com.svamp.planetwars.sprite.StarSprite;

/**
 * This class represents the "profile" image of a star, shown in the HUD.
 * It wraps a StarSprite, and uses it to draw.
 */
public class StarSelectionSprite extends AbstractSprite {
    private final static Paint numPaint = new Paint();
    private Bitmap star;

    private StarSprite realStar;
    private float subWidth;

    public StarSelectionSprite() {
        numPaint.setColor(Color.GREEN);
        numPaint.setStyle(Paint.Style.STROKE);
        numPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void loadStar(StarSprite s) {
        //this.star = s.getImage();
        realStar=s;
    }


    public void draw(float[] mvcMatrix) {
        /*
        Fleet homeFleet = realStar.getBattleField().getHomeFleet();
        c.drawRect(bounds,numPaint);
        c.drawRect(
                bounds.right-subWidth,
                bounds.bottom-subWidth,
                bounds.right,
                bounds.bottom,
                numPaint);
        c.drawRect(bounds.left,
                bounds.top,
                bounds.left+subWidth,
                bounds.top+subWidth,
                numPaint);anvas c
        c.drawRect(bounds.right-subWidth,
                bounds.top,
                bounds.right,
                bounds.top+subWidth,
                numPaint);

        c.drawText(
                homeFleet.getFighterNum() + "",
                bounds.left + subWidth / 2,
                bounds.top + subWidth,
                numPaint);

        c.drawText(
                homeFleet.getBomberNum()+"",
                bounds.right-subWidth/2,
                bounds.top+subWidth,
                numPaint);

        c.drawText(
                realStar.getOwnership().getPlayerNum()+"",
                bounds.right-subWidth/2,
                bounds.bottom,
                numPaint);
        c.drawBitmap(star,null,bounds,null);
        */
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width,height);
        subWidth=width*0.2f;
        numPaint.setTextSize(subWidth*0.9f);
    }
}
