package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void onRenderItemStart(EntityLivingBase entity, ItemStack itemStack, int pass, CallbackInfo ci) {
        RenderContext ctx = RenderContext.get();
        ctx.setBlockAccess(null);
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void onRenderItemEnd(CallbackInfo ci) {}

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"))
    private void onRenderItemInFirstPersonStart(float partialTickTime, CallbackInfo ci) {
        RenderContext ctx = RenderContext.get();
        ctx.setBlockAccess(null);
    }

    @Inject(method = "renderItemInFirstPerson", at = @At("RETURN"))
    private void onRenderItemInFirstPersonEnd(CallbackInfo ci) {}
}
