package com.github.wohaopa.MyCTMLib.render.quads;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.domain.BrightnessDomain;
import com.github.wohaopa.MyCTMLib.render.domain.ColorDomain;
import com.github.wohaopa.MyCTMLib.render.domain.GeometryDomain;
import com.github.wohaopa.MyCTMLib.render.domain.PositionDomain;
import com.github.wohaopa.MyCTMLib.render.domain.TileDomain;
import com.github.wohaopa.MyCTMLib.render.domain.UVDomain;

/**
 * RenderBlocks quad 渲染器（TextureReloc 分支专用）
 * 
 * <p>
 * 从 RenderBlocks 提取几何数据，适用于 TEXTURE_RELOC 分支。
 * </p>
 */
public final class RenderBlocksQuadRenderer {

    private RenderBlocksQuadRenderer() {}

    /**
     * 渲染 Connecting 材质面
     */
    public static void renderConnecting(RenderContext ctx) {
        assert ctx.getCtmSprite() != null;
        assert ctx.getRenderBlocks() != null;
        assert ctx.getBlockAccess() != null;
        assert ctx.getConnectionPredicate() != null;

        TileDomain.computeConnecting(ctx);
        GeometryDomain.fromRenderBlocks(ctx);
        UVDomain.calc(ctx);
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

        TileDomain.computeBase(ctx);
        GeometryDomain.fromRenderBlocks(ctx);
        UVDomain.calc(ctx);
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

        TileDomain.computeRandom(ctx);
        GeometryDomain.fromRenderBlocks(ctx);
        UVDomain.calc(ctx);
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
