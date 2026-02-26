package com.github.wohaopa.MyCTMLib.render.domain;

import java.util.List;

import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.util.ModelUtil;

public final class ModelDomain {

    private ModelDomain() {}

    public static boolean resolve(RenderContext ctx) {
        String modelId = ModelUtil.findModelId(ctx.getBlock(), ctx.getMeta());
        if (modelId == null) {
            ctx.warn("MODEL: No modelId found for block " + ctx.getBlock() + " meta " + ctx.getMeta());
            return false;
        }
        ctx.setModelId(modelId);

        ModelData modelData = ModelUtil.findModelData(modelId);
        if (modelData == null) {
            ctx.warn("MODEL: No modelData found for modelId: " + modelId);
            return false;
        }
        ctx.setModelData(modelData);

        List<ModelElement> elements = ModelUtil.findElements(modelData, ctx.getFace());
        if (elements == null) {
            ctx.warn("MODEL: No elements found for face " + ctx.getFace());
            return false;
        }
        ctx.setElements(elements);

        return true;
    }
}
