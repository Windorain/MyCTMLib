package com.github.wohaopa.MyCTMLib.render.phasegroups;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

/**
 * UV 坐标计算组
 * 
 * <p>本组方法假设输入数据有效，由调用方（Pipeline 层）负责验证前置条件。</p>
 */
public final class UVCalcGroup {

    private UVCalcGroup() {
        // 工具类，禁止实例化
    }

    /**
     * 统一 UV 公式计算
     * 
     * <p>前置条件：</p>
     * <ul>
     *   <li>{@code ctx.getTilePosition() != null}</li>
     *   <li>{@code ctx.getTextureData()} 不为 null</li>
     * </ul>
     * 
     * <p>后置条件：</p>
     * <ul>
     *   <li>{@code ctx.getDrawMinU/MaxU/MinV/MaxV()} 已更新</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void calcUV(RenderContext ctx) {
        int[] tile = ctx.getTilePosition();

        int gridW, gridH;
        if (ctx.getTextureData() instanceof RandomTextureData rtd) {
            gridW = rtd.getColumns();
            gridH = rtd.getRows();
        } else if (ctx.getTextureData() instanceof ConnectingTextureData ctd) {
            LayoutHandler handler = LayoutHandlers.get(ctd.getLayout());
            gridW = handler.getWidth();
            gridH = handler.getHeight();
        } else {
            gridW = 1;
            gridH = 1;
        }

        double uRange = ctx.getDrawMaxU() - ctx.getDrawMinU();
        double vRange = ctx.getDrawMaxV() - ctx.getDrawMinV();

        ctx.setDrawMinU(ctx.getDrawMinU() + uRange * tile[0] / gridW);
        ctx.setDrawMaxU(ctx.getDrawMinU() + uRange * (tile[0] + 1) / gridW);
        ctx.setDrawMinV(ctx.getDrawMinV() + vRange * tile[1] / gridH);
        ctx.setDrawMaxV(ctx.getDrawMinV() + vRange * (tile[1] + 1) / gridH);
    }
}
