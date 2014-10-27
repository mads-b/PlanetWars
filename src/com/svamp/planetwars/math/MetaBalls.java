package com.svamp.planetwars.math;

import android.graphics.RectF;
import android.util.Log;
import com.svamp.planetwars.network.Player;
import com.svamp.planetwars.sprite.StarSprite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MetaBalls {
    private final static int RESOLUTION = 80;
    private final MarchingSquares squares;
    public MetaBalls(RectF dimensions,Collection<StarSprite> starSprites) {
        Log.d("com.svamp.math.MetaBalls","Making metaballs on domain "+dimensions.toShortString());
        Sphere[] spheres = new Sphere[starSprites.size()];
        int i=0;
        for(StarSprite s : starSprites) {
            spheres[i] = new Sphere(
                    new Vector(s.getBounds().centerX(),s.getBounds().centerY()),
                    s.getBounds().height(),
                    s.getOwnership().getElementHash());
            i++;
        }
        squares = new MarchingSquares(new PowerFunction(spheres),dimensions,RESOLUTION);
    }

    public Collection<Vector> getBlobsFor(Player p) {
        long start = System.currentTimeMillis();
        Collection<Vector> result = squares.computeLines(0.2f,p.getElementHash());
        Log.d("com.svamp.math.MetaBalls","Computed blobs for "+p.getPlayerName()+" in "+(System.currentTimeMillis()-start)+"ms.");
        return result;
    }

    private class Sphere {
        private final Vector origin;
        private final float radius;
        private final int setID;
        private Sphere(Vector origin, float radius,int setID) {
            this.origin=origin;
            this.radius=radius;
            this.setID=setID;
        }
    }

    private class PowerFunction {
        private final static float EPSILON = 10e-6f;
        private final Sphere[] spheres;
        private PowerFunction(Sphere[] spheres) {
            this.spheres=spheres;
        }
        float getValue(Vector pos,int sphereSetId) {
            float accum = 0;
            for(Sphere s : spheres) {
                float dist = s.origin.distanceToSq(pos);
                float radSq = s.radius*s.radius;
                if(dist<EPSILON) dist = EPSILON;
                if(s.setID == sphereSetId) {
                    accum += radSq/dist;
                } else {
                    accum -= radSq/dist;
                }
            }
            return accum;
        }
    }

    private class MarchingSquares {
        private final PowerFunction func;
        private final int resolution;
        private final Vector[][] grid;
        private final float[][] scalars;
        private final int[] edgeTable = {
                0x0, 0x5, 0x3, 0x6,
                0xC, 0x9, 0xF, 0xA,
                0xA, 0xF, 0x9, 0xC,
                0x6, 0x3, 0x5, 0x0
        };
        private final int[][] lineTable = {
                {-1, -1, -1, -1, -1}, /*       0 nothing */
                { 4,  6, -1, -1, -1}, /*       1 inside */
                { 5,  4, -1, -1, -1}, /*       2 inside */
                { 5,  6, -1, -1, -1}, /*     1 2 inside */
                { 6,  7, -1, -1, -1}, /*       4 inside */
                { 4,  7, -1, -1, -1}, /*     4 1 inside */
                { 6,  7,  5,  4, -1}, /*     4 2 inside */
                { 5,  7, -1, -1, -1}, /*   4 2 1 inside */
                { 7,  5, -1, -1, -1}, /*       8 inside */
                { 4,  6,  7,  5, -1}, /*     8 1 inside */
                { 7,  4, -1, -1, -1}, /*     8 2 inside */
                { 7,  6, -1, -1, -1}, /*   8 2 1 inside */
                { 6,  5, -1, -1, -1}, /*     8 4 inside */
                { 4,  5, -1, -1, -1}, /*   8 4 1 inside */
                { 6,  4, -1, -1, -1}, /*   8 4 2 inside */
                {-1, -1, -1, -1, -1}, /* 8 4 2 1 inside */
        };


        private MarchingSquares(PowerFunction func, RectF size, int resolution) {
            this.func=func;
            this.resolution=resolution;
            grid = new Vector[resolution][resolution];
            scalars = new float[resolution][resolution];

            float reci = 1.0f / resolution;
            float xVal;
            float yVal;
            /* Generate a grid of size 'size' with (0,0) in center */
            for(int y=0; y<resolution; y++) {
                yVal = size.top+size.height() * reci * y;
                for(int x=0; x<resolution; x++) {
                    xVal = size.left+size.width() * reci * x;
                    grid[x][y] = new Vector(xVal,yVal);
                }
            }
        }
        private Collection<Vector> computeLines(float isoValue, int sphereSetId) {
            List<Vector> lineSegments = new ArrayList<Vector>();

            /* Fill the grid with the scalar values from the power function */
            for(int y = 0; y<resolution; y++){
                for(int x = 0; x<resolution; ++x){
                    //End grid elements are hardcoded for safety.
                    if(x==0 || y==0 || x==resolution-1 || y==resolution-1) {
                        scalars[x][y] = 0;
                    }
                    else {
                        scalars[x][y] = func.getValue(grid[x][y], sphereSetId);
                    }
                }
            }
            /* Calculate the polygons for every square in the grid */
            for(int yPrev=0,yNext=1; yNext<resolution; yPrev = yNext++){
                for(int xPrev=0,xNext=1; xNext<resolution; xPrev = xNext++){
                    /* LerpPoints defines four corners, and four interpolated points between corners.
                    0___4___1
                    |       |
                    6       5
                    |       |
                    2___7___3
                    */

                    Vector vUpperLeft = grid[xPrev][yPrev];
                    Vector vUpperRight = grid[xNext][yPrev];
                    Vector vBottomLeft = grid[xPrev][yNext];
                    Vector vBottomRight = grid[xNext][yNext];
                    float svUpperLeft = scalars[xPrev][yPrev];
                    float svUpperRight = scalars[xNext][yPrev];
                    float svBottomLeft = scalars[xPrev][yNext];
                    float svBottomRight = scalars[xNext][yNext];

                    int tableIndex = 0;

                    /* tableIndex has values from 0 to 15, depending on what configuration of
                    corners that are outside/inside the isovalue.
                     */

                    if(svUpperLeft   > isoValue) tableIndex |= 1;
                    if(svUpperRight  > isoValue) tableIndex |= 2;
                    if(svBottomLeft  > isoValue) tableIndex |= 4;
                    if(svBottomRight > isoValue) tableIndex |= 8;

                    // Zero squares are entirely outside/inside the isovalue.
                    if(tableIndex == 0 || tableIndex == 15)
                        continue;

                    Vector[] lerpPoints = new Vector[4];
                    if((edgeTable[tableIndex] & 1)!=0)
                        lerpPoints[0] = lerp(isoValue, vUpperLeft, vUpperRight, svUpperLeft, svUpperRight);
                    if((edgeTable[tableIndex] & 2)!=0)
                        lerpPoints[1] = lerp(isoValue, vUpperRight, vBottomRight, svUpperRight, svBottomRight);
                    if((edgeTable[tableIndex] & 4)!=0)
                        lerpPoints[2] = lerp(isoValue, vUpperLeft, vBottomLeft, svUpperLeft, svBottomLeft);
                    if((edgeTable[tableIndex] & 8)!=0)
                        lerpPoints[3] = lerp(isoValue, vBottomLeft, vBottomRight, svBottomLeft, svBottomRight);

                    for (int i=0; lineTable[tableIndex][i]!=-1; i+=2){
                        Vector lineStart = lerpPoints[lineTable[tableIndex][i]-4];
                        Vector lineEnd = lerpPoints[lineTable[tableIndex][i + 1]-4];
                        lineSegments.add(lineStart);
                        lineSegments.add(lineEnd);
                    }
                }
            }
            return lineSegments;
        }

        private Vector lerp(float isoValue, Vector p0, Vector p1,float sv0, float sv1) {
            float mu;
            Vector p = new Vector(0,0);
            float epsilon = 10e-6f;
            if(Math.abs(isoValue-sv0) < epsilon)
                return p0;
            if(Math.abs(isoValue-sv1) < epsilon)
                return p1;
            if(Math.abs(sv0-sv1) < epsilon)
                return p0;

            mu = (isoValue - sv0) / (sv1 - sv0);
            p.x = p0.x + mu * (p1.x - p0.x);
            p.y = p0.y + mu * (p1.y - p0.y);
            return p;
        }
    }
}
