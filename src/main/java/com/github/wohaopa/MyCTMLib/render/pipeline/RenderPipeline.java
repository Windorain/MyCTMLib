package com.github.wohaopa.MyCTMLib.render.pipeline;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipelines.BaseTilePipeline;
import com.github.wohaopa.MyCTMLib.render.pipelines.ConnectingTilePipeline;
import com.github.wohaopa.MyCTMLib.render.pipelines.RandomTilePipeline;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;

/**
 * 渲染管线（线性管道）
 * 
 * 执行流程：INIT → DECIDE → RENDER → COMPLETE
 */
public class RenderPipeline {

    public boolean execute(RenderContext ctx) {
        ctx.info(() -> "=== RenderPipeline Started ===");
        ctx.debug(() -> "INIT: Resetting context");
        ctx.resetPipelineFailed();
        ctx.setDrewAny(false);

        RenderBranch branch = decideRenderBranch(ctx);
        ctx.setRenderBranch(branch);
        ctx.info(() -> "DECIDE: Using branch: " + branch);

        ctx.debug(() -> "RENDER: Starting render");
        switch (branch) {
            case MODEL_ELEMENTS -> executeModelBranch(ctx);
            case TEXTURE_RELOC -> executeTextureRelocBranch(ctx);
            case LEGACY -> executeLegacyBranch(ctx);
            case ITEM -> executeItemBranch(ctx);
            case ENTITY, NONE -> {
                ctx.debug(() -> "RENDER: Skipping (ENTITY/NONE branch)");
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
                ctx.getX(),
                ctx.getY(),
                ctx.getZ(),
                ctx.getOriginalIcon(),
                ctx.getFace());
        }

        return drewAny;
    }

    private RenderBranch decideRenderBranch(RenderContext ctx) {
        ctx.trace(() -> "DECIDE: Checking render branch");

        if (ctx.isItemRender()) {
            ctx.debug(() -> "DECIDE: Item render detected");
            return RenderBranch.ITEM;
        }

        String modelId = ctx.getModelId();
        if (modelId != null) {
            ctx.debug(() -> "DECIDE: Model branch candidate (modelId=" + modelId + ")");
            if (!ctx.getElements()
                .isEmpty()) {
                ctx.info(() -> "DECIDE: Selected MODEL_ELEMENTS branch");
                return RenderBranch.MODEL_ELEMENTS;
            } else {
                ctx.debug(() -> "DECIDE: Model branch has no elements");
            }
        }

        String iconName = ctx.getIconName();
        if (shouldUseTextureReloc(iconName)) {
            ctx.debug(() -> "DECIDE: Selected TEXTURE_RELOC branch");
            return RenderBranch.TEXTURE_RELOC;
        }

        if (shouldUseLegacy(iconName)) {
            ctx.debug(() -> "DECIDE: Selected LEGACY branch");
            return RenderBranch.LEGACY;
        }

        ctx.debug(() -> "DECIDE: No matching branch (NONE)");
        return RenderBranch.NONE;
    }

    private boolean shouldUseTextureReloc(String iconName) {
        if (iconName == null) return false;
        return iconName.endsWith("_ctm") || isNumericSuffix(iconName);
    }

    private boolean shouldUseLegacy(String iconName) {
        return iconName != null && Textures.contain(iconName);
    }

    private boolean isNumericSuffix(String iconName) {
        int lastUnderscore = iconName.lastIndexOf('_');
        if (lastUnderscore > 0 && lastUnderscore < iconName.length() - 1) {
            try {
                Integer.parseInt(iconName.substring(lastUnderscore + 1));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private void executeModelBranch(RenderContext ctx) {
        ctx.debug(() -> "MODEL: Looping through " + ctx.getElements().size() + " elements");

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
        ctx.trace(() -> "MODEL: Using predicate: " + PredicateRegistry.getPredicateDebugName(ctx.getConnectionPredicate(), null));

        int index = 0;
        for (ModelElement element : ctx.getElements()) {
            ctx.setCurrentElement(element);
            final int currentIndex = index++;
            ctx.setCurrentElementIndex(currentIndex);
            ctx.resetPipelineFailed();
            ctx.trace(() -> "MODEL: Rendering element " + currentIndex);

            ModelFace faceData = element.getFace(ctx.getFace());
            if (faceData == null) {
                ctx.trace(() -> "RENDER: No face data for current direction");
                continue;
            }

            if (faceData.getTextureKey() == null) {
                ctx.trace(() -> "RENDER: Face has no texture key");
                continue;
            }

            String texturePath = com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer.resolveTexturePath(
                faceData.getTextureKey(),
                ctx.getModelData()
                    .getTextures());
            if (texturePath == null) {
                ctx.warn(() -> "RENDER: Could not resolve texture path for key: " + faceData.getTextureKey());
                continue;
            }

            String domain = extractDomain(ctx.getModelId());
            String textureKey = com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer
                .toCanonicalTextureKey(domain, texturePath);
            ctx.setTextureKey(textureKey);
            ctx.debug(() -> "RENDER: Resolved texture key: " + textureKey);

            TextureTypeData data = CTMRenderEntry.getConnectingData(textureKey);
            ctx.setTextureData(data);

            if (data == null) {
                ctx.warn(() -> "RENDER: No texture data found for key: " + textureKey);
                continue;
            }

            ctx.trace(() -> "RENDER: Texture parsed successfully: " + data.getClass().getSimpleName());

            if (data instanceof BaseTextureData btd) {
                ctx.setBaseData(btd);
                ctx.debug(() -> "RENDER: BaseTexture pipeline");
                BaseTilePipeline.execute(ctx);
            } else if (data instanceof RandomTextureData rtd) {
                ctx.debug(() -> "RENDER: RandomTexture pipeline");
                RandomTilePipeline.execute(ctx);
            } else if (data instanceof ConnectingTextureData ctd) {
                ctx.debug(() -> "RENDER: ConnectingTexture pipeline");
                ConnectingTilePipeline.execute(ctx);
            } else {
                ctx.debug(() -> "RENDER: Unknown texture data type: " + (data != null ? data.getClass().getSimpleName() : "null"));
                ctx.failPipeline("Unknown texture data type: " + data);
            }

            if (ctx.isPipelineFailed()) {
                ctx.debug(() -> "MODEL: Element " + currentIndex + " failed, continuing to next");
            } else {
                ctx.trace(() -> "MODEL: Element " + currentIndex + " rendered successfully");
            }
        }

        ctx.debug(() -> "MODEL: Completed processing all elements");
    }

    private String extractDomain(String modelId) {
        if (modelId == null || modelId.indexOf(':') < 0) {
            return "minecraft";
        }
        return modelId.substring(0, modelId.indexOf(':'));
    }

    private void executeTextureRelocBranch(RenderContext ctx) {
        ctx.info(() -> "TEXTURE_RELOC: Rendering with texture relocation");
    }

    private void executeLegacyBranch(RenderContext ctx) {
        ctx.info(() -> "LEGACY: Using legacy renderer");
        boolean result = Textures.renderWorldBlock(
            ctx.getRenderBlocks(),
            ctx.getBlockAccess(),
            ctx.getBlock(),
            ctx.getX(),
            ctx.getY(),
            ctx.getZ(),
            ctx.getOriginalIcon(),
            ctx.getFace());
        ctx.setDrewAny(result);
        ctx.debug(() -> "LEGACY: Result=" + result);
    }

    private void executeItemBranch(RenderContext ctx) {
        ctx.info(() -> "ITEM: Rendering item face");
        BaseTilePipeline.execute(ctx);
    }
}
