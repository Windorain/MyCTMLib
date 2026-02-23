package com.github.wohaopa.MyCTMLib.render.domain;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * 亮度数据域
 * 
 * 职责：
 * - computeUniform: 计算统一亮度（非 AO 模式）
 * - computeAO: 计算四角亮度（AO 模式）
 * - setFullUniform: 设置全亮（非 AO 模式，物品渲染）
 * - setFullAO: 设置全亮（AO 模式，物品渲染）
 */
public final class BrightnessDomain {

    private static final int FULL_BRIGHTNESS = 15728880;

    private BrightnessDomain() {}

    /**
     * 计算统一亮度（非 AO 模式）
     */
    public static void computeUniform(RenderContext ctx) {
        int brightness = ctx.getBlock()
            .getMixedBrightnessForBlock(ctx.getBlockAccess(), (int) ctx.getX(), (int) ctx.getY(), (int) ctx.getZ());
        ctx.setBrightnessTL(brightness);
        ctx.setBrightnessTR(brightness);
        ctx.setBrightnessBL(brightness);
        ctx.setBrightnessBR(brightness);
    }

    /**
     * 计算四角亮度（AO 模式）
     */
    public static void computeAO(RenderContext ctx) {
        net.minecraft.client.renderer.RenderBlocks rb = ctx.getRenderBlocks();
        ctx.setBrightnessTL(rb.brightnessTopLeft);
        ctx.setBrightnessTR(rb.brightnessTopRight);
        ctx.setBrightnessBL(rb.brightnessBottomLeft);
        ctx.setBrightnessBR(rb.brightnessBottomRight);
    }

    /**
     * 设置全亮（非 AO 模式，物品渲染）
     */
    public static void setFullUniform(RenderContext ctx) {
        ctx.setBrightnessTL(FULL_BRIGHTNESS);
        ctx.setBrightnessTR(FULL_BRIGHTNESS);
        ctx.setBrightnessBL(FULL_BRIGHTNESS);
        ctx.setBrightnessBR(FULL_BRIGHTNESS);
    }

    /**
     * 设置全亮（AO 模式，物品渲染）
     */
    public static void setFullAO(RenderContext ctx) {
        ctx.setBrightnessTL(FULL_BRIGHTNESS);
        ctx.setBrightnessTR(FULL_BRIGHTNESS);
        ctx.setBrightnessBL(FULL_BRIGHTNESS);
        ctx.setBrightnessBR(FULL_BRIGHTNESS);
    }
}
