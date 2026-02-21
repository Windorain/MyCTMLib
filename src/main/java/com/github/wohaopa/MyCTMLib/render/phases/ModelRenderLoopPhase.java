package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;

public class ModelRenderLoopPhase implements PipelinePhase {

    @Override
    public void process(RenderContext context) {
        context.resetElementIndex();
        do {
            ModelElement element = context.getCurrentElement();
            TextureTypeData data = resolveElementTexture(element, context);
            context.setCurrentElementTextureData(data);
            renderElement(element, context);
        } while (context.moveToNextElement());
    }

    private TextureTypeData resolveElementTexture(ModelElement element, RenderContext context) {
        return null;
    }

    private void renderElement(ModelElement element, RenderContext context) {}
}
