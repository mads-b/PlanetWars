package com.svamp.planetwars.sprite.hud;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import com.svamp.planetwars.Fleet;
import com.svamp.planetwars.GameEngine;
import com.svamp.planetwars.R;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.network.GameEvent;
import com.svamp.planetwars.network.PackageHeader;
import com.svamp.planetwars.sprite.Sprite;
import com.svamp.planetwars.sprite.SpriteFactory;
import com.svamp.planetwars.sprite.StarSprite;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Heads-up-display for game. Used by gameEngine.
 */
public class Hud extends AbstractHudSprite {

    private final Map<HudItem,Sprite> hudSprites = new HashMap<HudItem, Sprite>();
    private final SortedSet<AbstractHudSprite> zDepthCache = new TreeSet<AbstractHudSprite>().descendingSet();
    private final GameEngine gEngine;
    private int glTexId = -1;
    /* Identity matrix to be sent to draw calls to ensure HUD is not translated according to MVP-matrix. */
    private final float[] identityMatrix = new float[16];

    private static final String TAG = Hud.class.getCanonicalName();

    public Hud(GameEngine gameEngine) {
        this.gEngine = gameEngine;
        Matrix.setIdentityM(identityMatrix,0);
        setPos(-1,-1);
        Log.d(TAG, "HUD bounds: "+bounds.toShortString());
        //Hud background on level 0.
        setZVal(0);
    }

    public void draw(GL10 glUnused, float[] mvpMatrix) {
        //Texture not loaded. Load it. this is a hack. TODO: Preload textures.
        if(glTexId == -1) {
            glTexId = SpriteFactory.getInstance()
                    .makeAndRegisterDrawable(glUnused, R.drawable.planetwars_hud, GLES20.GL_CLAMP_TO_EDGE);
            super.setTexture(glTexId);
        }
        GLES20.glUseProgram(getProgramHandle());

        //Draw vertices.
        super.draw(glUnused,identityMatrix);

        //Draw all artifacts on HUD.
        synchronized (hudSprites) {
            for(Sprite s : zDepthCache) {
                s.draw(glUnused,identityMatrix);
            }
        }
    }

    public void update(float dt) {
        synchronized (hudSprites) {
            for(Sprite s : hudSprites.values()) {
                s.update(dt);
            }
        }
    }

    /**
     * Registers a touch on the HUD
     * @param coord Coords in screen space, x,y is in interval [-1,1]
     * @return True if something was touched on the HUD, false otherwise.
     */
    public boolean touch(Vector coord) {
        Sprite b = getHudItemAt(coord);
        if(b!=null && b instanceof ButtonSprite) {
            ((ButtonSprite)b).push();
            return true;
        }
        return contains(coord);
    }

    /**
     * Moves the slider bar the specified number of pixels.
     * @param start Point on screen the drag action started from
     * @param dist Distance over the screen dragged
     * @return False if a slider was dragged, true otherwise.
     */
    public boolean move(Vector start, Vector dist) {
        Sprite b = getHudItemAt(start);
        if(b!=null && b instanceof SliderSprite) {
            ((SliderSprite) b).move(dist);
            return true;
        }
        return contains(start);
    }

