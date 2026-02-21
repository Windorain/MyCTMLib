package com.github.wohaopa.MyCTMLib.render.strategy;

import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

public interface TextureRenderer {
    boolean render(RenderContext context);
    boolean render(RenderContext context, ModelElement element);
}
