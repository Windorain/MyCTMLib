package com.github.wohaopa.MyCTMLib.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javax.imageio.ImageIO;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import com.github.wohaopa.MyCTMLib.ctmkey.CTMKey;
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
     * 关联的 CTMKey（用于 load() 时拼接资源路径）
     */
    @Getter
    @Setter
    private CTMKey key;

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
     * 构造函数（推荐使用）
     * 
     * @param key CTMKey（包含完整的路径和类别信息）
     */
    public CTMTextureAtlasSprite(CTMKey key) {
        super(key.to(CTMKey.Format.TEXTURE_KEY));
        this.key = key;
    }

    /**
     * 构造函数（兼容旧代码）
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

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return getLayoutStyle() != null || getRandomCount() > 0;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        resetSprite();
        try {
            ResourceLocation fullLocation;
            if (this.key != null) {
                // 使用 CTMKey 字段手动构造正确的路径
                String purePath = this.key.path();
                if (purePath.startsWith("blocks/")) {
                    purePath = purePath.substring("blocks/".length());
                } else if (purePath.startsWith("items/")) {
                    purePath = purePath.substring("items/".length());
                }

                String resourcePath;
                if (this.key.textureCategory() == CTMKey.TextureCategory.ITEMS) {
                    resourcePath = this.key.domain() + ":textures/items/" + purePath + ".png";
                } else {
                    resourcePath = this.key.domain() + ":textures/blocks/" + purePath + ".png";
                }
                fullLocation = new ResourceLocation(resourcePath);
            } else {
                String path = location.getResourcePath();
                String resourcePath = "textures/blocks/" + path + ".png";
                fullLocation = new ResourceLocation(location.getResourceDomain(), resourcePath);
            }

            IResource resource = manager.getResource(fullLocation);
            try (InputStream in = resource.getInputStream()) {
                BufferedImage img = ImageIO.read(in);
                if (img == null) return true;
                int w = img.getWidth();
                int h = img.getHeight();
                setIconWidth(w);
                setIconHeight(h);
                int[] pixels = new int[w * h];
                img.getRGB(0, 0, w, h, pixels, 0, w);
                int[][] oneFrame = new int[][] { pixels };
                java.util.List<int[][]> frameList = Collections.singletonList(oneFrame);
                setFramesTextureData(frameList);
                return false;
            }
        } catch (IOException e) {
            return true;
        }
    }
}
