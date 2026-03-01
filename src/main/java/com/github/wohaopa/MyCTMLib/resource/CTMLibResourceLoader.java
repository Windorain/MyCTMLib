package com.github.wohaopa.MyCTMLib.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.blockstate.BlockStateParser;
import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.ctmkey.CTMKey;
import com.github.wohaopa.MyCTMLib.ctmkey.CTMKeyUtil;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelParser;
import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureMetadataSection;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 资源重载时清空 BlockState/Model 注册表并尝试加载 blockstates/*.json 与 models/block/*.json（标准路径 assets/&lt;modid&gt;/models/block/）。
 * 在 Mod 入口注册为 IResourceManagerReloadListener。
 */
@SideOnly(Side.CLIENT)
public class CTMLibResourceLoader implements net.minecraft.client.resources.IResourceManagerReloadListener {

    private static final JsonParser JSON_PARSER = new JsonParser();
    private static CTMLibResourceLoader instance;
    private final BlockStateParser blockStateParser = new BlockStateParser();
    private final ModelParser modelParser = new ModelParser();
    private boolean loaded;

    public CTMLibResourceLoader() {
        instance = this;
    }

    /**
     * 若尚未加载，则执行 loadBlockStates + loadModels。供 loadTextureAtlas HEAD 注入在 listener 顺序不确定时调用。
     */
    public static void ensureLoaded(IResourceManager resourceManager) {
        if (instance != null) instance.doEnsureLoaded(resourceManager);
    }

    /**
     * 从 pendingModelData 预填充纹理到 TextureRegistry
     * 在 TextureStitchEvent.Pre 中调用
     */
    public void prefillTexturesFromPendingModels(IResourceManager resourceManager,
        net.minecraft.client.renderer.texture.TextureMap textureMap) {

        for (Map.Entry<String, ModelData> entry : ModelRegistry.getInstance()
            .getPendingModelDataEntries()) {
            String modelId = entry.getKey();
            ModelData data = entry.getValue();

            prefillTextureRegistryForModel(resourceManager, data, textureMap, modelId);
        }
    }

    /**
     * 获取单例实例
     */
    public static CTMLibResourceLoader getInstance() {
        return instance;
    }

    private void doEnsureLoaded(IResourceManager resourceManager) {
        if (loaded) return;
        if (!(resourceManager instanceof IReloadableResourceManager)) return;
        doLoad(resourceManager);
        loaded = true;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        loaded = false;
        BlockStateRegistry.getInstance()
            .clear();
        ModelRegistry.getInstance()
            .clear();
        DebugErrorCollector.getInstance()
            .clear();
        ResourceLoadTrace.getInstance()
            .clear();
        ResourceLoadTrace.getInstance()
            .add("reload_start", "", null, true);

        if (!(resourceManager instanceof IReloadableResourceManager)) {
            return;
        }

        doLoad(resourceManager);
        loaded = true;
        ResourceLoadTrace.getInstance()
            .add("reload_end", "", null, true);

        if (MyCTMLib.debugMode) {
            BlockStateRegistry.getInstance()
                .dumpForDebug();
            ModelRegistry.getInstance()
                .dumpForDebug();
            TextureRegistry.dumpForDebug();
            DebugErrorCollector.getInstance()
                .flushToFile(new File(Minecraft.getMinecraft().mcDataDir, "config/ctmlib_debug_errors.json"));
        }
    }

    private void doLoad(IResourceManager resourceManager) {
        loadBlockStates(resourceManager);
        loadModels(resourceManager);
    }

    /**
     * 遍历 Block 注册表，对每个 blockId 尝试加载 blockstates/&lt;path&gt;.json。
     */
    private void loadBlockStates(IResourceManager resourceManager) {
        try {
            Iterator<?> it = Block.blockRegistry.getKeys()
                .iterator();
            while (it.hasNext()) {
                Object key = it.next();
                if (!(key instanceof String)) continue;
                String blockId = (String) key;
                int colon = blockId.indexOf(':');
                String domain = colon >= 0 ? blockId.substring(0, colon)
                    .toLowerCase(Locale.ROOT) : "minecraft";
                String path = colon >= 0 ? blockId.substring(colon + 1) : blockId;
                String blockstatePath = "blockstates/" + path + ".json";
                ResourceLocation blockstateLoc = new ResourceLocation(domain, blockstatePath);
                String attemptedPath = "assets/" + blockstateLoc.getResourceDomain()
                    + "/"
                    + blockstateLoc.getResourcePath();
                try {
                    IResource res = resourceManager.getResource(blockstateLoc);
                    try (InputStream in = res.getInputStream()) {
                        blockStateParser.parseAndRegister(blockId.toLowerCase(Locale.ROOT), in);
                    }
                    ResourceLoadTrace.getInstance()
                        .add("blockstate_file", domain + ":" + blockstatePath, attemptedPath, true);
                } catch (IOException e) {
                    ResourceLoadTrace.getInstance()
                        .add("blockstate_file", domain + ":" + blockstatePath, attemptedPath, false, null, e);
                    if (MyCTMLib.debugMode) {
                        DebugErrorCollector.getInstance()
                            .add("blockstate", domain + ":" + blockstatePath, attemptedPath, e);
                        MyCTMLib.LOG.warn("BlockState parse failed: " + attemptedPath, e);
                    }
                } catch (Exception e) {
                    ResourceLoadTrace.getInstance()
                        .add("blockstate_file", domain + ":" + blockstatePath, attemptedPath, false, null, e);
                    if (MyCTMLib.debugMode) {
                        DebugErrorCollector.getInstance()
                            .add("blockstate", domain + ":" + blockstatePath, attemptedPath, e);
                        MyCTMLib.LOG.warn("BlockState parse failed: " + attemptedPath, e);
                    }
                }
            }
        } catch (Throwable t) {
            ResourceLoadTrace.getInstance()
                .add("blockstate_scan", "blockstate_scan", null, false, null, t);
            if (MyCTMLib.debugMode) {
                DebugErrorCollector.getInstance()
                    .add("blockstate", "blockstate_scan", t);
                MyCTMLib.LOG.warn("BlockState scan failed", t);
            }
        }
    }

