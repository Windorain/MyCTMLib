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
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

import net.minecraft.world.World;

public class CalculateTexturePhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        TextureTypeData data = context.getTextureData();
        
        if (data instanceof RandomTextureData randomData) {
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
            context.setTilePosition(new int[]{
                randomIndex % randomData.getColumns(),
                randomIndex / randomData.getColumns()
            });
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
            context.setTilePosition(handler.getTilePosition(mask));
        }
        
        context.pushState(RenderState.CALCULATE_ELEMENT_BOUNDS);
        return PhaseResult.CONTINUE;
    }
}
