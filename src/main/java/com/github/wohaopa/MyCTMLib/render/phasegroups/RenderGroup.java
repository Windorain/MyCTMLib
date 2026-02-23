package com.github.wohaopa.MyCTMLib.render.phasegroups;

import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.GTNHIntegrationHelper;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * 渲染输出组（纯提交层，零计算）
 * 
 * <p>
 * 本组方法假设所有输入数据已由上游 Domain 计算完毕。
 * </p>
 */
public final class RenderGroup {

    private static final int CORNER_TOP_LEFT = 0;
    private static final int CORNER_TOP_RIGHT = 1;
    private static final int CORNER_BOTTOM_LEFT = 2;
    private static final int CORNER_BOTTOM_RIGHT = 3;

    private static final int[][] CORNER_ORDER_BY_FACE = {
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT }, // DOWN
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT }, // UP
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT }, // NORTH
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT }, // SOUTH
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT }, // WEST
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT }, // EAST
    };

    private RenderGroup() {}

    /**
     * 渲染面（纯提交，零计算）
     * 
     * <p>
     * 前置条件（由调用方保证）：
     * </p>
     * <ul>
     * <li>PositionDomain.calc() 已调用：worldX/Y/Z</li>
     * <li>GeometryDomain 已调用：relMinX/Y/Z, relMaxX/Y/Z</li>
     * <li>UVDomain.calc() 已调用：drawMinU/V, drawMaxU/V</li>
     * <li>BrightnessDomain 已调用：drawBrightness</li>
     * <li>ColorDomain 已调用：color 四角</li>
     * </ul>
     */
    public static void renderFace(RenderContext ctx) {
        Tessellator tes = GTNHIntegrationHelper.getGTNHLibTessellator();

        double minU = ctx.getDrawMinU();
        double maxU = ctx.getDrawMaxU();
        double minV = ctx.getDrawMinV();
        double maxV = ctx.getDrawMaxV();

        double baseX = ctx.getWorldX();
        double baseY = ctx.getWorldY();
        double baseZ = ctx.getWorldZ();

        double relMinX = ctx.getDrawRelMinX();
        double relMaxX = ctx.getDrawRelMaxX();
        double relMinY = ctx.getDrawRelMinY();
        double relMaxY = ctx.getDrawRelMaxY();
        double relMinZ = ctx.getDrawRelMinZ();
        double relMaxZ = ctx.getDrawRelMaxZ();

        double minX = baseX;
        double maxX = baseX - relMinX + relMaxX;
        double minY = baseY;
        double maxY = baseY - relMinY + relMaxY;
        double minZ = baseZ;
        double maxZ = baseZ - relMinZ + relMaxZ;

        if (ctx.getRenderBlocks().renderFromInside) {
            double t = minU;
            minU = maxU;
            maxU = t;
        }

        // 四角颜色
        float rTL = ctx.getColorTL_R();
        float gTL = ctx.getColorTL_G();
        float bTL = ctx.getColorTL_B();
        float rTR = ctx.getColorTR_R();
        float gTR = ctx.getColorTR_G();
        float bTR = ctx.getColorTR_B();
        float rBL = ctx.getColorBL_R();
        float gBL = ctx.getColorBL_G();
        float bBL = ctx.getColorBL_B();
        float rBR = ctx.getColorBR_R();
        float gBR = ctx.getColorBR_G();
        float bBR = ctx.getColorBR_B();

        // 四角亮度
        int brightTL = ctx.getBrightnessTL();
        int brightTR = ctx.getBrightnessTR();
        int brightBL = ctx.getBrightnessBL();
        int brightBR = ctx.getBrightnessBR();

        ForgeDirection face = ctx.getFace();
        int[] corners = CORNER_ORDER_BY_FACE[face.ordinal()];

        switch (face) {
            case DOWN:
                setVertex(tes, corners[0], rTL, gTL, bTL, brightTL);
                tes.addVertexWithUV(minX, minY, maxZ, minU, minV);

                setVertex(tes, corners[1], rBL, gBL, bBL, brightBL);
                tes.addVertexWithUV(minX, minY, minZ, minU, maxV);

                setVertex(tes, corners[2], rBR, gBR, bBR, brightBR);
                tes.addVertexWithUV(maxX, minY, minZ, maxU, maxV);

                setVertex(tes, corners[3], rTR, gTR, bTR, brightTR);
                tes.addVertexWithUV(maxX, minY, maxZ, maxU, minV);
                break;

            case UP:
                setVertex(tes, corners[0], rTL, gTL, bTL, brightTL);
                tes.addVertexWithUV(maxX, maxY, maxZ, maxU, maxV);

                setVertex(tes, corners[1], rBL, gBL, bBL, brightBL);
                tes.addVertexWithUV(maxX, maxY, minZ, maxU, minV);

                setVertex(tes, corners[2], rBR, gBR, bBR, brightBR);
                tes.addVertexWithUV(minX, maxY, minZ, minU, minV);

                setVertex(tes, corners[3], rTR, gTR, bTR, brightTR);
                tes.addVertexWithUV(minX, maxY, maxZ, minU, maxV);
                break;

            case NORTH:
                setVertex(tes, corners[0], rTL, gTL, bTL, brightTL);
                tes.addVertexWithUV(minX, maxY, minZ, maxU, minV);

                setVertex(tes, corners[1], rBL, gBL, bBL, brightBL);
                tes.addVertexWithUV(maxX, maxY, minZ, minU, minV);

                setVertex(tes, corners[2], rBR, gBR, bBR, brightBR);
                tes.addVertexWithUV(maxX, minY, minZ, minU, maxV);

                setVertex(tes, corners[3], rTR, gTR, bTR, brightTR);
                tes.addVertexWithUV(minX, minY, minZ, maxU, maxV);
                break;

            case SOUTH:
                setVertex(tes, corners[0], rTL, gTL, bTL, brightTL);
                tes.addVertexWithUV(minX, maxY, maxZ, minU, minV);

                setVertex(tes, corners[1], rBL, gBL, bBL, brightBL);
                tes.addVertexWithUV(minX, minY, maxZ, minU, maxV);

                setVertex(tes, corners[2], rBR, gBR, bBR, brightBR);
                tes.addVertexWithUV(maxX, minY, maxZ, maxU, maxV);

                setVertex(tes, corners[3], rTR, gTR, bTR, brightTR);
                tes.addVertexWithUV(maxX, maxY, maxZ, maxU, minV);
                break;

            case WEST:
                setVertex(tes, corners[0], rTL, gTL, bTL, brightTL);
                tes.addVertexWithUV(minX, maxY, maxZ, maxU, minV);

                setVertex(tes, corners[1], rBL, gBL, bBL, brightBL);
                tes.addVertexWithUV(minX, maxY, minZ, minU, minV);

                setVertex(tes, corners[2], rBR, gBR, bBR, brightBR);
                tes.addVertexWithUV(minX, minY, minZ, minU, maxV);

                setVertex(tes, corners[3], rTR, gTR, bTR, brightTR);
                tes.addVertexWithUV(minX, minY, maxZ, maxU, maxV);
                break;

            case EAST:
                setVertex(tes, corners[0], rTL, gTL, bTL, brightTL);
                tes.addVertexWithUV(maxX, minY, maxZ, minU, maxV);

                setVertex(tes, corners[1], rBL, gBL, bBL, brightBL);
                tes.addVertexWithUV(maxX, minY, minZ, maxU, maxV);

                setVertex(tes, corners[2], rBR, gBR, bBR, brightBR);
                tes.addVertexWithUV(maxX, maxY, minZ, maxU, minV);

                setVertex(tes, corners[3], rTR, gTR, bTR, brightTR);
                tes.addVertexWithUV(maxX, maxY, maxZ, minU, minV);
                break;

            default:
                break;
        }
    }

    private static void setVertex(Tessellator tes, int corner, float r, float g, float b, int brightness) {
        tes.setColorOpaque_F(r, g, b);
        tes.setBrightness(brightness);
    }
}
