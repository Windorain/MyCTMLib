package com.github.wohaopa.MyCTMLib.render.quads;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.domain.BrightnessDomain;
import com.github.wohaopa.MyCTMLib.render.domain.ColorDomain;
import com.github.wohaopa.MyCTMLib.render.domain.GeometryDomain;
import com.github.wohaopa.MyCTMLib.render.domain.PositionDomain;
import com.github.wohaopa.MyCTMLib.render.domain.TileDomain;
import com.github.wohaopa.MyCTMLib.render.domain.UVDomain;

/**
 * Item quad 渲染器（Item 分支专用）
 * 
 * <p>
 * 从 RenderBlocks 提取几何数据，适用于 ITEM 分支（物品渲染）。
 * </p>
 */
public final class ItemQuadRenderer {

    private ItemQuadRenderer() {}

    /**
     * 渲染 Base 材质面（物品渲染）
     */
    public static void renderBase(RenderContext ctx) {
        assert ctx.getRenderBlocks() != null;
        assert ctx.getCtmSprite() != null;

        TileDomain.computeBase(ctx);
        GeometryDomain.fromRenderBlocks(ctx);
        UVDomain.calc(ctx);
        PositionDomain.calc(ctx);

        BrightnessDomain.setFullUniform(ctx);
        ColorDomain.computeUniform(ctx);

        QuadRender.drawFace(ctx);

        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }
}
