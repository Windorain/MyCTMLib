package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public class DecideItemBranchPhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        context.popState();
        context.pushState(RenderState.PREPARE_TEXTURE_ICON);
        return PhaseResult.CONTINUE;
    }
}
