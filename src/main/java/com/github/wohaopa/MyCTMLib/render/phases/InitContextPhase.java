package com.github.wohaopa.MyCTMLib.render.phases;

import java.util.List;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public class InitContextPhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        if (MyCTMLib.debugMode && context.getDebugListener() == null) {
            context.setDebugListener(new PipelineDebugTrace());
        }
        context.setDrewAny(false);
        
        // 查询模型数据
        String blockId = CTMRenderEntry.getBlockId(context.getBlock());
        if (blockId != null) {
            int meta = context.getMeta();
            String modelId = BlockStateRegistry.getInstance().getModelId(blockId, meta);
            if (modelId != null) {
                context.setModelId(modelId);
                ModelData modelData = com.github.wohaopa.MyCTMLib.model.ModelRegistry.getInstance().get(modelId);
                if (modelData != null) {
                    context.setModelData(modelData);
                    List<ModelElement> elements = CTMRenderEntry.getElementsWithFace(modelData, context.getFace());
                    context.setElements(elements);
                    
                    // 设置 connectionPredicate
                    ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
                    if (!elements.isEmpty()) {
                        com.github.wohaopa.MyCTMLib.model.ModelFace firstFace = elements.get(0).getFace(context.getFace());
                        if (firstFace != null && firstFace.getConnectionKey() != null) {
                            ConnectionPredicate p = PredicateRegistry.getPredicate(
                                firstFace.getConnectionKey(), modelData.getConnections());
                            if (p != null) predicate = p;
                        }
                    }
                    context.setConnectionPredicate(predicate);
                }
            }
        }
        
        // 设置 brightness
        int brightness = context.getBlock().getMixedBrightnessForBlock(
            context.getBlockAccess(), 
            (int) context.getX(), 
            (int) context.getY(), 
            (int) context.getZ());
        context.setDrawBrightness(brightness);
        
        // 设置边界（从 RenderBlocks 复制）
        if (context.getRenderBlocks() != null) {
            context.setDrawRelMinX(context.getRenderBlocks().renderMinX);
            context.setDrawRelMaxX(context.getRenderBlocks().renderMaxX);
            context.setDrawRelMinY(context.getRenderBlocks().renderMinY);
            context.setDrawRelMaxY(context.getRenderBlocks().renderMaxY);
            context.setDrawRelMinZ(context.getRenderBlocks().renderMinZ);
            context.setDrawRelMaxZ(context.getRenderBlocks().renderMaxZ);
        }
        
        context.popState();
        context.pushState(RenderState.DECIDE);
        return PhaseResult.CONTINUE;
    }
}
