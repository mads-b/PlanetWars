package com.svamp.planetwars.sprite.hud;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import com.svamp.planetwars.sprite.SpriteFactory;

import javax.microedition.khronos.opengles.GL10;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Standard quad for drawing text.
 * It will enforce the height and position provided, but its width will vary depending on the
 * width of the text it is asked to represent. The reason for this is to enforce the
 * aspect ratio of the text, to avoid it being stretched.
 */
public class TextSprite extends HudSprite {
    private final Paint textPaint, strokePaint;
    private String curText = "";
    //True if we need to recreate texture on next draw.
    private AtomicBoolean textDirty = new AtomicBoolean(false);
    private int glTexId = -1;

    private final Rect tmp = new Rect();

    public TextSprite(Paint textPaint, Paint strokePaint) {
        this.textPaint = textPaint;
        this.strokePaint = strokePaint;
    }

    /**
     * Change the text this TextSprite shows.
     * @param text Text to change to.
     */
    public void changeText(String text) {
        curText = text;
        textDirty.set(true);
    }

    @Override
    public void draw(GL10 glUnused, float[] mvpMatrix) {
        if(textDirty.getAndSet(false)) {
            //Create new texture
            int newHandle = SpriteFactory.getInstance().makeAndRegisterText(glUnused, curText, textPaint, strokePaint);
            //Delete old texture
            SpriteFactory.getInstance().deleteTextureFromGL(glUnused,glTexId);
            //Create new texture, register it in GL, and set it as our texture.
            super.setTexture(newHandle);
            glTexId = newHandle;
            //Reset bounds. setSize automatically computes width.
            setSize(0,bounds.height());
        }
        super.draw(glUnused, mvpMatrix);
    }

    @Override
    public void setSize(float width, float height) {
        textPaint.getTextBounds(curText,0,curText.length(),tmp);
        float ratio = (float)tmp.width()/ tmp.height();
        // Set new size to new height, and width scaled by the correct aspect ratio.
        super.setSize(ratio*height,height);
    }

    public RectF getBounds() {
        if(curText.isEmpty()) throw new UnsupportedOperationException("Cannot get bounds when text is unset!");
        return super.getBounds();
    }

    public boolean isUninitialized() {
        return curText.isEmpty();
    }
}
