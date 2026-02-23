package com.github.wohaopa.MyCTMLib.render.pipeline;

/**
 * 渲染注入时机枚举（预留，暂时不使用）
 */
public enum RenderLevel {
    /** 面级别注入（每个面调用一次） */
    FACE,
    
    /** 方块级别注入（每个方块调用一次，内部循环 6 面） */
    BLOCK,
    
    /** 实体级别注入（实体渲染时） */
    ENTITY,
    
    /** 物品级别注入（物品渲染时） */
    ITEM,
    
    /** 不注入 */
    NONE
}
