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
public abstract class MixinRenderManager {

    @Inject(method = "renderEntityStatic", at = @At("HEAD"))
    private void onRenderEntityStaticStart(Entity entity, float partialTickTime, boolean flag, CallbackInfoReturnable<Boolean> cir) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.get();
        ctx.pushMethod(RenderMethod.RENDER_ENTITY_STATIC);
        ctx.setEntity(entity);
    }

    @Inject(method = "renderEntityStatic", at = @At("RETURN"))
    private void onRenderEntityStaticEnd(CallbackInfoReturnable<Boolean> cir) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.popMethod();
        ctx.setEntity(null);
    }
}
