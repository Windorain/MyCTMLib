package com.github.wohaopa.MyCTMLib.ctmkey;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * CTMKey 工具类。
 * <p>
 * 提供与 CTMKey 相关的静态工具方法。
 */
public final class CTMKeyUtil {

    private CTMKeyUtil() {}

    /**
     * 解析模型纹理引用（#key 形式）。
     * <p>
     * 支持引用链（如 "#particle" -> "#all" -> "stone"）及循环检测。
     * 兼容两种 Map 存储：key 带 #（如 "#all"）或不带 #（如 "all"）。
     *
     * @param key      纹理引用，如 "#all" 或 "all"
     * @param textures 模型 textures Map
     * @return 解析后的纹理路径，或 null
     */
    public static String resolveTextureRef(String key, Map<String, String> textures) {
        if (key == null || textures == null) return null;
        return resolveTextureRef(key, textures, new HashSet<>());
    }

    private static String resolveTextureRef(String key, Map<String, String> textures, Set<String> visiting) {
        String lookupKey = key.startsWith("#") ? key.substring(1)
            .trim() : key;
        if (visiting.contains(lookupKey)) return null;
        visiting.add(lookupKey);
        try {
            String v = textures.get(key);
            if (v == null && key.startsWith("#")) v = textures.get(lookupKey);
            if (v != null && v.startsWith("#")) {
                return resolveTextureRef(v, textures, visiting);
            }
            return v;
        } finally {
            visiting.remove(lookupKey);
        }
    }

    /**
     * 获取纹理图集基础路径。
     * <p>
     * BLOCKS → textures/blocks
     * ITEMS → textures/items
     * OTHER → textures/blocks（默认）
     *
     * @param category 纹理类别
     * @return 基础路径
     */
    public static String getBasePath(CTMKey.TextureCategory category) {
        if (category == CTMKey.TextureCategory.ITEMS) return "textures/items";
        return "textures/blocks";
    }

    /**
     * 根据 canonicalKey 中 blocks/、items/ 等返回纹理类别。
     *
     * @param canonicalKey canonical 格式的键
     * @return 纹理类别
     */
    public static CTMKey.TextureCategory getTextureCategory(String canonicalKey) {
        if (canonicalKey == null) return CTMKey.TextureCategory.OTHER;
        int colon = canonicalKey.indexOf(':');
        String pathPart = colon >= 0 ? canonicalKey.substring(colon + 1) : canonicalKey;
        if (pathPart.startsWith("blocks/")) return CTMKey.TextureCategory.BLOCKS;
        if (pathPart.startsWith("items/")) return CTMKey.TextureCategory.ITEMS;
        return CTMKey.TextureCategory.OTHER;
    }
}
