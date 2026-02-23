package com.github.wohaopa.MyCTMLib.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace.LogEntry;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace.LogLevel;
import com.github.wohaopa.MyCTMLib.render.PipelineInfo;
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
        String blockId = getBlockId(block);
        int meta = world.getBlockMetadata(x, y, z);
        lines.add("§f§lBlock:§r " + (blockId != null ? blockId : block.getClass().getSimpleName()) + " meta: " + meta);

        int hitSide = Math.min(mop.sideHit, 5);
        ForgeDirection hitFace = ForgeDirection.getOrientation(hitSide);

        PipelineDebugTrace newPipelineTrace = RenderPipelineDebugCache.get(x, y, z, hitFace);

        if (newPipelineTrace != null && !newPipelineTrace.getSteps().isEmpty()) {
            addNewPipelineInfo(lines, newPipelineTrace);
        } else {
            lines.add("§7=== NO NEW PIPELINE DATA ===");
        }

        lines.add("§f---");

        addOldPipelineInfo(lines, world, block, x, y, z, hitFace, hitSide);

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
        Boolean fallbackToTryRender = null;
        Boolean tryRenderResult = null;
        String degradationReason = trace.getDegradationReason();

        for (String step : trace.getSteps()) {
            if (step.startsWith("Branch: ")) {
                branch = step.substring("Branch: ".length());
            } else if (step.startsWith("New pipeline drewAny: ")) {
                drewAny = Boolean.parseBoolean(step.substring("New pipeline drewAny: ".length()));
            } else if (step.equals("Falling back to tryRender()")) {
                fallbackToTryRender = true;
            } else if (step.startsWith("tryRender() result: ")) {
                tryRenderResult = Boolean.parseBoolean(step.substring("tryRender() result: ".length()));
            }
        }

        String status = "§7UNKNOWN";
        if (Boolean.TRUE.equals(drewAny)) {
            status = "§aSUCCESS§r (drew directly)";
        } else if (Boolean.TRUE.equals(tryRenderResult)) {
            status = "§eFALLBACK§r (tryRender succeeded)";
        } else if (degradationReason != null) {
            status = "§4FAILED§r (vanilla fallback)";
        } else if (fallbackToTryRender != null) {
            status = "§eFALLBACK§r (to tryRender)";
        }

        lines.add("§f§lStatus:§r " + status);
        if (branch != null) {
            lines.add("§f§lBranch:§r " + branch);
        }
        if (drewAny != null) {
            lines.add("§f§ldrewAny:§r " + drewAny);
        }
        if (fallbackToTryRender != null) {
            lines.add("§f§lFallback to tryRender():§r " + fallbackToTryRender);
        }
        if (tryRenderResult != null) {
            lines.add("§f§ltryRender() result:§r " + tryRenderResult);
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

        boolean hasDetails = pred != null || tile != null || bits != null || 
                            synced != null || spriteName != null ||
                            iconUV != null || drawUV != null || gridInfo != null || textureKey != null;

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

    private void addOldPipelineInfo(List<String> lines, World world, Block block, int x, int y, int z,
        ForgeDirection hitFace, int hitSide) {
        lines.add("§f§l========== OLD PIPELINE ==========");

        StringBuilder summary = new StringBuilder("§fpipeline: §r");
        for (int s = 0; s < 6; s++) {
            if (s > 0) summary.append(" ");
            ForgeDirection face = ForgeDirection.getOrientation(s);
            PipelineInfo info = CTMRenderEntry.getPipelineInfo(world, block, x, y, z, face);
            summary.append(SIDE_NAMES[s])
                .append(":")
                .append(shortName(info.getType()));
        }
        lines.add(summary.toString());

        PipelineDebugTrace oldTrace = new PipelineDebugTrace();
        PipelineInfo hitInfo = CTMRenderEntry.getPipelineInfo(world, block, x, y, z, hitFace, oldTrace);
        lines.add("§fface: §r" + SIDE_NAMES_FULL[hitSide] + " §7|§r pipeline: " + shortName(hitInfo.getType()));
        lines.add("§ficon: §r" + hitInfo.getIconName());
        switch (hitInfo.getType()) {
            case MODEL:
                lines.add("§fmodelId: §r" + hitInfo.getModelId());
                lines.add("§ftextureKey: §r" + hitInfo.getTextureKey());
                if (oldTrace.getTexRegGetIconLookupKey() != null) {
                    Boolean synced = oldTrace.getTexRegTexMapSynced();
                    lines.add(
                        "§fTexReg/TexMap: §r" + (Boolean.TRUE.equals(synced) ? "§asynced"
                            : "§4OUT OF SYNC§r (getIcon null, fallback to block icon)"));
                }
                addDrawSpriteLine(lines, oldTrace);
                addLayoutMaskLine(lines, hitInfo);
                break;
            case TEXTURE_REGISTRY:
                lines.add("§flookupKey: §r" + hitInfo.getIconName());
                addDrawSpriteLine(lines, oldTrace);
                addLayoutMaskLine(lines, hitInfo);
                break;
            case LEGACY:
                lines.add("§flegacy: §rctmIconMap");
                break;
            case VANILLA:
                if (hitInfo.getSkipReason() != null && !hitInfo.getSkipReason().isEmpty()) {
                    lines.add("§fskip: §r" + hitInfo.getSkipReason());
                }
                break;
        }

        addPredicateTileConnLine(lines, oldTrace);
        if (oldTrace.getDegradationReason() != null && !oldTrace.getDegradationReason().isEmpty()) {
            lines.add("§fdegrade: §r" + oldTrace.getDegradationReason());
        }
        addTruncatedDecisionSteps(lines, oldTrace);
    }

    private static void addDrawSpriteLine(List<String> lines, PipelineDebugTrace trace) {
        if (trace.getDrawSpriteName() == null) return;
        String loaded = trace.getDrawSpriteLoaded();
        String suffix = "";
        if (loaded != null) {
            if (loaded.contains("0x0")) suffix = " §7(unloaded?)";
            else if ("16x16".equals(loaded)) suffix = " §7(16x16?)";
        }
        lines.add(
            "§fdrawSprite: §r" + trace.getDrawSpriteName()
                + (loaded != null && !loaded.isEmpty() ? " §7" + loaded : "")
                + suffix);
    }

    private static void addLayoutMaskLine(List<String> lines, PipelineInfo hitInfo) {
        if (hitInfo.getLayout() == null) return;
        if (hitInfo.getMask() >= 0) {
            lines.add("§flayout: §r" + hitInfo.getLayout() + " §fmask: §70x" + Integer.toHexString(hitInfo.getMask()));
        } else {
            lines.add("§flayout: §r" + hitInfo.getLayout());
        }
    }

    private static void addPredicateTileConnLine(List<String> lines, PipelineDebugTrace trace) {
        String pred = trace.getPredicateUsed();
        int[] tile = trace.getTilePos();
        int[] bits = trace.getConnectionBits();
        if (pred == null && tile == null && bits == null) return;
        
        StringBuilder sb = new StringBuilder();
        if (pred != null) sb.append("§fpred: §r").append(pred);
        if (tile != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("§ftile:(§r").append(tile[0]).append(",").append(tile[1]).append("§f)");
        }
        if (bits != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("§fconn:§r");
            for (int i = 0; i < 8; i++) {
                if (i > 0) sb.append(" ");
                sb.append(bits[i]);
            }
        }
        if (sb.length() > 0) lines.add(sb.toString());
    }

    private static void addTruncatedDecisionSteps(List<String> lines, PipelineDebugTrace trace) {
        List<String> steps = trace.getSteps();
        if (steps.isEmpty()) return;
        
        lines.add("§f§l--- decision ---");

        final String[] DECISION_KEYWORDS = { "getIcon", "HIT", "null", "miss", "degrade", "OUT OF SYNC", "不同步" };

        List<String> keySteps = new ArrayList<>();
        List<String> rest = new ArrayList<>();
        for (String s : steps) {
            boolean key = false;
            if (s != null) {
                for (String kw : DECISION_KEYWORDS) {
                    if (s.contains(kw)) {
                        key = true;
                        break;
                    }
                }
            }
            if (key) keySteps.add(s);
            else rest.add(s);
        }
        int maxSteps = 5;
        int shown = 0;
        for (String s : keySteps) {
            if (shown >= maxSteps) break;
            lines.add("§7" + s);
            shown++;
        }
        for (String s : rest) {
            if (shown >= maxSteps) break;
            lines.add("§7" + s);
            shown++;
        }
        if (steps.size() > maxSteps) {
            lines.add("§7... (" + (steps.size() - maxSteps) + " more)");
        }
    }

    private static String shortName(PipelineInfo.PipelineType t) {
        switch (t) {
            case MODEL:
                return "§bModel";
            case TEXTURE_REGISTRY:
                return "§dTexReg";
            case LEGACY:
                return "§cLegacy";
            case VANILLA:
                return "§7Vanilla";
            default:
                return t.name();
        }
    }

    private static String getBlockId(Block block) {
        if (block == null) return null;
        Iterator<?> it = Block.blockRegistry.getKeys().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            if (key instanceof String && Block.blockRegistry.getObject(key) == block) {
                return (String) key;
            }
        }
        return null;
    }
}
