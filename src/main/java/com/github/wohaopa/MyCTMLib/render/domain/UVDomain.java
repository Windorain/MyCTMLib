package com.github.wohaopa.MyCTMLib.render.domain;

import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;

/**
 * UV 坐标数据域
 * 
 * 职责：
 * - 步骤 1: 根据 tile 位置计算 baseUV（texture 切片）
 * - 步骤 2: 根据 face 和 relBounds 计算 drawUV（texture 缩放）
 */
public final class UVDomain {

    private UVDomain() {}

    /**
     * 计算最终 UV 坐标
     * 
     * @param ctx 渲染上下文
     */
    public static void calc(RenderContext ctx) {
        assert ctx.getCtmSprite() != null;

        CTMTextureAtlasSprite ctmSprite = ctx.getCtmSprite();
        int gridW = ctmSprite.getGridWidth();
        int gridH = ctmSprite.getGridHeight();
        int tileX = ctx.getTileX();
        int tileY = ctx.getTileY();

        double iconMinU = ctx.getIconMinU();
        double iconMaxU = ctx.getIconMaxU();
        double iconMinV = ctx.getIconMinV();
        double iconMaxV = ctx.getIconMaxV();

        // 步骤 1: 计算基础 UV（texture 切片）
        double baseMinU = iconMinU + (iconMaxU - iconMinU) * tileX / gridW;
        double baseMaxU = iconMinU + (iconMaxU - iconMinU) * (tileX + 1) / gridW;
        double baseMinV = iconMinV + (iconMaxV - iconMinV) * tileY / gridH;
        double baseMaxV = iconMinV + (iconMaxV - iconMinV) * (tileY + 1) / gridH;

        // 步骤 2: 根据 face 插值（texture 缩放）
        double uRange = baseMaxU - baseMinU;
        double vRange = baseMaxV - baseMinV;

        double drawMinU, drawMaxU, drawMinV, drawMaxV;
        ForgeDirection face = ctx.getFace();

        switch (face) {
            case DOWN, UP -> {
                // 水平面：U 对应 X 轴，V 对应 Z 轴
                drawMinU = baseMinU + uRange * ctx.getDrawRelMinX();
                drawMaxU = baseMinU + uRange * ctx.getDrawRelMaxX();
                drawMinV = baseMinV + vRange * ctx.getDrawRelMinZ();
                drawMaxV = baseMinV + vRange * ctx.getDrawRelMaxZ();
            }
            case NORTH, SOUTH -> {
                // 南北面：U 对应 X 轴，V 对应 Y 轴
                drawMinU = baseMinU + uRange * ctx.getDrawRelMinX();
                drawMaxU = baseMinU + uRange * ctx.getDrawRelMaxX();
                drawMinV = baseMinV + vRange * ctx.getDrawRelMinY();
                drawMaxV = baseMinV + vRange * ctx.getDrawRelMaxY();
            }
            case WEST, EAST -> {
                // 东西面：U 对应 Z 轴，V 对应 Y 轴
                drawMinU = baseMinU + uRange * ctx.getDrawRelMinZ();
                drawMaxU = baseMinU + uRange * ctx.getDrawRelMaxZ();
                drawMinV = baseMinV + vRange * ctx.getDrawRelMinY();
                drawMaxV = baseMinV + vRange * ctx.getDrawRelMaxY();
            }
            default -> {
                drawMinU = baseMinU;
                drawMaxU = baseMaxU;
                drawMinV = baseMinV;
                drawMaxV = baseMaxV;
            }
        }

        ctx.setDrawMinU(drawMinU);
        ctx.setDrawMaxU(drawMaxU);
        ctx.setDrawMinV(drawMinV);
        ctx.setDrawMaxV(drawMaxV);
    }
}
