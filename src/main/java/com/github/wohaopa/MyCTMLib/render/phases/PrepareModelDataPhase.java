package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public class PrepareModelDataPhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        // 触发模型数据懒加载（MODEL 分支专用）
        context.ensureModelDataLoaded();
        
        context.popState();
        context.pushState(RenderState.ELEMENT_LOOP_CONTROL);
        return PhaseResult.CONTINUE;
    }
}
