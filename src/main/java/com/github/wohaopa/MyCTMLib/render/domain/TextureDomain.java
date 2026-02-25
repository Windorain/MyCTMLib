package com.github.wohaopa.MyCTMLib.render.domain;

import java.util.Map;

import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
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
 * - resolveForElement: 解析材质数据（per-element，用于 MODEL_ELEMENTS 分支）
 * - findTextureReloc: 查找重定向纹理（用于 TEXTURE_RELOC 分支）
 */
public final class TextureDomain {

    private TextureDomain() {}

    /**
     * 解析材质数据（per-element）
     * 
     * @return true 如果解析成功
     */
    public static boolean resolveForElement(RenderContext ctx) {
        ModelElement element = ctx.getCurrentElement();
        ModelFace face = element.getFace(ctx.getFace());

        if (face == null) return false;
        if (face.getTextureKey() == null) return false;

        // Step 1: 解析 textureKey（处理 # 引用）
        String texturePath = resolveTexturePath(
            face.getTextureKey(),
            ctx.getModelData()
                .getTextures());
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
        net.minecraft.util.IIcon icon = TextureRegistry.getInstance()
            .getIcon(textureKey);
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

    /**
     * 查找重定向纹理（恒等 + _ctm 后缀）
     * 
     * @return true 如果找到重定向纹理
     */
    public static boolean findTextureReloc(RenderContext ctx) {
        String originalIconName = ctx.getOriginalIcon()
            .getIconName();
        String normalizedIconName = TextureKeyNormalizer.normalizeIconName(originalIconName);

        TextureTypeData data = CTMRenderEntry.getConnectingData(normalizedIconName);

        if (data == null) {
            String relocKey = normalizedIconName + "_ctm";
            data = CTMRenderEntry.getConnectingData(relocKey);
            if (data != null) {
                ctx.setTextureKey(relocKey);
            } else {
                return false;
            }
        } else {
            ctx.setTextureKey(normalizedIconName);
        }

        ctx.setTextureData(data);

        net.minecraft.util.IIcon icon = TextureRegistry.getInstance()
            .getIcon(normalizedIconName);
        if (icon == null) icon = ctx.getOriginalIcon();
        ctx.setDrawIcon(icon);
        ctx.setIconMinU(icon.getMinU());
        ctx.setIconMaxU(icon.getMaxU());
        ctx.setIconMinV(icon.getMinV());
        ctx.setIconMaxV(icon.getMaxV());

        int[] gridSize = calculateGridSize(data);
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
