package com.github.wohaopa.MyCTMLib.render.phases;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;

public class LegacyRenderPhase implements PipelinePhase {
    @Override
    public void process(RenderContext context) {
        RenderBlocks renderBlocks = context.getRenderBlocks();
        IBlockAccess blockAccess = context.getBlockAccess();
        Block block = context.getBlock();
        double x = context.getX();
        double y = context.getY();
        double z = context.getZ();
        IIcon icon = context.getOriginalIcon();
        ForgeDirection face = context.getFace();
        Textures.renderWorldBlock(renderBlocks, blockAccess, block, x, y, z, icon, face);
    }
}
