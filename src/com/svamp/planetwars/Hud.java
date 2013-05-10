package com.svamp.planetwars;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.network.GameEvent;
import com.svamp.planetwars.network.PackageHeader;
import com.svamp.planetwars.sprite.Sprite;
import com.svamp.planetwars.sprite.StarSprite;
import com.svamp.planetwars.sprite.hud.BuildSelectionSprite;
import com.svamp.planetwars.sprite.hud.ButtonSprite;
import com.svamp.planetwars.sprite.hud.SliderSprite;
import com.svamp.planetwars.sprite.hud.StarSelectionSprite;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Heads-up-display for game. Used by gameEngine.
 */
public class Hud {
    private static final Paint background = new Paint();
    private static final Paint topText = new Paint();

    private final Map<HudItem,Sprite> hudSprites = new HashMap<HudItem, Sprite>();
    private final GameEngine gEngine;
    private final Vector size;
    private static final String TAG = "com.svamp.Hud";

    public Hud(GameEngine gameEngine,Vector size) {
        this.gEngine = gameEngine;
        this.size=size;
        background.setColor(Color.DKGRAY);
        background.setStyle(Paint.Style.FILL);
        topText.setColor(Color.BLUE);
        topText.setTextSize(20);
        Log.d(TAG,"Hud size:"+size.x+"x"+size.y);


    }

    public synchronized void draw(Canvas c) {
        c.drawRect(0,0,size.x,size.y,background);
        for(Sprite sprite : hudSprites.values()) {
            //sprite.draw(c);
        }

        StarSprite source = gEngine.getLastSelectedSource();
        StarSprite target = gEngine.getLastSelectedTarget();
        if(source!=null) {
            c.drawText(source.getOwnershipDesc(),size.x*0.35f,size.x*0.10f,topText);
            c.drawText(source.getStatus(source),size.x*0.35f,size.x*0.20f,topText);
        }
        if(target!=null) {
            c.drawText(target.getOwnershipDesc(),10,size.y-size.x*0.15f,topText);
            c.drawText(target.getStatus(target),10,size.y-size.x*0.05f,topText);
        }
    }


    public boolean touch(Vector coord) {
        Sprite b = getHudItemAt(coord);
        if(b!=null && b instanceof ButtonSprite) {
            ((ButtonSprite)b).push();
            return true;
        }
        return coord.x<size.x && coord.y<size.y;
    }

    /**
     * Moves the slider bar the specified number of pixels.
     * @param start Point on screen the drag action started from
     * @param amount Distance over the scree dragged
     * @return False if a slider was dragged, true otherwise.
     */
    public boolean move(Vector start, Vector amount) {
        Sprite b = getHudItemAt(start);
        if(b!=null && b instanceof SliderSprite) {
            ((SliderSprite) b).move(amount);
            return true;
        }
        return start.x<size.x && start.y<size.y;
    }

    /**
     * Called by the GameEngine to inform the HUD of the fact that the star selection has been changed.
     */
    public synchronized void selectionChanged() {
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
            sss.setPos(0,0);
            sss.setSize(size.x * 0.3f, size.x * 0.3f);
            sss.loadStar(source);
            hudSprites.put(HudItem.SOURCE_SELECTION_ICON,sss);

            if(source.getOwnership()==GameEngine.getPlayer()) {
                BuildSelectionSprite bss = new BuildSelectionSprite(source,HudItem.BUILD_SELECTION);
                bss.setPos(0,size.x*0.35f);
                bss.setSize(size.x * 0.8f, size.x * 0.2f);
                bss.setVal((short) source.getBuildType());
                bss.setCallback(this);
                hudSprites.put(HudItem.BUILD_SELECTION,bss);
                SliderSprite sbs2 = new SliderSprite(source,HudItem.BOMBER_SLIDER);
                sbs2.setPos(0,size.x*0.6f);
                sbs2.setSize(size.x * 0.8f, size.x * 0.2f);
                sbs2.setVal(bomberNum);
                hudSprites.put(HudItem.BOMBER_SLIDER,sbs2);
                SliderSprite sbs = new SliderSprite(source,HudItem.FIGHTER_SLIDER);
                sbs.setPos(0,size.x*0.85f);
                sbs.setSize(size.x * 0.8f, size.x * 0.2f);
                sbs.setVal(fighterNum);
                hudSprites.put(HudItem.FIGHTER_SLIDER,sbs);
            }
        }
        if(target!=null) {
            StarSelectionSprite sss = new StarSelectionSprite();
            sss.setPos(size.x*0.7f,size.y-size.x*0.35f);
            sss.setSize(size.x*0.3f,size.x*0.3f);
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
            bs.setPos(0,size.x*1.1f);
            bs.setSize(size.x*0.8f,size.x*0.2f);
            hudSprites.put(HudItem.LAUNCH_BUTTON,bs);
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
        for(Sprite button : hudSprites.values()) {
            if(button.contains(coord)) {
                return button;
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
