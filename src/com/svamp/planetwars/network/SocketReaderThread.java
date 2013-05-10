package com.svamp.planetwars.network;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 */
class SocketReaderThread extends Thread {
    private boolean running;
    private final DatagramSocket socket;
    private final AbstractGameCommunicator receiver;
    private static final String TAG = "com.svamp.SocketReaderThread";

    public SocketReaderThread(DatagramSocket socket,AbstractGameCommunicator receiver) {
        this.socket=socket;
        this.receiver=receiver;
    }

    @Override
    public void run() {
        while(running) {
            byte[] buf = new byte[4096];
            DatagramPacket pack = new DatagramPacket(buf,buf.length);
            try {
                socket.receive(pack);
                receiver.receiveData(pack);
            } catch (IOException e) {
                if(running) {
                    Log.e(TAG,"Error occured while receiving packages",e);
                }
            }
        }
    }

    @Override
    public void start() {
        running=true;
        super.start();
    }

    public void finishAndStop() {
        running=false;
    }
}
