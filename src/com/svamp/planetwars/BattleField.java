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
    private final static float BOMBER_DAMAGE = 1;
    private final static float FIGHTER_DAMAGE = 1;

    //Only calculate battles every tick.
    private final static float TICK_LENGTH = 0.5f;

    private final StarSprite star;
    private Fleet homeFleet;
    private final Map<Player,Fleet> actors = new HashMap<Player,Fleet>();
    private float time = 0;

    //Number of ticks required before a ship is constructed and added to the home fleet.
    // Also dependent on star size.
    private final static int ADD_SHIP_MULTIPLIER = 800;
    //Maximum amount of ships to have before construction halts.
    private final static int MAX_UNITS_FOR_BUILD = 30;
    private int tickNum=0;



    public BattleField(StarSprite star) {
        short five = 5;
        this.homeFleet = new Fleet(Player.getNeutral(),five,five);
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
            if(homeFleet.getFighterNum()+homeFleet.getBomberNum()<MAX_UNITS_FOR_BUILD) {
                if(star.getBuildType()==0) //Fighter
                    homeFleet.add(new Fleet(homeFleet.getOwner(),(short)1,(short)0));
                if(star.getBuildType()==1) //Bomber
                    homeFleet.add(new Fleet(homeFleet.getOwner(),(short)0,(short)1));
                //No battle? Submit changes anyway.
                if(actors.size()==0) return 1;
            }
        }
        //If we have actors, we have a battle!
        if(actors.size()==0) { return 0; }
        //Every actor attacks in turn..:
        for(Fleet f : actors.values()) {
            //Bomb the star!
            star.damageStar(f.getBomberNum()* BOMBER_DAMAGE *TICK_LENGTH);
            //Destroy its ships!
            homeFleet.damageFleet(f.getFighterNum()*FIGHTER_DAMAGE*TICK_LENGTH);
        }
        //Home retaliates! Get first element in map. Sucks to be them.
        Fleet victim = actors.values().iterator().next();
        //Attack with star.
        victim.damageFleet(star.getAttack()*TICK_LENGTH);
        //Attack with home fleet
        victim.damageFleet(homeFleet.getFighterNum()*FIGHTER_DAMAGE*TICK_LENGTH);

        if(victim.isEmpty()) {
            actors.remove(victim.getOwner());
        }

        //Star has been conquered! Set owner to someone else...
        if(star.isDead() && homeFleet.isEmpty()) {
            actors.remove(victim.getOwner());
            homeFleet=victim;
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
