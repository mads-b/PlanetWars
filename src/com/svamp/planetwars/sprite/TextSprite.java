package com.svamp.planetwars.sprite;

import android.graphics.Paint;
import android.graphics.Rect;

import javax.microedition.khronos.opengles.GL10;

/**
 * Standard quad for drawing text.
 * It will enforce the height and position provided, but its width will vary depending on the
 * width of the text it is asked to represent. The reason for this is to enforce the
 * aspect ratio of the text, to avoid it being stretched.
 */
public class TextSprite extends AbstractSquareSprite {
    private final Paint textPaint, strokePaint;
    private String curText = "";
    private int glTexId = -1;

    public TextSprite(Paint textPaint, Paint strokePaint) {
        this.textPaint = textPaint;
        this.strokePaint = strokePaint;
    }

    public void changeText(GL10 glUnused, String text) {
        curText = text;

        //Create new texture
        int newHandle = SpriteFactory.getInstance().makeAndRegisterText(glUnused, text, textPaint, strokePaint);
        //Delete old texture
        SpriteFactory.getInstance().deleteTextureFromGL(glUnused,glTexId);
        //Create new texture, register it in GL, and set it as our texture.
        super.setTexture(newHandle);
        glTexId = newHandle;
        //Reset bounds. setSize automatically computes width.
        setSize(0,bounds.height());
    }

    public void setSize(float width, float height) {
        Rect size = new Rect();
        textPaint.getTextBounds(curText,0,curText.length(),size);
        float ratio = size.width()/size.height();
        // Set new size to new height, and width scaled by the correct aspect ratio.
        super.setSize(ratio*height,height);
    }
}
