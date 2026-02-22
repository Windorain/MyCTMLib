package com.github.wohaopa.MyCTMLib.render.pipeline;

import java.util.HashMap;
import java.util.Map;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.debug.PipelineDebugListener;
import com.github.wohaopa.MyCTMLib.render.debug.RenderPipelineDebugCache;
import com.github.wohaopa.MyCTMLib.render.phases.CalculateElementBoundsPhase;
import com.github.wohaopa.MyCTMLib.render.phases.CalculateTexturePhase;
import com.github.wohaopa.MyCTMLib.render.phases.CompletePhase;
import com.github.wohaopa.MyCTMLib.render.phases.DecideBlockBranchPhase;
import com.github.wohaopa.MyCTMLib.render.phases.DecideItemBranchPhase;
import com.github.wohaopa.MyCTMLib.render.phases.DecideLegacyFallbackPhase;
import com.github.wohaopa.MyCTMLib.render.phases.DecideRenderTypePhase;
import com.github.wohaopa.MyCTMLib.render.phases.ElementLoopControlPhase;
import com.github.wohaopa.MyCTMLib.render.phases.InitContextPhase;
import com.github.wohaopa.MyCTMLib.render.phases.PrepareElementDataPhase;
import com.github.wohaopa.MyCTMLib.render.phases.PrepareLegacyDataPhase;
import com.github.wohaopa.MyCTMLib.render.phases.PrepareModelDataPhase;
import com.github.wohaopa.MyCTMLib.render.phases.PrepareTextureDataPhase;
import com.github.wohaopa.MyCTMLib.render.phases.PrepareTextureIconPhase;
import com.github.wohaopa.MyCTMLib.render.phases.RenderFacePhase;
import com.github.wohaopa.MyCTMLib.render.phases.RenderLegacyPhase;

public class RenderPipeline {

    private final Map<RenderState, PipelinePhase> phaseMap = new HashMap<>();

    public RenderPipeline() {
        phaseMap.put(RenderState.INIT_CONTEXT, new InitContextPhase());
        phaseMap.put(RenderState.DECIDE_RENDER_TYPE, new DecideRenderTypePhase());
        phaseMap.put(RenderState.DECIDE_BLOCK_BRANCH, new DecideBlockBranchPhase());
        phaseMap.put(RenderState.DECIDE_ITEM_BRANCH, new DecideItemBranchPhase());
        phaseMap.put(RenderState.DECIDE_LEGACY_FALLBACK, new DecideLegacyFallbackPhase());
        phaseMap.put(RenderState.PREPARE_MODEL_DATA, new PrepareModelDataPhase());
        phaseMap.put(RenderState.PREPARE_TEXTURE_ICON, new PrepareTextureIconPhase());
        phaseMap.put(RenderState.PREPARE_TEXTURE_DATA, new PrepareTextureDataPhase());
        phaseMap.put(RenderState.PREPARE_ELEMENT_DATA, new PrepareElementDataPhase());
        phaseMap.put(RenderState.PREPARE_LEGACY_DATA, new PrepareLegacyDataPhase());
        phaseMap.put(RenderState.CALCULATE_TEXTURE, new CalculateTexturePhase());
        phaseMap.put(RenderState.CALCULATE_ELEMENT_BOUNDS, new CalculateElementBoundsPhase());
        phaseMap.put(RenderState.ELEMENT_LOOP_CONTROL, new ElementLoopControlPhase());
        phaseMap.put(RenderState.RENDER_FACE, new RenderFacePhase());
        phaseMap.put(RenderState.RENDER_LEGACY, new RenderLegacyPhase());
        phaseMap.put(RenderState.COMPLETE, new CompletePhase());
    }

