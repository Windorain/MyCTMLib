package com.github.wohaopa.MyCTMLib.render.pipeline;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.quads.ElementQuadRenderer;
import com.github.wohaopa.MyCTMLib.render.quads.ItemQuadRenderer;
import com.github.wohaopa.MyCTMLib.render.quads.RenderBlocksQuadRenderer;
import com.github.wohaopa.MyCTMLib.render.util.ModelUtil;
import com.github.wohaopa.MyCTMLib.render.util.TextureUtil;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;

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

        // Model 分支：查询 model 数据
        String modelId = ModelUtil.findModelId(ctx.getBlock(), ctx.getMeta());
        if (modelId != null) {
            ctx.setModelId(modelId);
            var modelData = ModelUtil.findModelData(modelId);
            if (modelData != null) {
                ctx.setModelData(modelData);
                var elements = ModelUtil.findElements(modelData, ctx.getFace());
                if (elements != null) {
                    ctx.setElements(elements);
                    return RenderBranch.MODEL_ELEMENTS;
                }
            }
        }

        // TextureReloc 分支：查找 CTM 重定向
        CTMTextureAtlasSprite ctmSprite = TextureUtil.findTextureReloc(ctx.getOriginalIcon());
        if (ctmSprite != null) {
            ctx.setCtmSprite(ctmSprite);
            ctx.setIconMinU(ctmSprite.getMinU());
            ctx.setIconMaxU(ctmSprite.getMaxU());
            ctx.setIconMinV(ctmSprite.getMinV());
            ctx.setIconMaxV(ctmSprite.getMaxV());
            return RenderBranch.TEXTURE_RELOC;
        }

        // Legacy 分支
        String iconName = TextureKeyNormalizer.normalizeIconName(ctx.getOriginalIcon().getIconName());
        if (shouldUseLegacy(iconName)) {
            return RenderBranch.LEGACY;
        }

        return RenderBranch.NONE;
    }

    private boolean shouldUseLegacy(String iconName) {
        return iconName != null && Textures.contain(iconName);
    }

    private void executeModelBranch(RenderContext ctx) {
        ctx.debug(() -> "MODEL: Looping through " + ctx.getElements().size() + " elements");

        ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
        if (!ctx.getElements().isEmpty()) {
            ModelFace firstFace = ctx.getElements().get(0).getFace(ctx.getFace());
            if (firstFace != null && firstFace.getConnectionKey() != null) {
                ConnectionPredicate p = PredicateRegistry.getPredicate(
                    firstFace.getConnectionKey(),
                    ctx.getModelData().getConnections());
                if (p != null) predicate = p;
            }
        }
        ctx.setConnectionPredicate(predicate);

        int index = 0;
        for (ModelElement element : ctx.getElements()) {
            ctx.setCurrentElement(element);
            ctx.setCurrentElementIndex(index++);
            ctx.resetPipelineFailed();

            CTMTextureAtlasSprite ctmSprite = TextureUtil.resolveForElement(element, ctx.getFace(), ctx.getModelData());
            if (ctmSprite == null) {
                ctx.warn(() -> "RENDER: Failed to resolve texture for element " + ctx.getCurrentElementIndex());
                continue;
            }

            ctx.setCtmSprite(ctmSprite);
            ctx.setIconMinU(ctmSprite.getMinU());
            ctx.setIconMaxU(ctmSprite.getMaxU());
            ctx.setIconMinV(ctmSprite.getMinV());
            ctx.setIconMaxV(ctmSprite.getMaxV());

            if (ctmSprite.getLayoutStyle() != null) {
                ElementQuadRenderer.renderConnecting(ctx);
            } else if (ctmSprite.getRandomCount() > 0) {
                ElementQuadRenderer.renderRandom(ctx);
            } else {
                ElementQuadRenderer.renderBase(ctx);
            }
        }
    }

    private void executeTextureRelocBranch(RenderContext ctx) {
        CTMTextureAtlasSprite ctmSprite = ctx.getCtmSprite();
        if (ctmSprite == null) return;

        if (ctmSprite.getLayoutStyle() != null) {
            RenderBlocksQuadRenderer.renderConnecting(ctx);
        } else if (ctmSprite.getRandomCount() > 0) {
            RenderBlocksQuadRenderer.renderRandom(ctx);
        } else {
            RenderBlocksQuadRenderer.renderBase(ctx);
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
        ItemQuadRenderer.renderBase(ctx);
    }
}
