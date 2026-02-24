package com.github.wohaopa.MyCTMLib.render.domain;

import java.util.List;

import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

/**
 * Model 数据域
 * 
 * 职责：
 * - findModelId: 查询并设置 modelId
 * - findModelData: 查询并设置 modelData
 * - findElements: 查询并设置 elements
 */
public final class ModelDomain {

    private ModelDomain() {}

    /**
     * 查询并设置 modelId
     * 
     * @return true 如果找到了 modelId
     */
    public static boolean findModelId(RenderContext ctx) {
        String blockId = getBlockId(ctx.getBlock());
        if (blockId == null) return false;

        String modelId = BlockStateRegistry.getInstance()
            .getModelId(blockId, ctx.getMeta());
        if (modelId == null) return false;

        ctx.setModelId(modelId);
        return true;
    }

    /**
     * 查询并设置 modelData
     * 
     * @return true 如果找到了 modelData
     */
    public static boolean findModelData(RenderContext ctx) {
        String modelId = ctx.getModelId();
        if (modelId == null) return false;

        ModelData modelData = ModelRegistry.getInstance()
            .get(modelId);
        if (modelData == null) return false;

        ctx.setModelData(modelData);
        return true;
    }

    /**
     * 查询并设置 elements
     * 
     * @return true 如果找到了非空的 elements
     */
    public static boolean findElements(RenderContext ctx) {
        ModelData modelData = ctx.getModelData();
        if (modelData == null) return false;

        List<ModelElement> elements = CTMRenderEntry.getElementsWithFace(modelData, ctx.getFace());
        if (elements.isEmpty()) return false;

        ctx.setElements(elements);
        return true;
    }

    private static String getBlockId(net.minecraft.block.Block block) {
        return block != null ? CTMRenderEntry.getBlockId(block) : null;
    }
}
