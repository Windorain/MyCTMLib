package com.github.wohaopa.MyCTMLib.texture;

/**
 * type=base 的 mcmeta 解析结果。支持 render_type、emissive、QuadTinting。
 */
public class BaseTextureData implements TextureTypeData {

    /**
     * 生物群系着色类型：草地色、树叶色、水色。
     */
    public enum QuadTinting {
        BIOME_GRASS,
        BIOME_FOLIAGE,
        BIOME_WATER
    }

    /**
     * 渲染类型：不透明、镂空、半透明。
     */
    public enum RenderType {

        OPAQUE("opaque"),
        CUTOUT("cutout"),
        TRANSLUCENT("translucent");

        private final String id;

        RenderType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static RenderType fromString(String s) {
            if (s == null) return null;
            String lower = s.toLowerCase();
            for (RenderType rt : values()) {
                if (rt.id.equals(lower)) return rt;
            }
            return null;
        }
    }

    private final RenderType renderType;
    private final boolean emissive;
    private final QuadTinting tinting;

    private BaseTextureData(RenderType renderType, boolean emissive, QuadTinting tinting) {
        this.renderType = renderType != null ? renderType : RenderType.OPAQUE;
        this.emissive = emissive;
        this.tinting = tinting;
    }

    @Override
    public String getType() {
        return "base";
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public boolean isEmissive() {
        return emissive;
    }

    public QuadTinting getTinting() {
        return tinting;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private RenderType renderType = RenderType.OPAQUE;
        private boolean emissive = false;
        private QuadTinting tinting = null;

        public Builder renderType(RenderType renderType) {
            this.renderType = renderType;
            return this;
        }

        public Builder emissive(boolean emissive) {
            this.emissive = emissive;
            return this;
        }

        public Builder tinting(QuadTinting tinting) {
            this.tinting = tinting;
            return this;
        }

        public BaseTextureData build() {
            return new BaseTextureData(renderType, emissive, tinting);
        }
    }
}
