package com.github.wohaopa.MyCTMLib.render.pipelines;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.domain.BrightnessDomain;
import com.github.wohaopa.MyCTMLib.render.domain.ColorDomain;
import com.github.wohaopa.MyCTMLib.render.domain.GeometryDomain;
import com.github.wohaopa.MyCTMLib.render.domain.PositionDomain;
import com.github.wohaopa.MyCTMLib.render.domain.TileDomain;
import com.github.wohaopa.MyCTMLib.render.domain.UVDomain;
import com.github.wohaopa.MyCTMLib.render.phasegroups.RenderGroup;

/**
 * Connecting 材质渲染管道
 */
public final class ConnectingTilePipeline {

    private ConnectingTilePipeline() {}

    public static void execute(RenderContext ctx) {
        TileDomain.computeConnecting(ctx);
        GeometryDomain.fromElement(ctx);
        UVDomain.calc(ctx);
        PositionDomain.calc(ctx);

        if (ctx.isItemRender()) {
            BrightnessDomain.setFullAO(ctx);
            ColorDomain.computeUniform(ctx);
        } else if (ctx.getRenderBlocks().enableAO) {
            BrightnessDomain.computeAO(ctx);
            ColorDomain.computeAO(ctx);
        } else {
            BrightnessDomain.computeUniform(ctx);
            ColorDomain.computeUniform(ctx);
        }

        RenderGroup.renderFace(ctx);

        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }
}
