package com.svamp.planetwars.sprite;

import com.svamp.planetwars.R;

/**
 * Enum for use to access SpriteSheets from drawables.
 */
public enum SpriteSheetType {
    //Add sprites here.
    FIGHTER_SPRITE (3,8, R.drawable.fighter_sprite),
    BOMBER_SPRITE (1,8, R.drawable.bomber_sprite),
    EXPLOSIONS (16,7, R.drawable.explosions);


    public int getAnimNum() {
        return animNum;
    }
    public int getRotNum() {
        return rotNum;
    }
    public int getId() {
        return id;
    }

    private final int animNum;
    private final int rotNum;
    private final int id;
    private SpriteSheetType(int animNum,int rotNum,int id) {
        this.animNum=animNum;
        this.rotNum=rotNum;
        this.id=id;
    }
}