package com.svamp.planetwars.sprite;

import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;
import com.svamp.planetwars.R;
import com.svamp.planetwars.math.Vector;

import javax.microedition.khronos.opengles.GL10;
import java.util.Arrays;
import java.util.Collection;


public class BlobSprite extends AbstractLineSprite {
    private float[] color = new float[4];

    private static final String TAG = BlobSprite.class.getCanonicalName();

    public BlobSprite(Collection<Vector> line,int color) {
        super(line);
        this.color[0] = Color.red(color)/255f;
        this.color[1] = Color.green(color)/255f;
        this.color[2] = Color.blue(color)/255f;
        this.color[3] = Color.alpha(color)/255f;
    }

    @Override
    public void draw(GL10 glUnused, float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgramHandle);
        //Handle to line positions
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //Apply color!
        GLES20.glUniform4f(mColorHandle,color[0],color[1],color[2],color[3]);
        super.draw(glUnused, mvpMatrix);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
