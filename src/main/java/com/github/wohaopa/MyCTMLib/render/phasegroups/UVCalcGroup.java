package com.github.wohaopa.MyCTMLib.render.phasegroups;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * UV 坐标计算组（基于原始数据一次性完成）
 * 
 * <p>
 * 本组方法假设输入数据有效，由调用方（Pipeline 层）负责验证前置条件。
 */
public final class UVCalcGroup {

    private UVCalcGroup() {
        // 工具类，禁止实例化
    }

    /**
     * 统一 UV 公式计算（基于原始数据一次性完成）
     * 
     * <p>
     * 前置条件：
     * <ul>
     * <li>{@code ctx.getTileX() != null}</li>
     * <li>{@code ctx.getTileY() != null}</li>
     * <li>{@code ctx.getGridW() != null}</li>
     * <li>{@code ctx.getGridH() != null}</li>
     * <li>{@code ctx.getIconMinU() != null}</li>
     * <li>{@code ctx.getIconMaxU() != null}</li>
     * <li>{@code ctx.getIconMinV() != null}</li>
     * <li>{@code ctx.getIconMaxV() != null}</li>
     * </ul>
     * 
     * <p>
     * 后置条件：
     * <ul>
     * <li>{@code ctx.getDrawMinU/MaxU/MinV/MaxV()} 已更新</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void calcUV(RenderContext ctx) {
        Integer tileX = ctx.getTileX();
        Integer tileY = ctx.getTileY();
        Integer gridW = ctx.getGridW();
        Integer gridH = ctx.getGridH();
        Double iconMinU = ctx.getIconMinU();
        Double iconMaxU = ctx.getIconMaxU();
        Double iconMinV = ctx.getIconMinV();
        Double iconMaxV = ctx.getIconMaxV();

        if (tileX == null || tileY == null
            || gridW == null
            || gridH == null
            || iconMinU == null
            || iconMaxU == null
            || iconMinV == null
            || iconMaxV == null) {
            ctx.failPipeline("Missing data for UV calculation: tileX/tileY/gridW/gridH or icon UV");
            return;
        }

        double uRange = iconMaxU - iconMinU;
        double vRange = iconMaxV - iconMinV;

        double newMinU = iconMinU + uRange * tileX / gridW;
        double newMaxU = iconMinU + uRange * (tileX + 1) / gridW;
        double newMinV = iconMinV + vRange * tileY / gridH;
        double newMaxV = iconMinV + vRange * (tileY + 1) / gridH;

        ctx.setDrawMinU(newMinU);
        ctx.setDrawMaxU(newMaxU);
        ctx.setDrawMinV(newMinV);
        ctx.setDrawMaxV(newMaxV);
    }
}
