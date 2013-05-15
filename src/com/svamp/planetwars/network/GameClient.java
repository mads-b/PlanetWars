package com.svamp.planetwars.network;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 */
public class GameClient extends AbstractGameCommunicator {
    private final InetSocketAddress host;
    //The networking module's internal representation of this user..
    private Player curPlayer = new Player((byte) 1);
    //List of all players except ours.
    private final Set<Player> peers = new HashSet<Player>();
    private final Set<DataPacketListener> listeners = Collections.synchronizedSet(new HashSet<DataPacketListener>());

    private static final String TAG = GameClient.class.getCanonicalName();

    /**
     * Constructor for the networker.
     * @param portNum Port to initialize the networker on.
     * @param host Address to host
     * @throws IOException If host is unreachable.
     */
    public GameClient(int portNum,InetSocketAddress host) throws IOException {
        super(portNum);
        new Vector();
        this.host=host;
        if(!host.getAddress().isReachable(5000))
            throw new IOException("Host is unreachable!");
        else
            Log.d(TAG,"Client up. address is reachable.");
    }

    @Override
    public void sendData(byte[] data) {
        try {
            sendData(new DatagramPacket(data,data.length,host));
        } catch (SocketException e) {
            Log.e(TAG, "Failed to send string \"" + Arrays.toString(data) + "\" to " + host.toString(), e);
        }
    }

    @Override
    public void receiveData(DatagramPacket pack) { //Package received from host!
        //Wrap the package in a GameEvent:
        GameEvent event = new GameEvent(pack.getData());
        switch (event.getHeader()) {
            case DISCONNECTED: //Another player has disconnected.
                //Remove player from peers list.
                peers.remove(new Player(event.getUserId()));
                break;
            //Refused. We'll disconnect then..
            case CONNECTION_REFUSED:case CONNECTION_REFUSED_SERVER_FULL:
                fireGameEvent(event); //Tell listeners of this.
                GameEvent disconnect = new GameEvent(PackageHeader.DISCONNECTED,curPlayer);
                sendData(disconnect.toByteArray()); //Send disconnect signal.
                this.stop();
                break;

            case REQUEST_PLAYER_DATA:
                //Payload is a user object with valid player number attached.
                Player dummyPlayer = new Player(event.getPayload());
                //Update our player object.
                curPlayer.setPlayerNum(dummyPlayer.getPlayerNum());

                GameEvent response = new GameEvent(PackageHeader.SUBMITTED_PLAYER_DATA,curPlayer);
                response.setPayload(curPlayer.getSerialization());
                sendData(response.toByteArray());
                break;
            case SUBMITTED_PLAYER_DATA:
                Player newData = new Player(event.getPayload());
                if(curPlayer.equals(newData)) //Got a new representation of ourselves.
                    curPlayer=newData;
                else { //Else, update peer data..
                peers.remove(newData);
                peers.add(newData);
                }
                fireGameEvent(event);
                break;
            case FLEET_DISPATCHED: case STAR_STATE_CHANGED:
                fireGameEvent(event); //Listeners need this data.
                break;
            case GAME_START:
                fireGameEvent(event);
                break;
            case PING:
                GameEvent pong = new GameEvent(PackageHeader.PONG,curPlayer);
                sendData(pong.toByteArray());
                break;
            default:
                throw new IllegalArgumentException("Received game package from host with illegal header:"+event.toString());
        }
    }

    @Override
    public Collection<Player> getPeers() {
        return peers;
    }

    public Player getPlayer() {
        return curPlayer;
    }

    public void registerListener(DataPacketListener listener) {
        this.listeners.add(listener);
    }
    public void unregisterListener(DataPacketListener listener) {
        this.listeners.remove(listener);
    }

    void fireGameEvent(GameEvent event) {
        for(DataPacketListener l : listeners) {
            l.receive(event);
        }
    }
}
