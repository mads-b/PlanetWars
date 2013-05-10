package com.svamp.planetwars.math;

/**
 * Listener for touch events
 */
public interface TouchCallback {
    public void touched(Vector pos);
    public void longTouched(Vector pos);
    public void move(Vector start,Vector dist);
    public void scale(Vector center, float degree);
}
