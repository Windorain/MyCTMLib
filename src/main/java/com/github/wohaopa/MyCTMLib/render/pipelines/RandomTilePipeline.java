package com.github.wohaopa.MyCTMLib.render.pipelines;

import com.github.wohaopa.MyCTMLib.render.domain.BrightnessDomain;
import com.github.wohaopa.MyCTMLib.render.domain.ColorDomain;
import com.github.wohaopa.MyCTMLib.render.domain.GeometryDomain;
import com.github.wohaopa.MyCTMLib.render.domain.IconDomain;
import com.github.wohaopa.MyCTMLib.render.domain.PositionDomain;
import com.github.wohaopa.MyCTMLib.render.domain.TileDomain;
import com.github.wohaopa.MyCTMLib.render.domain.UVDomain;
import com.github.wohaopa.MyCTMLib.render.phasegroups.RenderGroup;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * Random 材质渲染管道
 */
public final class RandomTilePipeline {

    private RandomTilePipeline() {
    }

    public static void execute(RenderContext ctx) {
        IconDomain.resolve(ctx);
        TileDomain.computeRandom(ctx);
        GeometryDomain.fromElement(ctx);
        UVDomain.calc(ctx);
        PositionDomain.calc(ctx);
        BrightnessDomain.compute(ctx);
        ColorDomain.computeAO(ctx);
        RenderGroup.renderFace(ctx);
        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }
}
