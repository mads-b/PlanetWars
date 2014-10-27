package com.svamp.planetwars.sprite;

import com.svamp.planetwars.opengl.TextureTool;
import com.svamp.planetwars.math.Vector;

import java.util.Collection;


public class BlobSprite extends AbstractLineSprite {
    private static final String TAG = BlobSprite.class.getCanonicalName();

    public BlobSprite(Collection<Vector> line,int color) {
        super(line);
        setColor(TextureTool.splitColor(color));
    }
}
