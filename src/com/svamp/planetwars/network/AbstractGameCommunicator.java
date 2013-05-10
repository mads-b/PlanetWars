package com.svamp.planetwars.network;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collection;

/**
 * Main class for game client networking. Can be host or client.
 * Supports asynchronous reading and writing.
 *
 * All UDP packages sent have one of the following bytes as headers:
 *
 */
public abstract class AbstractGameCommunicator {
    private final int port;
    private DatagramSocket socket;
    private SocketReaderThread reader;
    private SocketWriterThread writer;


    private static final String TAG = "com.svamp.AbstractGameCommunicator";

    /**
     * Constructor for the networker.
     * @param portNum Port to initialize the networker on.
     */
    AbstractGameCommunicator(int portNum) {
        this.port=portNum;
    }

    /**
     * Starts the sommunicator. Will wait for host to respond for 5 seconds
     * @throws IOException If host cannot be reached
     */
    public void start() throws IOException {
        try {
            socket = new DatagramSocket(port);
            socket.setBroadcast(true); //Broadcast for some packets.
            //Make reader and writer
            reader = new SocketReaderThread(socket,this);
            writer = new SocketWriterThread(socket);

        } catch (SocketException e) {
            Log.e(TAG,"Failed while making a new socket",e);
        }
        reader.start();
        writer.start();
        Log.d(TAG,"Networking module running on port "+port);
    }

    public void stop() {
        if(reader!=null)
            reader.finishAndStop();
        if(writer!=null)
            writer.finishAndStop();
        if(socket!=null) {
            socket.close();
        }
        Log.d(TAG,"Networking module stopped. Socket closed.");
    }

    void sendData(DatagramPacket pack) {
        GameEvent event = new GameEvent(pack.getData());
        if(event.getHeader()!=PackageHeader.PING && event.getHeader()!=PackageHeader.PONG)
            Log.d(TAG,"Sending package: "+event.getHeader().toString()+" of length "+pack.getLength());
        writer.send(pack);
    }

    public abstract void sendData(byte[] data);
    public abstract void receiveData(DatagramPacket pack);

    public int getPort() {
        return socket.getPort();
    }

    public abstract Collection<Player> getPeers();
}
