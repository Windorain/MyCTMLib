package com.github.wohaopa.MyCTMLib.render.debug;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.MainRenderState;
import com.github.wohaopa.MyCTMLib.render.pipeline.SubRenderState;

public interface PipelineDebugListener {

    void onStateStart(MainRenderState mainState, SubRenderState subState, RenderContext context);

    void onStateEnd(MainRenderState mainState, SubRenderState subState, RenderContext context);
}
