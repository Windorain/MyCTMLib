package com.github.wohaopa.MyCTMLib.model.baked;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.common.util.ForgeDirection;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BakedModel {

    private final List<BakedQuad> allQuads;
    private final Map<ForgeDirection, List<BakedQuad>> quadsByFace;

    public BakedModel(List<BakedQuad> quads) {
        this.allQuads = Collections.unmodifiableList(new ArrayList<>(quads));

        Map<ForgeDirection, List<BakedQuad>> byFace = new EnumMap<>(ForgeDirection.class);
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            byFace.put(dir, new ArrayList<>());
        }
        for (BakedQuad quad : quads) {
            byFace.get(quad.getFace())
                .add(quad);
        }
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            byFace.put(dir, Collections.unmodifiableList(byFace.get(dir)));
        }
        this.quadsByFace = Collections.unmodifiableMap(byFace);
    }

    public List<BakedQuad> getAllQuads() {
        return allQuads;
    }

    public List<BakedQuad> getQuads(ForgeDirection face) {
        return quadsByFace.getOrDefault(face, Collections.emptyList());
    }

    /**
     * 将模型数据序列化为 JsonObject（用于 dump）
     * 
     * @return JsonObject 包含所有 quad 数据
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        JsonArray quadsArray = new JsonArray();
        for (BakedQuad quad : allQuads) {
            quadsArray.add(quad.toJson());
        }
        json.add("quads", quadsArray);
        return json;
    }
}
