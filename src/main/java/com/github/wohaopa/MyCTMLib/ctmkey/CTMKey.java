package com.github.wohaopa.MyCTMLib.ctmkey;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

/**
 * CTMKey - 统一的纹理和模型键表示
 * 
 * <h2>设计原则</h2>
 * <ul>
 *   <li>所有 TEXTURE 类型的 path 统一使用 {@code blocks/} 或 {@code items/} 前缀</li>
 *   <li>所有 MODEL 类型的 path 统一使用 {@code block/} 或 {@code item/} 前缀</li>
 *   <li>domain 为 {@code minecraft} 时输入输出均省略前缀</li>
 *   <li>通过 {@link Format} 枚举显式指定解析和输出格式</li>
 * </ul>
 * 
 * <h2>内部存储规范</h2>
 * <table border="1">
 *   <tr><th>Type</th><th>path 前缀</th><th>textureCategory</th></tr>
 *   <tr><td>TEXTURE (BLOCKS)</td><td>{@code blocks/xxx}</td><td>BLOCKS</td></tr>
 *   <tr><td>TEXTURE (ITEMS)</td><td>{@code items/xxx}</td><td>ITEMS</td></tr>
 *   <tr><td>MODEL</td><td>{@code block/xxx} 或 {@code item/xxx}</td><td>NONE</td></tr>
 * </table>
 * 
 * <h2>使用示例</h2>
 * <pre>
 * // 解析模型纹理引用
 * CTMKey key = CTMKey.from(Format.MODEL_TEXTURE, "block/stone_ctm");
 * // 内部存储：{ type=TEXTURE, path=blocks/stone_ctm, textureCategory=BLOCKS }
 * 
 * // 解析纹理注册键
 * CTMKey key = CTMKey.from(Format.TEXTURE_KEY, "stone", TextureCategory.BLOCKS);
 * // 内部存储：{ type=TEXTURE, path=blocks/stone, textureCategory=BLOCKS }
 * 
 * // 输出为不同格式
 * key.to(Format.MODEL_TEXTURE);  // → "block/stone_ctm"
 * key.to(Format.TEXTURE_KEY);    // → "stone_ctm"
 * key.to(Format.FULL_PATH);      // → "textures/blocks/stone_ctm"
 * </pre>
 */
