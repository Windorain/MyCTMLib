package com.github.wohaopa.MyCTMLib.mixins;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.context.RenderInvocationContext;
import com.github.wohaopa.MyCTMLib.render.context.RenderInvocationContextHolder;
import com.github.wohaopa.MyCTMLib.render.context.RenderMethod;
import com.github.wohaopa.MyCTMLib.render.context.RenderType;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;

/**
 * 一个用于注入 Minecraft 渲染流程的 Mixin 类，用于拦截和替代 {@link RenderBlocks} 中的方块面渲染逻辑，
 * 从而实现连接纹理（CTM）或其他定制渲染方式。
 *
 * <p>
 * 本类通过注入六个面（上下南北东西）的 `renderFace*` 方法，在符合条件时调用自定义的
 * {@link Textures#renderWorldBlock} 方法进行渲染，并通过取消默认逻辑实现“完全替代”。
 */
@Mixin(RenderBlocks.class)
public abstract class MixinRenderBlocks {

    /**
     * 渲染器使用的世界访问接口，用于获取周围方块信息。
     * 由 Minecraft 渲染系统初始化赋值。
     */
    @Shadow
    public IBlockAccess blockAccess;

    @Shadow
    public abstract boolean hasOverrideBlockTexture();

    /**
     * 注入 renderFaceYNeg（底面）渲染方法，在匹配指定纹理时调用自定义渲染逻辑。
     */
    @Inject(method = "renderFaceYNeg", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceYNeg(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (iIcon == null) return;
        if (this.hasOverrideBlockTexture()) return;

        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx != null) {
            ctx.pushMethod(RenderMethod.RENDER_FACE_Y_NEG);
            ctx.setCurrentFace(ForgeDirection.DOWN);
            ctx.setCurrentIcon(iIcon);
            ctx.setIconName(iIcon.getIconName());
            ctx.incrementFaceRenderCount(ForgeDirection.DOWN);
        }

        if (blockAccess == null) {
            if (CTMRenderEntry
                .tryRenderItemFace((RenderBlocks) (Object) this, block, x, y, z, iIcon, ForgeDirection.DOWN)) {
                ci.cancel();
            }
            return;
        }

        String iconName = TextureKeyNormalizer.normalizeIconName(iIcon.getIconName());

        if (CTMRenderEntry.renderPipeline()) {
            ci.cancel();
            return;
        }

        if (CTMRenderEntry
            .tryRender((RenderBlocks) (Object) this, blockAccess, block, x, y, z, iIcon, ForgeDirection.DOWN)) {
            ci.cancel();
            return;
        }
        if (!Textures.contain(iconName)) return;
        if (Textures.renderWorldBlock(
            (RenderBlocks) ((Object) this),
            blockAccess,
            block,
            x,
            y,
            z,
            iIcon,
            ForgeDirection.DOWN)) {
            ci.cancel();
        }
    }

    /**
     * 注入 renderFaceYPos（顶面）渲染方法。
     */
    @Inject(method = "renderFaceYPos", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceYPos(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (iIcon == null) return;
        if (this.hasOverrideBlockTexture()) return;

        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx != null) {
            ctx.pushMethod(RenderMethod.RENDER_FACE_Y_POS);
            ctx.setCurrentFace(ForgeDirection.UP);
            ctx.setCurrentIcon(iIcon);
            ctx.setIconName(iIcon.getIconName());
            ctx.incrementFaceRenderCount(ForgeDirection.UP);
        }

        if (blockAccess == null) {
            if (CTMRenderEntry
                .tryRenderItemFace((RenderBlocks) (Object) this, block, x, y, z, iIcon, ForgeDirection.UP)) {
                ci.cancel();
            }
            return;
        }

        String iconName = TextureKeyNormalizer.normalizeIconName(iIcon.getIconName());

        if (CTMRenderEntry.renderPipeline()) {
            ci.cancel();
            return;
        }

        if (CTMRenderEntry
            .tryRender((RenderBlocks) (Object) this, blockAccess, block, x, y, z, iIcon, ForgeDirection.UP)) {
            ci.cancel();
            return;
        }
        if (!Textures.contain(iconName)) return;
        if (Textures
            .renderWorldBlock((RenderBlocks) ((Object) this), blockAccess, block, x, y, z, iIcon, ForgeDirection.UP)) {
            ci.cancel();
        }
    }

