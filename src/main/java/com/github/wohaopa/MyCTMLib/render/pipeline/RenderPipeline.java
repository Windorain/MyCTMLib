package com.github.wohaopa.MyCTMLib.render.pipeline;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.debug.PipelineDebugListener;
import com.github.wohaopa.MyCTMLib.render.phases.CompletionPhase;
import com.github.wohaopa.MyCTMLib.render.phases.LegacyRenderPhase;
import com.github.wohaopa.MyCTMLib.render.phases.ModelRenderLoopPhase;
import com.github.wohaopa.MyCTMLib.render.phases.TextureRegRenderPhase;

public class RenderPipeline {
    private final ModelRenderLoopPhase modelRenderLoopPhase = new ModelRenderLoopPhase();
    private final TextureRegRenderPhase textureRegRenderPhase = new TextureRegRenderPhase();
    private final LegacyRenderPhase legacyRenderPhase = new LegacyRenderPhase();
    private final CompletionPhase completionPhase = new CompletionPhase();

    public boolean execute(RenderContext context) {
        context.setMainState(MainRenderState.INITIAL);
        context.setSubState(SubRenderState.NONE);

        while (context.getMainState() != MainRenderState.DONE) {
            MainRenderState mainState = context.getMainState();
            SubRenderState subState = context.getSubState();

            notifyStateStart(mainState, subState, context);

            switch (mainState) {
                case INITIAL:
                    context.setMainState(MainRenderState.CONTEXT_READY);
                    break;

                case CONTEXT_READY:
                    context.setMainState(MainRenderState.BRANCH_SELECTED);
                    break;

                case BRANCH_SELECTED:
                    context.getTextureData();
                    context.setMainState(MainRenderState.TEXTURE_RESOLVED);
                    break;

                case TEXTURE_RESOLVED:
                    if (context.isItemRender()) {
                        context.setSubState(SubRenderState.ITEM_RENDER);
                    } else if (context.hasElements()) {
                        context.setSubState(SubRenderState.MODEL_RENDER_LOOP);
                    } else if (shouldUseLegacy(context)) {
                        context.setSubState(SubRenderState.LEGACY_RENDER);
                    } else {
                        context.setSubState(SubRenderState.TEXTURE_REG_RENDER);
                    }
                    context.setMainState(MainRenderState.RENDERING);
                    break;

                case RENDERING:
                    switch (subState) {
                        case MODEL_RENDER_LOOP:
                            modelRenderLoopPhase.process(context);
                            break;
                        case TEXTURE_REG_RENDER:
                            textureRegRenderPhase.process(context);
                            break;
                        case LEGACY_RENDER:
                            legacyRenderPhase.process(context);
                            break;
                        case ITEM_RENDER:
                            break;
                        case NONE:
                            break;
                    }
                    context.setMainState(MainRenderState.COMPLETION);
                    context.setSubState(SubRenderState.NONE);
                    break;

                case COMPLETION:
                    completionPhase.process(context);
                    context.setMainState(MainRenderState.DONE);
                    break;

                case DONE:
                    break;
            }

            notifyStateEnd(mainState, subState, context);
        }
        return true;
    }

    private boolean shouldUseLegacy(RenderContext context) {
        String iconName = context.getIconName();
        return iconName != null && Textures.contain(iconName);
    }

    private void notifyStateStart(MainRenderState main, SubRenderState sub, RenderContext context) {
        PipelineDebugListener listener = context.getDebugListener();
        if (listener != null) {
            listener.onStateStart(main, sub, context);
        }
    }

    private void notifyStateEnd(MainRenderState main, SubRenderState sub, RenderContext context) {
        PipelineDebugListener listener = context.getDebugListener();
        if (listener != null) {
            listener.onStateEnd(main, sub, context);
        }
    }
}
