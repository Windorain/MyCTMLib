package com.github.wohaopa.MyCTMLib.render;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;

/**
 * 根据 (tileX, tileY) 与 layout 网格尺寸将纹理切成单格 UV，用 Tessellator 绘制单面一个 quad。
 * 当 renderBlocks.enableAO 为 true 时使用四角亮度/颜色（与环境光遮挡一致），否则使用 fallback 单值。
 * 各面顶点顺序与 AO 四角对应均与原版 RenderBlocks.renderFace* 一致。
 *
 * 水平面（DOWN/UP）纹理方向约定：minU→西，maxU→东，minV→北（纹理上），maxV→南（纹理下）。
 */
public final class FaceRenderer {

    /** 四角顺序：0=TopLeft, 1=TopRight, 2=BottomLeft, 3=BottomRight。用于从 RenderBlocks 取亮度与颜色。 */
    private static final int CORNER_TOP_LEFT = 0;
    private static final int CORNER_TOP_RIGHT = 1;
    private static final int CORNER_BOTTOM_LEFT = 2;
    private static final int CORNER_BOTTOM_RIGHT = 3;

    /**
     * 各面顶点顺序对应的 AO 角索引（与 addVertexWithUV 的 4 个顶点一致）。
     * 原版 RenderBlocks.renderFace* 各面顶点顺序均为 TL→BL→BR→TR，保证正面朝向（CCW）与 AO 四角对应正确。
     */
    private static final int[][] CORNER_ORDER_BY_FACE = {
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT }, // DOWN
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT }, // UP
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT }, // NORTH
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT }, // SOUTH
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT }, // WEST
        { CORNER_TOP_LEFT, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, CORNER_TOP_RIGHT }, // EAST
    };

    private static final int FULL_BRIGHTNESS = 15728880;

    /** 在添加一个顶点前设置 Tessellator 的亮度与颜色（来自 RenderBlocks 四角 AO）。 */
    private static void setTessellatorAO(RenderBlocks rb, int corner) {
        float r, g, b;
        int bright;
        switch (corner) {
            case CORNER_TOP_LEFT:
                r = rb.colorRedTopLeft;
                g = rb.colorGreenTopLeft;
                b = rb.colorBlueTopLeft;
                bright = rb.brightnessTopLeft;
                break;
            case CORNER_TOP_RIGHT:
                r = rb.colorRedTopRight;
                g = rb.colorGreenTopRight;
                b = rb.colorBlueTopRight;
                bright = rb.brightnessTopRight;
                break;
            case CORNER_BOTTOM_LEFT:
                r = rb.colorRedBottomLeft;
                g = rb.colorGreenBottomLeft;
                b = rb.colorBlueBottomLeft;
                bright = rb.brightnessBottomLeft;
                break;
            case CORNER_BOTTOM_RIGHT:
                r = rb.colorRedBottomRight;
                g = rb.colorGreenBottomRight;
                b = rb.colorBlueBottomRight;
                bright = rb.brightnessBottomRight;
                break;
            default:
                r = g = b = 1.0F;
                bright = 0;
                break;
        }
        Tessellator.instance.setColorOpaque_F(r, g, b);
        Tessellator.instance.setBrightness(bright);
    }

    /**
     * 为顶点设置颜色与亮度。若 baseData 有生物群系着色则使用 biomeColor 与 AO 混合；否则使用 AO 或 (1,1,1)。
     */
    private static void setTessellatorColorForVertex(RenderBlocks rb, int corner, BaseTextureData baseData,
        int biomeColor, int fallbackBrightness) {
        float r, g, b;
        int bright;
        if (baseData != null && baseData.getTinting() != null && biomeColor >= 0) {
            float bioR = (biomeColor >> 16 & 255) / 255.0F;
            float bioG = (biomeColor >> 8 & 255) / 255.0F;
            float bioB = (biomeColor & 255) / 255.0F;
            if (rb.enableAO) {
                switch (corner) {
                    case CORNER_TOP_LEFT:
                        r = bioR * rb.colorRedTopLeft;
                        g = bioG * rb.colorGreenTopLeft;
                        b = bioB * rb.colorBlueTopLeft;
                        bright = baseData.isEmissive() ? FULL_BRIGHTNESS : rb.brightnessTopLeft;
                        break;
                    case CORNER_TOP_RIGHT:
                        r = bioR * rb.colorRedTopRight;
                        g = bioG * rb.colorGreenTopRight;
                        b = bioB * rb.colorBlueTopRight;
                        bright = baseData.isEmissive() ? FULL_BRIGHTNESS : rb.brightnessTopRight;
                        break;
                    case CORNER_BOTTOM_LEFT:
                        r = bioR * rb.colorRedBottomLeft;
                        g = bioG * rb.colorGreenBottomLeft;
                        b = bioB * rb.colorBlueBottomLeft;
                        bright = baseData.isEmissive() ? FULL_BRIGHTNESS : rb.brightnessBottomLeft;
                        break;
                    case CORNER_BOTTOM_RIGHT:
                        r = bioR * rb.colorRedBottomRight;
                        g = bioG * rb.colorGreenBottomRight;
                        b = bioB * rb.colorBlueBottomRight;
                        bright = baseData.isEmissive() ? FULL_BRIGHTNESS : rb.brightnessBottomRight;
                        break;
                    default:
                        r = bioR;
                        g = bioG;
                        b = bioB;
                        bright = baseData.isEmissive() ? FULL_BRIGHTNESS : fallbackBrightness;
                        break;
                }
            } else {
                r = bioR;
                g = bioG;
                b = bioB;
                bright = baseData.isEmissive() ? FULL_BRIGHTNESS : fallbackBrightness;
            }
        } else {
            if (rb.enableAO) {
                setTessellatorAO(rb, corner);
                return;
            }
            r = g = b = 1.0F;
            bright = fallbackBrightness;
        }
        Tessellator.instance.setColorOpaque_F(r, g, b);
        Tessellator.instance.setBrightness(bright);
    }

    /**
     * 绘制一个面，纹理取 layout 网格中的 (tileX, tileY) 格。
     *
     * @param renderBlocks       当前 RenderBlocks（含 renderMin/Max）
     * @param x                  block x
     * @param y                  block y
     * @param z                  block z
     * @param face               面方向
     * @param icon               整图 icon
     * @param tileX              格 x
     * @param tileY              格 y
     * @param gridW              layout 宽度
     * @param gridH              layout 高度
     * @param fallbackBrightness 非 AO 时使用的整面亮度
     */
    /**
     * 绘制一个面（子块 bounds 版本）。纹理取 layout 网格中的 (tileX, tileY) 格，顶点按 relMin/relMax 归一化坐标绘制。
     *
     * @param baseData    BaseTextureData，可为 null；若有 tinting 则应用生物群系着色
     * @param blockAccess 世界访问，用于获取生物群系颜色；物品渲染时传 null
     */
    public static void drawFace(RenderBlocks renderBlocks, double x, double y, double z, ForgeDirection face,
        IIcon icon, int tileX, int tileY, int gridW, int gridH, int fallbackBrightness, double relMinX, double relMaxX,
        double relMinY, double relMaxY, double relMinZ, double relMaxZ, BaseTextureData baseData,
        IBlockAccess blockAccess, int blockX, int blockY, int blockZ) {
        // 1. 计算连接纹理的基础 UV（基于 tileX/tileY 在 layout 网格中的位置）
        double baseMinU = icon.getMinU() + (icon.getMaxU() - icon.getMinU()) * tileX / gridW;
        double baseMaxU = icon.getMinU() + (icon.getMaxU() - icon.getMinU()) * (tileX + 1) / gridW;
        double baseMinV = icon.getMinV() + (icon.getMaxV() - icon.getMinV()) * tileY / gridH;
        double baseMaxV = icon.getMinV() + (icon.getMaxV() - icon.getMinV()) * (tileY + 1) / gridH;

        // 2. 根据 face 和 bounds 对 UV 进行插值，使台阶等非标准方块的纹理正确裁剪
        double minU, maxU, minV, maxV;
        double uRange = baseMaxU - baseMinU;
        double vRange = baseMaxV - baseMinV;

        switch (face) {
            case DOWN:
            case UP:
                // 水平面：U 对应 X 轴，V 对应 Z 轴
                minU = baseMinU + uRange * relMinX;
                maxU = baseMinU + uRange * relMaxX;
                minV = baseMinV + vRange * relMinZ;
                maxV = baseMinV + vRange * relMaxZ;
                break;
            case NORTH:
            case SOUTH:
                // 南北面：U 对应 X 轴，V 对应 Y 轴
                minU = baseMinU + uRange * relMinX;
                maxU = baseMinU + uRange * relMaxX;
                minV = baseMinV + vRange * relMinY;
                maxV = baseMinV + vRange * relMaxY;
                break;
            case WEST:
            case EAST:
                // 东西面：U 对应 Z 轴，V 对应 Y 轴
                minU = baseMinU + uRange * relMinZ;
                maxU = baseMinU + uRange * relMaxZ;
                minV = baseMinV + vRange * relMinY;
                maxV = baseMinV + vRange * relMaxY;
                break;
            default:
                minU = baseMinU;
                maxU = baseMaxU;
                minV = baseMinV;
                maxV = baseMaxV;
        }

        drawFace(
            renderBlocks,
            x,
            y,
            z,
            face,
            minU,
            maxU,
            minV,
            maxV,
            fallbackBrightness,
            relMinX,
            relMaxX,
            relMinY,
            relMaxY,
            relMinZ,
            relMaxZ,
            baseData,
            blockAccess,
            blockX,
            blockY,
            blockZ);
    }

    public static void drawFace(RenderBlocks renderBlocks, double x, double y, double z, ForgeDirection face,
        IIcon icon, int tileX, int tileY, int gridW, int gridH, int fallbackBrightness, BaseTextureData baseData,
        IBlockAccess blockAccess, int blockX, int blockY, int blockZ) {
        double minU = icon.getMinU() + (icon.getMaxU() - icon.getMinU()) * tileX / gridW;
        double maxU = icon.getMinU() + (icon.getMaxU() - icon.getMinU()) * (tileX + 1) / gridW;
        double minV = icon.getMinV() + (icon.getMaxV() - icon.getMinV()) * tileY / gridH;
        double maxV = icon.getMinV() + (icon.getMaxV() - icon.getMinV()) * (tileY + 1) / gridH;

        drawFace(renderBlocks, x, y, z, face, minU, maxU, minV, maxV, fallbackBrightness, 0, 1, 0, 1, 0, 1,
            baseData, blockAccess, blockX, blockY, blockZ);
    }

    /**
     * 绘制单面一个 quad（带 bounds）。enableAO 时每顶点使用 RenderBlocks 四角亮度/颜色，否则使用 fallbackBrightness 与 (1,1,1)。
     * 若 baseData 有 tinting 则应用生物群系着色。
     *
     * @param relMinX,relMaxX,relMinY,relMaxY,relMinZ,relMaxZ 相对于方块 (x,y,z) 的归一化坐标 (0–1)，用于子块绘制
     */
    public static void drawFace(RenderBlocks renderBlocks, double x, double y, double z, ForgeDirection face,
        double minU, double maxU, double minV, double maxV, int fallbackBrightness, double relMinX, double relMaxX,
        double relMinY, double relMaxY, double relMinZ, double relMaxZ, BaseTextureData baseData,
        IBlockAccess blockAccess, int blockX, int blockY, int blockZ) {
        double minX = x + relMinX;
        double maxX = x + relMaxX;
        double minY = y + relMinY;
        double maxY = y + relMaxY;
        double minZ = z + relMinZ;
        double maxZ = z + relMaxZ;

        Tessellator tes = Tessellator.instance;
        if (renderBlocks.renderFromInside) {
            double t;
            t = minU;
            minU = maxU;
            maxU = t;
        }

        int biomeColor = -1;
        boolean useBiomeTint = baseData != null && baseData.getTinting() != null && blockAccess != null;
        if (useBiomeTint) {
            biomeColor = BiomeTintingHelper.getBiomeColor(baseData.getTinting(), blockAccess, blockX, blockY, blockZ);
        }

        if (!useBiomeTint && !renderBlocks.enableAO) {
            tes.setBrightness(fallbackBrightness);
            tes.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        }

        int fi = face.ordinal();
        int[] corners = CORNER_ORDER_BY_FACE[fi];

        switch (face) {
            case DOWN:
                // 顶点顺序与原版 renderFaceYNeg 一致：TL→BL→BR→TR
                // UV 约定：minU→西 minV→北 maxU→东 maxV→南。交换 U 与 V 以满足约定。
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[0], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[0]);
                tes.addVertexWithUV(minX, minY, maxZ, minU, minV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[1], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[1]);
                tes.addVertexWithUV(minX, minY, minZ, minU, maxV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[2], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[2]);
                tes.addVertexWithUV(maxX, minY, minZ, maxU, maxV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[3], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[3]);
                tes.addVertexWithUV(maxX, minY, maxZ, maxU, minV);
                break;
            case UP:
                // 顶点顺序与原版 renderFaceYPos 一致：TL→BL→BR→TR
                // UV 约定：minU→西 minV→北 maxU→东 maxV→南。交换 U 与 V 以满足约定。
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[0], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[0]);
                tes.addVertexWithUV(maxX, maxY, maxZ, maxU, maxV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[1], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[1]);
                tes.addVertexWithUV(maxX, maxY, minZ, maxU, minV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[2], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[2]);
                tes.addVertexWithUV(minX, maxY, minZ, minU, minV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[3], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[3]);
                tes.addVertexWithUV(minX, maxY, maxZ, minU, maxV);
                break;
            case NORTH:
                // 顶点顺序与原版 renderFaceZNeg 一致：TL→BL→BR→TR
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[0], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[0]);
                tes.addVertexWithUV(minX, maxY, minZ, maxU, minV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[1], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[1]);
                tes.addVertexWithUV(maxX, maxY, minZ, minU, minV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[2], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[2]);
                tes.addVertexWithUV(maxX, minY, minZ, minU, maxV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[3], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[3]);
                tes.addVertexWithUV(minX, minY, minZ, maxU, maxV);
                break;
            case SOUTH:
                // 顶点顺序与原版 renderFaceZPos 一致：TL→BL→BR→TR
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[0], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[0]);
                tes.addVertexWithUV(minX, maxY, maxZ, minU, minV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[1], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[1]);
                tes.addVertexWithUV(minX, minY, maxZ, minU, maxV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[2], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[2]);
                tes.addVertexWithUV(maxX, minY, maxZ, maxU, maxV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[3], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[3]);
                tes.addVertexWithUV(maxX, maxY, maxZ, maxU, minV);
                break;
            case WEST:
                // 顶点顺序与原版 renderFaceXNeg 一致：TL→BL→BR→TR
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[0], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[0]);
                tes.addVertexWithUV(minX, maxY, maxZ, maxU, minV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[1], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[1]);
                tes.addVertexWithUV(minX, maxY, minZ, minU, minV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[2], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[2]);
                tes.addVertexWithUV(minX, minY, minZ, minU, maxV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[3], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[3]);
                tes.addVertexWithUV(minX, minY, maxZ, maxU, maxV);
                break;
            case EAST:
                // 顶点顺序与原版 renderFaceXPos 一致：TL→BL→BR→TR
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[0], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[0]);
                tes.addVertexWithUV(maxX, minY, maxZ, minU, maxV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[1], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[1]);
                tes.addVertexWithUV(maxX, minY, minZ, maxU, maxV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[2], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[2]);
                tes.addVertexWithUV(maxX, maxY, minZ, maxU, minV);
                if (useBiomeTint) setTessellatorColorForVertex(renderBlocks, corners[3], baseData, biomeColor, fallbackBrightness);
                else if (renderBlocks.enableAO) setTessellatorAO(renderBlocks, corners[3]);
                tes.addVertexWithUV(maxX, maxY, maxZ, minU, minV);
                break;
            default:
                break;
        }
    }
}
