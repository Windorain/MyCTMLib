package com.github.wohaopa.MyCTMLib.render.domain;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * 亮度数据域
 * 
 * 职责：
 * - computeUniform: 计算统一亮度（非 AO 模式）
 * - computeAO: 计算四角亮度（AO 模式，手动计算）
 * - setFromRenderBlocks: 从 RenderBlocks 字段读取亮度
 * - setFullUniform: 设置全亮（非 AO 模式，物品渲染）
 * - setFullAO: 设置全亮（AO 模式，物品渲染）
 */
public final class BrightnessDomain {

    private static final int FULL_BRIGHTNESS = 15728880;
    private static final int AO_BRIGHTNESS_MASK = 16711935;

    private BrightnessDomain() {}

    /**
     * 计算统一亮度（非 AO 模式）
     */
    public static void computeUniform(RenderContext ctx) {
        int brightness = ctx.getBlock()
            .getMixedBrightnessForBlock(
                ctx.getBlockAccess(),
                (int) ctx.getBlockX(),
                (int) ctx.getBlockY(),
                (int) ctx.getBlockZ());
        ctx.setBrightnessTL(brightness);
        ctx.setBrightnessTR(brightness);
        ctx.setBrightnessBL(brightness);
        ctx.setBrightnessBR(brightness);
    }

    /**
     * 从 RenderBlocks 字段读取亮度值
     * 
     * <p>
     * 适用场景：RenderBlocks 已正确计算亮度字段（原版方块渲染）
     * </p>
     */
    public static void setFromRenderBlocks(RenderContext ctx) {
        net.minecraft.client.renderer.RenderBlocks rb = ctx.getRenderBlocks();
        ctx.setBrightnessTL(rb.brightnessTopLeft);
        ctx.setBrightnessTR(rb.brightnessTopRight);
        ctx.setBrightnessBL(rb.brightnessBottomLeft);
        ctx.setBrightnessBR(rb.brightnessBottomRight);
    }

