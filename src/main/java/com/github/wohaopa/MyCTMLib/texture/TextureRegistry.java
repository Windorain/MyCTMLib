package com.github.wohaopa.MyCTMLib.texture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.ctmkey.CTMKey;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * 纹理注册表 - 静态工具类
 * 
 * <p>
 * 维护 CTMKey → CTMTextureAtlasSprite 映射
 * </p>
 * <p>
 * 按纹理类别分为两张表：BLOCKS 和 ITEMS
 * </p>
 * 
 * @author MyCTMLib
 * @since 2.0.0
 */
public class TextureRegistry {

    // ========== 数据结构 ==========

    /**
     * 方块纹理表
     */
    private static final Object2ObjectOpenHashMap<CTMKey, CTMTextureAtlasSprite> blockSprites = new Object2ObjectOpenHashMap<>();

    /**
     * 物品纹理表
     */
    private static final Object2ObjectOpenHashMap<CTMKey, CTMTextureAtlasSprite> itemSprites = new Object2ObjectOpenHashMap<>();

    // ========== 私有构造函数（禁止实例化） ==========

    private TextureRegistry() {}

    // ========== 注册 API ==========

    /**
     * 注册纹理到注册表
     * 
     * @param key    纹理键（CTMKey）
     * @param sprite CTM 纹理精灵
     */
    public static void put(CTMKey key, CTMTextureAtlasSprite sprite) {
        if (key == null || sprite == null) return;

        if (key.textureCategory() == CTMKey.TextureCategory.ITEMS) {
            itemSprites.put(key, sprite);
        } else {
            blockSprites.put(key, sprite);
        }
    }

    // ========== 查询 API ==========

    /**
     * 获取纹理精灵
     * 
     * @param key 纹理键
     * @return CTMTextureAtlasSprite，不存在则返回 null
     */
    public static CTMTextureAtlasSprite getSprite(CTMKey key) {
        if (key == null) return null;

        if (key.textureCategory() == CTMKey.TextureCategory.ITEMS) {
            return itemSprites.get(key);
        } else {
            return blockSprites.get(key);
        }
    }

    /**
     * 获取所有方块纹理键
     * 
     * @return 不可修改的方块纹理键集合
     */
    public static Collection<CTMKey> getAllBlockKeys() {
        return Collections.unmodifiableSet(blockSprites.keySet());
    }

    /**
     * 获取所有物品纹理键
     * 
     * @return 不可修改的物品纹理键集合
     */
    public static Collection<CTMKey> getAllItemKeys() {
        return Collections.unmodifiableSet(itemSprites.keySet());
    }

    // ========== 生命周期管理 ==========

    /**
     * 清空所有注册表
     */
    public static void clear() {
        blockSprites.clear();
        itemSprites.clear();
    }

    /**
     * 输出调试信息
     */
    public static void dumpForDebug() {
        if (!MyCTMLib.debugMode) return;
        MyCTMLib.LOG
            .info("[CTMLibFusion] TextureRegistry: blocks={}, items={}", blockSprites.size(), itemSprites.size());
    }

    /**
     * 获取方块纹理表大小（用于调试）
     * 
     * @return 方块纹理数量
     */
    public static int getBlockCount() {
        return blockSprites.size();
    }

    /**
     * 获取物品纹理表大小（用于调试）
     * 
     * @return 物品纹理数量
     */
    public static int getItemCount() {
        return itemSprites.size();
    }

    // ========== Dump API ==========

    /**
     * 导出 TextureRegistry 数据到 JSON 对象（用于 RegistryDumpUtil）
     * 
     * @return 包含 blocks、items 和 summary 的 JsonObject
     */
    public static Map<String, Object> dumpToJson() {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Map<String, Object>> blocks = new ArrayList<>();
        for (Map.Entry<CTMKey, CTMTextureAtlasSprite> entry : blockSprites.entrySet()) {
            blocks.add(serializeEntry(entry.getKey(), entry.getValue()));
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<CTMKey, CTMTextureAtlasSprite> entry : itemSprites.entrySet()) {
            items.add(serializeEntry(entry.getKey(), entry.getValue()));
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("blockCount", blockSprites.size());
        summary.put("itemCount", itemSprites.size());

        result.put("blocks", blocks);
        result.put("items", items);
        result.put("summary", summary);

        return result;
    }

    /**
     * 序列化单个纹理条目为 Map
     */
    private static Map<String, Object> serializeEntry(CTMKey key, CTMTextureAtlasSprite sprite) {
        Map<String, Object> entry = new LinkedHashMap<>();

        entry.put("key", key.to(CTMKey.Format.TEXTURE_KEY));
        entry.put("fullKey", key.toCanonicalString());
        entry.put("path", key.path());
        entry.put(
            "category",
            key.textureCategory()
                .name());

        Map<String, Object> spriteData = new LinkedHashMap<>();
        spriteData.put("iconName", sprite.getIconName());
        spriteData.put("gridWidth", sprite.getGridWidth());
        spriteData.put("gridHeight", sprite.getGridHeight());

        if (sprite.getRenderType() != null) {
            spriteData.put(
                "renderType",
                sprite.getRenderType()
                    .getId());
        } else {
            spriteData.put("renderType", null);
        }

        spriteData.put("emissive", sprite.isEmissive());

        if (sprite.getTinting() != null) {
            spriteData.put(
                "tinting",
                sprite.getTinting()
                    .name());
        } else {
            spriteData.put("tinting", null);
        }

        if (sprite.getLayoutStyle() != null) {
            spriteData.put(
                "layoutStyle",
                sprite.getLayoutStyle()
                    .name());
        } else {
            spriteData.put("layoutStyle", null);
        }

        spriteData.put("randomCount", sprite.getRandomCount());
        spriteData.put("randomSeed", sprite.getRandomSeed());

        entry.put("sprite", spriteData);

        return entry;
    }
}
