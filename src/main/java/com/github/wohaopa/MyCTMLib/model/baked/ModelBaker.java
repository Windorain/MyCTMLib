package com.github.wohaopa.MyCTMLib.model.baked;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.ctmkey.CTMKey;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.google.gson.JsonObject;

public final class ModelBaker {

    private ModelBaker() {}

    public static BakedModel bake(ModelData modelData) {
        List<BakedQuad> quads = new ArrayList<>();

        Map<String, CTMKey> resolvedTextures = resolveTextures(modelData);
        Map<String, ConnectionPredicate> resolvedPredicates = resolvePredicates(modelData);

        for (ModelElement element : modelData.getElements()) {
            float[] from = element.getFrom();
            float[] to = element.getTo();

            float relMinX = Math.min(from[0], to[0]) / 16.0f;
            float relMaxX = Math.max(from[0], to[0]) / 16.0f;
            float relMinY = Math.min(from[1], to[1]) / 16.0f;
            float relMaxY = Math.max(from[1], to[1]) / 16.0f;
            float relMinZ = Math.min(from[2], to[2]) / 16.0f;
            float relMaxZ = Math.max(from[2], to[2]) / 16.0f;

            for (Map.Entry<ForgeDirection, ModelFace> entry : element.getFaces()
                .entrySet()) {
                ForgeDirection face = entry.getKey();
                ModelFace modelFace = entry.getValue();

                CTMKey textureKey = resolvedTextures.get(modelFace.getTextureKey());
                if (textureKey == null) {
                    String lookupKey = modelFace.getTextureKey();
                    if (lookupKey != null && lookupKey.startsWith("#")) {
                        lookupKey = lookupKey.substring(1)
                            .trim();
                    }
                    textureKey = resolvedTextures.get(lookupKey);
                }

                CTMTextureAtlasSprite sprite = null;
                if (textureKey != null) {
                    sprite = (CTMTextureAtlasSprite) TextureRegistry.getInstance()
                        .getIcon(textureKey.toCanonicalString());
                }

                ConnectionPredicate predicate = null;
                if (modelFace.getConnectionKey() != null) {
                    predicate = resolvedPredicates.get(modelFace.getConnectionKey());
                }

                if (sprite != null) {
                    BakedQuad quad = new BakedQuad(
                        face,
                        relMinX,
                        relMaxX,
                        relMinY,
                        relMaxY,
                        relMinZ,
                        relMaxZ,
                        sprite,
                        predicate,
                        0,
                        -1,
                        null);
                    quads.add(quad);
                }
            }
        }

        return new BakedModel(quads);
    }

    private static Map<String, CTMKey> resolveTextures(ModelData modelData) {
        Map<String, String> textures = modelData.getTextures();
        Map<String, CTMKey> result = new HashMap<>();

        for (Map.Entry<String, String> entry : textures.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            String resolved = resolveTexturePath(value, textures, new HashSet<>());
            if (resolved != null) {
                CTMKey ctmKey = CTMKey.from(CTMKey.Format.MODEL_TEXTURE, resolved);
                if (ctmKey != null) {
                    result.put(key, ctmKey);
                }
            }
        }

        return result;
    }

    private static String resolveTexturePath(String key, Map<String, String> textures, Set<String> visiting) {
        if (key == null || textures == null) return null;
        String lookupKey = key.startsWith("#") ? key.substring(1)
            .trim() : key;
        if (visiting.contains(lookupKey)) return null;
        visiting.add(lookupKey);
        try {
            String v = textures.get(key);
            if (v == null && key.startsWith("#")) v = textures.get(lookupKey);
            if (v == null && !key.startsWith("#")) {
                return key;
            }
            if (v != null && v.startsWith("#")) {
                return resolveTexturePath(v, textures, visiting);
            }
            return v;
        } finally {
            visiting.remove(lookupKey);
        }
    }

    private static Map<String, ConnectionPredicate> resolvePredicates(ModelData modelData) {
        Map<String, Object> connections = modelData.getConnections();
        Map<String, ConnectionPredicate> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : connections.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            ConnectionPredicate predicate = resolvePredicate(key, value, connections, new HashSet<>());
            if (predicate != null) {
                result.put(key, predicate);
            }
        }

        return result;
    }

    private static ConnectionPredicate resolvePredicate(String key, Object value, Map<String, Object> connections,
        Set<String> visiting) {
        if (visiting.contains(key)) return null;

        if (value instanceof String) {
            String s = ((String) value).trim();
            if (s.startsWith("#")) {
                String refKey = s.substring(1)
                    .trim();
                visiting.add(key);
                try {
                    Object refValue = connections.get(refKey);
                    return resolvePredicate(refKey, refValue, connections, visiting);
                } finally {
                    visiting.remove(key);
                }
            }
            return null;
        }

        if (value instanceof JsonObject) {
            try {
                return PredicateRegistry.deserialize((JsonObject) value);
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }
}
