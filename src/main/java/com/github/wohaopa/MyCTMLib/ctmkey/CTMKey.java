package com.github.wohaopa.MyCTMLib.ctmkey;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;

public final class CTMKey implements Comparable<CTMKey>, Serializable {

    public enum Type {
        BLOCK,
        MODEL,
        TEXTURE,
        ITEM,
        UNKNOWN
    }

    public enum TextureCategory {
        BLOCKS,
        ITEMS,
        OTHER,
        NONE
    }

    private static final int MAX_CACHE_SIZE = 10000;

    private static final Object2ObjectLinkedOpenHashMap<String, CTMKey> CACHE =
        new Object2ObjectLinkedOpenHashMap<>();

    private static final LinkedList<String> ACCESS_ORDER = new LinkedList<>();

    static {
        CACHE.defaultReturnValue(null);
    }

    private final String domain;
    private final Type type;
    private final String path;
    private final String variant;
    private final TextureCategory textureCategory;

    private final String canonicalString;
    private final String safeString;
    private final int hashCode;

    private CTMKey(String domain, Type type, String path, String variant,
                   TextureCategory textureCategory) {
        this.domain = domain;
        this.type = type;
        this.path = path;
        this.variant = variant;
        this.textureCategory = textureCategory;

        this.canonicalString = buildCanonicalString();
        this.safeString = canonicalString;
        this.hashCode = computeHashCode();
    }

    public static CTMKey parse(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        synchronized (CACHE) {
            CTMKey existing = CACHE.get(input);
            if (existing != null) {
                updateAccessOrder(input);
                return existing;
            }
        }

        ParsedResult parsed = parseInternal(input);
        if (parsed == null) {
            return null;
        }

        CTMKey key = new CTMKey(
            parsed.domain,
            parsed.type,
            parsed.path,
            parsed.variant,
            parsed.textureCategory
        );

        synchronized (CACHE) {
            cachePut(key.safeString, key);
            if (!input.equals(key.safeString)) {
                cachePut(input, key);
            }
        }

        return key;
    }

    public static CTMKey of(String domain, Type type, String path) {
        return of(domain, type, path, null);
    }

    public static CTMKey of(String domain, Type type, String path, String variant) {
        String safeString = buildSafeString(domain, type, path, variant);

        synchronized (CACHE) {
            CTMKey existing = CACHE.get(safeString);
            if (existing != null) {
                updateAccessOrder(safeString);
                return existing;
            }
        }

        TextureCategory category = inferTextureCategory(type, path);
        CTMKey key = new CTMKey(domain, type, path, variant, category);

        synchronized (CACHE) {
            cachePut(key.safeString, key);
        }

        return key;
    }

