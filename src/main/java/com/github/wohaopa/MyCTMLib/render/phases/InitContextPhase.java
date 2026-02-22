package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public class InitContextPhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        if (MyCTMLib.debugMode && context.getDebugListener() == null) {
            context.setDebugListener(new PipelineDebugTrace());
        }
        context.setDrewAny(false);
        context.popState();
        context.pushState(RenderState.DECIDE);
        return PhaseResult.CONTINUE;
    }
}
