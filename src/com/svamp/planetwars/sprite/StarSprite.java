package com.svamp.planetwars.sprite;

import android.opengl.GLES20;
import com.svamp.planetwars.BattleField;
import com.svamp.planetwars.GameEngine;
import com.svamp.planetwars.StarMap;
import com.svamp.planetwars.network.Player;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;

/*
 * PlanetSprite is a specialization of a circle drawable.
 * Its only variable parameter is size.
 */

public class StarSprite extends AbstractSquareSprite {
    private final int drawableTexId;
    private int glTexId = -1;

    /*
     * Game mechanic variables follow
     */
    //Every star is a battlefield. Some stars have battlefields with multiple actors within.
    private final BattleField battleField = new BattleField(this);

    private int buildType=0;

    private StarMap hostMap;

    //Some literal strings, for gui purposes
    private static final String[] ownershipDesc = {"NEUTRAL","ENEMY","YOURS"};
    private static final String[] statusDesc = {"PEACEFUL","FIGHTING"};

    /**
     * StarSprite constructor
     * @param radius Radius of star
     * @param drawableTexId drawable ID (android R.drawable)
     */
    public StarSprite(float radius,int drawableTexId) {
        this.drawableTexId = drawableTexId;
        //Bounds == radius*2.
        this.setSize(radius*2, radius*2);
    }

    public void setCallback(StarMap hostMap) {
        this.hostMap=hostMap;
    }

    /*
     * Check if sprite is in screen and draw! Recalculate from global coords to screen coords!
     */
    @Override
    public void draw(GL10 glUnused, float[] mvpMatrix) {
        //Texture not loaded. Load it. this is a hack. TODO: Preload textures.
        if(glTexId == -1) {
            glTexId = SpriteFactory.getInstance()
                    .makeAndRegisterDrawable(glUnused, drawableTexId, GLES20.GL_CLAMP_TO_EDGE);
            super.setTexture(glTexId);
        }

        //Draw Quad
        super.draw(glUnused,mvpMatrix);
    }

    @Override
    public void update(float dt) {
            int result = battleField.update(dt);
            if(hostMap!=null && result!=0)
                hostMap.fireStarStateChanged(result,this);
    }

    @Override
    public byte[] getSerialization() {
        //Fetch data from ancestors, add it to the returned result.
        byte[] ancestor = super.getSerialization();

        //Fetch battlefield..
        byte[] _battleField = battleField.getSerialization();

        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());

        //Put current and max HP. 4*2 bytes.
        buffer.put(ancestor)
                .put((byte) buildType)
                .put(_battleField);
        return buffer.array();
    }

    @Override
    public void updateFromSerialization(ByteBuffer buffer) {
        super.updateFromSerialization(buffer); //Update ancestor
        buildType = buffer.get(); //Selected craft to build.
        battleField.updateFromSerialization(buffer);
    }

    @Override
    public int getSerializedSize() {
        return 1+super.getSerializedSize()+battleField.getSerializedSize();
    }

    /**
     * Set the selection parameter of this star.
     * @param val True if star is selected, false otherwise
     * @param isSource True if this star is designated to be the source star. The value here doesn't matter if val is false.
     */
    public void setSelected(boolean val,boolean isSource) {
        //TODO: Add sweet GfX for selected stars!
    }

    public BattleField getBattleField() { return battleField; }

    public Player getOwnership() { return getBattleField().getHomeFleet().getOwner(); }
    public String getOwnershipDesc() {
        if(getOwnership().getPlayerNum()==0) return ownershipDesc[0];
        else if(getOwnership()== GameEngine.getPlayer()) return ownershipDesc[2];
        return ownershipDesc[1];
    }
    public boolean containsPlayer(Player player) {
        return battleField.getFleetWithOwner(player) != null;
    }

    /**
     * Get the status string of this star. Can either be at war or currently at peace.
     * @return String
     */
    public String getStatus(StarSprite star) {
        return (star.getBattleField().numActors()==0 ? statusDesc[0] : statusDesc[1]);
    }


    public void setBuildType(int buildType) {
        this.buildType=buildType;
    }
    public int getBuildType() { return buildType; }

    public String toString() {
        return "[StarSprite at:"+bounds.centerX()+"x"+bounds.centerY()+"]";
    }

    public boolean equals(Object o) {
        return o instanceof StarSprite && ((StarSprite) o).getElementHash()==getElementHash();
    }
}
