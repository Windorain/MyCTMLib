package com.github.wohaopa.MyCTMLib.model.baked;

import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;
import com.google.gson.JsonObject;

public class BakedQuad {

    private final ForgeDirection face;
    private final float relMinX, relMaxX;
    private final float relMinY, relMaxY;
    private final float relMinZ, relMaxZ;
    private final CTMTextureAtlasSprite sprite;
    private final ConnectionPredicate predicate;
    private final int rotation;
    private final int tintindex;
    private final ForgeDirection cullface;

    public BakedQuad(ForgeDirection face, float relMinX, float relMaxX, float relMinY, float relMaxY, float relMinZ,
        float relMaxZ, CTMTextureAtlasSprite sprite, ConnectionPredicate predicate, int rotation, int tintindex,
        ForgeDirection cullface) {
        this.face = face;
        this.relMinX = relMinX;
        this.relMaxX = relMaxX;
        this.relMinY = relMinY;
        this.relMaxY = relMaxY;
        this.relMinZ = relMinZ;
        this.relMaxZ = relMaxZ;
        this.sprite = sprite;
        this.predicate = predicate;
        this.rotation = rotation;
        this.tintindex = tintindex;
        this.cullface = cullface;
    }

    public ForgeDirection getFace() {
        return face;
    }

    public float getRelMinX() {
        return relMinX;
    }

    public float getRelMaxX() {
        return relMaxX;
    }

    public float getRelMinY() {
        return relMinY;
    }

    public float getRelMaxY() {
        return relMaxY;
    }

    public float getRelMinZ() {
        return relMinZ;
    }

    public float getRelMaxZ() {
        return relMaxZ;
    }

    public CTMTextureAtlasSprite getSprite() {
        return sprite;
    }

    public ConnectionPredicate getPredicate() {
        return predicate;
    }

    public int getRotation() {
        return rotation;
    }

    public int getTintindex() {
        return tintindex;
    }

    public ForgeDirection getCullface() {
        return cullface;
    }

    /**
     * 将 quad 数据序列化为 JsonObject（用于 dump）
     * 
     * @return JsonObject 包含所有 quad 字段
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("face", face.name());
        json.addProperty("relMinX", relMinX);
        json.addProperty("relMaxX", relMaxX);
        json.addProperty("relMinY", relMinY);
        json.addProperty("relMaxY", relMaxY);
        json.addProperty("relMinZ", relMinZ);
        json.addProperty("relMaxZ", relMaxZ);

        if (sprite != null) {
            json.add("sprite", sprite.toJson());
        } else {
            json.add("sprite", null);
        }

        if (predicate != null) {
            json.add("predicate", predicate.toJson());
        } else {
            json.add("predicate", null);
        }

        json.addProperty("rotation", rotation);
        json.addProperty("tintindex", tintindex);

        if (cullface != null) {
            json.addProperty("cullface", cullface.name());
        } else {
            json.add("cullface", null);
        }

        return json;
    }
}
