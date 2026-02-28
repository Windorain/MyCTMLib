package com.github.wohaopa.MyCTMLib.render.domain;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * 颜色数据域
 * 
 * 职责：
 * - computeUniform: 设置默认白色（非 AO 模式）
 * - computeAO: 计算四角 AO 颜色（AO 模式，手动计算）
 * - setFromRenderBlocks: 从 RenderBlocks 字段读取颜色
 * - setFullUniform: 设置全亮（非 AO 模式，物品渲染）
 */
public final class ColorDomain {

    private ColorDomain() {}

    /**
     * 设置默认白色（非 AO 模式）
     */
    public static void computeUniform(RenderContext ctx) {
        ctx.setColorTL_R(1.0f);
        ctx.setColorTL_G(1.0f);
        ctx.setColorTL_B(1.0f);
        ctx.setColorTR_R(1.0f);
        ctx.setColorTR_G(1.0f);
        ctx.setColorTR_B(1.0f);
        ctx.setColorBL_R(1.0f);
        ctx.setColorBL_G(1.0f);
        ctx.setColorBL_B(1.0f);
        ctx.setColorBR_R(1.0f);
        ctx.setColorBR_G(1.0f);
        ctx.setColorBR_B(1.0f);
    }

    /**
     * 从 RenderBlocks 字段读取颜色值
     * 
     * <p>适用场景：RenderBlocks 已正确计算颜色字段（原版方块渲染）</p>
     */
    public static void setFromRenderBlocks(RenderContext ctx) {
        net.minecraft.client.renderer.RenderBlocks rb = ctx.getRenderBlocks();
        
        ctx.setColorTL_R(rb.colorRedTopLeft);
        ctx.setColorTL_G(rb.colorGreenTopLeft);
        ctx.setColorTL_B(rb.colorBlueTopLeft);
        
        ctx.setColorTR_R(rb.colorRedTopRight);
        ctx.setColorTR_G(rb.colorGreenTopRight);
        ctx.setColorTR_B(rb.colorBlueTopRight);
        
        ctx.setColorBL_R(rb.colorRedBottomLeft);
        ctx.setColorBL_G(rb.colorGreenBottomLeft);
        ctx.setColorBL_B(rb.colorBlueBottomLeft);
        
        ctx.setColorBR_R(rb.colorRedBottomRight);
        ctx.setColorBR_G(rb.colorGreenBottomRight);
        ctx.setColorBR_B(rb.colorBlueBottomRight);
    }

