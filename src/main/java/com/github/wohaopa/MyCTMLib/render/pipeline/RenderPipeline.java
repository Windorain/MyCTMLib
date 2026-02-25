package com.github.wohaopa.MyCTMLib.render.pipeline;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.domain.ModelDomain;
import com.github.wohaopa.MyCTMLib.render.domain.TextureDomain;
import com.github.wohaopa.MyCTMLib.render.pipelines.BaseTilePipeline;
import com.github.wohaopa.MyCTMLib.render.pipelines.ConnectingTilePipeline;
import com.github.wohaopa.MyCTMLib.render.pipelines.RandomTilePipeline;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;

/**
 * 渲染管线（线性管道）
 * 
 * 执行流程：INIT → DECIDE → RENDER → COMPLETE
 */
public class RenderPipeline {

    public boolean execute(RenderContext ctx) {
        ctx.info(() -> "=== RenderPipeline Started ===");
        ctx.reset();

        RenderBranch branch = decideRenderBranch(ctx);
        ctx.setRenderBranch(branch);
        ctx.info(() -> "DECIDE: Using branch: " + branch);

        switch (branch) {
            case MODEL_ELEMENTS -> executeModelBranch(ctx);
            case TEXTURE_RELOC -> executeTextureRelocBranch(ctx);
            case LEGACY -> executeLegacyBranch(ctx);
            case ITEM -> executeItemBranch(ctx);
            case ENTITY, NONE -> {
                ctx.setDrewAny(false);
            }
        }

        ctx.info(() -> "COMPLETE: drewAny=" + ctx.isDrewAny());

        boolean drewAny = ctx.isDrewAny();
        if (!drewAny) {
            ctx.warn(() -> "New pipeline failed, falling back to tryRender()");
            drewAny = CTMRenderEntry.tryRender(
                ctx.getRenderBlocks(),
                ctx.getBlockAccess(),
                ctx.getBlock(),
                ctx.getBlockX(),
                ctx.getBlockY(),
                ctx.getBlockZ(),
                ctx.getOriginalIcon(),
                ctx.getFace());
        }

        return drewAny;
    }

    private RenderBranch decideRenderBranch(RenderContext ctx) {
        if (ctx.getBlockAccess() == null) {
            return RenderBranch.ITEM;
        }

        if (ModelDomain.findModelId(ctx) && ModelDomain.findModelData(ctx) && ModelDomain.findElements(ctx)) {
            return RenderBranch.MODEL_ELEMENTS;
        }

        if (TextureDomain.findTextureReloc(ctx)) {
            return RenderBranch.TEXTURE_RELOC;
        }

        String iconName = TextureKeyNormalizer.normalizeIconName(
            ctx.getOriginalIcon()
                .getIconName());
        if (shouldUseLegacy(iconName)) {
            return RenderBranch.LEGACY;
        }

        return RenderBranch.NONE;
    }

    private boolean shouldUseLegacy(String iconName) {
        return iconName != null && Textures.contain(iconName);
    }

    private void executeModelBranch(RenderContext ctx) {
        ctx.debug(
            () -> "MODEL: Looping through " + ctx.getElements()
                .size() + " elements");

        ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
        if (!ctx.getElements()
            .isEmpty()) {
            ModelFace firstFace = ctx.getElements()
                .get(0)
                .getFace(ctx.getFace());
            if (firstFace != null && firstFace.getConnectionKey() != null) {
                ConnectionPredicate p = PredicateRegistry.getPredicate(
                    firstFace.getConnectionKey(),
                    ctx.getModelData()
                        .getConnections());
                if (p != null) predicate = p;
            }
        }
        ctx.setConnectionPredicate(predicate);

        int index = 0;
        for (ModelElement element : ctx.getElements()) {
            ctx.setCurrentElement(element);
            ctx.setCurrentElementIndex(index++);
            ctx.resetPipelineFailed();

            if (!TextureDomain.resolveForElement(ctx)) {
                ctx.warn(() -> "RENDER: Failed to resolve texture for element " + ctx.getCurrentElementIndex());
                continue;
            }

            if (ctx.getTextureData() instanceof BaseTextureData) {
                BaseTilePipeline.execute(ctx);
            } else if (ctx.getTextureData() instanceof RandomTextureData) {
                RandomTilePipeline.execute(ctx);
            } else if (ctx.getTextureData() instanceof ConnectingTextureData) {
                ConnectingTilePipeline.execute(ctx);
            } else {
                ctx.debug(() -> "RENDER: Unknown texture data type: " + ctx.getTextureData());
                ctx.failPipeline("Unknown texture data type: " + ctx.getTextureData());
            }
        }
    }

    private void executeTextureRelocBranch(RenderContext ctx) {
        TextureTypeData data = ctx.getTextureData();
        if (data instanceof BaseTextureData) {
            BaseTilePipeline.execute(ctx);
        } else if (data instanceof RandomTextureData) {
            RandomTilePipeline.execute(ctx);
        } else if (data instanceof ConnectingTextureData) {
            ConnectingTilePipeline.execute(ctx);
        }
    }

    private void executeLegacyBranch(RenderContext ctx) {
        boolean result = Textures.renderWorldBlock(
            ctx.getRenderBlocks(),
            ctx.getBlockAccess(),
            ctx.getBlock(),
            ctx.getBlockX(),
            ctx.getBlockY(),
            ctx.getBlockZ(),
            ctx.getOriginalIcon(),
            ctx.getFace());
        ctx.setDrewAny(result);
    }

    private void executeItemBranch(RenderContext ctx) {
        BaseTilePipeline.execute(ctx);
    }
}
