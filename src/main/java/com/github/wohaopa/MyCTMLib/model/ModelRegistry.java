package com.github.wohaopa.MyCTMLib.model;

import java.util.Collection;
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

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ModelRegistry {

    private static final ModelRegistry INSTANCE = new ModelRegistry();

    @Deprecated
    private final Map<String, ModelData> modelById = new ConcurrentHashMap<>();

    @Deprecated
    private final Map<String, List<TextureModelEntry>> textureToModel = new ConcurrentHashMap<>();

    private final Object2ObjectOpenHashMap<CTMKey, BakedModel> bakedModelByKey = new Object2ObjectOpenHashMap<>();

    private final Object2ObjectOpenHashMap<String, BakedModel> bakedModelById = new Object2ObjectOpenHashMap<>();

    private final Object2ObjectOpenHashMap<String, ModelData> pendingModelData = new Object2ObjectOpenHashMap<>();

    public static ModelRegistry getInstance() {
        return INSTANCE;
    }

    @Deprecated
    public void put(String modelId, ModelData data) {
        putRawModelData(modelId, data);
    }

    public void putRawModelData(String modelId, ModelData data) {
        String normalizedId = modelId.toLowerCase(Locale.ROOT);
        pendingModelData.put(normalizedId, data);
    }

    public void bakeAll() {
        for (Object2ObjectMap.Entry<String, ModelData> entry : pendingModelData.object2ObjectEntrySet()) {
            String modelId = entry.getKey();
            ModelData data = entry.getValue();
            try {
                BakedModel baked = ModelBaker.bake(data);
                bakedModelById.put(modelId, baked);

                CTMKey key = CTMKey.from(CTMKey.Format.MODEL_ID, modelId);
                if (key != null) {
                    bakedModelByKey.put(key, baked);
                }
            } catch (Exception e) {
                if (MyCTMLib.debugMode) {
                    MyCTMLib.LOG.warn("[CTMLibFusion] Failed to bake model: " + modelId, e);
                }
            }
        }
        pendingModelData.clear();
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
        textureToModel.computeIfAbsent(texturePath.toLowerCase(Locale.ROOT), k -> new java.util.ArrayList<>())
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
        pendingModelData.clear();
    }

    public Collection<Map.Entry<String, ModelData>> getPendingModelDataEntries() {
        return Collections.unmodifiableCollection(pendingModelData.entrySet());
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
