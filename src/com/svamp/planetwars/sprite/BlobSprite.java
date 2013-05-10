package com.svamp.planetwars.sprite;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.svamp.planetwars.math.Vector;

import javax.microedition.khronos.opengles.GL10;
import java.util.List;

/**
 */
public class BlobSprite extends AbstractSprite {
    private Path path = new Path();
    private Paint pathPaint = new Paint();

    /**
     * Initializes a blob (defined by an outline) of any shape
     * @param outline Outline defining blob shape.
     * @param color Color of the outline. @see Color class.
     */
    public BlobSprite(List<Vector> outline,int color) {
        pathPaint.setColor(color);
        pathPaint.setStyle(Paint.Style.STROKE);
        //pathPaint.setPathEffect(new CornerPathEffect(50));
        pathPaint.setPathEffect(new DashPathEffect(new float[] {30,10},0));
        pathPaint.setStrokeWidth(20);
        //path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(outline.get(0).x,outline.get(0).y);
        for(Vector v : outline) {
            path.lineTo(v.x,v.y);
        }
        //path.lineTo(outline.get(0).x, outline.get(0).y);

        RectF bounds = new RectF(0,0,0,0);
        path.computeBounds(bounds,false);
        this.bounds.set(bounds);
    }

    @Override
    public void draw(float[] mvpMatrix) {

    }
}
