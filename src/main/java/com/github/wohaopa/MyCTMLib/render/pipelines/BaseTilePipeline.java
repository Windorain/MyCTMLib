package com.github.wohaopa.MyCTMLib.render.pipelines;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.domain.BrightnessDomain;
import com.github.wohaopa.MyCTMLib.render.domain.ColorDomain;
import com.github.wohaopa.MyCTMLib.render.domain.GeometryDomain;
import com.github.wohaopa.MyCTMLib.render.domain.IconDomain;
import com.github.wohaopa.MyCTMLib.render.domain.PositionDomain;
import com.github.wohaopa.MyCTMLib.render.domain.TileDomain;
import com.github.wohaopa.MyCTMLib.render.domain.UVDomain;
import com.github.wohaopa.MyCTMLib.render.phasegroups.RenderGroup;

/**
 * Base 材质渲染管道
 */
public final class BaseTilePipeline {

    private BaseTilePipeline() {}

    public static void execute(RenderContext ctx) {
        IconDomain.resolve(ctx);
        TileDomain.computeBase(ctx);
        GeometryDomain.fromElement(ctx);
        UVDomain.calc(ctx);
        PositionDomain.calc(ctx);
        if (ctx.getRenderBlocks().enableAO) {
            BrightnessDomain.setFullAO(ctx);
            ColorDomain.computeAO(ctx);
        } else {
            BrightnessDomain.setFullUniform(ctx);
            ColorDomain.computeUniform(ctx);
        }
        RenderGroup.renderFace(ctx);
        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }
}
