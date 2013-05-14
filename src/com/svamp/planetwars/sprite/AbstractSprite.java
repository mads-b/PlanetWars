package com.svamp.planetwars.sprite;

import android.graphics.RectF;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.network.ByteSerializeable;

import java.nio.ByteBuffer;

/*
 * This abstract class will handle all the bounds modification.
 * Remember to call super at all times when calling methods overriding this class!
 */

public abstract class AbstractSprite implements Sprite,ByteSerializeable {
    //This sprite's position and size in the "world". This value is set only once if the sprite is not moving.
    protected final RectF bounds = new RectF(0,0,0,0);

    private static final String TAG = AbstractSprite.class.getCanonicalName();

    //Serialization content: hash
    private int elementHash=-1;

    public void move(float dx, float dy) {
        this.bounds.offset(dx, dy);
        updateVertices();
    }
    public void setPos(float x, float y) {
        this.bounds.set(x,y,x+bounds.width(),y+bounds.height());
        updateVertices();
    }

    public void setSize(float width, float height) {
        this.bounds.set(bounds.left, bounds.top, bounds.left+width, bounds.top+height);
        updateVertices();
    }

    @Override
    public void update(float dt) {}

    @Override
    public boolean contains(Vector pos) {
        return bounds.contains(pos.x,pos.y);
    }

    @Override
    public RectF getBounds() { return bounds; }

    @Override
    public byte[] getSerialization() {
        //Make sure this element has a hash before serializing...
        if(elementHash==-1) throw new IllegalStateException("Tried to serialize an object with no hash set!");

        ByteBuffer buffer = ByteBuffer.allocate(20);
        //Insert hash
        buffer.putInt(elementHash);
        //Put coordinates. 4*4=16 bytes
        buffer.putFloat(bounds.left).putFloat(bounds.top).putFloat(bounds.right).putFloat(bounds.bottom);
        return buffer.array();
    }

    @Override
    public void updateFromSerialization(ByteBuffer buffer) {
        //Make sure pointer points at this object's hash!
        int hash = buffer.getInt();
        if(hash==-1) throw new IllegalArgumentException("Tried to update sprite having a -1 hash!");
        if(hash!=elementHash) throw new IllegalStateException("Bytebuffer with wrongly placed pointer passed to element!");
        //Set bounds.
        bounds.left=buffer.getFloat();
        bounds.top=buffer.getFloat();
        bounds.right=buffer.getFloat();
        bounds.bottom=buffer.getFloat();
        updateVertices();
    }

    public void setElementHash(int hash) {
        this.elementHash=hash;
    }
    public int getElementHash() { return elementHash; }

    @Override
    public int getSerializedSize() {
        return 20;
    }

    /**
     * Called every time sprite has moved or changed shape,
     * requiring repositioning of the vertices.
     */
    protected abstract void updateVertices();


}
