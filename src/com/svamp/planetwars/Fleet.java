package com.svamp.planetwars;

import com.svamp.planetwars.network.ByteSerializeable;
import com.svamp.planetwars.network.Player;

import java.nio.ByteBuffer;

/**
 * A fleet consists of several spaceships.
 */
public class Fleet implements ByteSerializeable {
    private final static float SHIP_HP=5;

    private Player owner;
    private short fighterNum;
    private short bomberNum;

    private float damageAccumulator =0;

    public Fleet(Player owner,short fighterNum,short bomberNum) {
        this.owner=owner;
        this.fighterNum=fighterNum;
        this.bomberNum=bomberNum;
    }

    public Fleet(ByteBuffer buffer) {
        updateFromSerialization(buffer);
    }


    /**
     * Merges the provided fleet with this one. Resulting fleet in this one.
     * @param f Fleet to add to this object.
     */
    public void add(Fleet f) {
        this.fighterNum+=f.fighterNum;
        this.bomberNum+=f.bomberNum;
    }
    /**
     * Subtracts the unit number in the provided fleet from this one.
     * @param fleet Fleet to remove from this one.
     */
    public void subtract(Fleet fleet) {
        this.fighterNum-=fleet.fighterNum;
        this.bomberNum-=fleet.bomberNum;
    }

    public boolean damageFleet(float dmg) {
        damageAccumulator+=dmg;
        //Damage is over one ship's hp. Destroy a ship.
        if(damageAccumulator>SHIP_HP) {
            damageAccumulator-=SHIP_HP;
            //Randomly decide what ship gets destroyed.
            if(fighterNum>0 && bomberNum>0) {
                if(Math.random()>0.5)
                    fighterNum--;
                else
                    bomberNum--;
            } else if(fighterNum>0) {
                fighterNum--;
                return false;
            } else if(bomberNum>0) {
                bomberNum--;
                return false;
            }
            return true;
        }
        return false;
    }

    public short getFighterNum() { return fighterNum; }
    public short getBomberNum() { return  bomberNum; }
    public Player getOwner() { return owner; }
    public boolean isEmpty() { return fighterNum==0 && bomberNum==0; }

    @Override
    public byte[] getSerialization() {
        /*
         * Format:
         * playerId:int
         * damage: float
         * fighternum: short
         * bombernum: short
         */
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        buffer.putInt(owner.getElementHash())
                .putFloat(damageAccumulator)
                .putShort(fighterNum)
                .putShort(bomberNum);
        return buffer.array();
    }

    @Override
    public void updateFromSerialization(ByteBuffer buffer) {
        owner=GameEngine.getPlayer(buffer.getInt());
        damageAccumulator=buffer.getFloat();
        fighterNum=buffer.getShort();
        bomberNum=buffer.getShort();
    }

    @Override
    public int getSerializedSize() {
        return 12;
    }

    public String toString() {
        return "[Fleet owner="+owner.toString()+
                " fighters="+fighterNum+
                " bombers="+bomberNum+
                " damage= "+damageAccumulator+"]";
    }
}
