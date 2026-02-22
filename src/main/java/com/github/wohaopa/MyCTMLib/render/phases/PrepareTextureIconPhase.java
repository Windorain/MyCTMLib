package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.context.BlockRenderMode;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;

public class PrepareTextureIconPhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        if (context.getDrawIcon() == null) {
            String iconName = context.getIconName();
            if (iconName != null) {
                context.setDrawIcon(TextureRegistry.getInstance().getIcon(iconName));
            }
            if (context.getDrawIcon() == null) {
                context.setDrawIcon(context.getOriginalIcon());
            }
        }
        
        if (context.getBlockSubBranch() == BlockRenderMode.MODEL) {
            context.popState();
            context.pushState(RenderState.ELEMENT_LOOP_CONTROL);
        } else {
            context.popState();
            context.pushState(RenderState.PREPARE_TEXTURE_DATA);
        }
        return PhaseResult.CONTINUE;
    }
}
