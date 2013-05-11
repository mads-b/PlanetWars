package com.svamp.planetwars.network;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Simple thread module that
 */
class SocketWriterThread extends Thread {
    private boolean running;
    private final DatagramSocket socket;
    private final BlockingQueue<DatagramPacket> packets = new LinkedBlockingQueue<DatagramPacket>();
    private static final String TAG = SocketWriterThread.class.getCanonicalName();


    public SocketWriterThread(DatagramSocket socket) {
        this.socket=socket;
    }

    @Override
    public void run() {
        while(running) {
            try {
                socket.send(packets.take());
            } catch (IOException e) {
                Log.e(TAG,"Error occured while receiving package",e);
            } catch (InterruptedException e) {
                Log.d(TAG,"Interrupted while trying to fetch packets from send queue. " +
                        "Shutting down.. Dropped packets: "+packets.size());
            }
        }
    }

    public void send(DatagramPacket packet) {
        packets.add(packet);
    }

    @Override
    public void start() {
        running=true;
        super.start();
    }

    public void finishAndStop() {
        int waits =0;
        while(packets.size()!=0 && waits<10) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
            waits++;
        }
        running=false;
        super.interrupt();
    }
}
