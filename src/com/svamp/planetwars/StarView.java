package com.svamp.planetwars;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.svamp.planetwars.math.TouchCallback;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.network.GameClient;

public class StarView extends GLSurfaceView implements TouchCallback {
    //Game engine
    private GameEngine gEngine;

    //objects which house info about the screen
    private final Context context;

    //our Thread class which houses the game loop
    private GameRenderer renderer;

    private final TouchHandler touchHandler = new TouchHandler(this);

    private static final String TAG = GameRenderer.class.getCanonicalName();

    public StarView(Context context) {
        super(context);
        this.context=context;
    }

    public StarView(Context context, AttributeSet attrs) {
        super(context,attrs);
        this.context=context;
    }

    //initialization code
    void initView(GameClient communicator){
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);


        //initialize our game engine, send along pointer to this player.
        gEngine = new GameEngine(communicator);
        gEngine.init(context.getResources());

        //initialize our Thread class.
        renderer = new GameRenderer(getHolder(), new Handler(), gEngine);
        setRenderer(renderer);
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setDebugFlags(DEBUG_LOG_GL_CALLS);
        Log.d(TAG,"GameRenderer initialized.");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchHandler.onTouch(event);
        return true;
    }

    /*
     * Callback methods from touchHandler.
     */
    @Override
    public void touched(Vector pos) {
        renderer.scaleToGameCoords(pos);
        Log.d(TAG, "Touched z=0 plane at "+pos);
        gEngine.touched(pos);
    }

    @Override
    public void longTouched(Vector pos) {
        renderer.scaleToGameCoords(pos);
        Log.d(TAG, "LongTouched z=0 plane at "+pos);
        gEngine.longTouched(pos);
    }

    @Override
    public void move(Vector start, Vector dist) {
        renderer.move(start, dist);
    }

    @Override
    public void scale(Vector center, float degree) {
        renderer.scale(center, degree);
    }
}
