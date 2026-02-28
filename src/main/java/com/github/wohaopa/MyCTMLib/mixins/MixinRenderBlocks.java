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

import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;

@Mixin(RenderBlocks.class)
public abstract class MixinRenderBlocks {

    @Shadow
    public IBlockAccess blockAccess;

    @Shadow
    public abstract boolean hasOverrideBlockTexture();

    @Inject(method = "renderBlockByRenderType", at = @At("HEAD"), cancellable = true)
    private void onRenderBlockByRenderType(Block block, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        int meta = this.blockAccess != null ? this.blockAccess.getBlockMetadata(x, y, z) : 0;

        if (CTMRenderEntry.renderPipeline((RenderBlocks) (Object) this, this.blockAccess, block, x, y, z, meta)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "renderFaceYNeg", at = @At("HEAD"), cancellable = true)
    private void renderFaceYNeg(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (iIcon == null) return;
        if (this.hasOverrideBlockTexture()) return;

        int meta = this.blockAccess != null ? this.blockAccess.getBlockMetadata((int) x, (int) y, (int) z) : 0;

        if (blockAccess == null) {
            if (CTMRenderEntry
                .tryRenderItemFace((RenderBlocks) (Object) this, block, x, y, z, iIcon, ForgeDirection.DOWN)) {
                ci.cancel();
            }
            return;
        }

        if (CTMRenderEntry.renderPipeline(
            (RenderBlocks) (Object) this,
            this.blockAccess,
            block,
            x,
            y,
            z,
            meta,
            ForgeDirection.DOWN,
            iIcon)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFaceYPos", at = @At("HEAD"), cancellable = true)
    private void renderFaceYPos(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (iIcon == null) return;
        if (this.hasOverrideBlockTexture()) return;

        int meta = this.blockAccess != null ? this.blockAccess.getBlockMetadata((int) x, (int) y, (int) z) : 0;

        if (blockAccess == null) {
            if (CTMRenderEntry
                .tryRenderItemFace((RenderBlocks) (Object) this, block, x, y, z, iIcon, ForgeDirection.UP)) {
                ci.cancel();
            }
            return;
        }

        if (CTMRenderEntry.renderPipeline(
            (RenderBlocks) (Object) this,
            this.blockAccess,
            block,
            x,
            y,
            z,
            meta,
            ForgeDirection.UP,
            iIcon)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFaceZNeg", at = @At("HEAD"), cancellable = true)
    private void renderFaceZNeg(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (iIcon == null) return;
        if (this.hasOverrideBlockTexture()) return;

        int meta = this.blockAccess != null ? this.blockAccess.getBlockMetadata((int) x, (int) y, (int) z) : 0;

        if (blockAccess == null) {
            if (CTMRenderEntry
                .tryRenderItemFace((RenderBlocks) (Object) this, block, x, y, z, iIcon, ForgeDirection.NORTH)) {
                ci.cancel();
            }
            return;
        }

        if (CTMRenderEntry.renderPipeline(
            (RenderBlocks) (Object) this,
            this.blockAccess,
            block,
            x,
            y,
            z,
            meta,
            ForgeDirection.NORTH,
            iIcon)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFaceZPos", at = @At("HEAD"), cancellable = true)
    private void renderFaceZPos(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (iIcon == null) return;
        if (this.hasOverrideBlockTexture()) return;

        int meta = this.blockAccess != null ? this.blockAccess.getBlockMetadata((int) x, (int) y, (int) z) : 0;

        if (blockAccess == null) {
            if (CTMRenderEntry
                .tryRenderItemFace((RenderBlocks) (Object) this, block, x, y, z, iIcon, ForgeDirection.SOUTH)) {
                ci.cancel();
            }
            return;
        }

        if (CTMRenderEntry.renderPipeline(
            (RenderBlocks) (Object) this,
            this.blockAccess,
            block,
            x,
            y,
            z,
            meta,
            ForgeDirection.SOUTH,
            iIcon)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFaceXNeg", at = @At("HEAD"), cancellable = true)
    private void renderFaceXNeg(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (iIcon == null) return;
        if (this.hasOverrideBlockTexture()) return;

        int meta = this.blockAccess != null ? this.blockAccess.getBlockMetadata((int) x, (int) y, (int) z) : 0;

        if (blockAccess == null) {
            if (CTMRenderEntry
                .tryRenderItemFace((RenderBlocks) (Object) this, block, x, y, z, iIcon, ForgeDirection.WEST)) {
                ci.cancel();
            }
            return;
        }

        if (CTMRenderEntry.renderPipeline(
            (RenderBlocks) (Object) this,
            this.blockAccess,
            block,
            x,
            y,
            z,
            meta,
            ForgeDirection.WEST,
            iIcon)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFaceXPos", at = @At("HEAD"), cancellable = true)
    private void renderFaceXPos(Block block, double x, double y, double z, IIcon iIcon, CallbackInfo ci) {
        if (iIcon == null) return;
        if (this.hasOverrideBlockTexture()) return;

        int meta = this.blockAccess != null ? this.blockAccess.getBlockMetadata((int) x, (int) y, (int) z) : 0;

        if (blockAccess == null) {
            if (CTMRenderEntry
                .tryRenderItemFace((RenderBlocks) (Object) this, block, x, y, z, iIcon, ForgeDirection.EAST)) {
                ci.cancel();
            }
            return;
        }

        if (CTMRenderEntry.renderPipeline(
            (RenderBlocks) (Object) this,
            this.blockAccess,
            block,
            x,
            y,
            z,
            meta,
            ForgeDirection.EAST,
            iIcon)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStandardBlockWithAmbientOcclusion", at = @At("HEAD"))
    private void onRenderStandardBlockAOStart(Block block, int x, int y, int z, float f1, float f2, float f3,
        CallbackInfoReturnable<Boolean> cir) {
        RenderContext ctx = RenderContext.get();
        ctx.resetFaceRenderCount();
        ctx.setRenderBlocks((RenderBlocks) (Object) this);
        ctx.setBlockAccess(this.blockAccess);
        ctx.setBlock(block);
        ctx.setBlockX(x);
        ctx.setBlockY(y);
        ctx.setBlockZ(z);
        if (this.blockAccess != null) {
            ctx.setMeta(this.blockAccess.getBlockMetadata(x, y, z));
        }
    }

    @Inject(method = "renderStandardBlock", at = @At("HEAD"))
    private void onRenderStandardBlockStart(Block block, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        RenderContext ctx = RenderContext.get();
        ctx.resetFaceRenderCount();
        ctx.setRenderBlocks((RenderBlocks) (Object) this);
        ctx.setBlockAccess(this.blockAccess);
        ctx.setBlock(block);
        ctx.setBlockX(x);
        ctx.setBlockY(y);
        ctx.setBlockZ(z);
        if (this.blockAccess != null) {
            ctx.setMeta(this.blockAccess.getBlockMetadata(x, y, z));
        }
    }

    @Inject(method = "renderBlockAsItem", at = @At("HEAD"))
    private void onRenderBlockAsItemStart(Block block, int metadata, float color, CallbackInfo ci) {
        RenderContext ctx = RenderContext.get();
        ctx.setRenderBlocks((RenderBlocks) (Object) this);
        ctx.setBlock(block);
        ctx.setMeta(metadata);
    }
}
