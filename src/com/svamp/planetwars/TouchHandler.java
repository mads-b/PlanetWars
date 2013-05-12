package com.svamp.planetwars;

import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import com.svamp.planetwars.math.TouchCallback;
import com.svamp.planetwars.math.Vector;

class TouchHandler {
    //Maximum time in ms a touch can last. All gestures lasting less than this will be interpreted as a touch
    private final static int TOUCH_MAX_TIME_DOWN_MS =300;
    //Store instance of starView for touch callback
    private final TouchCallback callback;
    // We can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    // Remember some things for zooming
    private final Vector start = new Vector(0,0);
    private final Vector mid = new Vector(0,0);
    private float oldDist = 1f;

    //Constantly remember how far we have moved in the DRAG mode
    private final Vector motion=new Vector(0,0);
    //Temp vector to avoid initializing often.
    private final Vector tmp = new Vector(0,0);

    public TouchHandler(TouchCallback callback) {
        this.callback = callback;
    }

    /**
     * StarView noticed user input. Do the necessary operations. This method is a MESS!
     * TODO: Clean up
     * @param event User input event (screen pixel coords)
     */
    public void onTouch(MotionEvent event) {
        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: //One finger touch
                start.set(event.getX(), event.getY());
                motion.set(0,0);
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // >one finger touch
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    mode = ZOOM;
                }
                break;
            //Removed fingers from screen
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                tmp.set(event.getX(), event.getY());
                //Touch time less than max time. Notify GameEngine of click
                if(event.getEventTime()-event.getDownTime()< TOUCH_MAX_TIME_DOWN_MS) {
                    callback.touched(tmp);
                }
                //Not a click. Check if finger has moved so little that it counts as a long click. Also, a long click is defined as a drag action.
                else if (motion.lengthSq()<100 && mode==DRAG) {
                    callback.longTouched(tmp);
                }

                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    //Inform gEngine of motion since last message.
                    tmp.set(event.getX()-motion.x-start.x,event.getY()-motion.y-start.y);
                    callback.move(new Vector(start),new Vector(tmp));
                    //Register grand total moved.
                    motion.set(event.getX() - start.x, event.getY() - start.y);

                }
                else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        float scale = newDist / oldDist;
                        midPoint(mid, event);
                        callback.scale(mid,scale);
                        oldDist=newDist;
                    }
                }
                break;
        }
    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(Vector point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}




