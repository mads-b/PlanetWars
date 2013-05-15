package com.svamp.planetwars.sprite.hud;

import android.opengl.GLES20;
import com.svamp.planetwars.Fleet;
import com.svamp.planetwars.R;
import com.svamp.planetwars.math.Vector;
import com.svamp.planetwars.sprite.SpriteFactory;
import com.svamp.planetwars.sprite.StarSprite;

import javax.microedition.khronos.opengles.GL10;
import java.util.Collection;

/**
 *
 */
public class BuildSelectionSprite extends HudSprite {
    private int glTexId = -1;
    private int selectorGlTexId = -1;
    private final StarSprite curStar;
    private final Hud callback;
    private HudSprite selector = new HudSprite();
    // What position the selector is in.
    // Can be in the interval 0-2, where closest roundoff determines one of three possible positions.
    private float selectorPos;



    public BuildSelectionSprite(StarSprite star, Hud callback) {
        this.curStar = star;
        this.callback = callback;
        setZVal(-.1f);
        selector.setZVal(-.2f);
        // XXX: If ordinal numbering changes, this selector must too!
        selectorPos = curStar.getBuildType().ordinal();
    }

    public void move(Vector amount) {
        //Increment slider accordingly
        int oldPos = Math.round(selectorPos);
        selectorPos += 2*amount.x/bounds.width();
        int newPos = Math.max(0,Math.min(2,Math.round(selectorPos)));
        if(newPos != oldPos) {
            selector.setPos(bounds.left+(bounds.width()-selector.getBounds().width())/2*newPos,bounds.top);
            // Inform HUD that the build selection has changed!
            callback.buildSelectionChanged(Fleet.ShipType.getByOrdinal(newPos));
        }
    }

    @Override
    public void draw(GL10 glUnused, float[] mvpMatrix) {
        //Texture not loaded. Load it. this is a hack. TODO: Preload textures.
        if(glTexId == -1) {
            glTexId = SpriteFactory.getInstance()
                    .makeAndRegisterDrawable(glUnused, R.drawable.planetwars_selector, GLES20.GL_CLAMP_TO_EDGE);
            selectorGlTexId = SpriteFactory.getInstance()
                    .makeAndRegisterDrawable(glUnused, R.drawable.planetwars_selector_marker, GLES20.GL_CLAMP_TO_EDGE);
            super.setTexture(glTexId);
            selector.setTexture(selectorGlTexId);
        }
        super.draw(glUnused,mvpMatrix);
    }

    @Override
    public void updateVertices() {
        super.updateVertices();
        selector.setPos(bounds.left,bounds.top);
        selector.setSize(75*bounds.width()/200,bounds.height());
    }

    @Override
    public Collection<HudSprite> getSprites() {
        Collection<HudSprite> sprites = super.getSprites();
        sprites.add(selector);
        return sprites;
    }


}
