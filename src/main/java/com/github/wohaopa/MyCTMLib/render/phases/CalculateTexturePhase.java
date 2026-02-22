package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.FastRandom;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.render.ConnectionState;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/**
 * 计算纹理 UV 阶段：根据 TextureTypeData 计算 tile 位置和 UV 坐标。
 * 
 * 数据流：
 * 1. 获取 IIcon（从 drawIcon 或 originalIcon）
 * 2. instanceof 判断类型，计算 tileX, tileY, gridW, gridH
 * 3. 统一公式计算 UV
 */
public class CalculateTexturePhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        // 阶段 1：获取 IIcon
        IIcon icon = context.getDrawIcon();
        if (icon == null) {
            icon = context.getOriginalIcon();
        }
        
        TextureTypeData data = context.getTextureData();
        if (data == null) {
            context.popState();
            context.pushState(RenderState.CALCULATE_ELEMENT_BOUNDS);
            return PhaseResult.CONTINUE;
        }
        
        // 阶段 2：计算 tile 位置
        int tileX = 0, tileY = 0, gridW = 1, gridH = 1;
        BaseTextureData baseData = null;
        
        if (data instanceof BaseTextureData btd) {
            baseData = btd;
        } else if (data instanceof RandomTextureData rtd) {
            long worldSeed = (context.getBlockAccess() instanceof World w) 
                ? w.getSeed() : 0;
            int randomIndex = FastRandom.getRandomIndex(
                worldSeed, 
                (int) context.getX(), 
                (int) context.getY(), 
                (int) context.getZ(), 
                rtd.getCount());
            context.setRandomIndex(randomIndex);
            tileX = randomIndex % rtd.getColumns();
            tileY = randomIndex / rtd.getColumns();
            gridW = rtd.getColumns();
            gridH = rtd.getRows();
        } else if (data instanceof ConnectingTextureData ctd) {
            ConnectionPredicate predicate = context.getConnectionPredicate();
            if (predicate == null) {
                predicate = PredicateRegistry.defaultPredicate();
            }
            int mask = ConnectionState.computeMask(
                context.getBlockAccess(),
                (int) context.getX(),
                (int) context.getY(),
                (int) context.getZ(),
                context.getFace(),
                context.getBlock(),
                context.getMeta(),
                predicate);
            context.setConnectionMask(mask);
            
            LayoutHandler handler = LayoutHandlers.get(ctd.getLayout());
            int[] tilePos = handler.getTilePosition(mask);
            tileX = tilePos[0];
            tileY = tilePos[1];
            gridW = handler.getWidth();
            gridH = handler.getHeight();
        }
        
        // 阶段 3：统一 UV 计算
        double uRange = icon.getMaxU() - icon.getMinU();
        double vRange = icon.getMaxV() - icon.getMinV();
        
        context.setDrawMinU(icon.getMinU() + uRange * tileX / gridW);
        context.setDrawMaxU(icon.getMinU() + uRange * (tileX + 1) / gridW);
        context.setDrawMinV(icon.getMinV() + vRange * tileY / gridH);
        context.setDrawMaxV(icon.getMinV() + vRange * (tileY + 1) / gridH);
        
        if (baseData != null) {
            context.setBaseData(baseData);
        }
        
        // 阶段 4：转移状态
        context.popState();
        context.pushState(RenderState.CALCULATE_ELEMENT_BOUNDS);
        return PhaseResult.CONTINUE;
    }
}
