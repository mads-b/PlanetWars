package com.svamp.planetwars;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.sprite.AbstractSprite;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.Arrays;

class GameRenderer implements GLSurfaceView.Renderer {
    private GameEngine gEngine;
    private Context context;
    private float[] projMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] pvMatrix = new float[16];
    private Vector screendims = new Vector(0,0);

    private float scalation;

    private static final String TAG = "com.svamp.GameRenderer";
    //Time between updates, in seconds.
    private static final long UPDATE_INTERVAL_MS = 70;
    private long startTime = System.currentTimeMillis();

    //state of game (Running or Paused).
    int state = RUNNING;
    public final static int RUNNING = 1;
    public final static int STOPPED = 2;

    public GameRenderer(SurfaceHolder surfaceHolder, Context context, Handler handler, GameEngine gEngine) {
        //data about the screen
        this.context = context;
        this.gEngine = gEngine;
        Log.d(TAG,"GameRenderer instantiated.");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Enable blending using premultiplied alpha.
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glClearColor(0f,0f,0f,0f);
        Log.d(TAG, "SurfaceCreated.");
        Matrix.setLookAtM(viewMatrix, 0, 0, 0f, 2.5f, 0, 0f, 0, 0f, 1f, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        screendims.set(width, height);
        //Just rescale..
        GLES20.glViewport(0, 0, width, height);
        Log.d(TAG,"SurfaceChanged");
        float ratio = (float) width / height;
        Log.d(TAG,"Ratio: "+ratio);
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1, 1, 1f, 5f);

        Matrix.multiplyMM(pvMatrix, 0, projMatrix, 0, viewMatrix, 0);
        // Preload all shaders.
        AbstractSprite.initShaders(context);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Matrix.multiplyMM(pvMatrix, 0, projMatrix, 0, viewMatrix, 0);
        //Matrix.rotateM(viewMatrix,0,-.5f,.0f,.0f,1);
        // Timer block to limit framerate.
        long endTime = System.currentTimeMillis();
        long dt = endTime - startTime;
        if (dt < UPDATE_INTERVAL_MS)
            try {
                Thread.sleep(UPDATE_INTERVAL_MS - dt);
            } catch (InterruptedException ignored) { }
        startTime = System.currentTimeMillis();

        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gEngine.update(dt/1000f);
        gEngine.draw(pvMatrix);
    }


    public void move(Vector start, Vector dist) {
        Vector end = new Vector(start);
        end.add(dist.x, dist.y);
        scaleToGameCoords(start);
        scaleToGameCoords(end);
        Matrix.translateM(viewMatrix,0,end.x-start.x,end.y-start.y,0);
    }

    public void scale(Vector center, float degree) {
        scaleToGameCoords(center);
        Matrix.translateM(viewMatrix,0,0,0,degree-1);
    }


    /**
     * Scale screen coords to game coords using the VP matrix.
     * @param pos Screen coords to scale. Float array of length 4.
     */
    public void scaleToGameCoords(Vector pos) {
        float[] res = new float[4];
        float[] res2 = new float[4];
        GLU.gluUnProject(pos.x,screendims.y-pos.y,-1,
                viewMatrix,0,
                projMatrix,0,
                new int[] {0,0,(int) screendims.x, (int) screendims.y},0,
                res,0);
        GLU.gluUnProject(pos.x,screendims.y-pos.y,1,
                viewMatrix,0,
                projMatrix,0,
                new int[] {0,0,(int) screendims.x, (int) screendims.y},0,
                res2,0);
        res[0] /= res[3];
        res[1] /= res[3];
        res[2] /= res[3];
        res2[0] /= res2[3];
        res2[1] /= res2[3];
        res2[2] /= res2[3];

        //Compute where the line through these two points intersect the z=0 plane:
        float t = res[2]/(res[2]-res2[2]);
        pos.set(res[0]+t*(res2[0]-res[0]),res[1]+t*(res2[1]-res[1]));
    }
}
