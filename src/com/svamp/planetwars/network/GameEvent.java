package com.svamp.planetwars.network;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Game package with some fields and methods for creating and dissecting the byte arrays transmitted over the internet.
 * The contents of this class define the UDP package data structure.
 */
public class GameEvent {
    //User id responsible for change.
    private byte userId=-1;
    //Type of event. the event type determines additional data.
    private final PackageHeader type;

    private byte[] payload = new byte[0];

    private static final String TAG = "com.svamp.network.GameEvent";

    /**
     * Construct GameEvent from serialized data.
     * @param data Data to wrap in this class.
     */
    public GameEvent(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        //Set the fields always present:
        this.type = PackageHeader.get(buffer.get());
        this.userId=buffer.get();
        //We have stripped out the header info. Put the rest in payload.
        payload = new byte[buffer.remaining()];
        buffer.get(payload);
    }

    /**
     * Construct GameEvent from existing data.
     * @param type Type of event
     * @param user User creating package.
     */
    public GameEvent(PackageHeader type,Player user) {
        this.type=type;
        if(user!=null)
            this.userId=user.getPlayerNum();
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(2+payload.length);
        //Add tick number, userId and package type to buffer. These will always exist. 6 bytes, no spacers.
        //Ordinal is reduced to a byte to save space.
        buffer.put(type.valueOf()).put(userId);
        buffer.put(payload);
        return buffer.compact().array();
    }

    /**
     * Make a datagrampacket with this event to send to the provided address.
     * @param addr Address to send to
     * @return DatagramPacket containing this serialized event, addressed to the provided recipient.
     */
    public DatagramPacket makePacket(SocketAddress addr) {
        byte[] data = toByteArray();
        try {
            return new DatagramPacket(data,data.length,addr);
        } catch (SocketException e) {
            Log.e(TAG, "Error occurred making package.", e);
            return null;
        }
    }

    public PackageHeader getHeader() { return type; }
    public byte getUserId() { return userId; }
    public byte[] getPayload() { return payload; }

    public void setPayload(byte[] payload) { this.payload=payload; }

    @Override
    public String toString() {
        return "[GameEvent: " +
                " type: "+type.toString()+
                ", userId: "+userId+
                ", payload: "+ Arrays.toString(payload)+"]";
    }
}
