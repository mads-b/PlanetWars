package com.svamp.planetwars.network;

/**
 * Simple interface for all listeners intending to receive packages though the abstractGameCommunicator.
 */
public interface DataPacketListener {
    void receive(GameEvent packet);
}
