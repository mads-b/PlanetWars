package com.svamp.planetwars.math;

import android.graphics.RectF;
import android.util.Log;
import com.svamp.planetwars.network.Player;
import com.svamp.planetwars.sprite.StarSprite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaBalls {
    private final static int RESOLUTION = 80;
    private MarchingSquares squares;
    public MetaBalls(RectF dimensions,Collection<StarSprite> starSprites) {
        Log.d("com.svamp.math.MetaBalls","Making metaballs on domain "+dimensions.toShortString());
        Sphere[] spheres = new Sphere[starSprites.size()];
        int i=0;
        for(StarSprite s : starSprites) {
            spheres[i] = new Sphere(
                    new Vector(s.getBounds().centerX(),s.getBounds().centerY()),
                    s.getAttack()*100,
                    s.getOwnership().getElementHash());
            i++;
        }
        squares = new MarchingSquares(new PowerFunction(spheres),dimensions,RESOLUTION);
    }

    public Collection<List<Vector>> getBlobsFor(Player p) {
        long start = System.currentTimeMillis();
        Collection<List<Vector>> result = squares.computeLines(0.02f,p.getElementHash());
        Log.d("com.svamp.math.MetaBalls","Computed new set in "+(System.currentTimeMillis()-start)+"ms.");
        return result;
    }

    private class Sphere {
        private Vector origin;
        private float radius;
        private int setID;
        private Sphere(Vector origin, float radius,int setID) {
            this.origin=origin;
            this.radius=radius;
            this.setID=setID;
        }
    }

    private class PowerFunction {
        private final static float EPSILON = 1/1024f;
        private Sphere[] spheres;
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
        private PowerFunction func;
        private int resolution;
        private Vector[][] grid;
        private float[][] scalars;
        private int[] edgeTable = {
                0x0, 0x5, 0x3, 0x6,
                0xC, 0x9, 0xF, 0xA,
                0xA, 0xF, 0x9, 0xC,
                0x6, 0x3, 0x5, 0x0
        };
        private int[][] lineTable = {
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
        private Collection<List<Vector>> computeLines(float isoValue, int sphereSetId) {
            Map<Vector,Vector> line = new HashMap<Vector,Vector>();
            Map<Vector,Vector> line2 = new HashMap<Vector,Vector>();

            /* Fill the grid with the scalar values from the power function */
            for(int y = 0; y<resolution; y++){
                for(int x = 0; x<resolution; ++x){
                    //End grid elements are hardcoded for safety.
                    if(x==0 || y==0 || x==resolution-1 || y==resolution-1) {
                        scalars[x][y] = -.05f;
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
                        line.put(lineStart,lineEnd);
                        line2.put(lineEnd,lineStart);
                    }
                }
            }
            List<List<Vector>> blobs = new ArrayList<List<Vector>>();
            while(!line.isEmpty()) {
                List<Vector> blob = new ArrayList<Vector>();
                // Add random point to blob.
                blob.add(line.keySet().iterator().next());
                // Get successor:
                Vector successor = line.remove(blob.get(0));
                // While successor isn't the first point in the blob (indicating full circle)
                boolean isFullCircle=true;
                while(!successor.equals(blob.get(0))) {
                    blob.add(successor);
                    successor = line.remove(successor);
                    if(successor != null && successor != blob.get(blob.size()-2)) {
                        line2.remove(successor);
                        continue;
                    }
                    successor = line2.remove(successor);
                    if(successor != null && successor != blob.get(blob.size()-2)) {
                        line.remove(successor);
                        continue;
                    }
                    // No successor found, break. This means we haven't gone full circle,
                    // implying that the blob ends out of bounds!
                    // Check if there is more blob prior to the first blob element.
                    Log.d("com.svamp.math.MetaBalls","Non-circular blob! Result might be ugly..");
                    isFullCircle=false;
                    break;
                }
                if(isFullCircle) blob.add(blob.get(0));
                blobs.add(blob);
            }
            return blobs;
        }

        private Vector lerp(float isoValue, Vector p0, Vector p1,float sv0, float sv1) {
            float mu;
            Vector p = new Vector(0,0);
            float epsilon = 1/65536f;
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
