package com.github.wohaopa.MyCTMLib.render.domain;

import com.github.wohaopa.MyCTMLib.ctmkey.CTMKey;
import com.github.wohaopa.MyCTMLib.ctmkey.CTMKeyUtil;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.util.TextureUtil;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;

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

        String texturePath = CTMKeyUtil.resolveTextureRef(
            faceData.getTextureKey(),
            ctx.getModelData()
                .getTextures());
        if (texturePath == null) {
            ctx.warn("TEXTURE: Failed to resolve texturePath");
            return false;
        }

        String modelId = getModelIdFromModelData(ctx.getModelData());
        String domain = extractDomain(modelId);
        CTMKey key = CTMKey.from(CTMKey.Format.TEXTURE_KEY, domain + ":" + texturePath, CTMKey.TextureCategory.BLOCKS);
        if (key == null) {
            ctx.warn("TEXTURE: Failed to create textureKey");
            return false;
        }

        CTMTextureAtlasSprite ctmSprite = TextureRegistry.getSprite(key);
        if (ctmSprite == null) {
            ctx.warn("TEXTURE: No CTM sprite found for key: " + key);
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
