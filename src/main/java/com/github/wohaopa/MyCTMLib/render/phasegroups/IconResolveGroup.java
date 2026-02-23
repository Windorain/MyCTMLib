package com.github.wohaopa.MyCTMLib.render.phasegroups;

import net.minecraft.util.IIcon;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

/**
 * Icon 解析组
 * 
 * <p>
 * 本组方法假设输入数据有效，由调用方（Pipeline 层）负责验证前置条件。
 * 职责：从 textureData 和 drawIcon 获取 grid 尺寸和原始 UV
 */
public final class IconResolveGroup {

    private IconResolveGroup() {
        // 工具类，禁止实例化
    }

    /**
     * 完整的 Icon 解析（放在最前面）
     * 
     * <p>
     * 前置条件：
     * <ul>
     * <li>{@code ctx.getTextureData() != null}</li>
     * <li>{@code ctx.getOriginalIcon() != null}</li>
     * </ul>
     * 
     * <p>
     * 后置条件：
     * <ul>
     * <li>{@code ctx.getDrawIcon() = 从 TextureRegistry 或 originalIcon 获取}</li>
     * <li>{@code ctx.getIconMinU/MaxU/MinV/MaxV() = drawIcon 的原始 UV}</li>
     * <li>{@code ctx.getGridW/GridH() = 从 textureData 计算}</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void resolveIcon(RenderContext ctx) {
        IIcon drawIcon = ctx.getDrawIcon();

        if (drawIcon == null) {
            drawIcon = ctx.getOriginalIcon();
            ctx.setDrawIcon(drawIcon);
        }

        if (drawIcon == null) {
            ctx.failPipeline("drawIcon is null and originalIcon is null");
            return;
        }

        ctx.setIconMinU(drawIcon.getMinU());
        ctx.setIconMaxU(drawIcon.getMaxU());
        ctx.setIconMinV(drawIcon.getMinV());
        ctx.setIconMaxV(drawIcon.getMaxV());

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

        ctx.setGridW(gridW);
        ctx.setGridH(gridH);
    }
}
