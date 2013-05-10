package com.svamp.planetwars.sprite;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

import javax.microedition.khronos.opengles.GL10;

public class SpriteSheet {
    private final int texId;
    private final Rect srcRect=new Rect(0,0,0,0);
    private final Sprite sprite;

    private int curFrame=0; //Current animation frame.
    private int dirFrame=0; //Current direction frame
    private final int animNum; //Number of horizontal sprites in animation
    private final int numDirs; //Number of vertical directional sprites
    private float rotation=0; //Current heading
    private final double step; //Radians per direction
    private final static float ANIM_STEP =0.1f; //Change to next frame in animation after animstep seconds.
    private float time=0; //Time accumulator

    /**
     * Constructor for SpriteSheet
     * @param sprite Parent Sprite containing the position.
     * @param animNum See local animNum var
     * @param numDirs See local numDirs var
     * @param texId OpenGL texture ID the image We're using is bound to.
     */
    public SpriteSheet(Sprite sprite, int animNum,int numDirs, int texId) {
        this.numDirs = numDirs;
        this.animNum = animNum;
        this.texId = texId;
        this.sprite=sprite;
        //Number of radians contained in each frame
        step = Math.PI*2/numDirs;

    }

    /**
     * @param rot angle as parameter, and selects the best sprite from sheet.
     *            0 radians corresponds to direction right.
     *            Positive direction corresponds to couterclockwise direction.
     */
    public void addRot(float rot) {
        setRot(this.rotation+rot);
    }

    public void update(float dt) {
        time+=dt;
        if(time> ANIM_STEP) {
            time-= ANIM_STEP;
            curFrame++;
        }
        if(curFrame==animNum) curFrame=0;
        RectF b = sprite.getBounds();
    }

    public void setRot(float rad) {
        //Ensure 0<rad<2pi
        rotation = rad;

        while(rad<0) rad+=2*Math.PI;
        while(rad>2*Math.PI) rad-=2*Math.PI;

        dirFrame = (int)((rad+step/2)/step);

        if(dirFrame==numDirs) dirFrame=0;
    }

    public void draw(float[] mvpMatrix) {
        //Draw quad with subset of spritesheet on.
    }
}
