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
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace.LogEntry;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace.LogLevel;
import com.github.wohaopa.MyCTMLib.render.debug.RenderPipelineDebugCache;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DebugOverlayHandler {

    private static final String[] SIDE_NAMES = { "D", "U", "N", "S", "W", "E" };
    private static final String[] SIDE_NAMES_FULL = { "DOWN", "UP", "NORTH", "SOUTH", "WEST", "EAST" };

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

        PipelineDebugTrace newPipelineTrace = RenderPipelineDebugCache.get(x, y, z, hitFace);

        if (newPipelineTrace != null && !newPipelineTrace.getSteps()
            .isEmpty()) {
            addNewPipelineInfo(lines, newPipelineTrace);
        } else {
            lines.add("§7=== NO NEW PIPELINE DATA ===");
        }

        int lineHeight = mc.fontRenderer.FONT_HEIGHT;
        int xPos = 4;
        int yPos = 4;
        for (String line : lines) {
            mc.fontRenderer.drawStringWithShadow(line, xPos, yPos, 0xFFFFFF);
            yPos += lineHeight + 2;
        }
    }

    private void addNewPipelineInfo(List<String> lines, PipelineDebugTrace trace) {
        lines.add("§f§l========== NEW PIPELINE ==========");

        // 1. 状态摘要
        addStatusSummary(lines, trace);

        // 2. 按级别显示日志
        lines.add("§f§l--- Logs ---");
        addLogsByLevel(lines, trace, LogLevel.ERROR, "§4[ERROR] ");
        addLogsByLevel(lines, trace, LogLevel.WARN, "§e[WARN]  ");
        addLogsByLevel(lines, trace, LogLevel.INFO, "§a[INFO]  ");
        addLogsByLevel(lines, trace, LogLevel.DEBUG, "§7[DEBUG] ");
        addLogsByLevel(lines, trace, LogLevel.TRACE, "§8[TRACE] ");

        // 3. 决策步骤（完整显示，不省略）
        lines.add("§f§l--- Decision Steps ---");
        addAllDecisionSteps(lines, trace);

        // 4. 技术细节
        addTechnicalDetails(lines, trace);
    }

    private void addStatusSummary(List<String> lines, PipelineDebugTrace trace) {
        String branch = null;
        Boolean drewAny = null;
        String degradationReason = trace.getDegradationReason();

        for (String step : trace.getSteps()) {
            if (step.startsWith("Branch: ")) {
                branch = step.substring("Branch: ".length());
            } else if (step.startsWith("New pipeline drewAny: ")) {
                drewAny = Boolean.parseBoolean(step.substring("New pipeline drewAny: ".length()));
            }
        }

        String status = "§7UNKNOWN";
        if (Boolean.TRUE.equals(drewAny)) {
            status = "§aSUCCESS§r (drew directly)";
        } else if (degradationReason != null) {
            status = "§4FAILED§r (vanilla fallback)";
        }

        lines.add("§f§lStatus:§r " + status);
        if (branch != null) {
            lines.add("§f§lBranch:§r " + branch);
        }
        if (drewAny != null) {
            lines.add("§f§ldrewAny:§r " + drewAny);
        }
        if (degradationReason != null) {
            lines.add("§f§ldegrade:§r " + degradationReason);
        }
    }

    private void addLogsByLevel(List<String> lines, PipelineDebugTrace trace, LogLevel level, String prefix) {
        List<LogEntry> levelLogs = trace.getLogsByLevel(level);
        if (levelLogs.isEmpty()) return;

        for (LogEntry log : levelLogs) {
            lines.add(prefix + log.message);
        }
    }

    private void addAllDecisionSteps(List<String> lines, PipelineDebugTrace trace) {
        List<String> steps = trace.getSteps();
        if (steps.isEmpty()) {
            lines.add("§7(none)");
            return;
        }

        for (String s : steps) {
            lines.add("§f" + s);
        }
    }

    private void addTechnicalDetails(List<String> lines, PipelineDebugTrace trace) {
        String pred = trace.getPredicateUsed();
        int[] tile = trace.getTilePos();
        int[] bits = trace.getConnectionBits();
        Boolean synced = trace.getTexRegTexMapSynced();
        String lookupKey = trace.getTexRegGetIconLookupKey();
        String spriteName = trace.getDrawSpriteName();
        String spriteLoaded = trace.getDrawSpriteLoaded();

        // 新增 UV 调试信息
        String iconUV = trace.getIconUV();
        String drawUV = trace.getDrawUV();
        String gridInfo = trace.getGridInfo();
        String textureKey = trace.getTextureKey();

        boolean hasDetails = pred != null || tile != null
            || bits != null
            || synced != null
            || spriteName != null
            || iconUV != null
            || drawUV != null
            || gridInfo != null
            || textureKey != null;

        if (!hasDetails) return;

        lines.add("§f§l--- Details ---");

        // 第一行：纹理信息
        if (textureKey != null) {
            lines.add("§ftexKey: §7" + textureKey);
        }
        if (spriteName != null) {
            String loaded = spriteLoaded != null ? " §7" + spriteLoaded : "";
            lines.add("§fdrawSprite: §r" + spriteName + loaded);
        }
        if (synced != null) {
            String syncStatus = Boolean.TRUE.equals(synced) ? "§asynced" : "§4OUT OF SYNC";
            lines.add("§fTexReg/TexMap: §r" + syncStatus + (lookupKey != null ? " (§7" + lookupKey + "§r)" : ""));
        }

        // 第二行：UV 信息
        if (iconUV != null) {
            lines.add("§ficonUV:  §7" + iconUV);
        }
        if (drawUV != null) {
            lines.add("§fdrawUV:  §7" + drawUV);
        }
        if (gridInfo != null) {
            lines.add("§fgrid:    §7" + gridInfo);
        }

        // 第三行：连接信息
        if (tile != null) {
            lines.add("§ftile:    §7(" + tile[0] + "," + tile[1] + ")");
        }
        if (bits != null) {
            StringBuilder sb = new StringBuilder("§fconn:    §7");
            for (int i = 0; i < 8; i++) {
                if (i > 0) sb.append(" ");
                sb.append(bits[i]);
            }
            lines.add(sb.toString());
        }
        if (pred != null) {
            lines.add("§fpred:    §7" + pred);
        }
    }
}
