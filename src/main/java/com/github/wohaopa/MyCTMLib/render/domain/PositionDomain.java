package com.github.wohaopa.MyCTMLib.render.domain;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * 世界坐标数据域
 * 
 * 职责：
 * - 计算最终世界坐标：worldXYZ = blockXYZ + relBounds
 */
public final class PositionDomain {

    private PositionDomain() {}

    /**
     * 计算世界坐标
     */
    public static void calc(RenderContext ctx) {
        double blockX = ctx.getBlockX();
        double blockY = ctx.getBlockY();
        double blockZ = ctx.getBlockZ();

        ctx.setWorldX(blockX + ctx.getDrawRelMinX());
        ctx.setWorldY(blockY + ctx.getDrawRelMinY());
        ctx.setWorldZ(blockZ + ctx.getDrawRelMinZ());
    }
}
