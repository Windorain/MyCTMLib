package com.github.wohaopa.MyCTMLib.render.pipeline;

import java.util.List;

import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.render.util.ModelUtil;
import com.github.wohaopa.MyCTMLib.model.baked.BakedModel;
import com.github.wohaopa.MyCTMLib.model.baked.BakedQuad;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.domain.TextureDomain;
import com.github.wohaopa.MyCTMLib.render.quads.BakedQuadRenderer;
import com.github.wohaopa.MyCTMLib.render.quads.RenderBlocksQuadRenderer;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;

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
        ctx.info("RenderLevel: " + ctx.getRenderLevel());

        boolean result = false;
        RenderLevel level = ctx.getRenderLevel();
        if (level == null) {
            ctx.warn("RenderLevel is null");
            result = false;
        } else {
            switch (level) {
                case BLOCK:
                    result = executeBlockModelBranch(ctx);
                    break;
                case FACE:
                    result = executeTextureRelocBranch(ctx);
                    break;
                default:
                    ctx.warn("Unknown RenderLevel: " + level);
                    result = false;
            }
        }

        ctx.info("COMPLETE: drewAny=" + ctx.isDrewAny() + ", result=" + result);
        return result;
    }

    private boolean executeBlockModelBranch(RenderContext ctx) {
        ctx.info("BLOCK: Executing block model branch");

        String modelId = ModelUtil.findModelId(ctx.getBlock(), ctx.getMeta());
        if (modelId == null) {
            ctx.warn("BLOCK: No modelId found");
            return false;
        }
        ctx.info("BLOCK: Found modelId: " + modelId);

        BakedModel bakedModel = ModelRegistry.getInstance().getBakedModel(modelId);
        if (bakedModel == null) {
            ctx.warn("BLOCK: No BakedModel found for: " + modelId);
            return false;
        }
        ctx.setBakedModel(bakedModel);
        ctx.info("BLOCK: Using BakedModel with " + bakedModel.getAllQuads().size() + " quads");

        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
            ctx.setFace(face);
            List<BakedQuad> quads = bakedModel.getQuads(face);
            ctx.info("BLOCK: Rendering face " + face + " with " + quads.size() + " quads");
            
            for (BakedQuad quad : quads) {
                renderBakedQuad(quad, ctx);
            }
        }

        return ctx.isDrewAny();
    }

    private boolean executeTextureRelocBranch(RenderContext ctx) {
        ctx.info("FACE: Executing texture relocation branch");

        if (!TextureDomain.resolveReloc(ctx)) {
            ctx.warn("FACE: Texture relocation resolve failed");
            return false;
        }

        CTMTextureAtlasSprite ctmSprite = ctx.getCtmSprite();
        ctx.info("FACE: Using sprite: " + ctmSprite.getIconName());

        if (ctmSprite.getLayoutStyle() != null) {
            ctx.info("FACE: Using connecting render");
            RenderBlocksQuadRenderer.renderConnecting(ctx);
        } else if (ctmSprite.getRandomCount() > 0) {
            ctx.info("FACE: Using random render (count=" + ctmSprite.getRandomCount() + ")");
            RenderBlocksQuadRenderer.renderRandom(ctx);
        } else {
            ctx.info("FACE: Using base render");
            RenderBlocksQuadRenderer.renderBase(ctx);
        }

        return ctx.isDrewAny();
    }

    private void renderBakedQuad(BakedQuad quad, RenderContext ctx) {
        CTMTextureAtlasSprite sprite = quad.getSprite();
        if (sprite == null) {
            ctx.warn("BakedQuad has no sprite, skipping");
            return;
        }

        if (sprite.getLayoutStyle() != null) {
            ctx.trace(() -> "Rendering connecting quad for face " + ctx.getFace());
            BakedQuadRenderer.renderConnecting(quad, ctx);
        } else if (sprite.getRandomCount() > 0) {
            ctx.trace(() -> "Rendering random quad for face " + ctx.getFace());
            BakedQuadRenderer.renderRandom(quad, ctx);
        } else {
            ctx.trace(() -> "Rendering base quad for face " + ctx.getFace());
            BakedQuadRenderer.renderBase(quad, ctx);
        }
    }
}
