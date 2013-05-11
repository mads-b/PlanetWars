package com.svamp.planetwars.network;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Comparator;

/**
 * Class uniquely identifying every player. Two player objects are equal if they have the same unique ID
 */
public class Player implements ByteSerializeable,Comparator<Player> {
    private int uniqueID = (int) (Integer.MAX_VALUE*Math.random());

    private byte playerNum=1;
    private String userName = "Tony Stark";
    //Helper field used by gameHost. not for internal use.
    public boolean gameStartRequested=false;

    private static Player neutral;

    public static Player getNeutral() {
        if(neutral==null) {
            neutral=new Player((byte) 0);
            neutral.userName="Neutral";
            neutral.uniqueID=1234567;
        }
        return neutral;
    }

    public Player(byte playerNum) {
        this.playerNum=playerNum;
    }

    /**
     * Make new player object based on serialization.
     * @param serialization Serialization to build player object from.
     */
    public Player(byte[] serialization) {
        this.updateFromSerialization(ByteBuffer.wrap(serialization));
    }

    public byte getPlayerNum() {
        return playerNum;
    }
    public void setPlayerNum(byte playerNum) { this.playerNum=playerNum; }

    @Override
    public int compare(Player lhs, Player rhs) {
        return lhs.playerNum-rhs.playerNum;
    }

    public boolean equals(Object obj) {
        return obj instanceof Player && this.getElementHash() == ((Player) obj).getElementHash();
    }

    public int hashCode(){
        return uniqueID;
    }

    @Override
    public byte[] getSerialization() {
        byte[] userBytes = new byte[0];
        try {
            userBytes = userName.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ignored) {} //Never happens. UTF-8 is always supported.
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        buffer.putInt(uniqueID).put(playerNum).put((byte) userBytes.length).put(userBytes);
        return buffer.array();
    }

    @Override
    public void updateFromSerialization(ByteBuffer buffer) {
        this.uniqueID=buffer.getInt();
        this.playerNum = buffer.get();
        byte nameLength = buffer.get(); //Get length of the following string..
        byte[] nameBytes = new byte[nameLength]; //Init byte array.
        buffer.get(nameBytes,0,nameLength); //Copy string into array
        try {
            this.userName = new String(nameBytes,"UTF-8"); //Parse string
        } catch (UnsupportedEncodingException ignored) {} //Never happens. UTF-8 is always supported.
    }

    public void setElementHash(int hash) {
        this.uniqueID=hash;
    }

    public int getElementHash() {
        return uniqueID;
    }

    public String getPlayerName() {
        return userName;
    }

    @Override
    public int getSerializedSize() {
        return 6+userName.length();
    }

    public String toString() {
        return playerNum+" - "+userName;
    }
}
