package com.github.wohaopa.MyCTMLib.render.domain;

import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * 几何数据域
 * 
 * 职责：
 * - fromElement: 从 ModelElement 提取 bounds
 * - fromRenderBlocks: 从 RenderBlocks 提取 bounds
 * - default: 使用默认 bounds (0, 1)
 */
public final class GeometryDomain {

    private GeometryDomain() {}

    /**
     * 从 ModelElement 提取 bounds
     */
    public static void fromElement(RenderContext ctx) {
        ModelElement element = ctx.getCurrentElement();
        float[] f = element.getFrom();
        float[] t = element.getTo();

        ctx.setDrawRelMinX(Math.min(f[0], t[0]) / 16.0);
        ctx.setDrawRelMaxX(Math.max(f[0], t[0]) / 16.0);
        ctx.setDrawRelMinY(Math.min(f[1], t[1]) / 16.0);
        ctx.setDrawRelMaxY(Math.max(f[1], t[1]) / 16.0);
        ctx.setDrawRelMinZ(Math.min(f[2], t[2]) / 16.0);
        ctx.setDrawRelMaxZ(Math.max(f[2], t[2]) / 16.0);
    }

    /**
     * 从 RenderBlocks 提取 bounds
     */
    public static void fromRenderBlocks(RenderContext ctx) {
        ctx.setDrawRelMinX(ctx.getRenderBlocks().renderMinX);
        ctx.setDrawRelMaxX(ctx.getRenderBlocks().renderMaxX);
        ctx.setDrawRelMinY(ctx.getRenderBlocks().renderMinY);
        ctx.setDrawRelMaxY(ctx.getRenderBlocks().renderMaxY);
        ctx.setDrawRelMinZ(ctx.getRenderBlocks().renderMinZ);
        ctx.setDrawRelMaxZ(ctx.getRenderBlocks().renderMaxZ);
    }

    /**
     * 默认 bounds (0, 1)
     */
    public static void defaults(RenderContext ctx) {
        ctx.setDrawRelMinX(0.0);
        ctx.setDrawRelMaxX(1.0);
        ctx.setDrawRelMinY(0.0);
        ctx.setDrawRelMaxY(1.0);
        ctx.setDrawRelMinZ(0.0);
        ctx.setDrawRelMaxZ(1.0);
    }
}
