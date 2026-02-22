package com.github.wohaopa.MyCTMLib.render.pipeline;

public enum RenderState {
    
    INIT_CONTEXT(PhaseType.INIT),
    
    DECIDE(PhaseType.DECIDE),
    
    PREPARE_MODEL_DATA(PhaseType.PREPARE),
    PREPARE_TEXTURE_ICON(PhaseType.PREPARE),
    PREPARE_TEXTURE_DATA(PhaseType.PREPARE),
    PREPARE_ELEMENT_DATA(PhaseType.PREPARE),
    PREPARE_LEGACY_DATA(PhaseType.PREPARE),
    
    CALCULATE_TEXTURE(PhaseType.CALCULATE),
    CALCULATE_ELEMENT_BOUNDS(PhaseType.CALCULATE),
    
    ELEMENT_LOOP_CONTROL(PhaseType.CONTROL),
    
    RENDER_FACE(PhaseType.RENDER),
    RENDER_LEGACY(PhaseType.RENDER),
    
    COMPLETE(PhaseType.COMPLETE),
    COMPLETE_DEBUG(PhaseType.COMPLETE),
    CHECK_RENDER_LAYER(PhaseType.COMPLETE),
    
    DONE(PhaseType.SPECIAL);
    
    private final PhaseType type;
    
    RenderState(PhaseType type) {
        this.type = type;
    }
    
    public PhaseType getType() {
        return type;
    }
}
