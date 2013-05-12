package com.svamp.planetwars.sprite;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
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
    private final static float STAR_DAMAGE_COEFF=0.002f;
    private final static float STAR_HP_REGEN_COEFF = 0.1f;

    private final int drawableTexId;
    private int glTexId = -1;

    private static final Paint fighterPaint = new Paint();
    private static final Paint bomberPaint = new Paint();

    /*
     * Game mechanic variables follow
     */
    //Every star is a battlefield. Some stars have battlefields with multiple actors within.
    private final BattleField battleField = new BattleField(this);
    //Hit points for the star. It is reckoned to be "neutral" again when this is 0.
    private short HP;
    private short maxHP;
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
        //Hitpoints are proportional to size of star.
        this.maxHP= (short) (radius*10);
        //Bounds == radius*2.
        this.setSize(radius*2, radius*2);
        fighterPaint.setColor(Color.RED);
        fighterPaint.setStrokeWidth(10);
        fighterPaint.setStyle(Style.STROKE);
        bomberPaint.setColor(Color.BLUE);
        bomberPaint.setStrokeWidth(10);
        bomberPaint.setStyle(Style.STROKE);
        resetHP();
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
        if(glTexId == -1) glTexId = SpriteFactory.getInstance().getTextureId(glUnused,drawableTexId);
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgramHandle);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTexId);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTexCoordinateHandle, 0);

        GLES20.glEnableVertexAttribArray(mTexCoordinateHandle);

        GLES20.glVertexAttribPointer(mTexCoordinateHandle, 2, GLES20.GL_FLOAT, false,
                0, textureBuffer);
        //Draw vertices.
        super.draw(glUnused,mvpMatrix);
        GLES20.glDisableVertexAttribArray(mTexCoordinateHandle);
    }

    @Override
    public void update(float dt) {
        if(battleField.numActors()==0 && HP!=maxHP) { //Star only rebuilds when it's peaceful.
            //Let both clients and hosts update this. It's irrelevant outside of battle anyway.
            HP+=maxHP*STAR_HP_REGEN_COEFF*dt;
        }
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
                .putShort(HP)
                .putShort(maxHP)
                .put(_battleField);
        return buffer.array();
    }

    @Override
    public void updateFromSerialization(ByteBuffer buffer) {
        super.updateFromSerialization(buffer); //Update ancestor
        buildType = buffer.get(); //Selected craft to build.
        HP=buffer.getShort();
        maxHP=buffer.getShort();
        battleField.updateFromSerialization(buffer);
    }

    @Override
    public int getSerializedSize() {
        return 5+super.getSerializedSize()+battleField.getSerializedSize();
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

    /**
     * Methods manipulating or using Star HP.
     */
    public void damageStar(float amount) {
        HP-=amount;
        if(HP<0) HP=0;
    }
    void resetHP() { HP=maxHP; }
    public boolean isDead() { return HP <= 0; }
    public void setBuildType(int buildType) {
        this.buildType=buildType;
    }
    public int getBuildType() { return buildType; }

    /**
     * Gets the amount of damage this star does per tick.
     */
    public float getAttack() { return maxHP*STAR_DAMAGE_COEFF; }


    public String toString() {
        return "[StarSprite at:"+bounds.centerX()+"x"+bounds.centerY()+"]";
    }

    public boolean equals(Object o) {
        return o instanceof StarSprite && ((StarSprite) o).getElementHash()==getElementHash();
    }
}
