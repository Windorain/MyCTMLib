package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.render.context.BlockRenderSubBranch;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.context.RenderPipelineBranch;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public class DecideRenderTypePhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        if (context.isItemRender()) {
            context.setBranch(RenderPipelineBranch.ITEM);
            context.pushState(RenderState.DECIDE_ITEM_BRANCH);
        } else {
            context.setBranch(RenderPipelineBranch.BLOCK);
            context.pushState(RenderState.DECIDE_BLOCK_BRANCH);
        }
        return PhaseResult.CONTINUE;
    }
}
