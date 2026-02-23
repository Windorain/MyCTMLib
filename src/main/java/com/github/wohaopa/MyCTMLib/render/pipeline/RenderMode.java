package com.github.wohaopa.MyCTMLib.render.pipeline;

/**
 * 渲染模式枚举
 */
public enum RenderMode {
    /** 使用新管线（Group 组合） */
    PIPELINE,
    
    /** 使用旧版渲染器（Textures.renderWorldBlock） */
    LEGACY_RENDERER,
    
    /** fallback 到原版/Vanilla */
    FALLBACK
}
