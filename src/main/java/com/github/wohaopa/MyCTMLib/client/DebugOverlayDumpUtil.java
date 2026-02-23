package com.github.wohaopa.MyCTMLib.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace.LogEntry;
import com.github.wohaopa.MyCTMLib.render.debug.RenderPipelineDebugCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * HUD 信息导出工具
 * 
 * <p>将当前指向方块的 HUD 调试信息导出为 JSON 文件。</p>
 */
public class DebugOverlayDumpUtil {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    private DebugOverlayDumpUtil() {}

    /**
     * 导出当前 HUD 信息到文件
     * 
     * @param outputFile 输出文件
     * @throws IOException 写入失败
     */
    public static void dumpCurrentHUDToFile(File outputFile) throws IOException {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            throw new IllegalStateException("World not loaded");
        }

        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectType.BLOCK) {
            throw new IllegalStateException("Not looking at a block");
        }

        int x = mop.blockX;
        int y = mop.blockY;
        int z = mop.blockZ;
        World world = mc.theWorld;
        net.minecraft.block.Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);

        int hitSide = Math.min(mop.sideHit, 5);
        ForgeDirection hitFace = ForgeDirection.getOrientation(hitSide);

        PipelineDebugTrace newTrace = RenderPipelineDebugCache.get(x, y, z, hitFace);

        Map<String, Object> dump = new HashMap<>();

        // 基础信息
        dump.put("block", block.getClass().getName());
        dump.put("blockId", block.getUnlocalizedName());
        dump.put("meta", meta);
        dump.put("position", new int[]{x, y, z});
        dump.put("face", hitFace.name());

        // 新管线信息
        if (newTrace != null && !newTrace.getSteps().isEmpty()) {
            dump.put("newPipeline", buildNewPipelineData(newTrace));
        } else {
            Map<String, Object> noData = new HashMap<>();
            noData.put("available", false);
            noData.put("reason", "No pipeline data cached for this block/face");
            dump.put("newPipeline", noData);
        }

        // 写入文件
        outputFile.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(outputFile)) {
            GSON.toJson(dump, writer);
        }
    }

    /**
     * 构建新管线的 JSON 数据
     */
    private static Map<String, Object> buildNewPipelineData(PipelineDebugTrace trace) {
        Map<String, Object> data = new HashMap<>();

        // 解析步骤信息
        String branch = null;
        Boolean drewAny = null;
        String status = "UNKNOWN";

        for (String step : trace.getSteps()) {
            if (step.startsWith("Branch: ")) {
                branch = step.substring("Branch: ".length());
            } else if (step.startsWith("New pipeline drewAny: ")) {
                drewAny = Boolean.parseBoolean(step.substring("New pipeline drewAny: ".length()));
            }
        }

        if (Boolean.TRUE.equals(drewAny)) {
            status = "SUCCESS (drew directly)";
        } else if (trace.getDegradationReason() != null) {
            status = "FALLBACK (" + trace.getDegradationReason() + ")";
        }

        data.put("available", true);
        data.put("status", status);
        data.put("branch", branch);
        data.put("drewAny", drewAny);

        // 日志
        List<Map<String, String>> logs = new ArrayList<>();
        for (LogEntry entry : trace.getLogs()) {
            Map<String, String> log = new HashMap<>();
            log.put("level", entry.level.name());
            log.put("message", entry.message);
            logs.add(log);
        }
        data.put("logs", logs);

        // 决策步骤
        data.put("decisionSteps", new ArrayList<>(trace.getSteps()));

        // 技术细节
        Map<String, Object> details = new HashMap<>();
        
        if (trace.getTextureKey() != null) {
            details.put("texKey", trace.getTextureKey());
        }
        
        if (trace.getDrawSpriteName() != null) {
            Map<String, Object> spriteInfo = new HashMap<>();
            spriteInfo.put("name", trace.getDrawSpriteName());
            spriteInfo.put("size", trace.getDrawSpriteLoaded());
            details.put("drawSprite", spriteInfo);
        }
        
        if (trace.getTexRegTexMapSynced() != null) {
            details.put("texRegSynced", trace.getTexRegTexMapSynced());
            if (trace.getTexRegGetIconLookupKey() != null) {
                details.put("lookupKey", trace.getTexRegGetIconLookupKey());
            }
        }
        
        if (trace.getIconUV() != null) {
            details.put("iconUV", parseUV(trace.getIconUV()));
        }
        
        if (trace.getDrawUV() != null) {
            details.put("drawUV", parseUV(trace.getDrawUV()));
        }
        
        if (trace.getGridInfo() != null) {
            details.put("grid", trace.getGridInfo());
        }
        
        if (trace.getTilePos() != null) {
            details.put("tile", trace.getTilePos());
        }
        
        if (trace.getConnectionBits() != null) {
            details.put("connection", trace.getConnectionBits());
        }
        
        if (trace.getPredicateUsed() != null) {
            details.put("predicate", trace.getPredicateUsed());
        }

        data.put("details", details);

        return data;
    }

    /**
     * 解析 UV 字符串为数组
     * 格式：[minU,maxU,minV,maxV]
     */
    private static double[] parseUV(String uvString) {
        String content = uvString.substring(1, uvString.length() - 1);
        String[] parts = content.split(",");
        double[] result = new double[4];
        for (int i = 0; i < 4; i++) {
            result[i] = Double.parseDouble(parts[i].trim());
        }
        return result;
    }
}
