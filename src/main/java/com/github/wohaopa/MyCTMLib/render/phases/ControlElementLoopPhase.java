package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public class ControlElementLoopPhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        if (context.getCurrentElementIndex() >= context.getElements().size()) {
            context.popState();
            context.pushState(RenderState.COMPLETE);
            return PhaseResult.CONTINUE;
        }
        
        ModelElement element = context.getElements().get(context.getCurrentElementIndex());
        context.setCurrentElement(element);
        
        context.pushState(RenderState.RENDER_FACE);
        context.pushState(RenderState.CALCULATE_ELEMENT_BOUNDS);
        context.pushState(RenderState.CALCULATE_TEXTURE);
        context.pushState(RenderState.PREPARE_TEXTURE_DATA);
        context.pushState(RenderState.PREPARE_ELEMENT_DATA);
        
        return PhaseResult.CONTINUE;
    }
}
