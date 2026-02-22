package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.render.context.RenderInvocationContext;
import com.github.wohaopa.MyCTMLib.render.context.RenderInvocationContextHolder;
import com.github.wohaopa.MyCTMLib.render.context.RenderMethod;

@Mixin(RenderManager.class)
public abstract class MixinRender {

    @Inject(method = "func_147939_a", at = @At("HEAD"))
    private void onDoRenderEntityStart(Entity entity, double x, double y, double z, float yaw, float partialTickTime,
        boolean flag, CallbackInfoReturnable<Boolean> cir) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx != null) {
            ctx.pushMethod(RenderMethod.DO_RENDER_ENTITY);
            ctx.setEntity(entity);
            ctx.setRenderPos(x, y, z);
            ctx.setEntityYaw(yaw);
        }
    }

    @Inject(method = "func_147939_a", at = @At("RETURN"))
    private void onDoRenderEntityEnd(CallbackInfoReturnable<Boolean> cir) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx != null) {
            ctx.popMethod();
            ctx.setEntity(null);
            ctx.setRenderPos(0, 0, 0);
            ctx.setEntityYaw(0);
        }
    }
}
