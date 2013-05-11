package com.svamp.planetwars.sprite;

import com.svamp.planetwars.Fleet;
import com.svamp.planetwars.ShipMap;
import com.svamp.planetwars.math.Vector;

import javax.microedition.khronos.opengles.GL10;

/**
 * Abstract ship representing every ship in the game.
 */
public class ShipSprite extends AbstractSquareSprite {
    private final ShipMap shipMap;
    private final Vector speed = new Vector(0,0);
    private StarSprite dest;
    private Fleet fleet;


    private final static int MAX_SPEED =100; //100px/s

    private SpriteSheet spriteSheet;
    private final SpriteSheetType type;

    public ShipSprite(ShipMap shipMap,Fleet fleet,SpriteSheetType type) {
        this.shipMap = shipMap;
        this.fleet=fleet;
        this.type = type;

        this.setSize(50, 50); //TODO: Better width calc..
    }

    public Fleet getFleet() { return fleet; }
    public StarSprite getDest() { return dest; }

    @Override
    public void draw(GL10 glUnused, float[] mvpMatrix) {
        if(spriteSheet == null) spriteSheet = SpriteFactory.getInstance().makeSpriteSheet(glUnused,type, this);
        spriteSheet.draw(glUnused, mvpMatrix);
    }

    public void update(float dt) {
        spriteSheet.update(dt);
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
        spriteSheet.setRot((float) Math.atan2(-ySp, xSp));
        //setAttackDir(ORBIT_DISTANCE);
    }

    public void setPos(float x,float y) {
        super.setPos(x, y);
    }
}