    /**
     * 注入 renderFaceZNeg（北面）渲染方法。
     */
    @Inject(method = "renderFaceZNeg", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceZNeg(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (iIcon == null) return;
        if (this.hasOverrideBlockTexture()) return;

        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx != null) {
            ctx.pushMethod(RenderMethod.RENDER_FACE_Z_NEG);
            ctx.setCurrentFace(ForgeDirection.NORTH);
            ctx.setCurrentIcon(iIcon);
            ctx.setIconName(iIcon.getIconName());
            ctx.incrementFaceRenderCount(ForgeDirection.NORTH);
        }

        if (blockAccess == null) {
            if (CTMRenderEntry
                .tryRenderItemFace((RenderBlocks) (Object) this, block, x, y, z, iIcon, ForgeDirection.NORTH)) {
                ci.cancel();
            }
            return;
        }

        String iconName = TextureKeyNormalizer.normalizeIconName(iIcon.getIconName());

        if (CTMRenderEntry.renderPipeline()) {
            ci.cancel();
            return;
        }

        if (CTMRenderEntry
            .tryRender((RenderBlocks) (Object) this, blockAccess, block, x, y, z, iIcon, ForgeDirection.NORTH)) {
            ci.cancel();
            return;
        }
        if (!Textures.contain(iconName)) return;
        if (Textures.renderWorldBlock(
            (RenderBlocks) ((Object) this),
            blockAccess,
            block,
            x,
            y,
            z,
            iIcon,
            ForgeDirection.NORTH)) {
            ci.cancel();
        }
    }

    /**
     * 注入 renderFaceZPos（南面）渲染方法。
     */
    @Inject(method = "renderFaceZPos", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceZPos(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (iIcon == null) return;
        if (this.hasOverrideBlockTexture()) return;

        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx != null) {
            ctx.pushMethod(RenderMethod.RENDER_FACE_Z_POS);
            ctx.setCurrentFace(ForgeDirection.SOUTH);
            ctx.setCurrentIcon(iIcon);
            ctx.setIconName(iIcon.getIconName());
            ctx.incrementFaceRenderCount(ForgeDirection.SOUTH);
        }

        if (blockAccess == null) {
            if (CTMRenderEntry
                .tryRenderItemFace((RenderBlocks) (Object) this, block, x, y, z, iIcon, ForgeDirection.SOUTH)) {
                ci.cancel();
            }
            return;
        }

        String iconName = TextureKeyNormalizer.normalizeIconName(iIcon.getIconName());

        if (CTMRenderEntry.renderPipeline()) {
            ci.cancel();
            return;
        }

        if (CTMRenderEntry
            .tryRender((RenderBlocks) (Object) this, blockAccess, block, x, y, z, iIcon, ForgeDirection.SOUTH)) {
            ci.cancel();
            return;
        }
        if (!Textures.contain(iconName)) return;
        if (Textures.renderWorldBlock(
            (RenderBlocks) ((Object) this),
            blockAccess,
            block,
            x,
            y,
            z,
            iIcon,
            ForgeDirection.SOUTH)) {
            ci.cancel();
        }
    }

