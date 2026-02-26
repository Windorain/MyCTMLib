package com.github.wohaopa.MyCTMLib.render.quads;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.domain.BrightnessDomain;
import com.github.wohaopa.MyCTMLib.render.domain.ColorDomain;
import com.github.wohaopa.MyCTMLib.render.domain.GeometryDomain;
import com.github.wohaopa.MyCTMLib.render.domain.PositionDomain;
import com.github.wohaopa.MyCTMLib.render.domain.UVDomain;
import com.github.wohaopa.MyCTMLib.render.util.TileUtil;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;

/**
 * Element quad 渲染器（Model 分支专用）
 * 
 * <p>
 * 从 ModelElement 提取几何数据，适用于 MODEL_ELEMENTS 分支。
 * </p>
 */
public final class ElementQuadRenderer {

    private ElementQuadRenderer() {}

    /**
     * 渲染 Connecting 材质面
     */
    public static void renderConnecting(RenderContext ctx) {
        assert ctx.getCtmSprite() != null;
        assert ctx.getRenderBlocks() != null;
        assert ctx.getBlockAccess() != null;
        assert ctx.getConnectionPredicate() != null;
        
        CTMTextureAtlasSprite ctmSprite = ctx.getCtmSprite();
        int[] tilePos = TileUtil.computeConnecting(
            ctx.getBlockAccess(),
            ctx.getBlockX(), ctx.getBlockY(), ctx.getBlockZ(),
            ctx.getFace(),
            ctx.getBlock(),
            ctx.getMeta(),
            ctx.getConnectionPredicate(),
            ctmSprite.getLayoutStyle());

        GeometryDomain.fromElement(ctx);
        UVDomain.calc(ctx, tilePos[0], tilePos[1]);
        PositionDomain.calc(ctx);

        if (ctx.getRenderBlocks().enableAO) {
            BrightnessDomain.computeAO(ctx);
            ColorDomain.computeAO(ctx);
        } else {
            BrightnessDomain.computeUniform(ctx);
            ColorDomain.computeUniform(ctx);
        }

        QuadRender.drawFace(ctx);

        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }

    /**
     * 渲染 Base 材质面
     */
    public static void renderBase(RenderContext ctx) {
        assert ctx.getRenderBlocks() != null;
        
        GeometryDomain.fromElement(ctx);
        UVDomain.calc(ctx, 0, 0);
        PositionDomain.calc(ctx);

        if (ctx.getRenderBlocks().enableAO) {
            BrightnessDomain.setFullAO(ctx);
            ColorDomain.computeAO(ctx);
        } else {
            BrightnessDomain.setFullUniform(ctx);
            ColorDomain.computeUniform(ctx);
        }

        QuadRender.drawFace(ctx);

        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }

    /**
     * 渲染 Random 材质面
     */
    public static void renderRandom(RenderContext ctx) {
        assert ctx.getCtmSprite() != null;
        assert ctx.getRenderBlocks() != null;
        assert ctx.getBlockAccess() != null;
        
        CTMTextureAtlasSprite ctmSprite = ctx.getCtmSprite();
        int[] tilePos = TileUtil.computeRandom(
            ctx.getBlockAccess(),
            ctx.getBlockX(), ctx.getBlockY(), ctx.getBlockZ(),
            ctmSprite.getRandomCount(),
            ctmSprite.getGridWidth(),
            ctmSprite.getGridHeight());

        GeometryDomain.fromElement(ctx);
        UVDomain.calc(ctx, tilePos[0], tilePos[1]);
        PositionDomain.calc(ctx);

        if (ctx.getRenderBlocks().enableAO) {
            BrightnessDomain.computeAO(ctx);
            ColorDomain.computeAO(ctx);
        } else {
            BrightnessDomain.computeUniform(ctx);
            ColorDomain.computeUniform(ctx);
        }

        QuadRender.drawFace(ctx);

        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }
}