    /**
     * 计算四角亮度（AO 模式，手动计算）
     * 
     * <p>
     * 参考：RenderBlocks.renderStandardBlockWithAmbientOcclusion
     * </p>
     * 
     * <p>
     * 适用场景：Model 分支渲染，RenderBlocks 字段未初始化
     * </p>
     */
    public static void computeAO(RenderContext ctx) {
        Block block = ctx.getBlock();
        IBlockAccess world = ctx.getBlockAccess();
        int x = (int) ctx.getBlockX();
        int y = (int) ctx.getBlockY();
        int z = (int) ctx.getBlockZ();
        ForgeDirection face = ctx.getFace();
        net.minecraft.client.renderer.RenderBlocks rb = ctx.getRenderBlocks();

        ctx.info("BrightnessDomain.computeAO: face=" + face);
        ctx.debug("  Position: (" + x + "," + y + "," + z + ")");

        int currentBrightness = block.getMixedBrightnessForBlock(world, x, y, z);

        switch (face) {
            case DOWN: {
                int baseY = (rb.renderMinY <= 0.0) ? y - 1 : y;
                String boundaryInfo = (rb.renderMinY <= 0.0) ? " (boundary adjusted)" : "";
                ctx.debug("  baseY=" + baseY + boundaryInfo);

                int baseBrightness;
                if (rb.renderMinY <= 0.0 || !isOpaque(world, x, baseY, z)) {
                    baseBrightness = block.getMixedBrightnessForBlock(world, x, baseY, z);
                } else {
                    baseBrightness = currentBrightness;
                }

                int xNeg = block.getMixedBrightnessForBlock(world, x - 1, baseY, z);
                int xPos = block.getMixedBrightnessForBlock(world, x + 1, baseY, z);
                int zNeg = block.getMixedBrightnessForBlock(world, x, baseY, z - 1);
                int zPos = block.getMixedBrightnessForBlock(world, x, baseY, z + 1);
                int xNegZNeg = block.getMixedBrightnessForBlock(world, x - 1, baseY, z - 1);
                int xNegZPos = block.getMixedBrightnessForBlock(world, x - 1, baseY, z + 1);
                int xPosZNeg = block.getMixedBrightnessForBlock(world, x + 1, baseY, z - 1);
                int xPosZPos = block.getMixedBrightnessForBlock(world, x + 1, baseY, z + 1);

                ctx.setBrightnessTL(getAoBrightness(xNegZPos, xNeg, zPos, baseBrightness));
                ctx.setBrightnessTR(getAoBrightness(zPos, xPosZPos, xPos, baseBrightness));
                ctx.setBrightnessBR(getAoBrightness(zNeg, xPos, xPosZNeg, baseBrightness));
                ctx.setBrightnessBL(getAoBrightness(xNeg, xNegZNeg, zNeg, baseBrightness));

                ctx.debug(
                    "  Brightness: TL=" + ctx.getBrightnessTL()
                        + ", TR="
                        + ctx.getBrightnessTR()
                        + ", BL="
                        + ctx.getBrightnessBL()
                        + ", BR="
                        + ctx.getBrightnessBR());
                break;
            }

            case UP: {
                int baseY = (rb.renderMaxY >= 1.0) ? y + 1 : y;
                String boundaryInfo = (rb.renderMaxY >= 1.0) ? " (boundary adjusted)" : "";
                ctx.debug("  baseY=" + baseY + boundaryInfo);

                int baseBrightness;
                if (rb.renderMaxY >= 1.0 || !isOpaque(world, x, baseY, z)) {
                    baseBrightness = block.getMixedBrightnessForBlock(world, x, baseY, z);
                } else {
                    baseBrightness = currentBrightness;
                }

                int xNeg = block.getMixedBrightnessForBlock(world, x - 1, baseY, z);
                int xPos = block.getMixedBrightnessForBlock(world, x + 1, baseY, z);
                int zNeg = block.getMixedBrightnessForBlock(world, x, baseY, z - 1);
                int zPos = block.getMixedBrightnessForBlock(world, x, baseY, z + 1);
                int xNegZNeg = block.getMixedBrightnessForBlock(world, x - 1, baseY, z - 1);
                int xNegZPos = block.getMixedBrightnessForBlock(world, x - 1, baseY, z + 1);
                int xPosZNeg = block.getMixedBrightnessForBlock(world, x + 1, baseY, z - 1);
                int xPosZPos = block.getMixedBrightnessForBlock(world, x + 1, baseY, z + 1);

                ctx.setBrightnessTR(getAoBrightness(xNegZPos, xNeg, zPos, baseBrightness));
                ctx.setBrightnessTL(getAoBrightness(zPos, xPosZPos, xPos, baseBrightness));
                ctx.setBrightnessBL(getAoBrightness(zNeg, xPos, xPosZNeg, baseBrightness));
                ctx.setBrightnessBR(getAoBrightness(xNeg, xNegZNeg, zNeg, baseBrightness));

                ctx.debug(
                    "  Brightness: TL=" + ctx.getBrightnessTL()
                        + ", TR="
                        + ctx.getBrightnessTR()
                        + ", BL="
                        + ctx.getBrightnessBL()
                        + ", BR="
                        + ctx.getBrightnessBR());
                break;
            }

            case NORTH: {
                int baseZ = (rb.renderMinZ <= 0.0) ? z - 1 : z;
                String boundaryInfo = (rb.renderMinZ <= 0.0) ? " (boundary adjusted)" : "";
                ctx.debug("  baseZ=" + baseZ + boundaryInfo);

                int baseBrightness;
                if (rb.renderMinZ <= 0.0 || !isOpaque(world, x, y, baseZ)) {
                    baseBrightness = block.getMixedBrightnessForBlock(world, x, y, baseZ);
                } else {
                    baseBrightness = currentBrightness;
                }

                int xNeg = block.getMixedBrightnessForBlock(world, x - 1, y, baseZ);
                int xPos = block.getMixedBrightnessForBlock(world, x + 1, y, baseZ);
                int yNeg = block.getMixedBrightnessForBlock(world, x, y - 1, baseZ);
                int yPos = block.getMixedBrightnessForBlock(world, x, y + 1, baseZ);
                int xNegYNeg = block.getMixedBrightnessForBlock(world, x - 1, y - 1, baseZ);
                int xNegYPos = block.getMixedBrightnessForBlock(world, x - 1, y + 1, baseZ);
                int xPosYNeg = block.getMixedBrightnessForBlock(world, x + 1, y - 1, baseZ);
                int xPosYPos = block.getMixedBrightnessForBlock(world, x + 1, y + 1, baseZ);

                ctx.setBrightnessTL(getAoBrightness(xNeg, xNegYPos, yPos, baseBrightness));
                ctx.setBrightnessBL(getAoBrightness(yPos, xPos, xPosYPos, baseBrightness));
                ctx.setBrightnessBR(getAoBrightness(yNeg, xPosYNeg, xPos, baseBrightness));
                ctx.setBrightnessTR(getAoBrightness(xNegYNeg, xNeg, yNeg, baseBrightness));

                ctx.debug(
                    "  Brightness: TL=" + ctx.getBrightnessTL()
                        + ", TR="
                        + ctx.getBrightnessTR()
                        + ", BL="
                        + ctx.getBrightnessBL()
                        + ", BR="
                        + ctx.getBrightnessBR());
                break;
            }

            case SOUTH: {
                int baseZ = (rb.renderMaxZ >= 1.0) ? z + 1 : z;
                String boundaryInfo = (rb.renderMaxZ >= 1.0) ? " (boundary adjusted)" : "";
                ctx.debug("  baseZ=" + baseZ + boundaryInfo);

                int baseBrightness;
                if (rb.renderMaxZ >= 1.0 || !isOpaque(world, x, y, baseZ)) {
                    baseBrightness = block.getMixedBrightnessForBlock(world, x, y, baseZ);
                } else {
                    baseBrightness = currentBrightness;
                }

                int xNeg = block.getMixedBrightnessForBlock(world, x - 1, y, baseZ);
                int xPos = block.getMixedBrightnessForBlock(world, x + 1, y, baseZ);
                int yNeg = block.getMixedBrightnessForBlock(world, x, y - 1, baseZ);
                int yPos = block.getMixedBrightnessForBlock(world, x, y + 1, baseZ);
                int xNegYNeg = block.getMixedBrightnessForBlock(world, x - 1, y - 1, baseZ);
                int xNegYPos = block.getMixedBrightnessForBlock(world, x - 1, y + 1, baseZ);
                int xPosYNeg = block.getMixedBrightnessForBlock(world, x + 1, y - 1, baseZ);
                int xPosYPos = block.getMixedBrightnessForBlock(world, x + 1, y + 1, baseZ);

                ctx.setBrightnessTL(getAoBrightness(xNeg, xNegYPos, yPos, baseBrightness));
                ctx.setBrightnessTR(getAoBrightness(yPos, xPos, xPosYPos, baseBrightness));
                ctx.setBrightnessBR(getAoBrightness(yNeg, xPosYNeg, xPos, baseBrightness));
                ctx.setBrightnessBL(getAoBrightness(xNegYNeg, xNeg, yNeg, baseBrightness));

                ctx.debug(
                    "  Brightness: TL=" + ctx.getBrightnessTL()
                        + ", TR="
                        + ctx.getBrightnessTR()
                        + ", BL="
                        + ctx.getBrightnessBL()
                        + ", BR="
                        + ctx.getBrightnessBR());
                break;
            }

            case WEST: {
                int baseX = (rb.renderMinX <= 0.0) ? x - 1 : x;
                String boundaryInfo = (rb.renderMinX <= 0.0) ? " (boundary adjusted)" : "";
                ctx.debug("  baseX=" + baseX + boundaryInfo);

                int baseBrightness;
                if (rb.renderMinX <= 0.0 || !isOpaque(world, baseX, y, z)) {
                    baseBrightness = block.getMixedBrightnessForBlock(world, baseX, y, z);
                } else {
                    baseBrightness = currentBrightness;
                }

                int yNeg = block.getMixedBrightnessForBlock(world, baseX, y - 1, z);
                int zNeg = block.getMixedBrightnessForBlock(world, baseX, y, z - 1);
                int zPos = block.getMixedBrightnessForBlock(world, baseX, y, z + 1);
                int yPos = block.getMixedBrightnessForBlock(world, baseX, y + 1, z);
                int yNegZNeg = block.getMixedBrightnessForBlock(world, baseX, y - 1, z - 1);
                int yNegZPos = block.getMixedBrightnessForBlock(world, baseX, y - 1, z + 1);
                int yPosZNeg = block.getMixedBrightnessForBlock(world, baseX, y + 1, z - 1);
                int yPosZPos = block.getMixedBrightnessForBlock(world, baseX, y + 1, z + 1);

                ctx.setBrightnessTR(getAoBrightness(yNeg, yNegZPos, zPos, baseBrightness));
                ctx.setBrightnessTL(getAoBrightness(zPos, yPos, yPosZPos, baseBrightness));
                ctx.setBrightnessBL(getAoBrightness(zNeg, yPosZNeg, yPos, baseBrightness));
                ctx.setBrightnessBR(getAoBrightness(yNegZNeg, yNeg, zNeg, baseBrightness));

                ctx.debug(
                    "  Brightness: TL=" + ctx.getBrightnessTL()
                        + ", TR="
                        + ctx.getBrightnessTR()
                        + ", BL="
                        + ctx.getBrightnessBL()
                        + ", BR="
                        + ctx.getBrightnessBR());
                break;
            }

            case EAST: {
                int baseX = (rb.renderMaxX >= 1.0) ? x + 1 : x;
                String boundaryInfo = (rb.renderMaxX >= 1.0) ? " (boundary adjusted)" : "";
                ctx.debug("  baseX=" + baseX + boundaryInfo);

                int baseBrightness;
                if (rb.renderMaxX >= 1.0 || !isOpaque(world, baseX, y, z)) {
                    baseBrightness = block.getMixedBrightnessForBlock(world, baseX, y, z);
                } else {
                    baseBrightness = currentBrightness;
                }

                int yNeg = block.getMixedBrightnessForBlock(world, baseX, y - 1, z);
                int zNeg = block.getMixedBrightnessForBlock(world, baseX, y, z - 1);
                int zPos = block.getMixedBrightnessForBlock(world, baseX, y, z + 1);
                int yPos = block.getMixedBrightnessForBlock(world, baseX, y + 1, z);
                int yNegZNeg = block.getMixedBrightnessForBlock(world, baseX, y - 1, z - 1);
                int yNegZPos = block.getMixedBrightnessForBlock(world, baseX, y - 1, z + 1);
                int yPosZNeg = block.getMixedBrightnessForBlock(world, baseX, y + 1, z - 1);
                int yPosZPos = block.getMixedBrightnessForBlock(world, baseX, y + 1, z + 1);

                ctx.setBrightnessTL(getAoBrightness(yNeg, yNegZPos, zPos, baseBrightness));
                ctx.setBrightnessTR(getAoBrightness(zPos, yPos, yPosZPos, baseBrightness));
                ctx.setBrightnessBR(getAoBrightness(zNeg, yPosZNeg, yPos, baseBrightness));
                ctx.setBrightnessBL(getAoBrightness(yNegZNeg, yNeg, zNeg, baseBrightness));

                ctx.debug(
                    "  Brightness: TL=" + ctx.getBrightnessTL()
                        + ", TR="
                        + ctx.getBrightnessTR()
                        + ", BL="
                        + ctx.getBrightnessBL()
                        + ", BR="
                        + ctx.getBrightnessBR());
                break;
            }

            default: {
                ctx.setBrightnessTL(currentBrightness);
                ctx.setBrightnessTR(currentBrightness);
                ctx.setBrightnessBL(currentBrightness);
                ctx.setBrightnessBR(currentBrightness);
                ctx.debug("  Brightness: TL=" + currentBrightness + " (uniform)");
                break;
            }
        }
    }