    /**
     * 计算四角 AO 颜色（手动计算）
     * 
     * <p>
     * 参考：RenderBlocks.renderStandardBlockWithAmbientOcclusion
     * </p>
     * 
     * <p>适用场景：Model 分支渲染，RenderBlocks 字段未初始化</p>
     * 
     * <p>
     * 计算公式：最终颜色 = shade × aoCorner
     * - shade: 面方向基础系数（DOWN=0.5, UP=1.0, NORTH/SOUTH=0.8, WEST/EAST=0.6）
     * - aoCorner: 四角 AO 阴影系数（0.0~1.0）
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
        
        float shade = getShadeForFace(face);
        
        ctx.info("ColorDomain.computeAO: face=" + face + ", shade=" + shade);
        
        // 根据面查询 AO 值并计算四角系数
        float aoTL, aoTR, aoBR, aoBL;
        
        switch (face) {
            case DOWN: {
                int baseY = (rb.renderMinY <= 0.0) ? y - 1 : y;
                String boundaryInfo = (rb.renderMinY <= 0.0) ? " (boundary adjusted)" : "";
                ctx.debug("  baseY=" + baseY + boundaryInfo);
                
                float aoXNeg = getAOValue(world, x - 1, baseY, z);
                float aoXPos = getAOValue(world, x + 1, baseY, z);
                float aoZNeg = getAOValue(world, x, baseY, z - 1);
                float aoZPos = getAOValue(world, x, baseY, z + 1);
                float aoXNegZNeg = getAOValue(world, x - 1, baseY, z - 1);
                float aoXNegZPos = getAOValue(world, x - 1, baseY, z + 1);
                float aoXPosZNeg = getAOValue(world, x + 1, baseY, z - 1);
                float aoXPosZPos = getAOValue(world, x + 1, baseY, z + 1);
                float baseAO = getAOValue(world, x, baseY - 1, z);
                
                aoTL = (aoXNegZPos + aoXNeg + aoZPos + baseAO) / 4.0F;
                aoTR = (aoZPos + baseAO + aoXPosZPos + aoXPos) / 4.0F;
                aoBR = (baseAO + aoZNeg + aoXPos + aoXPosZNeg) / 4.0F;
                aoBL = (aoXNeg + aoXNegZNeg + baseAO + aoZNeg) / 4.0F;
                
                ctx.debug("  AO coeffs: TL=" + aoTL + ", TR=" + aoTR + ", BL=" + aoBL + ", BR=" + aoBR);
                ctx.debug("  Colors: TL=(" + (shade * aoTL) + "," + (shade * aoTL) + "," + (shade * aoTL) + ")");
                break;
            }
            
            case UP: {
                int baseY = (rb.renderMaxY >= 1.0) ? y + 1 : y;
                String boundaryInfo = (rb.renderMaxY >= 1.0) ? " (boundary adjusted)" : "";
                ctx.debug("  baseY=" + baseY + boundaryInfo);
                
                float aoXNeg = getAOValue(world, x - 1, baseY, z);
                float aoXPos = getAOValue(world, x + 1, baseY, z);
                float aoZNeg = getAOValue(world, x, baseY, z - 1);
                float aoZPos = getAOValue(world, x, baseY, z + 1);
                float aoXNegZNeg = getAOValue(world, x - 1, baseY, z - 1);
                float aoXNegZPos = getAOValue(world, x - 1, baseY, z + 1);
                float aoXPosZNeg = getAOValue(world, x + 1, baseY, z - 1);
                float aoXPosZPos = getAOValue(world, x + 1, baseY, z + 1);
                float baseAO = getAOValue(world, x, baseY + 1, z);
                
                aoTL = (aoXNegZPos + aoXNeg + aoZPos + baseAO) / 4.0F;
                aoTR = (aoZPos + baseAO + aoXPosZPos + aoXPos) / 4.0F;
                aoBR = (baseAO + aoZNeg + aoXPos + aoXPosZNeg) / 4.0F;
                aoBL = (aoXNeg + aoXNegZNeg + baseAO + aoZNeg) / 4.0F;
                
                ctx.debug("  AO coeffs: TL=" + aoTL + ", TR=" + aoTR + ", BL=" + aoBL + ", BR=" + aoBR);
                ctx.debug("  Colors: TL=(" + (shade * aoTL) + "," + (shade * aoTL) + "," + (shade * aoTL) + ")");
                break;
            }
            
            case NORTH: {
                int baseZ = (rb.renderMinZ <= 0.0) ? z - 1 : z;
                String boundaryInfo = (rb.renderMinZ <= 0.0) ? " (boundary adjusted)" : "";
                ctx.debug("  baseZ=" + baseZ + boundaryInfo);
                
                float aoXNeg = getAOValue(world, x - 1, y, baseZ);
                float aoXPos = getAOValue(world, x + 1, y, baseZ);
                float aoYNeg = getAOValue(world, x, y - 1, baseZ);
                float aoYPos = getAOValue(world, x, y + 1, baseZ);
                float aoXNegYNeg = getAOValue(world, x - 1, y - 1, baseZ);
                float aoXNegYPos = getAOValue(world, x - 1, y + 1, baseZ);
                float aoXPosYNeg = getAOValue(world, x + 1, y - 1, baseZ);
                float aoXPosYPos = getAOValue(world, x + 1, y + 1, baseZ);
                float baseAO = getAOValue(world, x, y, baseZ - 1);
                
                aoTL = (aoXNeg + aoXNegYPos + baseAO + aoYPos) / 4.0F;
                aoBL = (baseAO + aoYPos + aoXPos + aoXPosYPos) / 4.0F;
                aoBR = (aoYNeg + baseAO + aoXPosYNeg + aoXPos) / 4.0F;
                aoTR = (aoXNegYNeg + aoXNeg + aoYNeg + baseAO) / 4.0F;
                
                ctx.debug("  AO coeffs: TL=" + aoTL + ", TR=" + aoTR + ", BL=" + aoBL + ", BR=" + aoBR);
                ctx.debug("  Colors: TL=(" + (shade * aoTL) + "," + (shade * aoTL) + "," + (shade * aoTL) + ")");
                break;
            }
            
            case SOUTH: {
                int baseZ = (rb.renderMaxZ >= 1.0) ? z + 1 : z;
                String boundaryInfo = (rb.renderMaxZ >= 1.0) ? " (boundary adjusted)" : "";
                ctx.debug("  baseZ=" + baseZ + boundaryInfo);
                
                float aoXNeg = getAOValue(world, x - 1, y, baseZ);
                float aoXPos = getAOValue(world, x + 1, y, baseZ);
                float aoYNeg = getAOValue(world, x, y - 1, baseZ);
                float aoYPos = getAOValue(world, x, y + 1, baseZ);
                float aoXNegYNeg = getAOValue(world, x - 1, y - 1, baseZ);
                float aoXNegYPos = getAOValue(world, x - 1, y + 1, baseZ);
                float aoXPosYNeg = getAOValue(world, x + 1, y - 1, baseZ);
                float aoXPosYPos = getAOValue(world, x + 1, y + 1, baseZ);
                float baseAO = getAOValue(world, x, y, baseZ + 1);
                
                aoTL = (aoXNeg + aoXNegYPos + baseAO + aoYPos) / 4.0F;
                aoTR = (baseAO + aoYPos + aoXPos + aoXPosYPos) / 4.0F;
                aoBR = (aoYNeg + baseAO + aoXPosYNeg + aoXPos) / 4.0F;
                aoBL = (aoXNegYNeg + aoXNeg + aoYNeg + baseAO) / 4.0F;
                
                ctx.debug("  AO coeffs: TL=" + aoTL + ", TR=" + aoTR + ", BL=" + aoBL + ", BR=" + aoBR);
                ctx.debug("  Colors: TL=(" + (shade * aoTL) + "," + (shade * aoTL) + "," + (shade * aoTL) + ")");
                break;
            }
            
            case WEST: {
                int baseX = (rb.renderMinX <= 0.0) ? x - 1 : x;
                String boundaryInfo = (rb.renderMinX <= 0.0) ? " (boundary adjusted)" : "";
                ctx.debug("  baseX=" + baseX + boundaryInfo);
                
                float aoYNeg = getAOValue(world, baseX, y - 1, z);
                float aoZNeg = getAOValue(world, baseX, y, z - 1);
                float aoZPos = getAOValue(world, baseX, y, z + 1);
                float aoYPos = getAOValue(world, baseX, y + 1, z);
                float aoYNegZNeg = getAOValue(world, baseX, y - 1, z - 1);
                float aoYNegZPos = getAOValue(world, baseX, y - 1, z + 1);
                float aoYPosZNeg = getAOValue(world, baseX, y + 1, z - 1);
                float aoYPosZPos = getAOValue(world, baseX, y + 1, z + 1);
                float baseAO = getAOValue(world, baseX - 1, y, z);
                
                aoTR = (aoYNeg + aoYNegZPos + baseAO + aoZPos) / 4.0F;
                aoTL = (baseAO + aoZPos + aoYPos + aoYPosZPos) / 4.0F;
                aoBL = (aoZNeg + baseAO + aoYPosZNeg + aoYPos) / 4.0F;
                aoBR = (aoYNegZNeg + aoYNeg + baseAO + aoZNeg) / 4.0F;
                
                ctx.debug("  AO coeffs: TL=" + aoTL + ", TR=" + aoTR + ", BL=" + aoBL + ", BR=" + aoBR);
                ctx.debug("  Colors: TL=(" + (shade * aoTL) + "," + (shade * aoTL) + "," + (shade * aoTL) + ")");
                break;
            }
            
            case EAST: {
                int baseX = (rb.renderMaxX >= 1.0) ? x + 1 : x;
                String boundaryInfo = (rb.renderMaxX >= 1.0) ? " (boundary adjusted)" : "";
                ctx.debug("  baseX=" + baseX + boundaryInfo);
                
                float aoYNeg = getAOValue(world, baseX, y - 1, z);
                float aoZNeg = getAOValue(world, baseX, y, z - 1);
                float aoZPos = getAOValue(world, baseX, y, z + 1);
                float aoYPos = getAOValue(world, baseX, y + 1, z);
                float aoYNegZNeg = getAOValue(world, baseX, y - 1, z - 1);
                float aoYNegZPos = getAOValue(world, baseX, y - 1, z + 1);
                float aoYPosZNeg = getAOValue(world, baseX, y + 1, z - 1);
                float aoYPosZPos = getAOValue(world, baseX, y + 1, z + 1);
                float baseAO = getAOValue(world, baseX + 1, y, z);
                
                aoTL = (aoYNeg + aoYNegZPos + baseAO + aoZPos) / 4.0F;
                aoTR = (baseAO + aoZPos + aoYPos + aoYPosZPos) / 4.0F;
                aoBR = (aoZNeg + baseAO + aoYPosZNeg + aoYPos) / 4.0F;
                aoBL = (aoYNegZNeg + aoYNeg + baseAO + aoZNeg) / 4.0F;
                
                ctx.debug("  AO coeffs: TL=" + aoTL + ", TR=" + aoTR + ", BL=" + aoBL + ", BR=" + aoBR);
                ctx.debug("  Colors: TL=(" + (shade * aoTL) + "," + (shade * aoTL) + "," + (shade * aoTL) + ")");
                break;
            }
            
            default: {
                aoTL = 1.0F;
                aoTR = 1.0F;
                aoBR = 1.0F;
                aoBL = 1.0F;
                ctx.debug("  Colors: TL=(1.0,1.0,1.0) (uniform)");
                break;
            }
        }
        
        // 计算最终颜色 = shade × aoCorner
        ctx.setColorTL_R(shade * aoTL);
        ctx.setColorTL_G(shade * aoTL);
        ctx.setColorTL_B(shade * aoTL);
        
        ctx.setColorTR_R(shade * aoTR);
        ctx.setColorTR_G(shade * aoTR);
        ctx.setColorTR_B(shade * aoTR);
        
        ctx.setColorBL_R(shade * aoBL);
        ctx.setColorBL_G(shade * aoBL);
        ctx.setColorBL_B(shade * aoBL);
        
        ctx.setColorBR_R(shade * aoBR);
        ctx.setColorBR_G(shade * aoBR);
        ctx.setColorBR_B(shade * aoBR);
    }

    /**
     * 获取面方向的基础 Shade 值
     * 
     * @return DOWN=0.5F, UP=1.0F, NORTH/SOUTH=0.8F, WEST/EAST=0.6F
     */
    private static float getShadeForFace(ForgeDirection face) {
        switch (face) {
            case DOWN:  return 0.5F;
            case UP:    return 1.0F;
            case NORTH: return 0.8F;
            case SOUTH: return 0.8F;
            case WEST:  return 0.6F;
            case EAST:  return 0.6F;
            default:    return 1.0F;
        }
    }

    /**
     * 获取方块的 AO 亮度值
     * 
     * @return 方块的 getAmbientOcclusionLightValue()，如果方块为 null 则返回 1.0F
     */
    private static float getAOValue(IBlockAccess world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block == null) {
            return 1.0F;
        }
        return block.getAmbientOcclusionLightValue();
    }
}
