package com.github.wohaopa.MyCTMLib.render.debug;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public interface PipelineDebugListener {

    void beforePhase(RenderState state, RenderContext context);

    void afterPhase(RenderState state, RenderContext context, PhaseResult result);

    void onPhaseError(RenderState state, RenderContext context, Exception e);

    boolean onFallback(FallbackStrategy strategy, RenderContext context);

    enum FallbackStrategy {
        LEGACY,
        VANILLA,
        NONE
    }
}
