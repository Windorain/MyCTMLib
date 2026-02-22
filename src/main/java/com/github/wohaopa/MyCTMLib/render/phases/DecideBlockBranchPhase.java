package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.render.context.BlockRenderSubBranch;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public class DecideBlockBranchPhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        if (context.hasElements()) {
            context.setBlockSubBranch(BlockRenderSubBranch.MODEL);
            context.pushState(RenderState.PREPARE_MODEL_DATA);
        } else if (shouldUseLegacy(context)) {
            context.setBlockSubBranch(BlockRenderSubBranch.LEGACY);
            context.pushState(RenderState.PREPARE_LEGACY_DATA);
        } else {
            context.setBlockSubBranch(BlockRenderSubBranch.TEXTURE_RELOC);
            context.pushState(RenderState.PREPARE_TEXTURE_ICON);
        }
        return PhaseResult.CONTINUE;
    }

    private boolean shouldUseLegacy(RenderContext context) {
        String iconName = context.getIconName();
        return iconName != null && Textures.contain(iconName);
    }
}
