package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public class RenderLegacyPhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        boolean result = Textures.renderWorldBlock(
            context.getRenderBlocks(),
            context.getBlockAccess(),
            context.getBlock(),
            context.getX(),
            context.getY(),
            context.getZ(),
            context.getOriginalIcon(),
            context.getFace()
        );
        
        context.setDrewAny(result);
        context.pushState(RenderState.COMPLETE);
        return PhaseResult.CONTINUE;
    }
}
