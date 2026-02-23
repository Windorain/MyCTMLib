package com.github.wohaopa.MyCTMLib.render.domain;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

/**
 * Icon 数据域
 * 
 * 职责：
 * - 从 textureKey 查询 drawIcon
 * - 提取 icon 的原始 UV
 * - 计算 grid 尺寸
 */
public final class IconDomain {

    private IconDomain() {}

    /**
     * 解析 Icon 数据
     */
    public static void resolve(RenderContext ctx) {
        String textureKey = ctx.getTextureKey();
        TextureRegistry registry = TextureRegistry.getInstance();
        net.minecraft.util.IIcon icon = registry.getIcon(textureKey);
        if (icon == null) {
            icon = ctx.getOriginalIcon();
        }

        ctx.setDrawIcon(icon);
        ctx.setIconMinU(icon.getMinU());
        ctx.setIconMaxU(icon.getMaxU());
        ctx.setIconMinV(icon.getMinV());
        ctx.setIconMaxV(icon.getMaxV());

        int gridW, gridH;
        com.github.wohaopa.MyCTMLib.texture.TextureTypeData texData = ctx.getTextureData();
        if (texData instanceof RandomTextureData rtd) {
            gridW = rtd.getColumns();
            gridH = rtd.getRows();
        } else if (texData instanceof ConnectingTextureData ctd) {
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
