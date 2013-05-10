package com.svamp.planetwars.network;

import java.nio.ByteBuffer;

/**
 *
 */
public interface ByteSerializeable {
    /**
     * @return The Serialized form of this object.
     *     This serialization should be redable for the "updateFromSerialization" method.
     */
    byte[] getSerialization();

    /**
     * Updates this object's state given a byteBuffer with its pointer placed on the
     * start of the bytes relevant for this object. It is expected of the object to leave the byteBuffer pointer
     * on the end of its data, ready for the next object's data.
     * Serialized format is exactly the same as the format returned from getSerialization().
     * @param buffer Buffer to read from.
     */
    void updateFromSerialization(ByteBuffer buffer);

    /**
     * Gets the size of the array returned by getSerialization()
     * @return serialized size, in bytes.
     */
    int getSerializedSize();
}
