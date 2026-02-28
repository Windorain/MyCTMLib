package com.github.wohaopa.MyCTMLib.texture;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.ctmkey.CTMKey;
import com.github.wohaopa.MyCTMLib.ctmkey.CTMKeyUtil;
import com.github.wohaopa.MyCTMLib.mixins.AccessorTextureMap;

/**
 * 纹理路径（如 "modid:blocks/stone"）→ 解析后的 TextureTypeData。
 * 键语义：TexReg 内部统一用 canonicalKey（domain:blocks/name 或 domain:items/name）；图集 mapRegisteredSprites 的键为 mapKey（registerIcon
 * 传入名，各 mod 不统一）。
 * 本类维护 canonicalKey → mapKey 登记表（按 BLOCKS/ITEMS 分），每次我们向 map 写入 sprite 时登记；getIcon 优先用登记表单键查找，无记录时回退
 * getLookupCandidates。
 */
public class TextureRegistry {

    private static final TextureRegistry INSTANCE = new TextureRegistry();
    private final Map<String, TextureTypeData> pathToData = new ConcurrentHashMap<>();
    private final Map<CTMKey.TextureCategory, Map<String, String>> canonicalToMapKey = new EnumMap<>(
        CTMKey.TextureCategory.class);

    {
        canonicalToMapKey.put(CTMKey.TextureCategory.BLOCKS, new ConcurrentHashMap<>());
        canonicalToMapKey.put(CTMKey.TextureCategory.ITEMS, new ConcurrentHashMap<>());
    }

    public static TextureRegistry getInstance() {
        return INSTANCE;
    }

    public void put(String texturePath, TextureTypeData data) {
        if (texturePath == null || data == null) return;
        CTMKey.TextureCategory category = getCategoryFromPath(texturePath);
        CTMKey key = CTMKey.from(CTMKey.Format.TEXTURE_KEY, texturePath, category);
        if (key == null) return;
        pathToData.put(key.toCanonicalString(), data);
    }

    public TextureTypeData get(String texturePath) {
        if (texturePath == null) return null;
        CTMKey.TextureCategory category = getCategoryFromPath(texturePath);
        CTMKey key = CTMKey.from(CTMKey.Format.TEXTURE_KEY, texturePath, category);
        if (key == null) return null;
        return pathToData.get(key.toCanonicalString());
    }

    private CTMKey.TextureCategory getCategoryFromPath(String texturePath) {
        if (texturePath == null) return CTMKey.TextureCategory.BLOCKS;
        if (texturePath.contains("items/") || texturePath.contains(":items/")) {
            return CTMKey.TextureCategory.ITEMS;
        }
        return CTMKey.TextureCategory.BLOCKS;
    }

    /**
     * 登记 canonicalKey → mapKey（按图集分类）。在每次向 mapRegisteredSprites put 时调用，便于 getIcon 单键查找。
     */
    public void putCanonicalToMapKey(String canonicalKey, String mapKey,
        CTMKey.TextureCategory category) {
        if (canonicalKey == null || mapKey == null || category == null) return;
        Map<String, String> per = canonicalToMapKey.get(category);
        if (per != null) per.put(canonicalKey, mapKey);
    }

    /**
     * 从 TextureMap 的 mapRegisteredSprites 中按 texturePath 查找 IIcon。
     * 优先用登记表 canonical→mapKey 单键查找；无记录时回退 getLookupCandidates。
     */
    public IIcon getIcon(String texturePath) {
        return getIcon(texturePath, CTMKey.TextureCategory.BLOCKS);
    }

    public IIcon getIcon(String texturePath, CTMKey.TextureCategory category) {
        if (texturePath == null) return null;
        CTMKey key = CTMKey.from(CTMKey.Format.TEXTURE_KEY, texturePath, category);
        if (key == null) return null;
        String canonicalKey = key.toCanonicalString();
        if (get(canonicalKey) == null) return null;
        net.minecraft.util.ResourceLocation texMapLoc = CTMKey.TextureCategory.ITEMS == category
            ? net.minecraft.client.renderer.texture.TextureMap.locationItemsTexture
            : net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture;
        Object texObj = Minecraft.getMinecraft()
            .getTextureManager()
            .getTexture(texMapLoc);
        if (!(texObj instanceof TextureMap textureMap)) return null;
        Map<String, TextureAtlasSprite> map = ((AccessorTextureMap) textureMap).getMapRegisteredSprites();
        Map<String, String> per = canonicalToMapKey.get(category);
        if (per != null) {
            String mapKey = per.get(canonicalKey);
            if (mapKey != null) {
                TextureAtlasSprite sprite = map.get(mapKey);
                if (sprite != null) return sprite;
            }
        }
        return null;
    }

    public void clear() {
        pathToData.clear();
        for (Map<String, String> per : canonicalToMapKey.values()) {
            if (per != null) per.clear();
        }
    }

    /** 供 RegistryDumpUtil 导出，按 TextureTypeData 去重后每个 value 保留一个 representative key。 */
    public Map<String, TextureTypeData> getPathToDataForDump() {
        Map<TextureTypeData, String> firstKeyPerValue = new LinkedHashMap<>();
        for (Map.Entry<String, TextureTypeData> e : pathToData.entrySet()) {
            firstKeyPerValue.putIfAbsent(e.getValue(), e.getKey());
        }
        Map<String, TextureTypeData> result = new LinkedHashMap<>();
        for (Map.Entry<TextureTypeData, String> e : firstKeyPerValue.entrySet()) {
            result.put(e.getValue(), e.getKey());
        }
        return result;
    }

    /** debug 模式下仅打出 size 摘要，避免刷屏。 */
    public void dumpForDebug() {
        if (!MyCTMLib.debugMode) return;
        MyCTMLib.LOG.info("[CTMLibFusion] TextureRegistry size={}", pathToData.size());
    }
}
