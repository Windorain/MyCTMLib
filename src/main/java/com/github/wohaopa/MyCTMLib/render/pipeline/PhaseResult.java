package com.github.wohaopa.MyCTMLib.render.pipeline;

public enum PhaseResult {
    CONTINUE,
    SKIP_REMAINING,
    FALLBACK_TO_LEGACY,
    FALLBACK_TO_VANILLA,
    ERROR
}
