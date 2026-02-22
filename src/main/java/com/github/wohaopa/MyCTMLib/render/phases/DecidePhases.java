package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.render.context.BlockRenderMode;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.context.RenderType;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public class DecidePhases implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        RenderType renderType = context.getRenderType();
        
        if (renderType != RenderType.BLOCK) {
            // 非 BLOCK 分支：暂时不处理，直接 DONE
            context.setDrewAny(false);
            context.popState();
            context.pushState(RenderState.DONE);
            return PhaseResult.CONTINUE;
        }
        
        // BLOCK 分支的子分支判断
        decideBlockBranch(context);
        return PhaseResult.CONTINUE;
    }

    private void decideBlockBranch(RenderContext context) {
        // 1. Model 分支：必须有 modelId 且有 elements
        if (context.getModelId() != null && context.hasElements()) {
            context.setBlockSubBranch(BlockRenderMode.MODEL);
            context.popState();
            context.pushState(RenderState.ELEMENT_LOOP_CONTROL);
            return;
        }
        
        // 2. TextureReloc 分支
        if (shouldUseTextureReloc(context)) {
            context.setBlockSubBranch(BlockRenderMode.TEXTURE_RELOC);
            context.popState();
            context.pushState(RenderState.PREPARE_TEXTURE_ICON);
            return;
        }
        
        // 3. Legacy 分支
        if (shouldUseLegacy(context)) {
            context.setBlockSubBranch(BlockRenderMode.LEGACY);
            context.popState();
            context.pushState(RenderState.PREPARE_LEGACY_DATA);
            return;
        }
        
        // 4. 都失败 → 正常返回 DONE，renderPipeline 返回 false 走原版流程
        context.setBlockSubBranch(null);
        context.setDrewAny(false);
        context.popState();
        context.pushState(RenderState.DONE);
    }

    private boolean shouldUseTextureReloc(RenderContext context) {
        String iconName = context.getIconName();
        if (iconName == null) return false;
        
        // 判断条件：
        // 1. iconName 以 _ctm 结尾
        // 2. 或者 iconName 以 _ 加数字结尾（如 _0, _1, _2 等）
        if (iconName.endsWith("_ctm")) return true;
        
        // 检查是否以 _ 加数字结尾
        int lastUnderscore = iconName.lastIndexOf('_');
        if (lastUnderscore > 0 && lastUnderscore < iconName.length() - 1) {
            String suffix = iconName.substring(lastUnderscore + 1);
            try {
                Integer.parseInt(suffix);
                return true;
            } catch (NumberFormatException e) {
                // 不是数字后缀
            }
        }
        
        return false;
    }

    private boolean shouldUseLegacy(RenderContext context) {
        String iconName = context.getIconName();
        return iconName != null && Textures.contain(iconName);
    }
}
