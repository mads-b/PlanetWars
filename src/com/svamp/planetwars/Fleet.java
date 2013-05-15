package com.svamp.planetwars;

import android.util.FloatMath;
import android.util.Log;
import com.svamp.planetwars.network.ByteSerializeable;
import com.svamp.planetwars.network.Player;

import java.nio.ByteBuffer;

/**
 * A fleet consists of several spaceships.
 */
public class Fleet implements ByteSerializeable {
    private final static float SHIP_HP=5;
    /** How much more damage red does against green, green against blue, and blue against red. */
    private final static float ADVANTAGE_MULTIPLIER = 1.5f;
    /** How much less damage red does against blue, blue against green, and green against red. */
    private final static float WEAKNESS_MULTIPLIER = 0.6667f;
    /**How much base damage a ship does/second */
    private final static float BASE_DAMAGE = 2.5f;

    private Player owner;

    /** Number of ships are floats to allow for ships to be partially destroyed.
     * All getters will round up this number to show number of actual ships.  */
    private Ships redCrafts;
    private Ships blueCrafts;
    private Ships greenCrafts;

    public Fleet(Player owner, float redCrafts, float blueCrafts, float greenCrafts) {
        this.owner=owner;
        this.redCrafts = new Ships(redCrafts);
        this.blueCrafts = new Ships(blueCrafts);
        this.greenCrafts = new Ships(greenCrafts);
    }

    public Fleet(ByteBuffer buffer) {
        updateFromSerialization(buffer);
    }


    /**
     * Merges the provided fleet with this one. Resulting fleet in this one.
     * @param f Fleet to add to this object.
     */
    public void add(Fleet f) {
        this.redCrafts.add(f.redCrafts);
        this.blueCrafts.add(f.blueCrafts);
        this.greenCrafts.add(f.greenCrafts);
    }

    /**
     * Add ships of a specific type
     * @param t Type of ship
     * @param q Quantity of ship
     */
    public void add(ShipType t, int q) {
        if(t == ShipType.BLUE_SHIP) blueCrafts.add(q);
        if(t == ShipType.RED_SHIP) redCrafts.add(q);
        if(t == ShipType.GREEN_SHIP) greenCrafts.add(q);
    }
    /**
     * Subtracts the unit number in the provided fleet from this one.
     * @param fleet Fleet to remove from this one.
     */
    public void subtract(Fleet fleet) {
        this.redCrafts.subtract(fleet.redCrafts);
        this.blueCrafts.subtract(fleet.blueCrafts);
        this.greenCrafts.subtract(fleet.greenCrafts);
    }

    /**
     * Enemy fleet damages this fleet! This method calculates the damage done by the
     * enemy fleet to this one, and removes destroyed ships accordingly.
     * @param dt Time interval this fleet has attacked (seconds)
     * @param enemy enemy fleet doing the damaging.
     */
    public void absorbDamageBy(float dt, Fleet enemy) {
        // Enemy damage output
        float redDamage = enemy.getRedCrafts()*BASE_DAMAGE*dt;
        float greenDamage = enemy.getGreenCrafts()*BASE_DAMAGE*dt;
        float blueDamage = enemy.getBlueCrafts()*BASE_DAMAGE*dt;
        /*Compute how much damage is done to each ship type*/
        //First, weakness exploitation.
        redDamage = greenCrafts.damage(redDamage, ADVANTAGE_MULTIPLIER);
        greenDamage = blueCrafts.damage(greenDamage, ADVANTAGE_MULTIPLIER);
        blueDamage = redCrafts.damage(blueDamage, ADVANTAGE_MULTIPLIER);
        // No weakness, damage multiplier 1.
        redDamage = redCrafts.damage(redDamage, 1);
        greenDamage = greenCrafts.damage(greenDamage, 1);
        blueDamage = blueCrafts.damage(blueDamage, 1);
        // Attack stronger ships with the rest of the damage done.
        redCrafts.damage(greenDamage, WEAKNESS_MULTIPLIER);
        greenCrafts.damage(blueDamage, WEAKNESS_MULTIPLIER);
        blueCrafts.damage(redDamage, WEAKNESS_MULTIPLIER);
    }

    public short getRedCrafts() { return redCrafts.getNum(); }
    public short getBlueCrafts() { return blueCrafts.getNum(); }
    public short getGreenCrafts() { return greenCrafts.getNum(); }

    public Player getOwner() { return owner; }
    public boolean isEmpty() { return sum() == 0; }

    /**
     * Check whether this fleet is a subset of the provided fleet
     * (it has less or equal quantities of all types of ships.)
     * The method works with whole ships, and do not care about damage done to the fleet.
     * @param f Superset fleet to check
     * @return True if this fleet has less of every ship than the provided one.
     */
    public boolean isSubsetOf(Fleet f) {
        return redCrafts.getNum() <= f.redCrafts.getNum() &&
                greenCrafts.getNum() <= f.greenCrafts.getNum() &&
                blueCrafts.getNum() <= f.blueCrafts.getNum();

    }

    @Override
    public byte[] getSerialization() {
        /*
         * Format:
         * playerId:int
         * redcraftnum: float
         * bluecraftnum: float
         * greencraftnum: float
         */
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        buffer.putInt(owner.getElementHash())
                .putFloat(redCrafts.shipNum)
                .putFloat(blueCrafts.shipNum)
                .putFloat(greenCrafts.shipNum);
        return buffer.array();
    }

    @Override
    public void updateFromSerialization(ByteBuffer buffer) {
        owner = GameEngine.getPlayer(buffer.getInt());
        redCrafts = new Ships(buffer.getFloat());
        blueCrafts = new Ships(buffer.getFloat());
        greenCrafts = new Ships(buffer.getFloat());

        Log.d(Fleet.class.getCanonicalName(),"updated. "+toString());
    }

    @Override
    public int getSerializedSize() {
        return 16;
    }

    /**
     * Fetches the number of craft in this fleet
     * @return Number of functioning ships in the fleet.
     */
    public int sum() {
        return getBlueCrafts()+ getRedCrafts()+ getGreenCrafts();
    }

    public String toString() {
        return "[Fleet owner="+owner.toString()+
                " reds="+ redCrafts +
                " blues="+ blueCrafts +
                " greens= "+ greenCrafts +"]";
    }

    public enum ShipType {
        RED_SHIP,
        GREEN_SHIP,
        BLUE_SHIP;

        public static ShipType getByOrdinal(int ord) {
            return ShipType.values()[ord];
        }
    }

    private class Ships {
        private float shipNum;

        private Ships(float number) { this.shipNum = number; }

        private short getNum() { return (short) FloatMath.ceil(shipNum); }
        private void add(Ships s) { shipNum += s.shipNum;}
        private void add(float q) { shipNum += q;}
        private void subtract(Ships s) {shipNum = Math.max(0,shipNum-s.shipNum);}

        /**
         * Damage this ship type.
         * @param dmg Amount of damage done.
         * @param multiplier Damage multiplier to this ship type.
         * @return Damage left over because number of ships reached 0.
         */
        private float damage(float dmg,float multiplier) {
            float hpRemaining = shipNum*SHIP_HP-dmg*multiplier;
            shipNum = Math.max(0, hpRemaining*SHIP_HP);
            return -Math.min(0,hpRemaining/multiplier);
        }
    }
}
