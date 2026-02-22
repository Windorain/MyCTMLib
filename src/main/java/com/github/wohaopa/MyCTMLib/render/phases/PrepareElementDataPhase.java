package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;

public class PrepareElementDataPhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        ModelElement element = context.getCurrentElement();
        if (element != null) {
            ModelFace faceData = element.getFace(context.getFace());
            if (faceData != null && faceData.getTextureKey() != null) {
                String modelId = context.getModelId();
                String domain = modelId != null && modelId.indexOf(':') >= 0 
                    ? modelId.substring(0, modelId.indexOf(':')) 
                    : "minecraft";
                
                String texturePath = TextureKeyNormalizer.resolveTexturePath(
                    faceData.getTextureKey(), 
                    context.getModelData().getTextures());
                
                if (texturePath != null) {
                    String textureLookupKey = TextureKeyNormalizer.toCanonicalTextureKey(domain, texturePath);
                    context.setTextureData(CTMRenderEntry.getConnectingData(textureLookupKey));
                    
                    if (context.getDrawIcon() == null) {
                        context.setDrawIcon(TextureRegistry.getInstance().getIcon(textureLookupKey));
                    }
                    if (context.getDrawIcon() == null) {
                        context.setDrawIcon(context.getOriginalIcon());
                    }
                    
                    float[] f = element.getFrom(), t = element.getTo();
                    context.setDrawRelMinX(Math.min(f[0], t[0]) / 16.0);
                    context.setDrawRelMaxX(Math.max(f[0], t[0]) / 16.0);
                    context.setDrawRelMinY(Math.min(f[1], t[1]) / 16.0);
                    context.setDrawRelMaxY(Math.max(f[1], t[1]) / 16.0);
                    context.setDrawRelMinZ(Math.min(f[2], t[2]) / 16.0);
                    context.setDrawRelMaxZ(Math.max(f[2], t[2]) / 16.0);
                }
            }
        }
        
        context.popState();
        context.pushState(RenderState.CALCULATE_TEXTURE);
        return PhaseResult.CONTINUE;
    }
}
