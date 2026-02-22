package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public class CalculateElementBoundsPhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        context.setMinU(0);
        context.setMaxU(1);
        context.setMinV(0);
        context.setMaxV(1);
        
        context.pushState(RenderState.RENDER_FACE);
        return PhaseResult.CONTINUE;
    }
}