    public static CTMKey texture(String domain, String path, TextureCategory category) {
        String safeString = buildSafeString(domain, Type.TEXTURE, path, null);

        synchronized (CACHE) {
            CTMKey existing = CACHE.get(safeString);
            if (existing != null) {
                updateAccessOrder(safeString);
                return existing;
            }
        }

        CTMKey key = new CTMKey(domain, Type.TEXTURE, path, null, category);

        synchronized (CACHE) {
            cachePut(key.safeString, key);
        }

        return key;
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

    private static ParsedResult parseInternal(String input) {
        String cleaned = input.replace('\\', '/').trim();
        if (cleaned.isEmpty()) {
            return null;
        }

        ParsedResult result = new ParsedResult();

        int colonIdx = cleaned.indexOf(':');
        if (colonIdx >= 0) {
            result.domain = cleaned.substring(0, colonIdx).toLowerCase(Locale.ROOT);
            String remaining = cleaned.substring(colonIdx + 1);
            parsePathAndType(remaining, result);
        } else {
            result.domain = "minecraft";
            parsePathAndType(cleaned, result);
        }

        if (result.type == Type.UNKNOWN) {
            result.type = Type.BLOCK;
        }

        return result;
    }

    private static void parsePathAndType(String remaining, ParsedResult result) {
        int ampIdx = remaining.indexOf('&');
        String pathPart;
        if (ampIdx >= 0) {
            pathPart = remaining.substring(0, ampIdx);
            result.variant = remaining.substring(ampIdx + 1);
        } else {
            pathPart = remaining;
            result.variant = null;
        }

        if (pathPart.startsWith("models/block/")) {
            result.type = Type.MODEL;
            result.path = pathPart.substring("models/block/".length());
            result.textureCategory = TextureCategory.NONE;
        } else if (pathPart.startsWith("models/item/")) {
            result.type = Type.MODEL;
            result.path = pathPart.substring("models/item/".length());
            result.textureCategory = TextureCategory.NONE;
        } else if (pathPart.startsWith("blocks/")) {
            result.type = Type.TEXTURE;
            result.path = pathPart.substring("blocks/".length());
            result.textureCategory = TextureCategory.BLOCKS;
        } else if (pathPart.startsWith("items/")) {
            result.type = Type.TEXTURE;
            result.path = pathPart.substring("items/".length());
            result.textureCategory = TextureCategory.ITEMS;
        } else if (pathPart.startsWith("block/")) {
            result.type = Type.MODEL;
            result.path = pathPart.substring("block/".length());
            result.textureCategory = TextureCategory.NONE;
        } else if (pathPart.startsWith("item/")) {
            result.type = Type.MODEL;
            result.path = pathPart.substring("item/".length());
            result.textureCategory = TextureCategory.NONE;
        } else {
            result.type = Type.UNKNOWN;
            result.path = pathPart;
            result.textureCategory = TextureCategory.NONE;
        }
    }

    private static String buildSafeString(String domain, Type type, String path, String variant) {
        StringBuilder sb = new StringBuilder();
        sb.append(domain).append(':');

        switch (type) {
            case BLOCK:
                sb.append(path);
                break;
            case MODEL:
                sb.append("models/block/").append(path);
                break;
            case TEXTURE:
                TextureCategory cat = inferTextureCategory(type, path);
                if (cat == TextureCategory.BLOCKS) {
                    sb.append("blocks/");
                } else if (cat == TextureCategory.ITEMS) {
                    sb.append("items/");
                }
                sb.append(path);
                break;
            case ITEM:
                sb.append(path);
                break;
            default:
                sb.append(path);
        }

        if (variant != null) {
            sb.append('&').append(variant);
        }

        return sb.toString();
    }

    private static TextureCategory inferTextureCategory(Type type, String path) {
        if (type != Type.TEXTURE) {
            return TextureCategory.NONE;
        }
        if (path.startsWith("blocks/")) {
            return TextureCategory.BLOCKS;
        } else if (path.startsWith("items/")) {
            return TextureCategory.ITEMS;
        }
        return TextureCategory.BLOCKS;
    }

    private String buildCanonicalString() {
        StringBuilder sb = new StringBuilder();
        sb.append(domain).append(':');

        switch (type) {
            case BLOCK:
                sb.append(path);
                break;
            case MODEL:
                sb.append("models/block/").append(path);
                break;
            case TEXTURE:
                if (textureCategory == TextureCategory.BLOCKS) {
                    sb.append("blocks/");
                } else if (textureCategory == TextureCategory.ITEMS) {
                    sb.append("items/");
                }
                sb.append(path);
                break;
            case ITEM:
                sb.append(path);
                break;
            default:
                sb.append(path);
        }

        if (variant != null) {
            sb.append('&').append(variant);
        }

        return sb.toString();
    }

    private int computeHashCode() {
        int result = domain.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + (variant != null ? variant.hashCode() : 0);
        return result;
    }

    public String domain() {
        return domain;
    }

    public Type type() {
        return type;
    }

    public String path() {
        return path;
    }

    public String variant() {
        return variant;
    }

    public TextureCategory textureCategory() {
        return textureCategory;
    }

    public boolean hasVariant() {
        return variant != null;
    }

    public boolean isBlock() {
        return type == Type.BLOCK;
    }

    public boolean isModel() {
        return type == Type.MODEL;
    }

    public boolean isTexture() {
        return type == Type.TEXTURE;
    }

    public boolean isItem() {
        return type == Type.ITEM;
    }

    public String toCanonicalString() {
        return canonicalString;
    }

    @Deprecated
    public String toLegacyCanonicalKey() {
        return canonicalString;
    }

    @Deprecated
    public static CTMKey fromLegacyCanonicalKey(String legacyKey) {
        return parse(legacyKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CTMKey ctmKey = (CTMKey) o;
        return hashCode == ctmKey.hashCode &&
            domain.equals(ctmKey.domain) &&
            type == ctmKey.type &&
            path.equals(ctmKey.path) &&
            Objects.equals(variant, ctmKey.variant);
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

    @Override
    public String toString() {
        return canonicalString;
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
        String domain;
        Type type;
        String path;
        String variant;
        TextureCategory textureCategory;
    }
}
