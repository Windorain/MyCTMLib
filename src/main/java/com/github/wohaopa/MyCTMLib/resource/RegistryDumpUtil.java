package com.github.wohaopa.MyCTMLib.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 导出三大 Registry（BlockStateRegistry、ModelRegistry、TextureRegistry）的详细数据至 JSON 文件。
 * 由 /ctmlib dump_registry 命令调用。
 */
public class RegistryDumpUtil {

    /**
     * 导出完整 Registry dump 到指定文件。
     * 格式见计划文档。
     */
    public static void dumpToFile(File outputFile) {
        try {
            JsonObject root = new JsonObject();

            // BlockStateRegistry
            JsonObject bsRoot = new JsonObject();
            JsonArray bsEntries = new JsonArray();
            Map<String, Map<String, String>> blockToVariants = BlockStateRegistry.getInstance()
                .getBlockToVariantsForDump();
            for (Map.Entry<String, Map<String, String>> e : blockToVariants.entrySet()) {
                JsonObject entry = new JsonObject();
                entry.addProperty("blockId", e.getKey());
                JsonObject variants = new JsonObject();
                for (Map.Entry<String, String> v : e.getValue()
                    .entrySet()) {
                    variants.addProperty(v.getKey(), v.getValue());
                }
                entry.add("variants", variants);
                bsEntries.add(entry);
            }
            bsRoot.add("entries", bsEntries);
            root.add("blockStateRegistry", bsRoot);

            // ModelRegistry
            Map<String, Object> modelDump = ModelRegistry.getInstance().dumpToJson();
            JsonObject modelRoot = new JsonObject();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> pendingModels = (List<Map<String, Object>>) modelDump.get("pendingModels");
            JsonArray pendingArray = new JsonArray();
            for (Map<String, Object> entry : pendingModels) {
                pendingArray.add(new GsonBuilder().create().toJsonTree(entry));
            }
            modelRoot.add("pendingModels", pendingArray);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> bakedModels = (List<Map<String, Object>>) modelDump.get("bakedModels");
            JsonArray bakedArray = new JsonArray();
            for (Map<String, Object> entry : bakedModels) {
                bakedArray.add(new GsonBuilder().create().toJsonTree(entry));
            }
            modelRoot.add("bakedModels", bakedArray);

            @SuppressWarnings("unchecked")
            Map<String, Object> modelSummary = (Map<String, Object>) modelDump.get("summary");
            JsonObject modelSummaryJson = new JsonObject();
            modelSummaryJson.addProperty("pendingCount", ((Number) modelSummary.get("pendingCount")).intValue());
            modelSummaryJson.addProperty("bakedCount", ((Number) modelSummary.get("bakedCount")).intValue());
            modelRoot.add("summary", modelSummaryJson);

            root.add("modelRegistry", modelRoot);

            // TextureRegistry
            Map<String, Object> texDump = TextureRegistry.dumpToJson();
            JsonObject texRoot = new JsonObject();
            JsonArray texEntries = new JsonArray();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> blocks = (List<Map<String, Object>>) texDump.get("blocks");
            for (Map<String, Object> entry : blocks) {
                texEntries.add(new GsonBuilder().create().toJsonTree(entry));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) texDump.get("items");
            for (Map<String, Object> entry : items) {
                texEntries.add(new GsonBuilder().create().toJsonTree(entry));
            }

            texRoot.add("entries", texEntries);

            @SuppressWarnings("unchecked")
            Map<String, Object> summary = (Map<String, Object>) texDump.get("summary");
            JsonObject summaryJson = new JsonObject();
            summaryJson.addProperty("blockCount", ((Number) summary.get("blockCount")).intValue());
            summaryJson.addProperty("itemCount", ((Number) summary.get("itemCount")).intValue());
            texRoot.add("summary", summaryJson);

            root.add("textureRegistry", texRoot);

            outputFile.getParentFile()
                .mkdirs();
            try (OutputStreamWriter w = new OutputStreamWriter(
                new FileOutputStream(outputFile),
                StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create()
                    .toJson(root, w);
            }
            MyCTMLib.LOG.info(
                "[CTMLibFusion] RegistryDump written to {} (blockState={} model(pending={}, baked={}) texture={})",
                outputFile,
                bsEntries.size(),
                pendingArray.size(),
                bakedArray.size(),
                texEntries.size());
        } catch (Exception e) {
            MyCTMLib.LOG.warn("[CTMLibFusion] RegistryDump failed", e);
        }
    }
}
