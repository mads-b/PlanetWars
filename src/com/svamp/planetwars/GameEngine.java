package com.svamp.planetwars;

import android.content.res.Resources;
import android.util.Log;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.network.DataPacketListener;
import com.svamp.planetwars.network.GameClient;
import com.svamp.planetwars.network.GameEvent;
import com.svamp.planetwars.network.PackageHeader;
import com.svamp.planetwars.network.Player;
import com.svamp.planetwars.sprite.StarSprite;
import com.svamp.planetwars.sprite.hud.Hud;

import javax.microedition.khronos.opengles.GL10;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameEngine implements DataPacketListener {
    private StarMap starMap;
    private ShipMap shipMap;
    private Hud hud;
    private StarSprite lastSelectedSource;
    private StarSprite lastSelectedTarget;

    //static pointer to current player..
    private static Player curPlayer;
    //Map containing all players in the game.
    private static final Map<Integer,Player> players = new HashMap<Integer,Player>();
    //Our networker
    private final GameClient communicator;

    public GameEngine(GameClient communicator) {
        curPlayer=communicator.getPlayer();
        players.put(curPlayer.getElementHash(),curPlayer);
        players.put(Player.getNeutral().getElementHash(),Player.getNeutral());
        for(Player player : communicator.getPeers())
            players.put(player.getElementHash(),player);

        //Make GameEngine receive game events
        communicator.registerListener(this);
        this.communicator=communicator;
        //Build game
        starMap = new StarMap(communicator);
        shipMap = new ShipMap(communicator,starMap);

        hud = new Hud(this);
        //Hud is 30% of horizontal size, 100% of vertical size.
        hud.setSize(.6f,2);
        //Request map:
        GameEvent event = new GameEvent(PackageHeader.REQUEST_MAP,curPlayer);
        communicator.sendData(event.toByteArray());
    }

    /**
     * Update call thrown from GameRenderer. Called synchronously with draw(). Update positions and animations.
     * @param dt Time passed since last call.
     */
    public void update(float dt) {
        //NEVER UPDATE STARMAP HERE! STARS ARE STATIC; AND ONLY TO BE MANUALLY CHANGED FROM HOST!
        shipMap.update(dt);
        hud.update(dt);
    }

    /**
     * Draw call thrown from GameRenderer. Called synchronously with update().
     * @param mvpMatrix Transformation matrix.
     */
    public void draw(GL10 glUnused, float[] mvpMatrix) {
        starMap.draw(glUnused, mvpMatrix);
        shipMap.draw(glUnused, mvpMatrix);
        hud.draw(glUnused, mvpMatrix);
    }

    /**
     * Click. Set the star clicked to "target"
     * @param pos Position vector in world coordinates.
     * @return true if HUD was touched, false otherwise.
     */
    public void touched(Vector pos) {
        StarSprite star = starMap.getStarAtPosition(pos);
        if(star==null) return;
        Log.d(GameEngine.class.getCanonicalName(),"Selected star: "+star.toString()+" owned by: "+star.getOwnership().getPlayerName());
        if(lastSelectedTarget!=null) lastSelectedTarget.setSelected(false,false);
        lastSelectedTarget = star;
        lastSelectedTarget.setSelected(true,false);
        hud.selectionChanged();
    }

    /**
     * Long press. Set the star long clicked to "source"
     * @param pos Position vector in world coordinates.
     */
    public void longTouched(Vector pos) {
        StarSprite star = starMap.getStarAtPosition(pos);
        if(star==null) return;
        if(lastSelectedSource!=null) lastSelectedSource.setSelected(false,false);
        lastSelectedSource = star;
        lastSelectedSource.setSelected(true,true);
        hud.selectionChanged();
    }

    public StarSprite getLastSelectedSource() { return lastSelectedSource; }
    public StarSprite getLastSelectedTarget() { return lastSelectedTarget; }
    public GameClient getClient() { return communicator; }

    /**
     * @return The player on this device.
     */
    public static Player getPlayer() { return curPlayer; }
    public static Player getPlayer(int hash) { return players.get(hash); }
    public static Collection<Player> getPlayers() { return players.values(); }

    @Override
    public void receive(GameEvent packet) {
        //TODO: Listen to what?
    }

    public Hud getHud() { return hud; }
}