public final class CTMKey implements Comparable<CTMKey>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 键类型枚举
     */
    public enum Type {
        /** 方块（未使用，保留） */
        BLOCK,
        /** 模型 */
        MODEL,
        /** 纹理 */
        TEXTURE,
        /** 物品（未使用，保留） */
        ITEM,
        /** 未知类型 */
        UNKNOWN
    }

    /**
     * 纹理类别枚举
     */
    public enum TextureCategory {
        /** 方块图集（TextureMap.locationBlocksTexture） */
        BLOCKS,
        /** 物品图集（TextureMap.locationItemsTexture） */
        ITEMS,
        /** 其他（未使用） */
        OTHER,
        /** 无类别（用于 MODEL 类型） */
        NONE
    }

    private static final int MAX_CACHE_SIZE = 10000;
    private static final Object2ObjectLinkedOpenHashMap<String, CTMKey> CACHE = new Object2ObjectLinkedOpenHashMap<>();
    private static final LinkedList<String> ACCESS_ORDER = new LinkedList<>();

    static {
        CACHE.defaultReturnValue(null);
    }

    private final String domain;
    private final Type type;
    private final String path;
    private final String variant;
    private final TextureCategory textureCategory;

    private final String hashCodeString;
    private final int hashCode;

    private CTMKey(String domain, Type type, String path, String variant, TextureCategory textureCategory) {
        this.domain = domain;
        this.type = type;
        this.path = path;
        this.variant = variant;
        this.textureCategory = textureCategory;

        this.hashCodeString = buildHashCodeString();
        this.hashCode = computeHashCode();
    }

    /**
     * 格式枚举 - 定义输入输出的格式规范
     * 
     * <h2>使用说明</h2>
     * <ul>
     *   <li>{@link #from(Format, String)} - 将指定格式的输入解析为 CTMKey</li>
     *   <li>{@link #to(Format)} - 将 CTMKey 输出为指定格式的字符串</li>
     *   <li>{@link #TEXTURE_KEY} 格式需要使用 {@link #from(Format, String, TextureCategory)} 重载</li>
     * </ul>
     * 
     * <h2>各格式详解</h2>
     * 
     * <h3>MODEL_TEXTURE</h3>
     * <p><strong>用途</strong>：模型 JSON 中的纹理引用（用于 {@code ModelBaker.resolveTextures}）</p>
     * <p><strong>输入示例</strong>：</p>
     * <ul>
     *   <li>{@code "block/stone_ctm"}</li>
     *   <li>{@code "item/diamond"}</li>
     *   <li>{@code "minecraft:block/cobblestone"}</li>
     * </ul>
     * <p><strong>解析规则</strong>：</p>
     * <ul>
     *   <li>Type: TEXTURE</li>
     *   <li>path: {@code block/xxx} → {@code blocks/xxx}，{@code item/xxx} → {@code items/xxx}</li>
     *   <li>textureCategory: 根据前缀推断（{@code blocks/}→BLOCKS, {@code items/}→ITEMS）</li>
     * </ul>
     * <p><strong>输出规则</strong>：</p>
     * <ul>
     *   <li>{@code blocks/xxx} → {@code block/xxx}（用于模型 JSON）</li>
     *   <li>{@code items/xxx} → {@code item/xxx}（用于模型 JSON）</li>
     * </ul>
     * 
     * <h3>MODEL_ID</h3>
     * <p><strong>用途</strong>：模型 ID（用于 {@code ModelRegistry}）</p>
     * <p><strong>输入示例</strong>：</p>
     * <ul>
     *   <li>{@code "block/stone"}</li>
     *   <li>{@code "item/diamond"}</li>
     *   <li>{@code "minecraft:models/block/stone"}</li>
     * </ul>
     * <p><strong>解析规则</strong>：</p>
     * <ul>
     *   <li>Type: MODEL</li>
     *   <li>path: 保留 {@code block/} 或 {@code item/} 前缀</li>
     *   <li>textureCategory: NONE</li>
     * </ul>
     * <p><strong>输出规则</strong>：</p>
     * <ul>
     *   <li>直接输出 path（{@code block/xxx} 或 {@code item/xxx}）</li>
     * </ul>
     * 
     * <h3>TEXTURE_KEY</h3>
     * <p><strong>用途</strong>：纹理注册键（用于 {@code TextureRegistry} / {@code MixinTextureMap}）</p>
     * <p><strong>输入示例</strong>：</p>
     * <ul>
     *   <li>{@code "stone"}（mapKey，无前缀）</li>
     *   <li>{@code "minecraft:stone"}</li>
     *   <li>{@code "ic2:blockAlloyGlass"}</li>
     * </ul>
     * <p><strong>解析规则</strong>：</p>
     * <ul>
     *   <li>Type: TEXTURE</li>
     *   <li>path: 根据 TextureCategory 添加 {@code blocks/} 或 {@code items/} 前缀</li>
     *   <li>textureCategory: 由 {@link #from(Format, String, TextureCategory)} 参数指定</li>
     * </ul>
     * <p><strong>输出规则</strong>：</p>
     * <ul>
     *   <li>去掉 {@code blocks/} 或 {@code items/} 前缀，返回纯 mapKey</li>
     * </ul>
     * <p><strong>注意</strong>：此格式必须使用 {@link #from(Format, String, TextureCategory)} 重载方法</p>
     * 
     * <h3>FULL_PATH</h3>
     * <p><strong>用途</strong>：完整资源路径（用于外部输入/资源加载）</p>
     * <p><strong>输入示例</strong>：</p>
     * <ul>
     *   <li>{@code "textures/blocks/stone"}</li>
     *   <li>{@code "minecraft:textures/items/diamond"}</li>
     *   <li>{@code "assets/minecraft/models/block/stone"}</li>
     * </ul>
     * <p><strong>解析规则</strong>：</p>
     * <ul>
     *   <li>{@code textures/blocks/xxx} → Type.TEXTURE, path={@code blocks/xxx}, textureCategory=BLOCKS</li>
     *   <li>{@code textures/items/xxx} → Type.TEXTURE, path={@code items/xxx}, textureCategory=ITEMS</li>
     *   <li>{@code models/block/xxx} → Type.MODEL, path={@code block/xxx}, textureCategory=NONE</li>
     *   <li>{@code models/item/xxx} → Type.MODEL, path={@code item/xxx}, textureCategory=NONE</li>
     * </ul>
     * <p><strong>输出规则</strong>：</p>
     * <ul>
     *   <li>TEXTURE + BLOCKS → {@code textures/blocks/xxx}</li>
     *   <li>TEXTURE + ITEMS → {@code textures/items/xxx}</li>
     *   <li>MODEL → {@code models/block/xxx} 或 {@code models/item/xxx}</li>
     * </ul>
     */
    public enum Format {
        /**
         * 模型纹理引用格式
         * <p>用于模型 JSON 中的 textures 字段，如 {@code {"base": "block/stone_ctm"}}</p>
         */
        MODEL_TEXTURE,

        /**
         * 模型 ID 格式
         * <p>用于模型注册，如 {@code "minecraft:block/stone"}</p>
         */
        MODEL_ID,

        /**
         * 纹理注册键格式
         * <p>用于纹理注册到 TextureRegistry，需要配合 TextureCategory 使用</p>
         * <p>输入为不带前缀的 mapKey（如 {@code "stone"}），输出同理</p>
         */
        TEXTURE_KEY,

        /**
         * 完整资源路径格式
         * <p>用于文件系统路径解析，如 {@code "assets/minecraft/textures/blocks/stone.png"}</p>
         */
        FULL_PATH
    }

    /**
     * 根据指定格式解析输入字符串创建 CTMKey
     * 
     * @param format 解析格式
     * @param input 输入字符串
     * @return CTMKey 或 null（解析失败）
     */
    public static CTMKey from(Format format, String input) {
        return from(format, input, null);
    }

    /**
     * 根据指定格式解析输入字符串创建 CTMKey
     * 
     * @param format 解析格式
     * @param input 输入字符串
     * @param category 纹理类别（仅当 format=TEXTURE_KEY 时需要）
     * @return CTMKey 或 null（解析失败）
     */
    public static CTMKey from(Format format, String input, TextureCategory category) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String cacheKey = format.name() + ":" + input + ":" + (category != null ? category.name() : "");
        synchronized (CACHE) {
            CTMKey existing = CACHE.get(cacheKey);
            if (existing != null) {
                updateAccessOrder(cacheKey);
                return existing;
            }
        }

        ParsedResult parsed = parseByFormat(format, input, category);
        if (parsed == null) {
            return null;
        }

        CTMKey key = new CTMKey(parsed.domain, parsed.type, parsed.path, parsed.variant, parsed.textureCategory);

        synchronized (CACHE) {
            cachePut(cacheKey, key);
        }

        return key;
    }

    private static ParsedResult parseByFormat(Format format, String input, TextureCategory category) {
        String cleaned = input.replace('\\', '/').trim();
        if (cleaned.isEmpty()) {
            return null;
        }

        // 可选：移除 assets/ 前缀
        if (cleaned.startsWith("assets/")) {
            cleaned = cleaned.substring("assets/".length());
        }

        ParsedResult result = new ParsedResult();

        // 解析 domain
        int colonIdx = cleaned.indexOf(':');
        if (colonIdx >= 0) {
            result.domain = cleaned.substring(0, colonIdx).toLowerCase(Locale.ROOT);
            String remaining = cleaned.substring(colonIdx + 1);
            // 可选：移除 domain 后面的 assets/ 前缀
            if (remaining.startsWith("assets/")) {
                remaining = remaining.substring("assets/".length());
            }
            parsePathByFormat(format, remaining, result, category);
        } else {
            result.domain = "minecraft";
            parsePathByFormat(format, cleaned, result, category);
        }

        return result;
    }

    private static void parsePathByFormat(Format format, String pathPart, ParsedResult result, TextureCategory category) {
        // 处理 variant (& 符号)
        int ampIdx = pathPart.indexOf('&');
        if (ampIdx >= 0) {
            result.variant = pathPart.substring(ampIdx + 1);
            pathPart = pathPart.substring(0, ampIdx);
        } else {
            result.variant = null;
        }

        switch (format) {
            case MODEL_TEXTURE:
                // 模型纹理引用：Type=TEXTURE，path 转换为 blocks/ 或 items/ 前缀
                result.type = Type.TEXTURE;
                if (pathPart.startsWith("block/")) {
                    result.path = "blocks/" + pathPart.substring("block/".length());
                    result.textureCategory = TextureCategory.BLOCKS;
                } else if (pathPart.startsWith("item/")) {
                    result.path = "items/" + pathPart.substring("item/".length());
                    result.textureCategory = TextureCategory.ITEMS;
                } else if (pathPart.startsWith("blocks/")) {
                    result.path = pathPart;
                    result.textureCategory = TextureCategory.BLOCKS;
                } else if (pathPart.startsWith("items/")) {
                    result.path = pathPart;
                    result.textureCategory = TextureCategory.ITEMS;
                } else {
                    // 无前缀，默认添加 blocks/
                    result.path = "blocks/" + pathPart;
                    result.textureCategory = TextureCategory.BLOCKS;
                }
                break;

            case MODEL_ID:
                // 模型 ID：Type=MODEL，path 保留 block/ 或 item/ 前缀
                result.type = Type.MODEL;
                result.textureCategory = TextureCategory.NONE;
                if (pathPart.startsWith("models/block/")) {
                    result.path = pathPart.substring("models/".length());
                } else if (pathPart.startsWith("models/item/")) {
                    result.path = pathPart.substring("models/".length());
                } else if (pathPart.startsWith("block/")) {
                    result.path = pathPart;
                } else if (pathPart.startsWith("item/")) {
                    result.path = pathPart;
                } else {
                    // 无前缀，默认添加 block/
                    result.path = "block/" + pathPart;
                }
                break;

            case TEXTURE_KEY:
                // 纹理注册键：Type=TEXTURE，根据 category 添加 blocks/ 或 items/ 前缀
                result.type = Type.TEXTURE;
                if (category == TextureCategory.ITEMS) {
                    result.textureCategory = TextureCategory.ITEMS;
                    if (pathPart.startsWith("items/")) {
                        result.path = pathPart;
                    } else if (pathPart.startsWith("item/")) {
                        result.path = "items/" + pathPart.substring("item/".length());
                    } else {
                        result.path = "items/" + pathPart;
                    }
                } else {
                    // 默认 BLOCKS
                    result.textureCategory = TextureCategory.BLOCKS;
                    if (pathPart.startsWith("blocks/")) {
                        result.path = pathPart;
                    } else if (pathPart.startsWith("block/")) {
                        result.path = "blocks/" + pathPart.substring("block/".length());
                    } else {
                        result.path = "blocks/" + pathPart;
                    }
                }
                break;

            case FULL_PATH:
                // 完整资源路径：根据路径结构自动推断
                if (pathPart.startsWith("textures/blocks/")) {
                    result.type = Type.TEXTURE;
                    result.path = pathPart.substring("textures/".length());
                    result.textureCategory = TextureCategory.BLOCKS;
                } else if (pathPart.startsWith("textures/items/")) {
                    result.type = Type.TEXTURE;
                    result.path = pathPart.substring("textures/".length());
                    result.textureCategory = TextureCategory.ITEMS;
                } else if (pathPart.startsWith("models/block/")) {
                    result.type = Type.MODEL;
                    result.path = pathPart.substring("models/".length());
                    result.textureCategory = TextureCategory.NONE;
                } else if (pathPart.startsWith("models/item/")) {
                    result.type = Type.MODEL;
                    result.path = pathPart.substring("models/".length());
                    result.textureCategory = TextureCategory.NONE;
                } else {
                    result.type = Type.UNKNOWN;
                    result.path = pathPart;
                    result.textureCategory = TextureCategory.NONE;
                }
                break;

            default:
                return;
        }
    }

    /**
     * 将 CTMKey 转换为指定格式的字符串表示
     * 
     * @param format 目标格式
     * @return 格式化后的字符串
     * @throws IllegalArgumentException 当格式与内部类型不匹配时
     */
    public String to(Format format) {
        switch (format) {
            case MODEL_TEXTURE:
                if (type != Type.TEXTURE) {
                    throw new IllegalArgumentException("Cannot convert " + type + " to MODEL_TEXTURE format");
                }
                return outputModelTexture();

            case MODEL_ID:
                if (type != Type.MODEL) {
                    throw new IllegalArgumentException("Cannot convert " + type + " to MODEL_ID format");
                }
                return outputModelId();

            case TEXTURE_KEY:
                if (type != Type.TEXTURE) {
                    throw new IllegalArgumentException("Cannot convert " + type + " to TEXTURE_KEY format");
                }
                return outputTextureKey();

            case FULL_PATH:
                return outputFullPath();

            default:
                return toString();
        }
    }

    private String outputModelTexture() {
        StringBuilder sb = new StringBuilder();
        if (!"minecraft".equals(domain)) {
            sb.append(domain).append(':');
        }
        
        // blocks/ → block/, items/ → item/
        if (path.startsWith("blocks/")) {
            sb.append("block/").append(path.substring("blocks/".length()));
        } else if (path.startsWith("items/")) {
            sb.append("item/").append(path.substring("items/".length()));
        } else {
            sb.append(path);
        }
        
        if (variant != null) {
            sb.append('&').append(variant);
        }
        return sb.toString();
    }

    private String outputModelId() {
        StringBuilder sb = new StringBuilder();
        if (!"minecraft".equals(domain)) {
            sb.append(domain).append(':');
        }
        sb.append(path);
        if (variant != null) {
            sb.append('&').append(variant);
        }
        return sb.toString();
    }

    private String outputTextureKey() {
        StringBuilder sb = new StringBuilder();
        if (!"minecraft".equals(domain)) {
            sb.append(domain).append(':');
        }
        
        // 去掉 blocks/ 或 items/ 前缀，返回纯 mapKey
        String cleanPath = path;
        if (cleanPath.startsWith("blocks/")) {
            cleanPath = cleanPath.substring("blocks/".length());
        } else if (cleanPath.startsWith("items/")) {
            cleanPath = cleanPath.substring("items/".length());
        }
        sb.append(cleanPath);
        
        if (variant != null) {
            sb.append('&').append(variant);
        }
        return sb.toString();
    }

    private String outputFullPath() {
        StringBuilder sb = new StringBuilder();
        if (!"minecraft".equals(domain)) {
            sb.append(domain).append(':');
        }
        
        switch (type) {
            case TEXTURE:
                sb.append("textures/");
                if (textureCategory == TextureCategory.ITEMS) {
                    sb.append("items/");
                    String cleanPath = path.startsWith("items/") ? path.substring("items/".length()) : path;
                    sb.append(cleanPath);
                } else {
                    sb.append("blocks/");
                    String cleanPath = path.startsWith("blocks/") ? path.substring("blocks/".length()) : path;
                    sb.append(cleanPath);
                }
                break;
            case MODEL:
                sb.append("models/").append(path);
                break;
            default:
                sb.append(path);
        }
        
        if (variant != null) {
            sb.append('&').append(variant);
        }
        return sb.toString();
    }

    /**
     * 获取内部存储的 path（规范格式）
     * <p>可直接用于 TextureRegistry 查找</p>
     * 
     * @return 规范化的 path（如 {@code blocks/stone} 或 {@code items/diamond}）
     */
    public String path() {
        return path;
    }

    /**
     * 获取 domain
     */
    public String domain() {
        return domain;
    }

    /**
     * 获取 Type
     */
    public Type type() {
        return type;
    }

    /**
     * 获取 variant（如 {@code &5} 中的 {@code 5}）
     */
    public String variant() {
        return variant;
    }

    /**
     * 获取 TextureCategory
     */
    public TextureCategory textureCategory() {
        return textureCategory;
    }

    /**
     * 判断是否有 variant
     */
    public boolean hasVariant() {
        return variant != null;
    }

    /**
     * 判断是否为 TEXTURE 类型
     */
    public boolean isTexture() {
        return type == Type.TEXTURE;
    }

    /**
     * 判断是否为 MODEL 类型
     */
    public boolean isModel() {
        return type == Type.MODEL;
    }

    /**
     * 获取完整查找键（用于 TextureRegistry 查找）
     * <p>格式：{@code domain:blocks/xxx} 或 {@code domain:items/xxx}</p>
     * 
     * @return 完整查找键
     */
    public String toCanonicalString() {
        StringBuilder sb = new StringBuilder();
        sb.append(domain).append(':');
        sb.append(path);
        if (variant != null) {
            sb.append('&').append(variant);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toCanonicalString();
    }

    private String buildHashCodeString() {
        StringBuilder sb = new StringBuilder();
        sb.append(domain).append(':');
        sb.append(type.name()).append(':');
        sb.append(path);
        if (variant != null) {
            sb.append('&').append(variant);
        }
        return sb.toString();
    }

    private int computeHashCode() {
        return hashCodeString.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CTMKey ctmKey = (CTMKey) o;
        return hashCode == ctmKey.hashCode 
            && domain.equals(ctmKey.domain)
            && type == ctmKey.type
            && path.equals(ctmKey.path)
            && Objects.equals(variant, ctmKey.variant);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public int compareTo(CTMKey other) {
        int d = domain.compareTo(other.domain);
        if (d != 0) return d;
        d = type.compareTo(other.type);
        if (d != 0) return d;
        d = path.compareTo(other.path);
        if (d != 0) return d;
        if (variant == null && other.variant == null) return 0;
        if (variant == null) return -1;
        if (other.variant == null) return 1;
        return variant.compareTo(other.variant);
    }

    private static void cachePut(String cacheKey, CTMKey value) {
        if (CACHE.size() >= MAX_CACHE_SIZE && !CACHE.containsKey(cacheKey)) {
            String eldest = ACCESS_ORDER.removeFirst();
            CACHE.remove(eldest);
        }
        CACHE.put(cacheKey, value);
        ACCESS_ORDER.remove(cacheKey);
        ACCESS_ORDER.addLast(cacheKey);
    }

    private static void updateAccessOrder(String cacheKey) {
        ACCESS_ORDER.remove(cacheKey);
        ACCESS_ORDER.addLast(cacheKey);
    }

    public static void clearCache() {
        synchronized (CACHE) {
            CACHE.clear();
            ACCESS_ORDER.clear();
        }
    }

    public static int cacheSize() {
        synchronized (CACHE) {
            return CACHE.size();
        }
    }

    private static class ParsedResult {
        String domain = "minecraft";
        Type type = Type.UNKNOWN;
        String path = "";
        String variant = null;
        TextureCategory textureCategory = TextureCategory.NONE;
    }
}
