package com.github.wohaopa.MyCTMLib.render.pipeline;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipelines.BaseTilePipeline;
import com.github.wohaopa.MyCTMLib.render.pipelines.ConnectingTilePipeline;
import com.github.wohaopa.MyCTMLib.render.pipelines.RandomTilePipeline;
import com.github.wohaopa.MyCTMLib.render.phasegroups.GeometryGroup;
import com.github.wohaopa.MyCTMLib.render.phasegroups.IconResolveGroup;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
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
        // 阶段 1：初始化
        ctx.logDebug("INIT: Resetting context");
        ctx.resetPipelineFailed();
        ctx.setDrewAny(false);

        // 阶段 2：决策
        RenderBranch branch = decideRenderBranch(ctx);
        ctx.setRenderBranch(branch);
        ctx.logDebug("DECIDE: Using branch: " + branch);

        // 阶段 3：渲染（根据 branch 执行）
        ctx.logDebug("RENDER: Starting render");
        switch (branch) {
            case MODEL_ELEMENTS -> executeModelBranch(ctx);
            case TEXTURE_RELOC -> executeTextureRelocBranch(ctx);
            case LEGACY -> executeLegacyBranch(ctx);
            case ITEM -> executeItemBranch(ctx);
            case ENTITY, NONE -> ctx.setDrewAny(false);
        }

        // 阶段 4：清理
        ctx.logDebug("COMPLETE: drewAny=" + ctx.isDrewAny());

        // 如果新管线失败，fallback 到 tryRender
        boolean drewAny = ctx.isDrewAny();
        if (!drewAny) {
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

    // ========== 决策逻辑 ==========

    private RenderBranch decideRenderBranch(RenderContext ctx) {
        // 1. 物品渲染优先判断
        if (ctx.isItemRender()) {
            return RenderBranch.ITEM;
        }

        // 2. Model 分支：需要查询，触发懒加载
        String modelId = ctx.getModelId();
        if (modelId != null) {
            if (!ctx.getElements().isEmpty()) {
                return RenderBranch.MODEL_ELEMENTS;
            }
        }

        // 3. 纹理重定位分支
        String iconName = ctx.getIconName();
        if (shouldUseTextureReloc(iconName)) {
            return RenderBranch.TEXTURE_RELOC;
        }

        // 4. Legacy 分支
        if (shouldUseLegacy(iconName)) {
            return RenderBranch.LEGACY;
        }

        // 5. 无匹配
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

    // ========== 分支执行函数 ==========

    private void executeModelBranch(RenderContext ctx) {
        ctx.logDebug("MODEL: Looping through " + ctx.getElements().size() + " elements");

        for (ModelElement element : ctx.getElements()) {
            ctx.setCurrentElement(element);
            ctx.resetPipelineFailed();
            ctx.logDebug("MODEL: Rendering element");

            renderElement(ctx);

            if (ctx.isPipelineFailed()) {
                ctx.logDebug("MODEL: Element failed, continuing to next");
            }
        }
    }

    private void renderElement(RenderContext ctx) {
        // 子阶段 1：解析材质
        if (!parseTextureForElement(ctx)) {
            ctx.logDebug("RENDER: Skip element (no texture)");
            return;
        }

        // 子阶段 2：几何数据
        GeometryGroup.boundsFromElement(ctx);
        if (ctx.isPipelineFailed()) return;

        // 子阶段 3：Icon
        IconResolveGroup.resolveIcon(ctx);
        if (ctx.isPipelineFailed()) return;

        // 子阶段 4：根据材质类型选择管道
        TextureTypeData data = ctx.getTextureData();
        if (data instanceof BaseTextureData btd) {
            ctx.setBaseData(btd);
            ctx.logDebug("RENDER: BaseTexture pipeline");
            BaseTilePipeline.execute(ctx);
        } else if (data instanceof RandomTextureData rtd) {
            ctx.logDebug("RENDER: RandomTexture pipeline");
            RandomTilePipeline.execute(ctx);
        } else if (data instanceof ConnectingTextureData ctd) {
            ctx.logDebug("RENDER: ConnectingTexture pipeline");
            ConnectingTilePipeline.execute(ctx);
        } else {
            ctx.logDebug("RENDER: Unknown texture data type");
            ctx.failPipeline("Unknown texture data type: " + data);
        }
    }

    private boolean parseTextureForElement(RenderContext ctx) {
        ModelElement element = ctx.getCurrentElement();
        if (element == null) {
            ctx.failPipeline("Current element is null");
            return false;
        }

        ModelFace faceData = element.getFace(ctx.getFace());
        if (faceData == null || faceData.getTextureKey() == null) {
            return false;
        }

        String texturePath = com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer.resolveTexturePath(
            faceData.getTextureKey(), ctx.getModelData().getTextures());
        if (texturePath == null) {
            return false;
        }

        String domain = extractDomain(ctx.getModelId());
        String textureKey = com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer.toCanonicalTextureKey(domain, texturePath);
        ctx.setTextureData(CTMRenderEntry.getConnectingData(textureKey));

        return true;
    }

    private String extractDomain(String modelId) {
        if (modelId == null || modelId.indexOf(':') < 0) {
            return "minecraft";
        }
        return modelId.substring(0, modelId.indexOf(':'));
    }

    private void executeTextureRelocBranch(RenderContext ctx) {
        ctx.logDebug("TEXTURE_RELOC: Rendering with texture relocation");
        // TODO: 实现纹理重定位渲染
    }

    private void executeLegacyBranch(RenderContext ctx) {
        ctx.logDebug("LEGACY: Using legacy renderer");
        boolean result = Textures.renderWorldBlock(
            ctx.getRenderBlocks(),
            ctx.getBlockAccess(),
            ctx.getBlock(),
            ctx.getX(),
            ctx.getY(),
            ctx.getZ(),
            ctx.getOriginalIcon(),
            ctx.getFace()
        );
        ctx.setDrewAny(result);
    }

    private void executeItemBranch(RenderContext ctx) {
        ctx.logDebug("ITEM: Rendering item face");
        // 物品渲染使用 BaseTilePipeline
        BaseTilePipeline.execute(ctx);
    }
}