    /**
     * 从 BlockStateRegistry 已注册的 modelId 中收集并加载对应模型。
     */
    private void loadModels(IResourceManager resourceManager) {
        for (String modelId : BlockStateRegistry.getInstance()
            .getAllModelIds()) {
            try {
                loadModel(resourceManager, modelId);
            } catch (Throwable t) {
                ResourceLoadTrace.getInstance()
                    .add("model_file", modelId, null, false, null, t);
                if (MyCTMLib.debugMode) {
                    DebugErrorCollector.getInstance()
                        .add("model", modelId, t);
                    MyCTMLib.LOG.warn("Model load failed: " + modelId, t);
                }
            }
        }
    }

    /**
     * 按 modelId（如 "modid:block/stone" 或 "modid:item/diamond"）加载并解析模型，写入 ModelRegistry。
     * 标准路径：assets/&lt;domain&gt;/models/block/&lt;name&gt;.json 或 models/item/&lt;name&gt;.json。
     */
    public void loadModel(IResourceManager resourceManager, String modelId) {
        int colon = modelId.indexOf(':');
        String domain = colon >= 0 ? modelId.substring(0, colon)
            .toLowerCase(Locale.ROOT) : "minecraft";
        String path = colon >= 0 ? modelId.substring(colon + 1) : modelId;
        String resourcePath = (path.startsWith("block/") || path.startsWith("item/")) ? "models/" + path + ".json"
            : "models/block/" + path + ".json";
        ResourceLocation modelLoc = new ResourceLocation(domain, resourcePath);
        String attemptedPath = "assets/" + modelLoc.getResourceDomain() + "/" + modelLoc.getResourcePath();
        try {
            tryLoadOneModel(resourceManager, domain, resourcePath, modelId);
            ResourceLoadTrace.getInstance()
                .add("model_file", modelId, attemptedPath, true);
        } catch (Exception e) {
            ResourceLoadTrace.getInstance()
                .add("model_file", modelId, attemptedPath, false, null, e);
            if (MyCTMLib.debugMode) {
                DebugErrorCollector.getInstance()
                    .add("model", modelId, attemptedPath, e);
                MyCTMLib.LOG.warn("Model load failed: " + attemptedPath, e);
            }
        }
    }

