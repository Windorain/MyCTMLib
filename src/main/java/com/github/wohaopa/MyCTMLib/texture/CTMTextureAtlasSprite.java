package com.github.wohaopa.MyCTMLib.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import com.github.wohaopa.MyCTMLib.texture.BaseTextureData.QuadTinting;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData.RenderType;
import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;
import com.google.gson.JsonObject;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Getter;
import lombok.Setter;

/**
 * CTM 材质容器类
 * 
 * 继承 TextureAtlasSprite，复用 IIcon 实现
 * 作为纯容器，存储 CTM 配置字段
 * 
 * @author MyCTMLib
 * @since 1.0.0
 */
@SideOnly(Side.CLIENT)
public class CTMTextureAtlasSprite extends TextureAtlasSprite {

    // ========== CTM 配置字段 ==========

    /**
     * 网格宽度（用于 Random 和 Connecting 纹理）
     */
    @Getter
    @Setter
    private int gridWidth;

    /**
     * 网格高度（用于 Random 和 Connecting 纹理）
     */
    @Getter
    @Setter
    private int gridHeight;

    /**
     * 渲染类型（用于 Base 纹理）
     */
    @Getter
    @Setter
    private RenderType renderType;

    /**
     * 是否发光（用于 Base 纹理）
     */
    @Getter
    @Setter
    private boolean emissive;

    /**
     * 生物群系着色（用于 Base 纹理）
     */
    @Getter
    @Setter
    private QuadTinting tinting;

    /**
     * 连接布局类型（用于 Connecting 纹理）
     */
    @Getter
    @Setter
    private ConnectingLayout layoutStyle;

    /**
     * 随机瓦片数量（用于 Random 纹理）
     */
    @Getter
    @Setter
    private int randomCount;

    /**
     * 随机种子（用于 Random 纹理，0 表示使用世界种子）
     */
    @Getter
    @Setter
    private long randomSeed;

    // ========== 构造函数 ==========

    /**
     * 构造函数
     * 
     * @param iconName 纹理名称（通常带 _ctm 后缀）
     */
    public CTMTextureAtlasSprite(String iconName) {
        super(iconName);
        // 字段默认为 null/0
    }

    // ========== JSON 序列化 ==========

    /**
     * 将 sprite 数据序列化为 JsonObject（用于 dump）
     * 
     * @return JsonObject 包含所有 CTM 配置字段
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("iconName", getIconName());
        json.addProperty("gridWidth", getGridWidth());
        json.addProperty("gridHeight", getGridHeight());

        if (getRenderType() != null) {
            json.addProperty("renderType", getRenderType().getId());
        } else {
            json.add("renderType", null);
        }

        json.addProperty("emissive", isEmissive());

        if (getTinting() != null) {
            json.addProperty("tinting", getTinting().name());
        } else {
            json.add("tinting", null);
        }

        if (getLayoutStyle() != null) {
            json.addProperty("layoutStyle", getLayoutStyle().name());
        } else {
            json.add("layoutStyle", null);
        }

        json.addProperty("randomCount", getRandomCount());
        json.addProperty("randomSeed", getRandomSeed());

        return json;
    }
}
