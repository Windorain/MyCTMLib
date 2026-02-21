package com.github.wohaopa.MyCTMLib.render.strategy;

import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

public class ConnectingTextureRenderer implements TextureRenderer {

    @Override
    public boolean render(RenderContext context) {
        return true;
    }

    @Override
    public boolean render(RenderContext context, ModelElement element) {
        return true;
    }
}
