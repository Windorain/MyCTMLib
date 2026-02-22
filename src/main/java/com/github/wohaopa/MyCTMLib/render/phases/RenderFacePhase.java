package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.render.FaceRenderer;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.context.BlockRenderSubBranch;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

public class RenderFacePhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        TextureTypeData data = context.getTextureData();
        if (data == null) {
            return PhaseResult.CONTINUE;
        }
        
        int tileX = 0, tileY = 0;
        int width = 1, height = 1;
        BaseTextureData baseData = null;
        
        if (data instanceof BaseTextureData btd) {
            baseData = btd;
        } else if (data instanceof RandomTextureData rtd) {
            int[] pos = context.getTilePosition();
            if (pos != null) {
                tileX = pos[0];
                tileY = pos[1];
            }
            width = rtd.getColumns();
            height = rtd.getRows();
        } else if (data instanceof ConnectingTextureData ctd) {
            int[] pos = context.getTilePosition();
            if (pos != null) {
                tileX = pos[0];
                tileY = pos[1];
            }
            LayoutHandler handler = LayoutHandlers.get(ctd.getLayout());
            width = handler.getWidth();
            height = handler.getHeight();
        }
        
        FaceRenderer.drawFace(
            context.getRenderBlocks(),
            context.getX(),
            context.getY(),
            context.getZ(),
            context.getFace(),
            context.getDrawIcon(),
            tileX,
            tileY,
            width,
            height,
            context.getBrightness(),
            context.getRelMinX(),
            context.getRelMaxX(),
            context.getRelMinY(),
            context.getRelMaxY(),
            context.getRelMinZ(),
            context.getRelMaxZ(),
            baseData,
            context.getBlockAccess(),
            (int) context.getX(),
            (int) context.getY(),
            (int) context.getZ()
        );
        
        context.setDrewAny(true);
        
        if (context.getBlockSubBranch() == BlockRenderSubBranch.MODEL) {
            context.setCurrentElementIndex(context.getCurrentElementIndex() + 1);
        } else {
            context.pushState(RenderState.COMPLETE);
        }
        
        return PhaseResult.CONTINUE;
    }
}
