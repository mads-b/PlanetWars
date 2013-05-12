package com.svamp.planetwars.math;

/**
 * Listener for touch events
 */
public interface TouchCallback {
    /**
     * Callback from TouchHandler used to notify the listener about a
     * point on the screen being pressed.
     * @param pos Position touched, in screen pixel coordinates
     */
    public void touched(Vector pos);

    /**
     * Callback from TouchHandler used to notify the listener about a
     * point on the screen being long pressed.
     * @param pos Position long touched, in screen pixel coordinates.
     */
    public void longTouched(Vector pos);
    /**
     * Callback from TouchHandler used to notify the listener about the
     * user dragging his finger across the screen.
     * @param start Position the dragging action originated from. Screen pixel coordinates.
     * @param dist Distance dragged. This is a vector originating from the starting position.
     */
    public void move(Vector start,Vector dist);

    /**
     * Callback from TouchHandler used to notify the listener about a two finger pinch.
     * The degree of the pinch is calculated as follows: No pinch gives a degree of 1.
     * If the fingers were spaced 100 pixels apart, but are now 50 pixels apart, degree equals 2.
     * This means: degree>1 implies zoom out, and degree<1 implies zoom in.
     *
     * @param center Center position (point between fingers on screen), pixel coordinates.
     * @param degree Degree of pinch.
     */
    public void scale(Vector center, float degree);
}
