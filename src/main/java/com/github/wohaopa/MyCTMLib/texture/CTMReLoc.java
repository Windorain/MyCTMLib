package com.github.wohaopa.MyCTMLib.texture;

import java.util.Map;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

import com.github.wohaopa.MyCTMLib.mixins.AccessorTextureMap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * CTM 材质重定向表
 * 
 * 建立原版材质 → CTM 材质的映射
 * 例如："minecraft:stone" → "minecraft:stone_ctm" 的 sprite
 * 
 * 构建时机：TextureStitchEvent.Pre
 * 查询性能：~15-25ns（fastutil Object2ObjectOpenHashMap）
 * 
 * @author MyCTMLib
 * @since 1.0.0
 */
@SideOnly(Side.CLIENT)
public class CTMReLoc {

    /**
     * 重定向表：基础名称 → CTMTextureAtlasSprite
     */
    private static final Object2ObjectOpenHashMap<String, CTMTextureAtlasSprite> relocTable = new Object2ObjectOpenHashMap<>();

    /**
     * 构建重定向表
     * 在 TextureStitchEvent.Pre 时调用
     * 
     * @param textureMap 纹理贴图
     */
    static void build(TextureMap textureMap) {
        // 清空旧表（支持资源重载）
        relocTable.clear();

        // 获取所有已注册的纹理（通过 Accessor）
        Map<String, TextureAtlasSprite> sprites = ((AccessorTextureMap) textureMap).getMapRegisteredSprites();

        // 遍历所有纹理
        for (Map.Entry<String, TextureAtlasSprite> entry : sprites.entrySet()) {
            String name = entry.getKey();
            TextureAtlasSprite sprite = entry.getValue();

            // 识别 CTMTextureAtlasSprite（带 _ctm 后缀）
            if (sprite instanceof CTMTextureAtlasSprite ctm) {
                // 提取基础名称（去掉 _ctm 后缀）
                String baseName = stripCtmSuffix(name);

                // 如果基础名称存在，建立重定向
                if (baseName != null && sprites.containsKey(baseName)) {
                    relocTable.put(baseName, ctm);
                }
            }
        }
    }

    /**
     * 去掉 _ctm 后缀
     * 
     * @param name 纹理名称（如 "minecraft:stone_ctm"）
     * @return 基础名称（如 "minecraft:stone"），如果不是 _ctm 后缀则返回 null
     */
    private static String stripCtmSuffix(String name) {
        if (name == null || name.length() <= 4) {
            return null;
        }
        if (name.endsWith("_ctm")) {
            return name.substring(0, name.length() - 4);
        }
        return null;
    }

    /**
     * 查询重定向的 CTM sprite
     * 
     * @param textureName 纹理名称（如 "minecraft:stone"）
     * @return CTMTextureAtlasSprite，如果没有重定向则返回 null
     */
    public static CTMTextureAtlasSprite getSprite(String textureName) {
        return relocTable.get(textureName);
    }

    /**
     * 检查是否有 CTM 重定向
     * 
     * @param textureName 纹理名称
     * @return true 表示有 CTM 重定向
     */
    public static boolean hasCTM(String textureName) {
        return relocTable.containsKey(textureName);
    }
}
