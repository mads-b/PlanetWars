package com.svamp.planetwars.sprite;

import android.graphics.Canvas;
import android.graphics.RectF;
import com.svamp.planetwars.math.Vector;

import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;

/*
 * Concrete sprite composite.
 * Acts as a single Sprite,
 * but works as a collection of them.
 */
public class CompositeSprite implements Sprite {
    private final ArrayList<Sprite> sprites = new ArrayList<Sprite>();

    //Iterate over and draw all sprites inside
    public void draw(float[] mvpMatrix) {
        for(Sprite sprite : sprites)
            sprite.draw(mvpMatrix);
    }
    //Add sprite to collection
    public void add(Sprite sprite) {
        sprites.add(sprite);
    }

    //Remove sprite from collection
    public void remove(Sprite sprite) {
        sprites.remove(sprite);
    }

    public RectF getBounds() {
        RectF temp = new RectF();
        for(Sprite sprite : sprites)
            temp.union(sprite.getBounds());
        return temp;
    }

    public void update(float dt) {
        for(Sprite sprite : sprites)
            sprite.update(dt);
    }

    @Override
    public boolean contains(Vector v) {
        return getBounds().contains(v.x,v.y);
    }
}
