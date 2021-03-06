package com.svamp.planetwars;

import com.svamp.planetwars.network.ByteSerializeable;
import com.svamp.planetwars.network.Player;
import com.svamp.planetwars.sprite.StarSprite;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Instantiated by a star to keep track of all the units attacking each other.
 */
public class BattleField implements ByteSerializeable {
    //Only calculate battles every tick.(seconds)
    private final static float TICK_LENGTH = 1f;

    private final StarSprite star;
    private Fleet homeFleet;
    private final Map<Player,Fleet> actors = new HashMap<Player,Fleet>();
    private float time = 0;

    //Number of ticks required before a ship is constructed and added to the home fleet.
    // Also dependent on star size.
    private final static float ADD_SHIP_MULTIPLIER = .8f;
    //Maximum amount of ships to have before construction halts.
    private final static int MAX_UNITS_FOR_BUILD = 0;
    private int tickNum=0;



    public BattleField(StarSprite star) {
        short five = 5;
        this.homeFleet = new Fleet(Player.getNeutral(),five,five,five);
        this.star = star;
    }


    /**
     * Inform the battlefield of the progress of time.
     * @param dt Amount of time passed.
     * @return 0 if nothing changed, 1 if ships were destroyed/made, 2 if battlefield home fleet changed owner.
     */
    public int update(float dt) {
        time+=dt;
        if(time<TICK_LENGTH) { return 0; } //Not yet time for a tick
        time-= TICK_LENGTH;
        tickNum++;
        if(tickNum % (int)(ADD_SHIP_MULTIPLIER/star.getBounds().width())==0
                && homeFleet.getOwner()!=Player.getNeutral()) { //Time to make a ship. Only non-neutral players make ships.
            tickNum=0;
            if(homeFleet.sum() < MAX_UNITS_FOR_BUILD) {
                homeFleet.add(star.getBuildType(),1);

                //No battle? Submit changes anyway. We've got more ships after all.
                if(actors.size()==0) return 1;
            }
        }
        //If we have actors, we have a battle!
        if(actors.size()==0) { return 0; }
        // A rotten battle for the home fleet: All attackers attack the home fleet!
        for(Fleet actor : actors.values()) {
            homeFleet.absorbDamageBy(dt, actor);
        }
        // The home fleet always attacks the first actor. Sucks to be him
        Player firstPlayer = actors.keySet().iterator().next();
        Fleet firstFleet = actors.get(firstPlayer);
        firstFleet.absorbDamageBy(dt,homeFleet);
        // If the first fleet is obliterated by this attack, delete him from the list of actors.
        if(firstFleet.isEmpty()) {
            actors.remove(firstPlayer);
        }
        // If homeFleet is annihilated, the first enemy gets control over the star!
        if(homeFleet.isEmpty()) {
            actors.remove(firstPlayer);
            setHomeFleet(firstFleet);
            return 2;
        }
        return 1;
    }

    public Fleet getHomeFleet() { return homeFleet; }
    public void setHomeFleet(Fleet fleet) { homeFleet=fleet; }
    public int numActors() { return actors.size(); }

    /**
     * Gets a fleet in this battlefield with the specified owner.
     * @param player Owner of fleet to find.
     * @return Fleet belonging to owner, or null if it does not exist.
     */
    public Fleet getFleetWithOwner(Player player) {
        if(homeFleet.getOwner().equals(player)) return homeFleet;
        else return actors.get(player);
    }

    public void addFleet(Fleet f) {
        Fleet existing = getFleetWithOwner(f.getOwner());
        if(existing!=null) existing.add(f);
        else {
            actors.put(f.getOwner(),f);
        }
    }

    @Override
    public byte[] getSerialization() {
        /*
         * Format:
         * fleetCount: byte
         * homefleet: 12*byte
         * [actors]: size*12*byte
         */
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        //Insert actor size and home fleet.
        buffer.put((byte) actors.size())
                .put(homeFleet.getSerialization());
        for(Fleet actor : actors.values())
            buffer.put(actor.getSerialization());
        return buffer.array();
    }

    @Override
    public void updateFromSerialization(ByteBuffer buffer) {
        byte size = buffer.get(); //number of fleets.
        //Update home fleet.
        homeFleet.updateFromSerialization(buffer);
        //TODO: Better way than rebuilding the entire battlefield on update?
        actors.clear();

        for(int i=0;i<size;i++) {
            Player player = GameEngine.getPlayer(buffer.getInt());
            //Rewind buffer 4 bytes. We're just peeking!
            buffer.position(buffer.position()-4);
            actors.put(player,new Fleet(buffer));
        }
    }

    @Override
    public int getSerializedSize() {
        return 1+homeFleet.getSerializedSize()*(actors.size()+1);
    }
}