    /**
     * 设置全亮（非 AO 模式，物品渲染）
     */
    public static void setFullUniform(RenderContext ctx) {
        ctx.setBrightnessTL(FULL_BRIGHTNESS);
        ctx.setBrightnessTR(FULL_BRIGHTNESS);
        ctx.setBrightnessBL(FULL_BRIGHTNESS);
        ctx.setBrightnessBR(FULL_BRIGHTNESS);
    }

    /**
     * 设置全亮（AO 模式，物品渲染）
     */
    public static void setFullAO(RenderContext ctx) {
        ctx.setBrightnessTL(FULL_BRIGHTNESS);
        ctx.setBrightnessTR(FULL_BRIGHTNESS);
        ctx.setBrightnessBL(FULL_BRIGHTNESS);
        ctx.setBrightnessBR(FULL_BRIGHTNESS);
    }

    /**
     * AO 亮度平均公式（参考 RenderBlocks.getAoBrightness）
     * 
     * @param p1 角落对角亮度
     * @param p2 第一切线方向亮度
     * @param p3 第二切线方向亮度
     * @param p4 基准亮度（法线方向）
     * @return 平均后的亮度值
     */
    private static int getAoBrightness(int p1, int p2, int p3, int p4) {
        if (p1 == 0) p1 = p4;
        if (p2 == 0) p2 = p4;
        if (p3 == 0) p3 = p4;
        return (p1 + p2 + p3 + p4 >> 2) & AO_BRIGHTNESS_MASK;
    }

    /**
     * 检查方块是否不透明
     */
    private static boolean isOpaque(IBlockAccess world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return block != null && block.isOpaqueCube();
    }
}