    public boolean execute(RenderContext context) {
        PipelineDebugTrace trace = null;

        if (MyCTMLib.debugMode) {
            trace = new PipelineDebugTrace();
            if (context.getDebugListener() == null) {
                context.setDebugListener(trace);
            }
            trace.addStep("New pipeline started");
        }

        context.pushState(RenderState.INIT_CONTEXT);

        while (context.getCurrentState() != RenderState.DONE) {
            RenderState currentState = context.getCurrentState();
            if (currentState == null) {
                break;
            }

            notifyBeforePhase(currentState, context);

            try {
                PipelinePhase phase = phaseMap.get(currentState);
                PhaseResult result = PhaseResult.CONTINUE;

                if (phase != null) {
                    result = phase.process(context);
                }

                notifyAfterPhase(currentState, context, result);
                handlePhaseResult(result, context);

            } catch (RenderPipelineException e) {
                notifyOnPhaseError(currentState, context, e);
                handleFallback(e.getFallback(), context);
            }

            if (context.getCurrentState() == currentState) {
                context.popState();
            }
        }

        boolean drewAny = context.isDrewAny();

        if (MyCTMLib.debugMode && trace != null) {
            trace.addStep("New pipeline drewAny: " + drewAny);

            if (!drewAny) {
                trace.addStep("Falling back to tryRender()");
                boolean tryRenderResult = CTMRenderEntry.tryRender(
                    context.getRenderBlocks(),
                    context.getBlockAccess(),
                    context.getBlock(),
                    context.getX(),
                    context.getY(),
                    context.getZ(),
                    context.getOriginalIcon(),
                    context.getFace());
                trace.addStep("tryRender() result: " + tryRenderResult);
                drewAny = tryRenderResult;

                if (!drewAny) {
                    trace.setDegradationReason("New pipeline and tryRender() both failed, falling back to vanilla");
                }
            }

            int x = (int) context.getX();
            int y = (int) context.getY();
            int z = (int) context.getZ();
            RenderPipelineDebugCache.record(x, y, z, context.getFace(), trace);
        } else {
            if (!drewAny) {
                drewAny = CTMRenderEntry.tryRender(
                    context.getRenderBlocks(),
                    context.getBlockAccess(),
                    context.getBlock(),
                    context.getX(),
                    context.getY(),
                    context.getZ(),
                    context.getOriginalIcon(),
                    context.getFace());
            }
        }

        return drewAny;
    }

    private void handlePhaseResult(PhaseResult result, RenderContext context) {
        switch (result) {
            case CONTINUE:
                break;
            case SKIP_REMAINING:
                context.pushState(RenderState.COMPLETE);
                break;
            case FALLBACK_TO_LEGACY:
                context.pushState(RenderState.PREPARE_LEGACY_DATA);
                break;
            case FALLBACK_TO_VANILLA:
                context.setDrewAny(false);
                context.pushState(RenderState.DONE);
                break;
            case ERROR:
                context.setDrewAny(false);
                context.pushState(RenderState.DONE);
                break;
        }
    }

    private void handleFallback(RenderPipelineException.FallbackStrategy fallback, RenderContext context) {
        switch (fallback) {
            case LEGACY:
                context.pushState(RenderState.PREPARE_LEGACY_DATA);
                break;
            case VANILLA:
            case NONE:
            default:
                context.setDrewAny(false);
                context.pushState(RenderState.DONE);
                break;
        }
    }

    private void notifyBeforePhase(RenderState state, RenderContext context) {
        PipelineDebugListener listener = context.getDebugListener();
        if (listener != null) {
            listener.beforePhase(state, context);
        }
    }

    private void notifyAfterPhase(RenderState state, RenderContext context, PhaseResult result) {
        PipelineDebugListener listener = context.getDebugListener();
        if (listener != null) {
            listener.afterPhase(state, context, result);
        }
    }

    private void notifyOnPhaseError(RenderState state, RenderContext context, Exception e) {
        PipelineDebugListener listener = context.getDebugListener();
        if (listener != null) {
            listener.onPhaseError(state, context, e);
        }
    }
}
