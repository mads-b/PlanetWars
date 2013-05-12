package com.svamp.planetwars;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;
import com.svamp.planetwars.math.MetaBalls;
import com.svamp.planetwars.math.QuadTree;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.network.ByteSerializeable;
import com.svamp.planetwars.network.DataPacketListener;
import com.svamp.planetwars.network.GameClient;
import com.svamp.planetwars.network.GameEvent;
import com.svamp.planetwars.network.Player;
import com.svamp.planetwars.sprite.BlobSprite;
import com.svamp.planetwars.sprite.Sprite;
import com.svamp.planetwars.sprite.StarSprite;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StarMap implements ByteSerializeable,DataPacketListener {
    //Complete set of stars on map.
    private final QuadTree<StarSprite> stars = new QuadTree<StarSprite>(new RectF(-MAX_RADIUS,-MAX_RADIUS,MAX_RADIUS,MAX_RADIUS),null);
    //Cheat/hack to allow for quick lookup of stars by their hash.
    private final SparseArray<StarSprite> starsMap = new SparseArray<StarSprite>();
    //Subset of stars present in screen.
    private List<StarSprite> visibleStars = new ArrayList<StarSprite>();
    //Instruction to remake the list of visible stars.
    private boolean rebuildStarList = true;
    //Subset of stars having changed state. Only host fills in this.
    private List<Sprite> dirtyStars = new ArrayList<Sprite>();
    private byte dirtyStarsSeverity = 0;

    //Ownership blob.
    private List<BlobSprite> blobs = new ArrayList<BlobSprite>();

    private static final int[] starDrawables = {
            R.drawable.sun_black,
            R.drawable.sun_blu,
            R.drawable.sun_gre,
            R.drawable.sun_org,
            R.drawable.sun_red};

    private final static float MAX_RADIUS = 2.5f;

    private static final String TAG = StarMap.class.getCanonicalName();

    private final static Paint pathPaint = new Paint();

    public StarMap(GameClient client) {
        client.registerListener(this);
        pathPaint.setColor(Color.argb(128, 0, 90, 4));
        pathPaint.setStyle(Paint.Style.STROKE);
        //pathPaint.setPathEffect(new CornerPathEffect(50));
        pathPaint.setPathEffect(new DashPathEffect(new float[] {30,10},0));
        pathPaint.setStrokeWidth(20);
    }
    //Internal constructor.
    private StarMap() {}

    public void draw(GL10 glUnused, float[] mvpMatrix) {
        for(Sprite s : blobs) {
            s.draw(glUnused, mvpMatrix);
        }
        //Viewport changed since last time. Rebuild list of Stars to draw.
        //if(rebuildStarList) {
        //    visibleStars = stars.queryRange(canvas.getClipBounds());
         //   rebuildStarList=false;
        //}
        for(Sprite ss : stars)
            ss.draw(glUnused, mvpMatrix);
    }

    public void update(float dt) {
        for(StarSprite star : stars)
            star.update(dt);
    }

    /**
     * Fetches star at given position.
     * @param pos Position to find star to select
     * @return StarSprite at/close to this position, or null if nothing nearby.
     */
    public StarSprite getStarAtPosition(Vector pos) {
        //Get key to star at this position. Range to search: .3 units
        return stars.getClosest(pos,0.3f);
    }

    public StarSprite getStarWithHash(int hash) {
        return starsMap.get(hash);
    }

    void viewPortChanged() {
        rebuildStarList=true;
    }

    /**
     * Callback from starsprite when state has changed.
     * @param cause @see BattleField.update(float dt)
     * @param star The StarSprite who is cause for the change.
     */
    public void fireStarStateChanged(int cause,Sprite star) {
        synchronized (dirtyStars) {
            if(cause == 0) return;
            dirtyStars.add(star);
            dirtyStarsSeverity = (byte) Math.max(dirtyStarsSeverity,cause);
        }
    }

    public boolean isDirty() {
        return !dirtyStars.isEmpty();
    }

    /**
     * Only serializes stars marked as dirty (stars changed since last call to this function).
     * @return Serialized dirty stars.
     */
    @Override
    public byte[] getSerialization() {
        synchronized (dirtyStars) {
            ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
            buffer.putShort((short) (dirtyStars.size()));
            buffer.put(dirtyStarsSeverity);

            for(Sprite s : dirtyStars) {
                buffer.put(((ByteSerializeable)s).getSerialization());
            }
            dirtyStars.clear(); // Clear dirty stars after transmission.
            dirtyStarsSeverity=0;
            return buffer.array();
        }
    }

    @Override
    public void updateFromSerialization(ByteBuffer buffer) {
        short starNum = buffer.getShort();
        byte severity = buffer.get();
        Log.d(TAG,"Updating "+starNum+" stars..:");

        for(int i=0;i<starNum;i++) {
            int elementHash = buffer.getInt();
            //Rewind; Just peeking!
            buffer.position(buffer.position()-4);
            if(starsMap.indexOfKey(elementHash)>=0) {
                starsMap.get(elementHash).updateFromSerialization(buffer);
            } else { //Star not registered!
                //Make new star
                StarSprite star = new StarSprite(0, getRandomStarTextureId());
                star.setElementHash(elementHash);
                //Feed it info
                star.updateFromSerialization(buffer);
                //Add it to BOTH registers!
                stars.add(star);
                starsMap.put(elementHash,star);
            }
        }
        viewPortChanged();
        if(severity == 2) //Only recompute blobs if update is a little "heavy".
            new Thread(new BlobMaker()).start();
    }

    @Override
    public int getSerializedSize() {
        synchronized (dirtyStars) {
            int size=3; //First short, plus one "severity byte"
            for(Sprite item : dirtyStars) {
                size += ((StarSprite) item).getSerializedSize();
            }
            return size;
        }
    }

    public void makeAllDirty() {
        dirtyStars = new ArrayList<Sprite>();
        Log.d(TAG,"Making "+stars.size()+" stars dirty..");
        for(Sprite s : stars)
            fireStarStateChanged(2,s);
    }

    public void setSpawns(Collection<Player> players) {
        float angle = (float) (2*Math.PI/players.size());
        Vector pos = new Vector(0,MAX_RADIUS);
        for(Player p : players) {
            Fleet home = new Fleet(p,(short)20,(short)20);
            StarSprite spawn = stars.getClosest(pos,MAX_RADIUS);
            Log.d(TAG,"Set spawn for "+p.getPlayerName()+" spawn at: "+spawn.getBounds().centerX()+" X "+spawn.getBounds().centerY());
            spawn.getBattleField().setHomeFleet(home);
            pos.rotate(angle);
        }
    }

    private static int getRandomStarTextureId() {
        return starDrawables[((int) (starDrawables.length * Math.random()))];
    }

    @Override
    public void receive(GameEvent packet) {
        switch (packet.getHeader()) {
            case STAR_STATE_CHANGED:
                updateFromSerialization(ByteBuffer.wrap(packet.getPayload()));
                break;
        }
    }

    /**
     * Comprehensive algorithm for creating a game galaxy with specified size,
     * number of elements, and number of arms.
     * @param numStars Number of stars in the galaxy
     * @return A gameMap.
     */
    public static StarMap makeSpiralGalaxy(int numStars) {
        Log.d(TAG,"Generating stars, Number:"+numStars);
        //Init array. +1 for big center star/black hole
        final StarSprite[] stars = new StarSprite[numStars];
        //logarithmic spiral constant a*e^bt
        final double a = 1;
        final double b = 0.2;
        //rotation degree (PI means each arm has one rotation
        final double windings = 4;

        //tMax is maximum angle (windings*2pi)
        final double tMax = 2.0 * Math.PI * windings;

        // How far stars may be away from spiral arm centers.
        final float drift = 0.004f;

        //Create center star
        stars[0] = new StarSprite(0.15f,starDrawables[0]);
        stars[0].setPos(-0.15f,-0.15f);
        stars[0].setElementHash(1337);

        for(int i=1;i<numStars;i++) {

            double t = 0.5+tMax * Math.pow(Math.random(),0.15f);
            double x = a * Math.exp(b * t) * Math.cos(t);
            x += ((drift * t * x * (Math.random() - Math.random())));
            double y = a * Math.exp(b * t) * Math.sin(t);
            y += ((drift * t * y * (Math.random() - Math.random())));
            //Scale for maxRad
            x *= MAX_RADIUS / (a * Math.exp(b * tMax));
            y *= MAX_RADIUS / (a * Math.exp(b * tMax));
            //Create actual planet
            stars[i] = new StarSprite((float) (0.03f+0.02f*Math.random()),
                    getRandomStarTextureId());
            stars[i].setElementHash(1337+i);
            //2 spiral arms. Move planet to correct spot.
            //Remember to move them so no planets have negative coords.
            //Galaxy is now centered on origin.
            if(Math.random() > 0.5) {
                stars[i].setPos((float) (x), (float) (y));
                //Log.d("PLANET IN ARM 1","Created at "+(x+maxRad)+" x"+(y+maxRad));
            }
            else {
                stars[i].setPos((float) (-x), (float) (-y));
                //Log.d("PLANET IN ARM 2","Created at "+(-x+maxRad)+" x"+(-y+maxRad));
            }
        }
        /*
         * Begin dirty code to check if stars are properly spread out
         */
        boolean collision=true;
        Log.d(TAG,"Resolving collisions between stars..");
        while(collision) {
            collision=false;
            for(StarSprite s1 : stars) {
                for(StarSprite s2 : stars) {
                    //s1 radius+s2 radius *2

                    float minLength = (s1.getBounds().width()+s2.getBounds().width());
                    //If distance from one star to another is less than their min length, they collide. Stars' bounds are twice their actual size.
                    double distSq = Math.pow(s1.getBounds().centerX()-s2.getBounds().centerX(),2)+
                            Math.pow(s1.getBounds().centerY()-s2.getBounds().centerY(),2);

                    if(Math.sqrt(distSq)<minLength && s1!=s2) {
                        collision=true;
                        float dx=s1.getBounds().centerX()-s2.getBounds().centerX();
                        float dy=s1.getBounds().centerY()-s2.getBounds().centerY();
                        //Just add some star's radius in direction of speed*2, for a little distance. Never center star. Center star is always center.
                        (s1==stars[0] ? s2 : s1).move(Math.signum(dx)*s2.getBounds().width()*2, Math.signum(dy)*s2.getBounds().height()*2);
                    }
                }
            }
        }
        //Generation complete. Build data structure.
        StarMap map = new StarMap();
        for(StarSprite star : stars) {
            //Add to quad tree
            map.stars.add(star);
            //Add to our "hack"
            map.starsMap.put(star.getElementHash(),star);
            //Host callback for star state changes.
            star.setCallback(map);
        }
        return map;
    }

    private class BlobMaker implements Runnable {

        @Override
        public void run() {
            int[] colors = new int[] {
                    Color.argb(160,0,90,0),
                    Color.argb(160,90,0,0),
                    Color.argb(160,0,0,90),
                    Color.argb(160,90,0,90)
            };
            //Regenerate metaballs..
            List<BlobSprite> newBlobs = new ArrayList<BlobSprite>();
            RectF blobBound = new RectF(
                    -MAX_RADIUS*1.1f,
                    -MAX_RADIUS*1.1f,
                    MAX_RADIUS*1.1f,
                    MAX_RADIUS*1.1f);

            MetaBalls metaBalls = new MetaBalls(blobBound,stars);
            int i=0;
            Log.d(TAG,"There are "+GameEngine.getPlayers().size()+" players in this blob generation (and "+stars.size()+" stars)");
            for(Player p : GameEngine.getPlayers()) {
                Collection<List<Vector>> userBlobs = metaBalls.getBlobsFor(p);
                for(List<Vector> path : userBlobs) {
                    Log.d(TAG,"Blob for: "+p.getPlayerName()+" has "+path.size()+"Vertices");
                    newBlobs.add(new BlobSprite(path, colors[i]));
                }
                i++;
            }
            blobs=newBlobs;
        }
    }
}
