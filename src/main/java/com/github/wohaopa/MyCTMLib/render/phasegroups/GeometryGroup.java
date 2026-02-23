package com.github.wohaopa.MyCTMLib.render.phasegroups;

import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * 几何 bounds 计算组
 * 
 * <p>本组方法假设输入数据有效，由调用方（Pipeline 层）负责验证前置条件。</p>
 */
public final class GeometryGroup {

    private GeometryGroup() {
        // 工具类，禁止实例化
    }

    /**
     * 从 Element 提取 bounds
     * 
     * <p>前置条件：</p>
     * <ul>
     *   <li>{@code ctx.getCurrentElement() != null}</li>
     *   <li>{@code ctx.getCurrentElement().getFrom/To() != null}</li>
     * </ul>
     * 
     * <p>后置条件：</p>
     * <ul>
     *   <li>{@code ctx.getDrawRelMinX/MaxX/MinY/MaxY/MinZ/MaxZ()} 已更新</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void boundsFromElement(RenderContext ctx) {
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
     * 
     * <p>前置条件：</p>
     * <ul>
     *   <li>{@code ctx.getRenderBlocks() != null}</li>
     * </ul>
     * 
     * <p>后置条件：</p>
     * <ul>
     *   <li>{@code ctx.getDrawRelMinX/MaxX/MinY/MaxY/MinZ/MaxZ()} 已更新</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void boundsFromRenderBlocks(RenderContext ctx) {
        ctx.setDrawRelMinX(ctx.getRenderBlocks().renderMinX);
        ctx.setDrawRelMaxX(ctx.getRenderBlocks().renderMaxX);
        ctx.setDrawRelMinY(ctx.getRenderBlocks().renderMinY);
        ctx.setDrawRelMaxY(ctx.getRenderBlocks().renderMaxY);
        ctx.setDrawRelMinZ(ctx.getRenderBlocks().renderMinZ);
        ctx.setDrawRelMaxZ(ctx.getRenderBlocks().renderMaxZ);
    }

    /**
     * 默认 bounds(0,1)
     * 
     * <p>后置条件：</p>
     * <ul>
     *   <li>{@code ctx.getDrawRelMinX/MaxX() = 0.0, 1.0}</li>
     *   <li>{@code ctx.getDrawRelMinY/MaxY() = 0.0, 1.0}</li>
     *   <li>{@code ctx.getDrawRelMinZ/MaxZ() = 0.0, 1.0}</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void boundsDefault(RenderContext ctx) {
        ctx.setDrawRelMinX(0.0);
        ctx.setDrawRelMaxX(1.0);
        ctx.setDrawRelMinY(0.0);
        ctx.setDrawRelMaxY(1.0);
        ctx.setDrawRelMinZ(0.0);
        ctx.setDrawRelMaxZ(1.0);
    }
}
