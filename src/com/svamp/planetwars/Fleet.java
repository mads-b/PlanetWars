package com.svamp.planetwars;

import android.util.Log;
import com.svamp.planetwars.network.ByteSerializeable;
import com.svamp.planetwars.network.Player;

import java.nio.ByteBuffer;

/**
 * A fleet consists of several spaceships.
 */
public class Fleet implements ByteSerializeable {
    private final static float SHIP_HP=5;

    private Player owner;
    private short redCraftNum;
    private short blueCraftNum;
    private short greenCraftNum;

    private float damageAccumulator =0;

    public Fleet(Player owner,short redCraftNum,short blueCraftNum,short greenCraftNum) {
        this.owner=owner;
        this.redCraftNum = redCraftNum;
        this.blueCraftNum = blueCraftNum;
        this.greenCraftNum = greenCraftNum;
    }

    public Fleet(ByteBuffer buffer) {
        updateFromSerialization(buffer);
    }


    /**
     * Merges the provided fleet with this one. Resulting fleet in this one.
     * @param f Fleet to add to this object.
     */
    public void add(Fleet f) {
        this.redCraftNum +=f.redCraftNum;
        this.blueCraftNum +=f.blueCraftNum;
        this.greenCraftNum += f.greenCraftNum;
    }
    /**
     * Subtracts the unit number in the provided fleet from this one.
     * @param fleet Fleet to remove from this one.
     */
    public void subtract(Fleet fleet) {
        this.redCraftNum -= fleet.redCraftNum;
        this.blueCraftNum -= fleet.blueCraftNum;
        this.greenCraftNum -= fleet.greenCraftNum;
    }

    public boolean damageFleet(float dmg) {
        return false;
    }

    public short getRedCraftNum() { return redCraftNum; }
    public short getBlueCraftNum() { return blueCraftNum; }
    public short getGreenCraftNum() { return greenCraftNum; }

    public Player getOwner() { return owner; }
    public boolean isEmpty() { return redCraftNum == 0 && blueCraftNum == 0 && greenCraftNum == 0; }

    @Override
    public byte[] getSerialization() {
        /*
         * Format:
         * playerId:int
         * damage: float
         * redcraftnum: short
         * bluecraftnum: short
         * greencraftnum: short
         */
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        buffer.putInt(owner.getElementHash())
                .putFloat(damageAccumulator)
                .putShort(redCraftNum)
                .putShort(blueCraftNum)
                .putShort(greenCraftNum);
        return buffer.array();
    }

    @Override
    public void updateFromSerialization(ByteBuffer buffer) {
        owner = GameEngine.getPlayer(buffer.getInt());
        damageAccumulator = buffer.getFloat();
        redCraftNum = buffer.getShort();
        blueCraftNum = buffer.getShort();
        greenCraftNum = buffer.getShort();


        Log.d(Fleet.class.getCanonicalName(),"updated. "+toString());
    }

    @Override
    public int getSerializedSize() {
        return 14;
    }

    public String toString() {
        return "[Fleet owner="+owner.toString()+
                " reds="+ redCraftNum +
                " blues="+ blueCraftNum +
                " greens= "+greenCraftNum+
                " damage= "+damageAccumulator+"]";
    }
}
