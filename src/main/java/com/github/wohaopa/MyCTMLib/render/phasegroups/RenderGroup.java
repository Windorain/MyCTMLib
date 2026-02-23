package com.github.wohaopa.MyCTMLib.render.phasegroups;

import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.GTNHIntegrationHelper;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * 渲染输出组
 * 
 * <p>本组方法假设输入数据有效，由调用方（Pipeline 层）负责验证前置条件。</p>
 */
public final class RenderGroup {

    private static final int FULL_BRIGHTNESS = 15728880;
    private static final int CORNER_TOP_LEFT = 0;
    private static final int CORNER_TOP_RIGHT = 1;
    private static final int CORNER_BOTTOM_LEFT = 2;
    private static final int CORNER_BOTTOM_RIGHT = 3;

    private RenderGroup() {
        // 工具类，禁止实例化
    }

    /**
     * 渲染面
     * 
     * <p>前置条件（由调用方保证）：</p>
     * <ul>
     *   <li>{@code ctx.getRenderBlocks() != null}</li>
     *   <li>{@code ctx.getDrawIcon() != null}</li>
     *   <li>{@code ctx.getFace() != null}</li>
     *   <li>{@code ctx.getDrawMinU/MaxU/MinV/MaxV() != null}</li>
     *   <li>{@code ctx.getDrawRelMinX/MaxX/MinY/MaxY/MinZ/MaxZ() != null}</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void renderFace(RenderContext ctx) {
        Tessellator tes = GTNHIntegrationHelper.getGTNHLibTessellator();

        double minU = ctx.getDrawMinU();
        double maxU = ctx.getDrawMaxU();
        double minV = ctx.getDrawMinV();
        double maxV = ctx.getDrawMaxV();

        double x = ctx.getX();
        double y = ctx.getY();
        double z = ctx.getZ();

        double minX = x + ctx.getDrawRelMinX();
        double maxX = x + ctx.getDrawRelMaxX();
        double minY = y + ctx.getDrawRelMinY();
        double maxY = y + ctx.getDrawRelMaxY();
        double minZ = z + ctx.getDrawRelMinZ();
        double maxZ = z + ctx.getDrawRelMaxZ();

        if (ctx.getRenderBlocks().renderFromInside) {
            double t = minU;
            minU = maxU;
            maxU = t;
        }

        // 预计算 4 个顶点的颜色和亮度
        float r0, g0, b0, r1, g1, b1, r2, g2, b2, r3, g3, b3;
        int bright0, bright1, bright2, bright3;

        boolean useBiomeTint = ctx.needsBiomeTinting();
        if (useBiomeTint && ctx.getBiomeColor() != null) {
            // TODO: 支持群系着色
            r0 = g0 = b0 = 1.0F;
            r1 = g1 = b1 = 1.0F;
            r2 = g2 = b2 = 1.0F;
            r3 = g3 = b3 = 1.0F;
            bright0 = bright1 = bright2 = bright3 = ctx.getDrawBrightness();
        } else if (ctx.getRenderBlocks().enableAO) {
            r0 = ctx.getRenderBlocks().colorRedTopLeft;
            g0 = ctx.getRenderBlocks().colorGreenTopLeft;
            b0 = ctx.getRenderBlocks().colorBlueTopLeft;
            bright0 = ctx.getDrawBrightness();

            r1 = ctx.getRenderBlocks().colorRedTopRight;
            g1 = ctx.getRenderBlocks().colorGreenTopRight;
            b1 = ctx.getRenderBlocks().colorBlueTopRight;
            bright1 = ctx.getDrawBrightness();

            r2 = ctx.getRenderBlocks().colorRedBottomLeft;
            g2 = ctx.getRenderBlocks().colorGreenBottomLeft;
            b2 = ctx.getRenderBlocks().colorBlueBottomLeft;
            bright2 = ctx.getDrawBrightness();

            r3 = ctx.getRenderBlocks().colorRedBottomRight;
            g3 = ctx.getRenderBlocks().colorGreenBottomRight;
            b3 = ctx.getRenderBlocks().colorBlueBottomRight;
            bright3 = ctx.getDrawBrightness();
        } else {
            r0 = g0 = b0 = 1.0F;
            r1 = g1 = b1 = 1.0F;
            r2 = g2 = b2 = 1.0F;
            r3 = g3 = b3 = 1.0F;
            bright0 = bright1 = bright2 = bright3 = ctx.getDrawBrightness();
        }

        // 根据面方向输出顶点
        ForgeDirection face = ctx.getFace();
        switch (face) {
            case DOWN:
                tes.setColorOpaque_F(r0, g0, b0);
                tes.setBrightness(bright0);
                tes.addVertexWithUV(minX, minY, maxZ, minU, minV);

                tes.setColorOpaque_F(r1, g1, b1);
                tes.setBrightness(bright1);
                tes.addVertexWithUV(minX, minY, minZ, minU, maxV);

                tes.setColorOpaque_F(r2, g2, b2);
                tes.setBrightness(bright2);
                tes.addVertexWithUV(maxX, minY, minZ, maxU, maxV);

                tes.setColorOpaque_F(r3, g3, b3);
                tes.setBrightness(bright3);
                tes.addVertexWithUV(maxX, minY, maxZ, maxU, minV);
                break;

            case UP:
                tes.setColorOpaque_F(r0, g0, b0);
                tes.setBrightness(bright0);
                tes.addVertexWithUV(maxX, maxY, maxZ, maxU, maxV);

                tes.setColorOpaque_F(r1, g1, b1);
                tes.setBrightness(bright1);
                tes.addVertexWithUV(maxX, maxY, minZ, maxU, minV);

                tes.setColorOpaque_F(r2, g2, b2);
                tes.setBrightness(bright2);
                tes.addVertexWithUV(minX, maxY, minZ, minU, minV);

                tes.setColorOpaque_F(r3, g3, b3);
                tes.setBrightness(bright3);
                tes.addVertexWithUV(minX, maxY, maxZ, minU, maxV);
                break;

            case NORTH:
                tes.setColorOpaque_F(r0, g0, b0);
                tes.setBrightness(bright0);
                tes.addVertexWithUV(minX, maxY, minZ, maxU, minV);

                tes.setColorOpaque_F(r1, g1, b1);
                tes.setBrightness(bright1);
                tes.addVertexWithUV(maxX, maxY, minZ, minU, minV);

                tes.setColorOpaque_F(r2, g2, b2);
                tes.setBrightness(bright2);
                tes.addVertexWithUV(maxX, minY, minZ, minU, maxV);

                tes.setColorOpaque_F(r3, g3, b3);
                tes.setBrightness(bright3);
                tes.addVertexWithUV(minX, minY, minZ, maxU, maxV);
                break;

            case SOUTH:
                tes.setColorOpaque_F(r0, g0, b0);
                tes.setBrightness(bright0);
                tes.addVertexWithUV(minX, maxY, maxZ, minU, minV);

                tes.setColorOpaque_F(r1, g1, b1);
                tes.setBrightness(bright1);
                tes.addVertexWithUV(minX, minY, maxZ, minU, maxV);

                tes.setColorOpaque_F(r2, g2, b2);
                tes.setBrightness(bright2);
                tes.addVertexWithUV(maxX, minY, maxZ, maxU, maxV);

                tes.setColorOpaque_F(r3, g3, b3);
                tes.setBrightness(bright3);
                tes.addVertexWithUV(maxX, maxY, maxZ, maxU, minV);
                break;

            case WEST:
                tes.setColorOpaque_F(r0, g0, b0);
                tes.setBrightness(bright0);
                tes.addVertexWithUV(minX, maxY, maxZ, maxU, minV);

                tes.setColorOpaque_F(r1, g1, b1);
                tes.setBrightness(bright1);
                tes.addVertexWithUV(minX, maxY, minZ, minU, minV);

                tes.setColorOpaque_F(r2, g2, b2);
                tes.setBrightness(bright2);
                tes.addVertexWithUV(minX, minY, minZ, minU, maxV);

                tes.setColorOpaque_F(r3, g3, b3);
                tes.setBrightness(bright3);
                tes.addVertexWithUV(minX, minY, maxZ, maxU, maxV);
                break;

            case EAST:
                tes.setColorOpaque_F(r0, g0, b0);
                tes.setBrightness(bright0);
                tes.addVertexWithUV(maxX, minY, maxZ, minU, maxV);

                tes.setColorOpaque_F(r1, g1, b1);
                tes.setBrightness(bright1);
                tes.addVertexWithUV(maxX, minY, minZ, maxU, maxV);

                tes.setColorOpaque_F(r2, g2, b2);
                tes.setBrightness(bright2);
                tes.addVertexWithUV(maxX, maxY, minZ, maxU, minV);

                tes.setColorOpaque_F(r3, g3, b3);
                tes.setBrightness(bright3);
                tes.addVertexWithUV(maxX, maxY, maxZ, minU, minV);
                break;

            default:
                break;
        }
    }
}
