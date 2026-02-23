package com.github.wohaopa.MyCTMLib.render.domain;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * 颜色数据域
 * 
 * 职责：
 * - computeUniform: 设置默认白色（非 AO 模式）
 * - computeAO: 从 RenderBlocks 获取四角颜色（AO 模式）
 */
public final class ColorDomain {

    private ColorDomain() {
    }

    /**
     * 设置默认白色（非 AO 模式）
     */
    public static void computeUniform(RenderContext ctx) {
        ctx.setColorTL_R(1.0f);
        ctx.setColorTL_G(1.0f);
        ctx.setColorTL_B(1.0f);
        ctx.setColorTR_R(1.0f);
        ctx.setColorTR_G(1.0f);
        ctx.setColorTR_B(1.0f);
        ctx.setColorBL_R(1.0f);
        ctx.setColorBL_G(1.0f);
        ctx.setColorBL_B(1.0f);
        ctx.setColorBR_R(1.0f);
        ctx.setColorBR_G(1.0f);
        ctx.setColorBR_B(1.0f);
    }

    /**
     * 从 RenderBlocks 获取四角颜色（AO 模式）
     */
    public static void computeAO(RenderContext ctx) {
        net.minecraft.client.renderer.RenderBlocks rb = ctx.getRenderBlocks();
        
        ctx.setColorTL_R(rb.colorRedTopLeft);
        ctx.setColorTL_G(rb.colorGreenTopLeft);
        ctx.setColorTL_B(rb.colorBlueTopLeft);
        
        ctx.setColorTR_R(rb.colorRedTopRight);
        ctx.setColorTR_G(rb.colorGreenTopRight);
        ctx.setColorTR_B(rb.colorBlueTopRight);
        
        ctx.setColorBL_R(rb.colorRedBottomLeft);
        ctx.setColorBL_G(rb.colorGreenBottomLeft);
        ctx.setColorBL_B(rb.colorBlueBottomLeft);
        
        ctx.setColorBR_R(rb.colorRedBottomRight);
        ctx.setColorBR_G(rb.colorGreenBottomRight);
        ctx.setColorBR_B(rb.colorBlueBottomRight);
    }
}
