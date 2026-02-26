package com.github.wohaopa.MyCTMLib.render.domain;

import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.util.TextureUtil;
import com.github.wohaopa.MyCTMLib.texture.CTMReLoc;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;

public final class TextureDomain {

    private TextureDomain() {}

    public static boolean resolveReloc(RenderContext ctx) {
        CTMTextureAtlasSprite ctmSprite = TextureUtil.findTextureReloc(ctx.getOriginalIcon());
        if (ctmSprite == null) {
            ctx.warn(
                "TEXTURE: No CTM sprite found for icon: " + (ctx.getOriginalIcon() != null ? ctx.getOriginalIcon()
                    .getIconName() : "null"));
            return false;
        }

        ctx.setCtmSprite(ctmSprite);
        ctx.setIconMinU(ctmSprite.getMinU());
        ctx.setIconMaxU(ctmSprite.getMaxU());
        ctx.setIconMinV(ctmSprite.getMinV());
        ctx.setIconMaxV(ctmSprite.getMaxV());

        return true;
    }

    public static boolean resolveForElement(RenderContext ctx) {
        if (ctx.getCurrentElement() == null || ctx.getModelData() == null) {
            ctx.warn("TEXTURE: element or modelData is null");
            return false;
        }

        ModelFace faceData = ctx.getCurrentElement()
            .getFace(ctx.getFace());
        if (faceData == null || faceData.getTextureKey() == null) {
            ctx.warn("TEXTURE: faceData or textureKey is null");
            return false;
        }

        String texturePath = TextureKeyNormalizer.resolveTexturePath(
            faceData.getTextureKey(),
            ctx.getModelData()
                .getTextures());
        if (texturePath == null) {
            ctx.warn("TEXTURE: Failed to resolve texturePath");
            return false;
        }

        String modelId = getModelIdFromModelData(ctx.getModelData());
        String domain = extractDomain(modelId);
        String textureKey = TextureKeyNormalizer.toCanonicalTextureKey(domain, texturePath);
        if (textureKey == null) {
            ctx.warn("TEXTURE: Failed to create textureKey");
            return false;
        }

        CTMTextureAtlasSprite ctmSprite = CTMReLoc.getSprite(textureKey);
        if (ctmSprite == null) {
            ctx.warn("TEXTURE: No CTM sprite found for key: " + textureKey);
            return false;
        }

        ctx.setCtmSprite(ctmSprite);
        ctx.setIconMinU(ctmSprite.getMinU());
        ctx.setIconMaxU(ctmSprite.getMaxU());
        ctx.setIconMinV(ctmSprite.getMinV());
        ctx.setIconMaxV(ctmSprite.getMaxV());

        return true;
    }

    private static String getModelIdFromModelData(com.github.wohaopa.MyCTMLib.model.ModelData modelData) {
        return "minecraft";
    }

    private static String extractDomain(String modelId) {
        if (modelId == null || modelId.indexOf(':') < 0) {
            return "minecraft";
        }
        return modelId.substring(0, modelId.indexOf(':'));
    }
}
