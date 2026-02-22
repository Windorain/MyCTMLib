package com.github.wohaopa.MyCTMLib.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.debug.PipelineDebugListener;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public final class PipelineDebugTrace implements PipelineDebugListener {

    private final List<String> steps = new ArrayList<>();
    private String degradationReason;
    private String predicateUsed;
    private int[] tilePos;
    private int[] connectionBits;
    private Boolean texRegTexMapSynced;
    private String texRegGetIconLookupKey;
    private String drawSpriteName;
    private String drawSpriteLoaded;

    public PipelineDebugTrace() {}

    public void addStep(String step) {
        if (step != null && !step.isEmpty()) {
            steps.add(step);
        }
    }

    public void setDegradationReason(String reason) {
        this.degradationReason = reason;
    }

    public void setPredicateUsed(String predicate) {
        this.predicateUsed = predicate;
    }

    public void setTilePos(int tileX, int tileY) {
        this.tilePos = new int[] { tileX, tileY };
    }

    public void setConnectionBits(int mask) {
        this.connectionBits = new int[8];
        for (int d = 0; d < 8; d++) {
            connectionBits[d] = (mask & (1 << d)) != 0 ? 1 : 0;
        }
    }

    public void setTexRegTexMapSync(boolean synced, String lookupKey) {
        this.texRegTexMapSynced = synced;
        this.texRegGetIconLookupKey = lookupKey;
    }

    public Boolean getTexRegTexMapSynced() {
        return texRegTexMapSynced;
    }

    public String getTexRegGetIconLookupKey() {
        return texRegGetIconLookupKey;
    }

    public void setDrawSpriteInfo(String iconName, int width, int height) {
        this.drawSpriteName = iconName;
        this.drawSpriteLoaded = (width > 0 && height > 0) ? (width + "x" + height) : "0x0(unloaded)";
    }

    public String getDrawSpriteName() {
        return drawSpriteName;
    }

    public String getDrawSpriteLoaded() {
        return drawSpriteLoaded;
    }

    public List<String> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public String getDegradationReason() {
        return degradationReason;
    }

    public String getPredicateUsed() {
        return predicateUsed;
    }

    public int[] getTilePos() {
        return tilePos;
    }

    public int[] getConnectionBits() {
        return connectionBits;
    }

    @Override
    public void beforePhase(RenderState state, RenderContext context) {
        addStep("Phase START: " + state);
    }

    @Override
    public void afterPhase(RenderState state, RenderContext context, PhaseResult result) {
        addStep("Phase END: " + state + " -> " + result);
    }

    @Override
    public void onPhaseError(RenderState state, RenderContext context, Exception e) {
        addStep("Phase ERROR: " + state + " - " + e.getMessage());
    }

    @Override
    public boolean onFallback(FallbackStrategy strategy, RenderContext context) {
        addStep("Fallback: " + strategy);
        return true;
    }
}
