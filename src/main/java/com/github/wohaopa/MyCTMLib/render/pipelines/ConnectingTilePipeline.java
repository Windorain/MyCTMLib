package com.github.wohaopa.MyCTMLib.render.pipelines;

import com.github.wohaopa.MyCTMLib.render.phasegroups.GeometryGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.IconResolveGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.RenderGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.ShadingGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.TilePositionGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.UVCalcGroup;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * Connecting 材质渲染管道
 */
public final class ConnectingTilePipeline {

    private ConnectingTilePipeline() {
        // 工具类，禁止实例化
    }

    /**
     * 执行 Connecting 材质渲染
     */
    public static void execute(RenderContext ctx) {
        // Group 1: Tile 位置（使用预计算的 mask）
        if (!ctx.hasConnectionMask()) {
            TilePositionGroup.calcConnectionMask(ctx);
            if (ctx.isPipelineFailed()) return;
        }
        TilePositionGroup.lookupTileFromMask(ctx);
        if (ctx.isPipelineFailed()) return;

        // Group 2: UV
        UVCalcGroup.calcUV(ctx);
        if (ctx.isPipelineFailed()) return;

        // Group 3: Icon
        IconResolveGroup.resolveIcon(ctx);
        if (ctx.isPipelineFailed()) return;

        // Group 4: 着色
        if (ctx.isItemRender()) {
            ShadingGroup.setFullBrightness(ctx);
        } else {
            if (ctx.getDrawBrightness() == null) {
                ShadingGroup.calcBrightness(ctx);
                if (ctx.isPipelineFailed()) return;
            }
        }

        // Group 5: 渲染
        RenderGroup.renderFace(ctx);
        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
        }
    }
}
