package com.github.wohaopa.MyCTMLib.render.domain;

import java.util.Map;

import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

/**
 * 材质数据域（合并原 IconDomain 职责）
 * 
 * 职责：
 * - resolveForElement: 解析材质数据（per-element）
 *   - 解析 textureKey（处理 # 引用）
 *   - 查询 textureData
 *   - 查询 icon + 提取 UV
 *   - 计算 grid 尺寸
 */
public final class TextureDomain {

    private TextureDomain() {}

    /**
     * 解析材质数据（per-element）
     * @return true 如果解析成功
     */
    public static boolean resolveForElement(RenderContext ctx) {
        ModelElement element = ctx.getCurrentElement();
        ModelFace face = element.getFace(ctx.getFace());
        
        if (face == null) return false;
        if (face.getTextureKey() == null) return false;
        
        // Step 1: 解析 textureKey（处理 # 引用）
        String texturePath = resolveTexturePath(face.getTextureKey(), ctx.getModelData().getTextures());
        if (texturePath == null) return false;
        
        // Step 2: 转为 canonical key
        String domain = extractDomain(ctx.getModelId());
        String textureKey = TextureKeyNormalizer.toCanonicalTextureKey(domain, texturePath);
        if (textureKey == null) return false;
        
        ctx.setTextureKey(textureKey);
        
        // Step 3: 查询 textureData
        TextureTypeData textureData = CTMRenderEntry.getConnectingData(textureKey);
        if (textureData == null) return false;
        
        ctx.setTextureData(textureData);
        
        // Step 4: 查询 icon
        net.minecraft.util.IIcon icon = TextureRegistry.getInstance().getIcon(textureKey);
        if (icon == null) icon = ctx.getOriginalIcon();
        
        ctx.setDrawIcon(icon);
        
        // Step 5: 设置 icon UV
        ctx.setIconMinU(icon.getMinU());
        ctx.setIconMaxU(icon.getMaxU());
        ctx.setIconMinV(icon.getMinV());
        ctx.setIconMaxV(icon.getMaxV());
        
        // Step 6: 计算 grid
        int[] gridSize = calculateGridSize(textureData);
        ctx.setGridW(gridSize[0]);
        ctx.setGridH(gridSize[1]);
        
        return true;
    }

    private static String resolveTexturePath(String textureKey, Map<String, String> textures) {
        if (textureKey == null || textures == null) return null;
        return TextureKeyNormalizer.resolveTexturePath(textureKey, textures);
    }

    private static int[] calculateGridSize(TextureTypeData data) {
        if (data instanceof RandomTextureData rtd) {
            return new int[] { rtd.getColumns(), rtd.getRows() };
        }
        if (data instanceof ConnectingTextureData ctd) {
            LayoutHandler handler = LayoutHandlers.get(ctd.getLayout());
            return new int[] { handler.getWidth(), handler.getHeight() };
        }
        return new int[] { 1, 1 };
    }

    private static String extractDomain(String modelId) {
        if (modelId == null || modelId.indexOf(':') < 0) {
            return "minecraft";
        }
        return modelId.substring(0, modelId.indexOf(':'));
    }
}
