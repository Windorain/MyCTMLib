package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.render.context.RenderInvocationContext;
import com.github.wohaopa.MyCTMLib.render.context.RenderInvocationContextHolder;
import com.github.wohaopa.MyCTMLib.render.context.RenderMethod;

@Mixin(Render.class)
public abstract class MixinRender {

    @Inject(method = "doRender", at = @At("HEAD"))
    private void onDoRenderStart(Entity entity, double x, double y, double z, float yaw, float partialTickTime,
        CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx != null) {
            ctx.pushMethod(RenderMethod.DO_RENDER);
            ctx.setEntity(entity);
            ctx.setRenderPos(x, y, z);
            ctx.setEntityYaw(yaw);
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    private void onDoRenderEnd(CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx != null) {
            ctx.popMethod();
            ctx.setEntity(null);
            ctx.setRenderPos(0, 0, 0);
            ctx.setEntityYaw(0);
        }
    }
}
