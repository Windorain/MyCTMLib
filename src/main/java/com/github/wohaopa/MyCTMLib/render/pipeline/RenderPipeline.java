package com.github.wohaopa.MyCTMLib.render.pipeline;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.domain.ModelDomain;
import com.github.wohaopa.MyCTMLib.render.domain.TextureDomain;
import com.github.wohaopa.MyCTMLib.render.quads.ElementQuadRenderer;
import com.github.wohaopa.MyCTMLib.render.quads.ItemQuadRenderer;
import com.github.wohaopa.MyCTMLib.render.quads.RenderBlocksQuadRenderer;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;

public class RenderPipeline {

    public boolean execute() {
        return executeInternal(RenderContext.get(), false);
    }

    public void executeDryRun(RenderContext ctx) {
        executeInternal(ctx, true);
    }

    public boolean execute(RenderContext ctx) {
        return executeInternal(ctx, false);
    }

    private boolean executeInternal(RenderContext ctx, boolean dryRun) {
        ctx.reset();
        ctx.setDryRun(dryRun);

        ctx.info("=== RenderPipeline Started " + (dryRun ? "(DRY RUN)" : "") + " ===");

        RenderBranch branch = decideRenderBranch(ctx);
        ctx.setRenderBranch(branch);
        ctx.info("DECIDE: Using branch: " + branch);

        switch (branch) {
            case MODEL_ELEMENTS -> executeModelBranch(ctx);
            case TEXTURE_RELOC -> executeTextureRelocBranch(ctx);
            case LEGACY -> executeLegacyBranch(ctx);
            case ITEM -> executeItemBranch(ctx);
            case ENTITY, NONE -> {
                ctx.setDrewAny(false);
            }
        }

        ctx.info("COMPLETE: drewAny=" + ctx.isDrewAny());

        return ctx.isDrewAny();
    }

    private RenderBranch decideRenderBranch(RenderContext ctx) {
        if (ctx.getBlockAccess() == null) {
            ctx.info("DECIDE: blockAccess is null, using ITEM branch");
            return RenderBranch.ITEM;
        }

        if (ModelDomain.resolve(ctx)) {
            ctx.info("DECIDE: Using MODEL_ELEMENTS branch");
            return RenderBranch.MODEL_ELEMENTS;
        }

        if (TextureDomain.resolveReloc(ctx)) {
            ctx.info("DECIDE: Using TEXTURE_RELOC branch");
            return RenderBranch.TEXTURE_RELOC;
        }

        String iconName = TextureKeyNormalizer.normalizeIconName(
            ctx.getOriginalIcon()
                .getIconName());
        if (shouldUseLegacy(iconName)) {
            ctx.info("DECIDE: Using LEGACY branch for icon: " + iconName);
            return RenderBranch.LEGACY;
        }

        ctx.info("DECIDE: No branch matched, using NONE");
        return RenderBranch.NONE;
    }

    private boolean shouldUseLegacy(String iconName) {
        return iconName != null && Textures.contain(iconName);
    }

    private void executeModelBranch(RenderContext ctx) {
        ctx.info(
            "MODEL: Looping through " + ctx.getElements()
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
                if (p != null) {
                    predicate = p;
                    ctx.info("MODEL: Using custom connection predicate: " + firstFace.getConnectionKey());
                }
            }
        }
        ctx.setConnectionPredicate(predicate);

        int index = 0;
        for (ModelElement element : ctx.getElements()) {
            ctx.setCurrentElement(element);
            ctx.setCurrentElementIndex(index++);
            ctx.resetPipelineFailed();

            if (!TextureDomain.resolveForElement(ctx)) {
                continue;
            }

            CTMTextureAtlasSprite ctmSprite = ctx.getCtmSprite();
            ctx.info("MODEL: Element " + ctx.getCurrentElementIndex() + " using sprite: " + ctmSprite.getIconName());

            if (ctmSprite.getLayoutStyle() != null) {
                ctx.info("MODEL: Element " + ctx.getCurrentElementIndex() + " using connecting render");
                ElementQuadRenderer.renderConnecting(ctx);
            } else if (ctmSprite.getRandomCount() > 0) {
                ctx.info(
                    "MODEL: Element " + ctx.getCurrentElementIndex()
                        + " using random render (count="
                        + ctmSprite.getRandomCount()
                        + ")");
                ElementQuadRenderer.renderRandom(ctx);
            } else {
                ctx.info("MODEL: Element " + ctx.getCurrentElementIndex() + " using base render");
                ElementQuadRenderer.renderBase(ctx);
            }
        }
    }

    private void executeTextureRelocBranch(RenderContext ctx) {
        CTMTextureAtlasSprite ctmSprite = ctx.getCtmSprite();
        ctx.info("TEXTURE_RELOC: Using sprite: " + ctmSprite.getIconName());

        if (ctmSprite.getLayoutStyle() != null) {
            ctx.info("TEXTURE_RELOC: Using connecting render");
            RenderBlocksQuadRenderer.renderConnecting(ctx);
        } else if (ctmSprite.getRandomCount() > 0) {
            ctx.info("TEXTURE_RELOC: Using random render (count=" + ctmSprite.getRandomCount() + ")");
            RenderBlocksQuadRenderer.renderRandom(ctx);
        } else {
            ctx.info("TEXTURE_RELOC: Using base render");
            RenderBlocksQuadRenderer.renderBase(ctx);
        }
    }

    private void executeLegacyBranch(RenderContext ctx) {
        ctx.info("LEGACY: Calling Textures.renderWorldBlock");
        boolean result = Textures.renderWorldBlock(
            ctx.getRenderBlocks(),
            ctx.getBlockAccess(),
            ctx.getBlock(),
            ctx.getBlockX(),
            ctx.getBlockY(),
            ctx.getBlockZ(),
            ctx.getOriginalIcon(),
            ctx.getFace());
        ctx.info("LEGACY: renderWorldBlock returned: " + result);
        ctx.setDrewAny(result);
    }

    private void executeItemBranch(RenderContext ctx) {
        ctx.info("ITEM: Calling ItemQuadRenderer.renderBase");
        ItemQuadRenderer.renderBase(ctx);
    }
}
