package com.github.wohaopa.MyCTMLib.render.pipeline;

/**
 * 渲染对象类型枚举
 */
public enum RenderObject {
    /** 模型（多 Element） */
    MODEL,

    /** 纹理（单图） */
    TEXTURE,

    /** 原版方块 */
    BLOCK,

    /** 物品 */
    ITEM,

    /** 实体（预留） */
    ENTITY,

    /** 无 */
    NONE
}
