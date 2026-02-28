package com.github.wohaopa.MyCTMLib.render.util;

import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.ctmkey.CTMKey;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;
import com.github.wohaopa.MyCTMLib.texture.CTMReLoc;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;

/**
 * 材质数据查询工具类
 * 
 * 职责：查询材质相关数据（中间参数）
 * - findTextureReloc: 查找 CTM 重定向纹理
 * - resolveForElement: 解析 Model 元素的材质
 * 
 * @author MyCTMLib
 * @since 1.0.0
 */
public final class TextureUtil {

    private TextureUtil() {}

    /**
     * 查找 CTM 重定向纹理
     * 
     * @param originalIcon 原始 IIcon
     * @return CTMTextureAtlasSprite，无重定向返回 null
     */
    public static CTMTextureAtlasSprite findTextureReloc(IIcon originalIcon) {
        if (originalIcon == null) return null;
        return CTMReLoc.getSprite(originalIcon.getIconName());
    }

    /**
     * 解析 Model 元素的材质
     * 
     * @param element   Model 元素
     * @param face      面方向
     * @param modelData Model 数据
     * @return CTMTextureAtlasSprite，解析失败返回 null
     */
    public static CTMTextureAtlasSprite resolveForElement(ModelElement element, ForgeDirection face,
        ModelData modelData) {
        if (element == null || modelData == null) return null;

        ModelFace faceData = element.getFace(face);
        if (faceData == null || faceData.getTextureKey() == null) return null;

        // Step 1: 解析 texturePath（处理 # 引用）
        String texturePath = TextureKeyNormalizer.resolveTexturePath(faceData.getTextureKey(), modelData.getTextures());
        if (texturePath == null) return null;

        // Step 2: 转为 canonical key
        String modelId = getModelIdFromModelData(modelData);
        String domain = extractDomain(modelId);
        CTMKey key = CTMKey.from(CTMKey.Format.TEXTURE_KEY, domain + ":" + texturePath, CTMKey.TextureCategory.BLOCKS);
        String textureKey = key != null ? key.toCanonicalString() : null;
        if (textureKey == null) return null;

        // Step 3: 查找 CTM sprite
        return CTMReLoc.getSprite(textureKey);
    }

    private static String getModelIdFromModelData(ModelData modelData) {
        // 从 ModelData 反推 modelId（用于提取 domain）
        // 这里需要根据实际情况调整
        return "minecraft";
    }

    private static String extractDomain(String modelId) {
        if (modelId == null || modelId.indexOf(':') < 0) {
            return "minecraft";
        }
        return modelId.substring(0, modelId.indexOf(':'));
    }
}