    /**
     * 注入 renderFaceXNeg（西面）渲染方法。
     */
    @Inject(method = "renderFaceXNeg", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceXNeg(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (iIcon == null) return;
        if (this.hasOverrideBlockTexture()) return;

        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx != null) {
            ctx.pushMethod(RenderMethod.RENDER_FACE_X_NEG);
            ctx.setCurrentFace(ForgeDirection.WEST);
            ctx.setCurrentIcon(iIcon);
            ctx.setIconName(iIcon.getIconName());
            ctx.incrementFaceRenderCount(ForgeDirection.WEST);
        }

        if (blockAccess == null) {
            if (CTMRenderEntry
                .tryRenderItemFace((RenderBlocks) (Object) this, block, x, y, z, iIcon, ForgeDirection.WEST)) {
                ci.cancel();
            }
            return;
        }

        String iconName = TextureKeyNormalizer.normalizeIconName(iIcon.getIconName());

        if (CTMRenderEntry.renderPipeline()) {
            ci.cancel();
            return;
        }

        if (CTMRenderEntry
            .tryRender((RenderBlocks) (Object) this, blockAccess, block, x, y, z, iIcon, ForgeDirection.WEST)) {
            ci.cancel();
            return;
        }
        if (!Textures.contain(iconName)) return;
        if (Textures.renderWorldBlock(
            (RenderBlocks) ((Object) this),
            blockAccess,
            block,
            x,
            y,
            z,
            iIcon,
            ForgeDirection.WEST)) {
            ci.cancel();
        }
    }

    /**
     * 注入 renderFaceXPos（东面）渲染方法。
     */
    @Inject(method = "renderFaceXPos", at = @At("HEAD"), cancellable = true)
    private void redirect$renderFaceXPos(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (iIcon == null) return;
        if (this.hasOverrideBlockTexture()) return;

        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx != null) {
            ctx.pushMethod(RenderMethod.RENDER_FACE_X_POS);
            ctx.setCurrentFace(ForgeDirection.EAST);
            ctx.setCurrentIcon(iIcon);
            ctx.setIconName(iIcon.getIconName());
            ctx.incrementFaceRenderCount(ForgeDirection.EAST);
        }

        if (blockAccess == null) {
            if (CTMRenderEntry
                .tryRenderItemFace((RenderBlocks) (Object) this, block, x, y, z, iIcon, ForgeDirection.EAST)) {
                ci.cancel();
            }
            return;
        }

        String iconName = TextureKeyNormalizer.normalizeIconName(iIcon.getIconName());

        if (CTMRenderEntry.renderPipeline()) {
            ci.cancel();
            return;
        }

        if (CTMRenderEntry
            .tryRender((RenderBlocks) (Object) this, blockAccess, block, x, y, z, iIcon, ForgeDirection.EAST)) {
            ci.cancel();
            return;
        }
        if (!Textures.contain(iconName)) return;
        if (Textures.renderWorldBlock(
            (RenderBlocks) ((Object) this),
            blockAccess,
            block,
            x,
            y,
            z,
            iIcon,
            ForgeDirection.EAST)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderBlockByRenderType", at = @At("HEAD"))
    private void onRenderBlockByRenderTypeStart(Block block, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.get();
        ctx.pushMethod(RenderMethod.RENDER_BLOCK_BY_RENDER_TYPE);
        ctx.setRenderType(RenderType.BLOCK);
        ctx.setRenderBlocks((RenderBlocks) (Object) this);
        ctx.setBlockAccess(this.blockAccess);
        ctx.setBlock(block);
        ctx.setX(x);
        ctx.setY(y);
        ctx.setZ(z);
        if (this.blockAccess != null) {
            ctx.setMeta(this.blockAccess.getBlockMetadata(x, y, z));
        }
    }

    @Inject(method = "renderBlockByRenderType", at = @At("RETURN"))
    private void onRenderBlockByRenderTypeEnd(CallbackInfoReturnable<Boolean> cir) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.popMethod();
        ctx.setRenderBlocks(null);
        ctx.setBlockAccess(null);
        ctx.setBlock(null);
        ctx.setX(0);
        ctx.setY(0);
        ctx.setZ(0);
        ctx.setMeta(0);
    }

