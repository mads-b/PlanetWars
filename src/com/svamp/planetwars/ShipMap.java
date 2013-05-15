package com.svamp.planetwars;

import android.util.Log;
import com.svamp.planetwars.network.AbstractGameCommunicator;
import com.svamp.planetwars.network.DataPacketListener;
import com.svamp.planetwars.network.GameClient;
import com.svamp.planetwars.network.GameEvent;
import com.svamp.planetwars.network.GameHost;
import com.svamp.planetwars.network.PackageHeader;
import com.svamp.planetwars.sprite.ShipSprite;
import com.svamp.planetwars.sprite.StarSprite;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Eyecandy class containing all the fancy animations for flying ships.
 */
public class ShipMap implements DataPacketListener {
    //Thread safe set. RLY SLOW! todo: learn thread safety so we can do better.
    private final Set<ShipSprite> ships = new CopyOnWriteArraySet<ShipSprite>();
    private final StarMap starMap;
    private final AbstractGameCommunicator communicator;

    private static final String TAG = ShipMap.class.getCanonicalName();

    public ShipMap(AbstractGameCommunicator communicator,StarMap starMap) {
        this.communicator=communicator;
        this.starMap=starMap;
        if(communicator instanceof GameClient) ((GameClient) communicator).registerListener(this);
    }

    public void draw(GL10 glUnused, float[] mvpMatrix) {
        for(ShipSprite ship : ships) {
            ship.draw(glUnused, mvpMatrix);
        }
    }
    public void update(float dt) {
        for(ShipSprite ship : ships) {
            ship.update(dt);
        }
    }

    @Override
    public void receive(GameEvent packet) {
        switch (packet.getHeader()) {
            case FLEET_DISPATCHED:
                ByteBuffer buffer = ByteBuffer.wrap(packet.getPayload());
                StarSprite source = starMap.getStarWithHash(buffer.getInt());
                StarSprite target = starMap.getStarWithHash(buffer.getInt());
                Fleet fleet = new Fleet(buffer);
                //Make new ship sprite. This is only eyecandy. No star states change from this sprite.
                ShipSprite shipSprite = new ShipSprite(this,fleet);
                shipSprite.setSrc(source);
                shipSprite.setDest(target);
                ships.add(shipSprite);
                break;
        }
    }

    /**
     * Instruction in Host ordering a fleet to be sent.
     * Validates the move and subtracts the fleet from source.
     * It's up to Host to give fleet to target star on arrival.
     * @param buffer ByteBuffer describing the action.
     * @return True if this is an allowed action, false otherwise.
     */
    public boolean sendShips(ByteBuffer buffer) {
        StarSprite source = starMap.getStarWithHash(buffer.getInt());
        StarSprite target = starMap.getStarWithHash(buffer.getInt());
        //Dummy fleet.
        Fleet fleet = new Fleet(buffer);
        //Source same as target? Not allowed!
        if(source.equals(target)) return false;

        //Find fleet belonging to owner.
        Fleet starFleet = source.getBattleField().getFleetWithOwner(fleet.getOwner());

        /*
         * If the fleet in this star is too small to accommodate
         * splitting out the fleet ordered to be sent, just return false.
         * Also, return false if this player has no fleet here.
         */
        if(!fleet.isSubsetOf(starFleet)) {
            Log.d(TAG, "Error! Tried to send a starFleet the did not exist! Tried to send:" + fleet + " from fleet " + starFleet);
            return false;
        }
        //Remove the provided number of units from the starFleet.
        starFleet.subtract(fleet);

        //Send new state.
        GameEvent event = new GameEvent(PackageHeader.STAR_STATE_CHANGED,fleet.getOwner());
        ByteBuffer starBuffer = ByteBuffer.allocate(3+source.getSerializedSize());
        starBuffer.putShort((short)1); //Number of stars affected
        starBuffer.put((byte)1); //Severity.
        starBuffer.put(source.getSerialization());
        event.setPayload(starBuffer.array());
        communicator.sendData(event.toByteArray());
        return true;
    }

    public void shipArrived(ShipSprite ship) {
        ships.remove(ship);
        //If this map is tied to a host, update some state. Notify host of change.
        if(communicator instanceof GameHost) {
            Log.d(TAG,"Host registered that a ship had arrived.");
            StarSprite dest = ship.getDest();
            dest.getBattleField().addFleet(ship.getFleet());
            starMap.fireStarStateChanged(1,dest);
        }
    }
}
