package com.github.wohaopa.MyCTMLib.texture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * 反序列化 type=base 的 mcmeta。解析 render_type、emissive、tinting。
 */
public enum BaseTextureDataDeserializer implements TextureTypeRegistry.TextureTypeDeserializer<BaseTextureData> {

    INSTANCE;

    @Override
    public BaseTextureData deserialize(JsonObject json) throws JsonParseException {
        BaseTextureData.Builder builder = BaseTextureData.builder();

        if (json.has("render_type") && json.get("render_type").isJsonPrimitive()) {
            String rtStr = json.get("render_type").getAsString();
            BaseTextureData.RenderType rt = BaseTextureData.RenderType.fromString(rtStr);
            if (rt != null) {
                builder.renderType(rt);
            }
        }

        if (json.has("emissive") && json.get("emissive").isJsonPrimitive()) {
            builder.emissive(json.get("emissive").getAsBoolean());
        }

        if (json.has("tinting") && json.get("tinting").isJsonPrimitive()) {
            String tintStr = json.get("tinting").getAsString();
            builder.tinting(parseQuadTinting(tintStr));
        }

        return builder.build();
    }

    private static BaseTextureData.QuadTinting parseQuadTinting(String s) {
        if (s == null || s.isEmpty()) return null;
        String lower = s.toLowerCase();
        if ("biome_grass".equals(lower)) return BaseTextureData.QuadTinting.BIOME_GRASS;
        if ("biome_foliage".equals(lower)) return BaseTextureData.QuadTinting.BIOME_FOLIAGE;
        if ("biome_water".equals(lower)) return BaseTextureData.QuadTinting.BIOME_WATER;
        return null;
    }
}
