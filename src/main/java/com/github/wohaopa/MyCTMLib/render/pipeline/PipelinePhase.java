package com.github.wohaopa.MyCTMLib.render.pipeline;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

public interface PipelinePhase {
    void process(RenderContext context);
}
