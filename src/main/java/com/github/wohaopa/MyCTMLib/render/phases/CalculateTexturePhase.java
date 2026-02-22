package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.FastRandom;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.render.ConnectionState;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class CalculateTexturePhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        TextureTypeData data = context.getTextureData();
        IIcon icon = context.getDrawIcon();
        
        if (icon == null) {
            icon = context.getOriginalIcon();
        }
        
        if (data instanceof BaseTextureData baseData) {
            context.setBaseData(baseData);
            context.setDrawMinU(icon.getMinU());
            context.setDrawMaxU(icon.getMaxU());
            context.setDrawMinV(icon.getMinV());
            context.setDrawMaxV(icon.getMaxV());
        } else if (data instanceof RandomTextureData randomData) {
            long worldSeed = 0;
            if (context.getBlockAccess() instanceof World) {
                worldSeed = ((World) context.getBlockAccess()).getSeed();
            }
            int randomIndex = FastRandom.getRandomIndex(
                worldSeed, 
                (int) context.getX(), 
                (int) context.getY(), 
                (int) context.getZ(), 
                randomData.getCount());
            context.setRandomIndex(randomIndex);
            int tileX = randomIndex % randomData.getColumns();
            int tileY = randomIndex / randomData.getColumns();
            context.setTilePosition(new int[]{tileX, tileY});
            
            double uRange = icon.getMaxU() - icon.getMinU();
            double vRange = icon.getMaxV() - icon.getMinV();
            context.setDrawMinU(icon.getMinU() + uRange * tileX / randomData.getColumns());
            context.setDrawMaxU(icon.getMinU() + uRange * (tileX + 1) / randomData.getColumns());
            context.setDrawMinV(icon.getMinV() + vRange * tileY / randomData.getRows());
            context.setDrawMaxV(icon.getMinV() + vRange * (tileY + 1) / randomData.getRows());
        } else if (data instanceof ConnectingTextureData texData) {
            ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
            if (context.getConnectionPredicate() != null) {
                predicate = context.getConnectionPredicate();
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
            
            LayoutHandler handler = LayoutHandlers.get(texData.getLayout());
            int[] tilePos = handler.getTilePosition(mask);
            context.setTilePosition(tilePos);
            
            double uRange = icon.getMaxU() - icon.getMinU();
            double vRange = icon.getMaxV() - icon.getMinV();
            context.setDrawMinU(icon.getMinU() + uRange * tilePos[0] / handler.getWidth());
            context.setDrawMaxU(icon.getMinU() + uRange * (tilePos[0] + 1) / handler.getWidth());
            context.setDrawMinV(icon.getMinV() + vRange * tilePos[1] / handler.getHeight());
            context.setDrawMaxV(icon.getMinV() + vRange * (tilePos[1] + 1) / handler.getHeight());
        }
        
        context.popState();
        context.pushState(RenderState.CALCULATE_ELEMENT_BOUNDS);
        return PhaseResult.CONTINUE;
    }
}