    /**
     * Called by the GameEngine to inform the HUD of the fact that the star selection has been changed.
     */
    public void selectionChanged() {
        synchronized (hudSprites) {
            float w = bounds.width();
            //Get sprites
            StarSprite source = gEngine.getLastSelectedSource();
            StarSprite target = gEngine.getLastSelectedTarget();

            //Rebuild:
            hudSprites.clear();
            zDepthCache.clear();

            if(source!=null) {
                StarSelectionSprite sss = new StarSelectionSprite();
                sss.setPos(-1,0);
                sss.setSize(w * 0.3f, w * 0.3f);
                sss.loadStar(source);
                addSprite(HudItem.SOURCE_SELECTION_ICON, sss);

                if(source.getOwnership()==GameEngine.getPlayer()) {
                    //TODO: Build selection sprite
                    SliderSprite sbs = new SliderSprite(source,HudItem.BLUE_SLIDER);
                    sbs.setPos(-1,w * 0.4f);
                    sbs.setSize(w * 0.7f, w * 0.2f);
                    addSprite(HudItem.BLUE_SLIDER, sbs);
                    SliderSprite sbs2 = new SliderSprite(source,HudItem.RED_SLIDER);
                    sbs2.setPos(-1,w*0.15f);
                    sbs2.setSize(w * 0.7f, w * 0.2f);
                    addSprite(HudItem.RED_SLIDER, sbs2);
                    SliderSprite sbs3 = new SliderSprite(source,HudItem.GREEN_SLIDER);
                    sbs3.setPos(-1,-w*0.10f);
                    sbs3.setSize(w * 0.7f, w * 0.2f);
                    addSprite(HudItem.GREEN_SLIDER, sbs3);
                }
            }
            if(target!=null) {
                StarSelectionSprite sss = new StarSelectionSprite();
                sss.setPos(-1+w*0.7f,-1);
                sss.setSize(w*0.3f,w*0.3f);
                sss.loadStar(target);
                addSprite(HudItem.TARGET_SELECTION_ICON, sss);
            }

            //Attack button, if source and target are selected.
            if(source!=null
                    && target!=null
                    && !source.equals(target)
                    && GameEngine.getPlayer()==source.getOwnership()) {
                String text = "Attack!";
                if(target.getOwnership()==GameEngine.getPlayer()) {
                    text = "Transfer units";
                }
                ButtonSprite bs = new ButtonSprite(text, this,source);
                bs.setPos(-1,-w*0.35f);
                bs.setSize(w*0.9f,w*0.2f);
                addSprite(HudItem.LAUNCH_BUTTON, bs);
            }
        }
    }

    /**
     * Callback from a buttonSprite when it is pushed.
     * @param star Source star to do operations on.
     */
    public void buttonPushed(StarSprite star) {
        short redNum = ((SliderSprite)hudSprites.get(HudItem.RED_SLIDER)).getVal();
        short blueNum = ((SliderSprite)hudSprites.get(HudItem.BLUE_SLIDER)).getVal();
        short greenNum = ((SliderSprite)hudSprites.get(HudItem.GREEN_SLIDER)).getVal();

        Fleet f = new Fleet(star.getOwnership(),redNum,blueNum,greenNum);

        GameEvent event = new GameEvent(PackageHeader.FLEET_DISPATCHED,star.getOwnership());
        int startStar = gEngine.getLastSelectedSource().getElementHash();
        byte[] sentFleet = f.getSerialization();
        int targetStar = gEngine.getLastSelectedTarget().getElementHash();
        ByteBuffer buffer = ByteBuffer.allocate(8+sentFleet.length);
        buffer.putInt(startStar).put(sentFleet).putInt(targetStar);
        event.setPayload(buffer.array());
        gEngine.getClient().sendData(event.toByteArray());
    }

    public void buildSelectionChanged(StarSprite star) {
        GameEvent event = new GameEvent(PackageHeader.NEW_BUILD_ORDERS,GameEngine.getPlayer());
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.putInt(star.getElementHash())
                .put((byte) star.getBuildType().ordinal());
        event.setPayload(buffer.array());
        gEngine.getClient().sendData(event.toByteArray());
    }

    private Sprite getHudItemAt(Vector coord) {
        synchronized (hudSprites) {
            for(Sprite button : hudSprites.values()) {
                if(button.contains(coord)) {
                    return button;
                }
            }
        }
        return null;
    }

    private void addSprite(HudItem item, AbstractHudSprite sprite) {
        hudSprites.put(item,sprite);
        zDepthCache.addAll(sprite.getSprites());
    }

    @Override
    public Collection<AbstractHudSprite> getSprites() {
        return zDepthCache;
    }

    public enum HudItem {
        SOURCE_SELECTION_ICON(0),
        TARGET_SELECTION_ICON(0),
        BLUE_SLIDER(Color.BLUE),
        RED_SLIDER(Color.RED),
        GREEN_SLIDER(Color.GREEN),
        BUILD_SELECTION(Color.GRAY),
        LAUNCH_BUTTON(0);

        private final int color;
        HudItem(int c) {
            color = c;
        }
        int getColor() { return color; }
    }
}
