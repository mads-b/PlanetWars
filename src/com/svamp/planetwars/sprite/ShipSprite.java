package com.svamp.planetwars.sprite;

import android.opengl.GLES20;
import com.svamp.planetwars.Fleet;
import com.svamp.planetwars.R;
import com.svamp.planetwars.ShipMap;
import com.svamp.planetwars.math.Vector;

import javax.microedition.khronos.opengles.GL10;

/**
 * Abstract ship representing every ship in the game.
 */
public class ShipSprite extends AbstractSquareSprite {
    private int glTexId = -1;
    private final ShipMap shipMap;
    private final Vector speed = new Vector(0,0);
    private StarSprite dest;
    private final Fleet fleet;


    private final static float MAX_SPEED = .3f;


    /**
     * Constructor for sprite representing a fleet
     * @param shipMap Map to be notified of ship arrival.
     * @param fleet Fleet to represent
     */
    public ShipSprite(ShipMap shipMap,Fleet fleet) {
        this.shipMap = shipMap;
        this.fleet=fleet;
        this.setSize(0.1f, 0.1f);
    }

    public Fleet getFleet() { return fleet; }
    public StarSprite getDest() { return dest; }

    @Override
    public void draw(GL10 glUnused, float[] mvpMatrix) {
        //Texture not loaded. Load it. this is a hack. TODO: Preload textures.
        if(glTexId == -1) {
            glTexId = SpriteFactory.getInstance()
                    .makeAndRegisterDrawable(glUnused, R.drawable.ic_launcher, GLES20.GL_CLAMP_TO_EDGE);
            super.setTexture(glTexId);
        }

        //Draw Quad
        super.draw(glUnused,mvpMatrix);
    }

    public void update(float dt) {
        if(speed.x!=0 && speed.y!=0) {
            float dx=bounds.centerX()-dest.getBounds().centerX();
            float dy=bounds.centerY()-dest.getBounds().centerY();
            if(Math.abs(dx)+Math.abs(dy)<=(Math.abs(speed.x)+Math.abs(speed.y))*dt) {
                //Signal to shipMap that we've arrived.
                shipMap.shipArrived(this);
            }
            else {
                this.move(speed.x*dt, speed.y*dt);
            }
        }
    }

    /**
     * Helper method to position ship dead on top of a star sprite.
     * @param src Sprite to place ship on.
     */
    public void setSrc(StarSprite src) {
        setPos(src.getBounds().centerX()-bounds.width()/2,
               src.getBounds().centerY()-bounds.height()/2);
    }

    /**
     * Set ship destination. This causes the ship to start moving.
     * @param dest Sprite to approach.
     */
    //
    public void setDest(StarSprite dest) {
        //Dummy speed x-y vars. Only important thing is that the direction is correct
        float ySp = dest.getBounds().centerY()-bounds.centerY();
        float xSp = dest.getBounds().centerX()-bounds.centerX();
        //Scale factor to assure speed squared equals MAX_SPEED_SQ
        this.dest=dest;
        speed.set(xSp,ySp);
        speed.setLength(MAX_SPEED);
    }
}
