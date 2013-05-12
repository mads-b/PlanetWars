package com.svamp.planetwars.sprite.hud;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import com.svamp.planetwars.Fleet;
import com.svamp.planetwars.GameEngine;
import com.svamp.planetwars.R;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.network.GameEvent;
import com.svamp.planetwars.network.PackageHeader;
import com.svamp.planetwars.sprite.AbstractSquareSprite;
import com.svamp.planetwars.sprite.Sprite;
import com.svamp.planetwars.sprite.SpriteFactory;
import com.svamp.planetwars.sprite.StarSprite;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Heads-up-display for game. Used by gameEngine.
 */
public class Hud extends AbstractSquareSprite {

    private final Map<HudItem,Sprite> hudSprites = new HashMap<HudItem, Sprite>();
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
    }

    public void draw(GL10 glUnused, float[] mvpMatrix) {
        //Texture not loaded. Load it. this is a hack. TODO: Preload textures.
        if(glTexId == -1) {
            glTexId = SpriteFactory.getInstance()
                    .getTextureId(glUnused, R.drawable.planetwars_hud, GLES20.GL_CLAMP_TO_EDGE);
        }
        GLES20.glUseProgram(getProgramHandle());

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTexId);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTexCoordinateHandle, 0);

        GLES20.glEnableVertexAttribArray(mTexCoordinateHandle);

        GLES20.glVertexAttribPointer(mTexCoordinateHandle, 2, GLES20.GL_FLOAT, false,
                0, textureBuffer);
        //Draw vertices.
        super.draw(glUnused,identityMatrix);
        GLES20.glDisableVertexAttribArray(mTexCoordinateHandle);

        //Draw all artifacts on HUD.
        synchronized (hudSprites) {
            for(Sprite s : hudSprites.values()) {
                s.draw(glUnused,identityMatrix);
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
            float h = bounds.height();
            //Get sprites
            StarSprite source = gEngine.getLastSelectedSource();
            StarSprite target = gEngine.getLastSelectedTarget();
            short fighterNum = 0;
            short bomberNum = 0;
            //If sliders exist, store state:
            if(hudSprites.containsKey(HudItem.FIGHTER_SLIDER)) {
                fighterNum = ((SliderSprite)hudSprites.get(HudItem.FIGHTER_SLIDER)).getVal();
                bomberNum = ((SliderSprite)hudSprites.get(HudItem.BOMBER_SLIDER)).getVal();
            }
            //Rebuild:
            hudSprites.clear();

            if(source!=null) {
                StarSelectionSprite sss = new StarSelectionSprite();
                sss.setPos(-1,0);
                sss.setSize(w * 0.3f, w * 0.3f);
                sss.loadStar(source);
                hudSprites.put(HudItem.SOURCE_SELECTION_ICON,sss);

                if(source.getOwnership()==GameEngine.getPlayer()) {
                    BuildSelectionSprite bss = new BuildSelectionSprite(source,HudItem.BUILD_SELECTION);
                    bss.setPos(-1, w * 0.35f);
                    bss.setSize(w * 0.8f, w * 0.2f);
                    bss.setVal((short) source.getBuildType());
                    bss.setCallback(this);
                    hudSprites.put(HudItem.BUILD_SELECTION,bss);
                    SliderSprite sbs2 = new SliderSprite(source,HudItem.BOMBER_SLIDER);
                    sbs2.setPos(-1,w * 0.6f);
                    sbs2.setSize(w * 0.8f, w * 0.2f);
                    sbs2.setVal(bomberNum);
                    hudSprites.put(HudItem.BOMBER_SLIDER,sbs2);
                    SliderSprite sbs = new SliderSprite(source,HudItem.FIGHTER_SLIDER);
                    sbs.setPos(-1,w*0.85f);
                    sbs.setSize(w * 0.8f, w * 0.2f);
                    sbs.setVal(fighterNum);
                    hudSprites.put(HudItem.FIGHTER_SLIDER,sbs);
                }
            }
            if(target!=null) {
                StarSelectionSprite sss = new StarSelectionSprite();
                sss.setPos(-1+w*0.7f,h-w*0.35f);
                sss.setSize(w*0.3f,w*0.3f);
                sss.loadStar(target);
                hudSprites.put(HudItem.TARGET_SELECTION_ICON,sss);
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
                bs.setPos(-1,w*1.1f);
                bs.setSize(w*0.8f,w*0.2f);
                hudSprites.put(HudItem.LAUNCH_BUTTON,bs);
            }
        }
    }

    /**
     * Callback from a buttonSprite when it is pushed.
     * @param star Source star to do operations on.
     */
    public void buttonPushed(StarSprite star) {
        short fighterNum = ((SliderSprite)hudSprites.get(HudItem.FIGHTER_SLIDER)).getVal();
        short bomberNum = ((SliderSprite)hudSprites.get(HudItem.BOMBER_SLIDER)).getVal();
        Fleet f = new Fleet(star.getOwnership(),fighterNum,bomberNum);

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
                .put((byte) star.getBuildType());
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

    public enum HudItem {
        SOURCE_SELECTION_ICON,
        TARGET_SELECTION_ICON,
        BOMBER_SLIDER,
        FIGHTER_SLIDER,
        BUILD_SELECTION,
        LAUNCH_BUTTON
    }
}
