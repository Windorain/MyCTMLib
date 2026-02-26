package com.github.wohaopa.MyCTMLib.render.util;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;

/**
 * Model 数据查询工具类
 * 
 * 职责：查询 Model 相关数据（中间参数）
 * - findModelId: 查询 modelId
 * - findModelData: 查询 modelData
 * - findElements: 查询 elements
 * 
 * @author MyCTMLib
 * @since 1.0.0
 */
public final class ModelUtil {

    private ModelUtil() {}

    /**
     * 查询 modelId
     * 
     * @param block 方块
     * @param meta 元数据
     * @return modelId，找不到返回 null
     */
    public static String findModelId(Block block, int meta) {
        String blockId = getBlockId(block);
        if (blockId == null) return null;

        return BlockStateRegistry.getInstance().getModelId(blockId, meta);
    }

    /**
     * 查询 modelData
     * 
     * @param modelId model 标识
     * @return ModelData，找不到返回 null
     */
    public static ModelData findModelData(String modelId) {
        if (modelId == null) return null;
        return ModelRegistry.getInstance().get(modelId);
    }

    /**
     * 查询 elements
     * 
     * @param modelData model 数据
     * @param face 面方向
     * @return elements 列表，找不到或为空返回 null
     */
    public static List<ModelElement> findElements(ModelData modelData, ForgeDirection face) {
        if (modelData == null) return null;

        List<ModelElement> elements = CTMRenderEntry.getElementsWithFace(modelData, face);
        return elements.isEmpty() ? null : elements;
    }

    private static String getBlockId(Block block) {
        return block != null ? CTMRenderEntry.getBlockId(block) : null;
    }
}
