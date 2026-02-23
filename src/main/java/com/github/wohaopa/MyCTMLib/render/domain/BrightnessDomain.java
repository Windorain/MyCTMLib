package com.github.wohaopa.MyCTMLib.render.domain;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * 亮度数据域
 * 
 * 职责：
 * - compute: 从世界查询亮度
 * - setFull: 设置全亮（物品渲染）
 */
public final class BrightnessDomain {

    private static final int FULL_BRIGHTNESS = 15728880;

    private BrightnessDomain() {
    }

    /**
     * 从世界查询亮度
     */
    public static void compute(RenderContext ctx) {
        int brightness = ctx.getBlock().getMixedBrightnessForBlock(
            ctx.getBlockAccess(),
            (int) ctx.getX(),
            (int) ctx.getY(),
            (int) ctx.getZ());
        ctx.setDrawBrightness(brightness);
    }

    /**
     * 设置全亮（物品渲染）
     */
    public static void setFull(RenderContext ctx) {
        ctx.setDrawBrightness(FULL_BRIGHTNESS);
    }
}
