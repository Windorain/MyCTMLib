package com.github.wohaopa.MyCTMLib.render.pipeline;

/**
 * 渲染分支三元组枚举
 * 
 * 每个分支由三个维度组成：
 * - RenderObject: 渲染什么（MODEL/TEXTURE/BLOCK/ITEM/ENTITY）
 * - RenderLevel:  注入时机（FACE/BLOCK/ITEM/ENTITY）← 暂时预留
 * - RenderMode:   怎么渲染（PIPELINE/LEGACY_RENDERER/FALLBACK）
 */
public enum RenderBranch {
    /** Model 分支：多 Element 循环，使用新管线 */
    MODEL_ELEMENTS(RenderObject.MODEL, RenderLevel.BLOCK, RenderMode.PIPELINE),
    
    /** 纹理重定位：简单 UV 偏移，使用新管线 */
    TEXTURE_RELOC(RenderObject.TEXTURE, RenderLevel.FACE, RenderMode.PIPELINE),
    
    /** Legacy 分支：旧版渲染器 */
    LEGACY(RenderObject.BLOCK, RenderLevel.BLOCK, RenderMode.LEGACY_RENDERER),
    
    /** 物品渲染：单元素，全亮度，使用新管线 */
    ITEM(RenderObject.ITEM, RenderLevel.ITEM, RenderMode.PIPELINE),
    
    /** 实体渲染（预留） */
    ENTITY(RenderObject.MODEL, RenderLevel.ENTITY, RenderMode.PIPELINE),
    
    /** 无匹配，fallback 到原版 */
    NONE(RenderObject.NONE, RenderLevel.NONE, RenderMode.FALLBACK);
    
    private final RenderObject object;
    private final RenderLevel level;
    private final RenderMode mode;
    
    RenderBranch(RenderObject object, RenderLevel level, RenderMode mode) {
        this.object = object;
        this.level = level;
        this.mode = mode;
    }
    
    public RenderObject getObject() {
        return object;
    }
    
    public RenderLevel getLevel() {
        return level;
    }
    
    public RenderMode getMode() {
        return mode;
    }
}
