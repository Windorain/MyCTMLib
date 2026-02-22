package com.github.wohaopa.MyCTMLib.render.pipeline;

public class RenderPipelineException extends RuntimeException {
    
    private final FallbackStrategy fallback;
    
    public RenderPipelineException(String message, FallbackStrategy fallback) {
        super(message);
        this.fallback = fallback;
    }
    
    public RenderPipelineException(String message, Throwable cause, FallbackStrategy fallback) {
        super(message, cause);
        this.fallback = fallback;
    }
    
    public FallbackStrategy getFallback() {
        return fallback;
    }
    
    public enum FallbackStrategy {
        LEGACY,
        VANILLA,
        NONE
    }
}
