package com.github.wohaopa.MyCTMLib.render.quads;

import com.github.wohaopa.MyCTMLib.model.baked.BakedQuad;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.domain.BrightnessDomain;
import com.github.wohaopa.MyCTMLib.render.domain.ColorDomain;
import com.github.wohaopa.MyCTMLib.render.domain.PositionDomain;
import com.github.wohaopa.MyCTMLib.render.domain.TileDomain;
import com.github.wohaopa.MyCTMLib.render.domain.UVDomain;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;

public final class BakedQuadRenderer {

    private BakedQuadRenderer() {}

    public static void renderConnecting(BakedQuad quad, RenderContext ctx) {
        assert quad != null;
        assert quad.getSprite() != null;
        assert ctx.getRenderBlocks() != null;
        assert ctx.getBlockAccess() != null;

        CTMTextureAtlasSprite sprite = quad.getSprite();

        ctx.debug("  enableAO=" + ctx.isEnableAO());
        ctx.debug("  Sprite: " + sprite.getIconName());

        ctx.setCtmSprite(sprite);
        ctx.setConnectionPredicate(quad.getPredicate());

        ctx.setDrawRelMinX(quad.getRelMinX());
        ctx.setDrawRelMaxX(quad.getRelMaxX());
        ctx.setDrawRelMinY(quad.getRelMinY());
        ctx.setDrawRelMaxY(quad.getRelMaxY());
        ctx.setDrawRelMinZ(quad.getRelMinZ());
        ctx.setDrawRelMaxZ(quad.getRelMaxZ());

        ctx.setIconMinU(sprite.getMinU());
        ctx.setIconMaxU(sprite.getMaxU());
        ctx.setIconMinV(sprite.getMinV());
        ctx.setIconMaxV(sprite.getMaxV());

        TileDomain.computeConnecting(ctx);
        UVDomain.calc(ctx);
        PositionDomain.calc(ctx);

        if (ctx.isEnableAO()) {
            ctx.trace("  Calling BrightnessDomain.computeAO()");
            BrightnessDomain.computeAO(ctx);
            ctx.trace("  Calling ColorDomain.computeAO()");
            ColorDomain.computeAO(ctx);
        } else {
            ctx.trace("  Calling computeUniform() (AO disabled)");
            BrightnessDomain.computeUniform(ctx);
            ColorDomain.computeUniform(ctx);
        }

        QuadRender.drawFace(ctx);

        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }

    public static void renderBase(BakedQuad quad, RenderContext ctx) {
        assert quad != null;
        assert quad.getSprite() != null;
        assert ctx.getRenderBlocks() != null;

        CTMTextureAtlasSprite sprite = quad.getSprite();

        ctx.debug("  enableAO=" + ctx.isEnableAO());
        ctx.debug("  Sprite: " + sprite.getIconName());

        ctx.setCtmSprite(sprite);

        ctx.setDrawRelMinX(quad.getRelMinX());
        ctx.setDrawRelMaxX(quad.getRelMaxX());
        ctx.setDrawRelMinY(quad.getRelMinY());
        ctx.setDrawRelMaxY(quad.getRelMaxY());
        ctx.setDrawRelMinZ(quad.getRelMinZ());
        ctx.setDrawRelMaxZ(quad.getRelMaxZ());

        ctx.setIconMinU(sprite.getMinU());
        ctx.setIconMaxU(sprite.getMaxU());
        ctx.setIconMinV(sprite.getMinV());
        ctx.setIconMaxV(sprite.getMaxV());

        TileDomain.computeBase(ctx);
        UVDomain.calc(ctx);
        PositionDomain.calc(ctx);

        if (ctx.isEnableAO()) {
            ctx.trace("  Calling BrightnessDomain.setFullAO()");
            BrightnessDomain.setFullAO(ctx);
            ctx.trace("  Calling ColorDomain.computeAO()");
            ColorDomain.computeAO(ctx);
        } else {
            ctx.trace("  Calling setFullUniform() (AO disabled)");
            BrightnessDomain.setFullUniform(ctx);
            ColorDomain.computeUniform(ctx);
        }

        QuadRender.drawFace(ctx);

        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }

    public static void renderRandom(BakedQuad quad, RenderContext ctx) {
        assert quad != null;
        assert quad.getSprite() != null;
        assert ctx.getRenderBlocks() != null;
        assert ctx.getBlockAccess() != null;

        CTMTextureAtlasSprite sprite = quad.getSprite();

        ctx.debug("  enableAO=" + ctx.isEnableAO());
        ctx.debug("  Sprite: " + sprite.getIconName());

        ctx.setCtmSprite(sprite);

        ctx.setDrawRelMinX(quad.getRelMinX());
        ctx.setDrawRelMaxX(quad.getRelMaxX());
        ctx.setDrawRelMinY(quad.getRelMinY());
        ctx.setDrawRelMaxY(quad.getRelMaxY());
        ctx.setDrawRelMinZ(quad.getRelMinZ());
        ctx.setDrawRelMaxZ(quad.getRelMaxZ());

        ctx.setIconMinU(sprite.getMinU());
        ctx.setIconMaxU(sprite.getMaxU());
        ctx.setIconMinV(sprite.getMinV());
        ctx.setIconMaxV(sprite.getMaxV());

        TileDomain.computeRandom(ctx);
        UVDomain.calc(ctx);
        PositionDomain.calc(ctx);

        if (ctx.isEnableAO()) {
            ctx.trace("  Calling BrightnessDomain.computeAO()");
            BrightnessDomain.computeAO(ctx);
            ctx.trace("  Calling ColorDomain.computeAO()");
            ColorDomain.computeAO(ctx);
        } else {
            ctx.trace("  Calling computeUniform() (AO disabled)");
            BrightnessDomain.computeUniform(ctx);
            ColorDomain.computeUniform(ctx);
        }

        QuadRender.drawFace(ctx);

        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }
}
