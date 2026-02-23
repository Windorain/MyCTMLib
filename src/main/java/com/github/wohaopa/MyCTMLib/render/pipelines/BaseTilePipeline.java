package com.github.wohaopa.MyCTMLib.render.pipelines;

import com.github.wohaopa.MyCTMLib.render.phasegroups.IconResolveGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.RenderGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.ShadingGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.TilePositionGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.UVCalcGroup;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * Base 材质渲染管道
 */
public final class BaseTilePipeline {

    private BaseTilePipeline() {
        // 工具类，禁止实例化
    }

    /**
     * 执行 Base 材质渲染
     */
    public static void execute(RenderContext ctx) {
        // Group 1: Tile 位置（默认）
        TilePositionGroup.setDefaultTile(ctx);

        // Group 2: UV
        UVCalcGroup.calcUV(ctx);
        if (ctx.isPipelineFailed()) return;

        // Group 3: Icon
        IconResolveGroup.resolveIcon(ctx);
        if (ctx.isPipelineFailed()) return;

        // Group 4: 着色（全亮）
        ShadingGroup.setFullBrightness(ctx);

        // Group 5: 渲染
        RenderGroup.renderFace(ctx);
        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }
}
