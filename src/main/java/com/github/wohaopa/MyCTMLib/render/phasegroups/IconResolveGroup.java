package com.github.wohaopa.MyCTMLib.render.phasegroups;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * Icon 解析组
 * 
 * <p>本组方法假设输入数据有效，由调用方（Pipeline 层）负责验证前置条件。</p>
 */
public final class IconResolveGroup {

    private IconResolveGroup() {
        // 工具类，禁止实例化
    }

    /**
     * 从 TextureRegistry 获取 Icon
     * 
     * <p>前置条件：</p>
     * <ul>
     *   <li>{@code ctx.getTextureData() != null}</li>
     * </ul>
     * 
     * <p>后置条件：</p>
     * <ul>
     *   <li>{@code ctx.getDrawIcon() != null}（如果成功）</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void resolveIcon(RenderContext ctx) {
        // 简化处理：直接使用 originalIcon
        if (ctx.getDrawIcon() == null) {
            ctx.setDrawIcon(ctx.getOriginalIcon());
        }
    }

    /**
     * 带 fallback 的 Icon 解析
     * 
     * <p>前置条件：</p>
     * <ul>
     *   <li>{@code ctx.getTextureData() != null}</li>
     * </ul>
     * 
     * <p>后置条件：</p>
     * <ul>
     *   <li>{@code ctx.getDrawIcon() != null}（总是有效， fallback 到 originalIcon）</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void resolveIconWithFallback(RenderContext ctx) {
        resolveIcon(ctx);
        if (ctx.getDrawIcon() == null) {
            ctx.setDrawIcon(ctx.getOriginalIcon());
        }
    }
}
