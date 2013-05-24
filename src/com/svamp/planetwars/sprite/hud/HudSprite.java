package com.svamp.planetwars.sprite.hud;

import com.svamp.planetwars.sprite.AbstractSquareSprite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is nearly identical to its superclass,
 * but supports ordering of the sprite based on a new z-Level.
 * Note: Because these sprites need to be ordered according to Z-level,
 * their draw methods MUST contain the draw calls to the base sprite ONLY!
 */
public class HudSprite extends AbstractSquareSprite implements Comparable<HudSprite> {
    private float zVal;

    public void setZVal(float zVal) {
        this.zVal = zVal;
        updateVertices();
    }

    @Override
    protected void updateVertices() {
        vertexBuffer
                .put(bounds.left).put(bounds.bottom).put(zVal)
                .put(bounds.left).put(bounds.top).put(zVal)
                .put(bounds.right).put(bounds.top).put(zVal)
                .put(bounds.right).put(bounds.bottom).put(zVal);
        vertexBuffer.rewind();
    }

    /**
     * This method allows for extraction of sprites contained within another sprite.
     * Note: This base method only returns a collection of itself.
     * Any composite sprites must extend this method.
     * @return All the sprites contained within this sprite, including itself.
     */
    public Collection<HudSprite> getSprites() {
        List<HudSprite> spriteList = new ArrayList<HudSprite>();
        spriteList.add(this);
        return spriteList;
    }

    @Override
    public int compareTo(HudSprite s) {
        // The small addition is here to ensure no two sprites evaluate as equal.
        return (int)(10000*(zVal-s.zVal+0.0001f));
    }
}