    @Inject(method = "renderStandardBlockWithAmbientOcclusion", at = @At("HEAD"))
    private void onRenderStandardBlockAOStart(Block block, int x, int y, int z, float f1, float f2, float f3,
        CallbackInfoReturnable<Boolean> cir) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.get();
        ctx.pushMethod(RenderMethod.RENDER_STANDARD_BLOCK_WITH_AO);
        ctx.setRenderType(RenderType.BLOCK);
        ctx.setRenderBlocks((RenderBlocks) (Object) this);
        ctx.setBlockAccess(this.blockAccess);
        ctx.setBlock(block);
        ctx.setX(x);
        ctx.setY(y);
        ctx.setZ(z);
        if (this.blockAccess != null) {
            ctx.setMeta(this.blockAccess.getBlockMetadata(x, y, z));
        }
    }

    @Inject(method = "renderStandardBlockWithAmbientOcclusion", at = @At("RETURN"))
    private void onRenderStandardBlockAOEnd(CallbackInfoReturnable<Boolean> cir) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.popMethod();
        ctx.setRenderBlocks(null);
        ctx.setBlockAccess(null);
        ctx.setBlock(null);
        ctx.setX(0);
        ctx.setY(0);
        ctx.setZ(0);
        ctx.setMeta(0);
    }

    @Inject(method = "renderStandardBlock", at = @At("HEAD"))
    private void onRenderStandardBlockStart(Block block, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.get();
        ctx.pushMethod(RenderMethod.RENDER_STANDARD_BLOCK);
        ctx.setRenderType(RenderType.BLOCK);
        ctx.setRenderBlocks((RenderBlocks) (Object) this);
        ctx.setBlockAccess(this.blockAccess);
        ctx.setBlock(block);
        ctx.setX(x);
        ctx.setY(y);
        ctx.setZ(z);
        if (this.blockAccess != null) {
            ctx.setMeta(this.blockAccess.getBlockMetadata(x, y, z));
        }
    }

    @Inject(method = "renderStandardBlock", at = @At("RETURN"))
    private void onRenderStandardBlockEnd(CallbackInfoReturnable<Boolean> cir) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.popMethod();
        ctx.setRenderBlocks(null);
        ctx.setBlockAccess(null);
        ctx.setBlock(null);
        ctx.setX(0);
        ctx.setY(0);
        ctx.setZ(0);
        ctx.setMeta(0);
    }

    @Inject(method = "renderBlockAsItem", at = @At("HEAD"))
    private void onRenderBlockAsItemStart(Block block, int metadata, float color, CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.get();
        ctx.pushMethod(RenderMethod.RENDER_BLOCK_AS_ITEM);
        ctx.setRenderType(RenderType.BLOCK_AS_ITEM);
        ctx.setRenderBlocks((RenderBlocks) (Object) this);
        ctx.setBlock(block);
        ctx.setMeta(metadata);
    }

    @Inject(method = "renderBlockAsItem", at = @At("RETURN"))
    private void onRenderBlockAsItemEnd(CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.popMethod();
        ctx.setRenderBlocks(null);
        ctx.setBlock(null);
        ctx.setMeta(0);
    }

    @Inject(method = "renderFaceYPos", at = @At("RETURN"))
    private void onRenderFaceYPosEnd(CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.setCurrentFace(null);
        ctx.setCurrentIcon(null);
        ctx.popMethod();
    }

    @Inject(method = "renderFaceYNeg", at = @At("RETURN"))
    private void onRenderFaceYNegEnd(CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.setCurrentFace(null);
        ctx.setCurrentIcon(null);
        ctx.popMethod();
    }

    @Inject(method = "renderFaceXPos", at = @At("RETURN"))
    private void onRenderFaceXPosEnd(CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.setCurrentFace(null);
        ctx.setCurrentIcon(null);
        ctx.popMethod();
    }

    @Inject(method = "renderFaceXNeg", at = @At("RETURN"))
    private void onRenderFaceXNegEnd(CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.setCurrentFace(null);
        ctx.setCurrentIcon(null);
        ctx.popMethod();
    }

    @Inject(method = "renderFaceZPos", at = @At("RETURN"))
    private void onRenderFaceZPosEnd(CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.setCurrentFace(null);
        ctx.setCurrentIcon(null);
        ctx.popMethod();
    }

    @Inject(method = "renderFaceZNeg", at = @At("RETURN"))
    private void onRenderFaceZNegEnd(CallbackInfo ci) {
        RenderInvocationContext ctx = RenderInvocationContextHolder.getIfAvailable();
        if (ctx == null) return;

        ctx.setCurrentFace(null);
        ctx.setCurrentIcon(null);
        ctx.popMethod();
    }

}
