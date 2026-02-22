package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.render.context.RenderInvocationContext;
import com.github.wohaopa.MyCTMLib.render.context.RenderInvocationContextHolder;
import com.github.wohaopa.MyCTMLib.render.context.RenderMethod;
import com.github.wohaopa.MyCTMLib.render.context.RenderType;

@Mixin(RenderItem.class)
public abstract class MixinItemRenderer {

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void onRenderItemStart(EntityLivingBase entity, ItemStack itemStack, int pass, CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.get();
        ctx.pushMethod(RenderMethod.RENDER_ITEM);
        ctx.setRenderType(RenderType.ITEM);
        ctx.setEntity(entity);
        ctx.setItemStack(itemStack);
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void onRenderItemEnd(CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.popMethod();
        ctx.setEntity(null);
        ctx.setItemStack(null);
    }

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"))
    private void onRenderItemInFirstPersonStart(float partialTickTime, CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.get();
        ctx.pushMethod(RenderMethod.RENDER_ITEM_IN_FIRST_PERSON);
        ctx.setRenderType(RenderType.ITEM);
    }

    @Inject(method = "renderItemInFirstPerson", at = @At("RETURN"))
    private void onRenderItemInFirstPersonEnd(CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.popMethod();
    }
}
