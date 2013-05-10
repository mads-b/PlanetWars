package com.svamp.planetwars.network;

/**
 * Generic DatagramPackage headers for communication
 */
public enum PackageHeader {
    //Enums in use during initial connection.
    BROADCAST_IP, //Host broadcasts its IP address.
    REQUEST_CONNECTION, //Client requests connection
    DISCONNECTED, //Client disconnects..
    CONNECTION_REFUSED, //Host refuses connection
    CONNECTION_REFUSED_SERVER_FULL, //Same as above, with valid reason.
    REQUEST_GAME_START, //Client requests starting the game.
    REQUEST_MAP, //Client requesting map.
    GAME_START, //Host notifying clients of game start.
    //Headers with additional payload
    REQUEST_PLAYER_DATA, //Host requests player data. Incomplete player object in payload (with valid ID).
    SUBMITTED_PLAYER_DATA, //Player data payload submitted. Host & client.
    FLEET_DISPATCHED, //Sent by host & client.
    NEW_BUILD_ORDERS, //Sent from a client to order what to build at a star.
    STAR_STATE_CHANGED, //Sent by host.
    PING, //Host poll
    PONG; //Client answer

    private static final PackageHeader[] cache = PackageHeader.values();

    public byte valueOf() { return (byte) this.ordinal(); }

    public static PackageHeader get(byte b) {
        return cache[b];
    }
}
