package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.render.context.RenderInvocationContext;
import com.github.wohaopa.MyCTMLib.render.context.RenderInvocationContextHolder;
import com.github.wohaopa.MyCTMLib.render.context.RenderMethod;
import com.github.wohaopa.MyCTMLib.render.context.RenderType;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Inject(method = "renderEntities", at = @At("HEAD"))
    private void onRenderEntitiesStart(EntityLivingBase player, ICamera camera, float partialTickTime,
        CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.get();
        ctx.pushMethod(RenderMethod.RENDER_ENTITIES);
        ctx.setRenderType(RenderType.ENTITY);
        ctx.setEntity(player);
    }

    @Inject(method = "renderEntities", at = @At("RETURN"))
    private void onRenderEntitiesEnd(CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.popMethod();
        ctx.setEntity(null);
    }
}
