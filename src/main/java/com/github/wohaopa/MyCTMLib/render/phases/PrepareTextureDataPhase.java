package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.context.BlockRenderSubBranch;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public class PrepareTextureDataPhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        String iconName = context.getIconName();
        if (iconName != null && context.getTextureData() == null) {
            context.setTextureData(CTMRenderEntry.getConnectingData(iconName));
        }
        
        context.pushState(RenderState.CALCULATE_TEXTURE);
        return PhaseResult.CONTINUE;
    }
}
