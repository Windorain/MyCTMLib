package com.github.wohaopa.MyCTMLib.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.ctmkey.CTMKey;
import com.github.wohaopa.MyCTMLib.model.baked.BakedModel;
import com.github.wohaopa.MyCTMLib.model.baked.ModelBaker;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ModelRegistry {

    private static final ModelRegistry INSTANCE = new ModelRegistry();

    @Deprecated
    private final Map<String, ModelData> modelById = new ConcurrentHashMap<>();

    @Deprecated
    private final Map<String, List<TextureModelEntry>> textureToModel = new ConcurrentHashMap<>();

    private final Object2ObjectOpenHashMap<CTMKey, BakedModel> bakedModelByKey = new Object2ObjectOpenHashMap<>();

    private final Object2ObjectOpenHashMap<String, BakedModel> bakedModelById = new Object2ObjectOpenHashMap<>();

    public static ModelRegistry getInstance() {
        return INSTANCE;
    }

    @Deprecated
    public void put(String modelId, ModelData data) {
        String normalizedId = modelId.toLowerCase(Locale.ROOT);
        modelById.put(normalizedId, data);

            try {
                BakedModel baked = ModelBaker.bake(data);
                bakedModelById.put(normalizedId, baked);

                CTMKey key = CTMKey.from(CTMKey.Format.MODEL_ID, normalizedId);
                if (key != null) {
                    bakedModelByKey.put(key, baked);
                }
            } catch (Exception e) {
            if (MyCTMLib.debugMode) {
                MyCTMLib.LOG.warn("[CTMLibFusion] Failed to bake model: " + modelId, e);
            }
        }
    }

    @Deprecated
    public ModelData get(String modelId) {
        return modelById.get(modelId.toLowerCase(Locale.ROOT));
    }

    public void put(CTMKey key, BakedModel model) {
        bakedModelByKey.put(key, model);
    }

    public BakedModel get(CTMKey key) {
        return bakedModelByKey.get(key);
    }

    public BakedModel getBakedModel(String modelId) {
        return bakedModelById.get(modelId.toLowerCase(Locale.ROOT));
    }

    @Deprecated
    public void putTextureFallback(String texturePath, String modelId, int faceOrdinal) {
        TextureModelEntry e = new TextureModelEntry(modelId, faceOrdinal);
        textureToModel
            .computeIfAbsent(texturePath.toLowerCase(Locale.ROOT), k -> new java.util.ArrayList<>())
            .add(e);
    }

    @Deprecated
    public List<TextureModelEntry> getModelsForTexture(String texturePath) {
        List<TextureModelEntry> list = textureToModel.get(texturePath.toLowerCase(Locale.ROOT));
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }

    public void clear() {
        modelById.clear();
        textureToModel.clear();
        bakedModelByKey.clear();
        bakedModelById.clear();
    }

    @Deprecated
    public Map<String, ModelData> getModelByIdForDump() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(modelById));
    }

    public void dumpForDebug() {
        if (!MyCTMLib.debugMode) return;
        MyCTMLib.LOG
            .info("[CTMLibFusion] ModelRegistry size={}, bakedSize={}", modelById.size(), bakedModelById.size());
    }

    @Deprecated
    public static class TextureModelEntry {

        public final String modelId;
        public final int faceOrdinal;

        public TextureModelEntry(String modelId, int faceOrdinal) {
            this.modelId = modelId;
            this.faceOrdinal = faceOrdinal;
        }
    }
}
