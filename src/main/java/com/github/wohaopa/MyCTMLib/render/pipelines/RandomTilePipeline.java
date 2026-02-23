package com.github.wohaopa.MyCTMLib.render.pipelines;

import com.github.wohaopa.MyCTMLib.render.phasegroups.GeometryGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.IconResolveGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.RenderGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.ShadingGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.TilePositionGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.UVCalcGroup;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * Random 材质渲染管道
 */
public final class RandomTilePipeline {

    private RandomTilePipeline() {
        // 工具类，禁止实例化
    }

    /**
     * 执行 Random 材质渲染
     */
    public static void execute(RenderContext ctx) {
        // Phase 1: Icon 解析（最前面）
        IconResolveGroup.resolveIcon(ctx);
        if (ctx.isPipelineFailed()) return;

        // Phase 2: Tile 位置
        TilePositionGroup.calcRandomIndex(ctx);
        if (ctx.isPipelineFailed()) return;

        TilePositionGroup.lookupTileFromRandom(ctx);
        if (ctx.isPipelineFailed()) return;

        // Phase 3: UV
        UVCalcGroup.calcUV(ctx);
        if (ctx.isPipelineFailed()) return;

        // Phase 4: 几何 bounds
        GeometryGroup.boundsFromElement(ctx);
        if (ctx.isPipelineFailed()) return;

        // Phase 5: 着色
        if (ctx.getDrawBrightness() == null) {
            ShadingGroup.calcBrightness(ctx);
            if (ctx.isPipelineFailed()) return;
        }

        // Phase 6: 渲染
        RenderGroup.renderFace(ctx);
        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }
}