    private void tryLoadOneModel(IResourceManager resourceManager, String domain, String path, String modelId)
        throws IOException {
        IResource res = resourceManager.getResource(new ResourceLocation(domain, path));
        try (InputStream in = res.getInputStream()) {
            JsonObject root = JSON_PARSER
                .parse(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
                .getAsJsonObject();
            if (!modelParser.isSupported(root)) return;
            ModelData data = modelParser.parse(root);
            CTMKey key = CTMKey.from(CTMKey.Format.MODEL_ID, modelId);
            ModelRegistry.getInstance()
                .putRawModelData(modelId.toLowerCase(Locale.ROOT), data);
            if (MyCTMLib.debugMode && data.getTextures() != null
                && !data.getTextures()
                    .isEmpty()) {
                MyCTMLib.LOG.info("[CTMLibFusion] Model loaded: modelId={} textures={}", modelId, data.getTextures());
            }
            // 注意：不在这里调用 prefillTextureRegistryForModel
            // 预填充将在 TextureStitchEvent.Pre 中统一处理
        }
    }

    /**
     * 根据模型引用的纹理路径预填充 TextureRegistry。
     * 若已存在则跳过；若不存在则尝试加载纹理资源，有 ctmlib mcmeta 则创建 sprite 并注册。
     * 
     * @param resourceManager 资源管理器
     * @param data            模型数据
     * @param textureMap      纹理贴图（用于 registerIcon）
     * @param modelId         模型 ID（用于日志和 domain 提取）
     */
    private void prefillTextureRegistryForModel(IResourceManager resourceManager, ModelData data,
        net.minecraft.client.renderer.texture.TextureMap textureMap, String modelId) {

        Map<String, String> textures = data.getTextures();
        if (textures == null || textures.isEmpty()) return;

        // 从 modelId 提取 domain
        String domain = "minecraft";
        if (modelId != null && modelId.contains(":")) {
            domain = modelId.substring(0, modelId.indexOf(":"));
        }

        for (String value : textures.values()) {
            if (value == null) continue;

            String resolved = value.startsWith("#") ? CTMKeyUtil.resolveTextureRef(value, textures) : value;
            if (resolved == null || resolved.startsWith("#")) continue;

            // 使用 MODEL_TEXTURE 格式解析（会自动设置正确的 textureCategory）
            CTMKey key = CTMKey.from(CTMKey.Format.MODEL_TEXTURE, resolved);
            if (key == null) {
                if (MyCTMLib.debugMode) {
                    MyCTMLib.LOG.warn("[CTMLibFusion] Prefill failed: key=null modelId={} texture={}", modelId, value);
                }
                continue;
            }

            // 检查是否已注册
            if (TextureRegistry.getSprite(key) != null) {
                if (MyCTMLib.debugMode) {
                    MyCTMLib.LOG.debug("[CTMLibFusion] Prefill skip: already registered key={}", key);
                }
                continue;
            }

            // 构建资源位置
            String texPath = key.to(CTMKey.Format.TEXTURE_RESOURCE_LOCATION);
            ResourceLocation texRes = new ResourceLocation(key.domain(), texPath);
            String fullPath = "assets/" + texRes.getResourceDomain() + "/" + texRes.getResourcePath() + ".png";

            try {
                IResource resource = resourceManager.getResource(texRes);

                IMetadataSection sec;
                try {
                    sec = resource.getMetadata("ctmlib");
                } catch (Exception deserEx) {
                    if (MyCTMLib.debugMode) {
                        MyCTMLib.LOG.warn(
                            "[CTMLibFusion] Prefill failed: metadata deserialize failed key={} modelId={} error={}",
                            key,
                            modelId,
                            deserEx.getMessage());
                    }
                    ResourceLoadTrace.getInstance()
                        .add("texture_prefill_deserialize", key.toString(), fullPath, false, null, deserEx);
                    continue;
                }

                if (sec instanceof TextureMetadataSection tms) {
                    TextureTypeData typeData = tms.getData();

                    // 创建 CTMTextureAtlasSprite
                    String iconName = key.to(CTMKey.Format.TEXTURE_KEY);
                    CTMTextureAtlasSprite sprite = new CTMTextureAtlasSprite(key);

                    // 设置 CTM 字段
                    if (typeData instanceof ConnectingTextureData ctd) {
                        LayoutHandler handler = LayoutHandlers.get(ctd.getLayout());
                        sprite.setGridWidth(handler.getWidth());
                        sprite.setGridHeight(handler.getHeight());
                        sprite.setLayoutStyle(ctd.getLayout());
                    } else if (typeData instanceof RandomTextureData rtd) {
                        sprite.setGridWidth(rtd.getColumns());
                        sprite.setGridHeight(rtd.getRows());
                        sprite.setRandomCount(rtd.getCount());
                        sprite.setRandomSeed(rtd.getSeed() != null ? rtd.getSeed() : 0L);
                    } else if (typeData instanceof BaseTextureData btd) {
                        sprite.setRenderType(btd.getRenderType());
                        sprite.setEmissive(btd.isEmissive());
                        sprite.setTinting(btd.getTinting());
                    }

                    // 注册到 TextureRegistry 和 TextureMap
                    TextureRegistry.put(key, sprite);
                    textureMap.registerIcon(iconName);

                    if (MyCTMLib.debugMode) {
                        MyCTMLib.LOG
                            .info("[CTMLibFusion] Prefill OK: key={} modelId={} icon={}", key, modelId, iconName);
                    }
                    ResourceLoadTrace.getInstance()
                        .add("texture_prefill_ok", key.toString(), fullPath, true);

                } else {
                    if (MyCTMLib.debugMode) {
                        MyCTMLib.LOG
                            .debug("[CTMLibFusion] Prefill skip: no ctmlib metadata key={} modelId={}", key, modelId);
                    }
                    ResourceLoadTrace.getInstance()
                        .add("texture_prefill_skip", key.toString(), fullPath, true);
                }

            } catch (IOException e) {
                // 纹理不存在，静默跳过（debug 模式下记录）
                if (MyCTMLib.debugMode) {
                    MyCTMLib.LOG.debug(
                        "[CTMLibFusion] Prefill skip: resource not found key={} modelId={} path={}",
                        key,
                        modelId,
                        fullPath);
                }
                ResourceLoadTrace.getInstance()
                    .add("texture_prefill", key.toString(), fullPath, false, null, e);
            } catch (Exception e) {
                if (MyCTMLib.debugMode) {
                    MyCTMLib.LOG.warn(
                        "[CTMLibFusion] Prefill failed: unexpected error key={} modelId={} error={}",
                        key,
                        modelId,
                        e.getMessage());
                }
                ResourceLoadTrace.getInstance()
                    .add("texture_prefill", key.toString(), fullPath, false, null, e);
                DebugErrorCollector.getInstance()
                    .add("texture_prefill", key.toString(), fullPath, e);
            }
        }
    }
}
