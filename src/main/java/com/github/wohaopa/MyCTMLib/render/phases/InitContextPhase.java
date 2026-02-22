package com.github.wohaopa.MyCTMLib.render.phases;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PhaseResult;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;

public class InitContextPhase implements PipelinePhase {

    @Override
    public PhaseResult process(RenderContext context) {
        // 1. 设置调试监听器
        if (MyCTMLib.debugMode && context.getDebugListener() == null) {
            context.setDebugListener(new PipelineDebugTrace());
        }
        
        // 2. 重置绘制标志
        context.setDrewAny(false);
        
        // 3. 记录调试信息（坐标、iconName、renderType 等）
        if (MyCTMLib.debugMode && context.getDebugListener() instanceof PipelineDebugTrace) {
            PipelineDebugTrace trace = (PipelineDebugTrace) context.getDebugListener();
            trace.addStep("Init: pos=(" + (int)context.getX() + "," + (int)context.getY() + "," + (int)context.getZ() 
                + "), icon=" + context.getIconName()
                + ", type=" + context.getRenderType());
        }
        
        // 4. 转移状态
        context.popState();
        context.pushState(RenderState.DECIDE);
        return PhaseResult.CONTINUE;
    }
}
