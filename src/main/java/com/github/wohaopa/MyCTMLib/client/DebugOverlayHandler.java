package com.github.wohaopa.MyCTMLib.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderPipeline;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DebugOverlayHandler {

    private static final RenderPipeline PIPELINE = new RenderPipeline();

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (!MyCTMLib.debugMode || event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectType.BLOCK) {
            return;
        }
        int x = mop.blockX;
        int y = mop.blockY;
        int z = mop.blockZ;
        World world = mc.theWorld;
        Block block = world.getBlock(x, y, z);

        List<String> lines = new ArrayList<>();
        String blockId = (String) Block.blockRegistry.getNameForObject(block);
        int meta = world.getBlockMetadata(x, y, z);
        lines.add(
            "§f§lBlock:§r " + (blockId != null ? blockId
                : block.getClass()
                    .getSimpleName())
                + " meta: "
                + meta);

        int hitSide = Math.min(mop.sideHit, 5);
        ForgeDirection hitFace = ForgeDirection.getOrientation(hitSide);

        RenderContext ctx = new RenderContext();
        ctx.setBlockAccess(world);
        ctx.setBlock(block);
        ctx.setBlockX(x);
        ctx.setBlockY(y);
        ctx.setBlockZ(z);
        ctx.setMeta(meta);
        ctx.setFace(hitFace);
        ctx.setOriginalIcon(block.getIcon(hitSide, meta));
        ctx.setRenderBlocks(new net.minecraft.client.renderer.RenderBlocks(world));

        PIPELINE.executeDryRun(ctx);

        lines.add("§f§l========== RENDER CONTEXT ==========");
        lines.add("Block: " + safeStr(ctx.getBlock()));
        lines.add(
            "Position: (" + (int) Math.round(ctx.getBlockX())
                + ", "
                + (int) Math.round(ctx.getBlockY())
                + ", "
                + (int) Math.round(ctx.getBlockZ())
                + ")");
        lines.add("Face: " + ctx.getFace());
        lines.add("Meta: " + ctx.getMeta());
        lines.add("§a§lRenderLevel:§r " + safeStr(ctx.getRenderLevel()));
        if (ctx.getBakedModel() != null) {
            lines.add(
                "§a§lBakedModel:§r " + ctx.getBakedModel()
                    .getAllQuads()
                    .size() + " quads");
        }
        lines.add("drewAny: " + ctx.isDrewAny());
        if (ctx.getCtmSprite() != null) {
            lines.add(
                "CTM Sprite: " + ctx.getCtmSprite()
                    .getIconName());
        }

        if (!ctx.getLog()
            .isEmpty()) {
            lines.add("");
            lines.add("§f§l========== LOG ==========");
            lines.addAll(
                ctx.getLog()
                    .getLines());
        }

        int lineHeight = mc.fontRenderer.FONT_HEIGHT;
        int xPos = 4;
        int yPos = 4;
        for (String line : lines) {
            mc.fontRenderer.drawStringWithShadow(line, xPos, yPos, 0xFFFFFF);
            yPos += lineHeight + 2;
        }
    }

    private static String safeStr(Object obj) {
        return obj != null ? obj.toString() : "null";
    }
}
