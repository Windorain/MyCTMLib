package com.github.wohaopa.MyCTMLib.render.phases;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.GTNHIntegrationHelper;
import com.github.wohaopa.MyCTMLib.render.BiomeTintingHelper;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;

/**
 * 渲染管线最终阶段：根据预计算的 UV 和坐标数据绘制面。
 * 
 * 数据流：
 * 1. 从 RenderContext 获取预计算的 UV 和坐标
 * 2. 预计算 4 个顶点的颜色和亮度（平铺变量）
 * 3. switch 按面输出 4 个顶点（纯数据传送，无判断）
 */
public class RenderFacePhase implements PipelinePhase {

    private static final int FULL_BRIGHTNESS = 15728880;

    @Override
    public PhaseResult process(RenderContext context) {
        TextureTypeData data = context.getTextureData();
        if (data == null) {
            return PhaseResult.CONTINUE;
        }
        
        RenderBlocks rb = context.getRenderBlocks();
        double x = context.getX();
        double y = context.getY();
        double z = context.getZ();
        ForgeDirection face = context.getFace();
        
        double minU = context.getDrawMinU();
        double maxU = context.getDrawMaxU();
        double minV = context.getDrawMinV();
        double maxV = context.getDrawMaxV();
        
        double relMinX = context.getDrawRelMinX();
        double relMaxX = context.getDrawRelMaxX();
        double relMinY = context.getDrawRelMinY();
        double relMaxY = context.getDrawRelMaxY();
        double relMinZ = context.getDrawRelMinZ();
        double relMaxZ = context.getDrawRelMaxZ();
        
        BaseTextureData baseData = context.getBaseData();
        int fallbackBrightness = context.getDrawBrightness();
        IBlockAccess blockAccess = context.getBlockAccess();
        int blockX = (int) x;
        int blockY = (int) y;
        int blockZ = (int) z;
        
        double minX = x + relMinX;
        double maxX = x + relMaxX;
        double minY = y + relMinY;
        double maxY = y + relMaxY;
        double minZ = z + relMinZ;
        double maxZ = z + relMaxZ;
        
        Tessellator tes = GTNHIntegrationHelper.getGTNHLibTessellator();
        
        if (rb.renderFromInside) {
            double t = minU;
            minU = maxU;
            maxU = t;
        }
        
        float r0, g0, b0, r1, g1, b1, r2, g2, b2, r3, g3, b3;
        int bright0, bright1, bright2, bright3;
        
        int biomeColor = -1;
        boolean useBiomeTint = baseData != null && baseData.getTinting() != null && blockAccess != null;
        if (useBiomeTint) {
            biomeColor = BiomeTintingHelper.getBiomeColor(baseData.getTinting(), blockAccess, blockX, blockY, blockZ);
        }
        
        if (useBiomeTint && biomeColor >= 0) {
            float bioR = (biomeColor >> 16 & 255) / 255.0F;
            float bioG = (biomeColor >> 8 & 255) / 255.0F;
            float bioB = (biomeColor & 255) / 255.0F;
            
            if (rb.enableAO) {
                r0 = bioR * rb.colorRedTopLeft;
                g0 = bioG * rb.colorGreenTopLeft;
                b0 = bioB * rb.colorBlueTopLeft;
                bright0 = baseData.isEmissive() ? FULL_BRIGHTNESS : rb.brightnessTopLeft;
                
                r1 = bioR * rb.colorRedTopRight;
                g1 = bioG * rb.colorGreenTopRight;
                b1 = bioB * rb.colorBlueTopRight;
                bright1 = baseData.isEmissive() ? FULL_BRIGHTNESS : rb.brightnessTopRight;
                
                r2 = bioR * rb.colorRedBottomLeft;
                g2 = bioG * rb.colorGreenBottomLeft;
                b2 = bioB * rb.colorBlueBottomLeft;
                bright2 = baseData.isEmissive() ? FULL_BRIGHTNESS : rb.brightnessBottomLeft;
                
                r3 = bioR * rb.colorRedBottomRight;
                g3 = bioG * rb.colorGreenBottomRight;
                b3 = bioB * rb.colorBlueBottomRight;
                bright3 = baseData.isEmissive() ? FULL_BRIGHTNESS : rb.brightnessBottomRight;
            } else {
                int bright = baseData.isEmissive() ? FULL_BRIGHTNESS : fallbackBrightness;
                r0 = bioR; g0 = bioG; b0 = bioB; bright0 = bright;
                r1 = bioR; g1 = bioG; b1 = bioB; bright1 = bright;
                r2 = bioR; g2 = bioG; b2 = bioB; bright2 = bright;
                r3 = bioR; g3 = bioG; b3 = bioB; bright3 = bright;
            }
        } else if (rb.enableAO) {
            r0 = rb.colorRedTopLeft;
            g0 = rb.colorGreenTopLeft;
            b0 = rb.colorBlueTopLeft;
            bright0 = rb.brightnessTopLeft;
            
            r1 = rb.colorRedTopRight;
            g1 = rb.colorGreenTopRight;
            b1 = rb.colorBlueTopRight;
            bright1 = rb.brightnessTopRight;
            
            r2 = rb.colorRedBottomLeft;
            g2 = rb.colorGreenBottomLeft;
            b2 = rb.colorBlueBottomLeft;
            bright2 = rb.brightnessBottomLeft;
            
            r3 = rb.colorRedBottomRight;
            g3 = rb.colorGreenBottomRight;
            b3 = rb.colorBlueBottomRight;
            bright3 = rb.brightnessBottomRight;
        } else {
            r0 = g0 = b0 = 1.0F; bright0 = fallbackBrightness;
            r1 = g1 = b1 = 1.0F; bright1 = fallbackBrightness;
            r2 = g2 = b2 = 1.0F; bright2 = fallbackBrightness;
            r3 = g3 = b3 = 1.0F; bright3 = fallbackBrightness;
        }
        
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
        }
        
        context.setDrewAny(true);
        context.popState();
        
        return PhaseResult.CONTINUE;
    }
}
