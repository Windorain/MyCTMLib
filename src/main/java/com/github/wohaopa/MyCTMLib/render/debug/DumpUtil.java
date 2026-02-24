package com.github.wohaopa.MyCTMLib.render.debug;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.context.RenderInvocationContext;

/**
 * 诊断信息导出工具
 * 
 * 将异常发生时的完整上下文状态导出为 JSON 文件，便于调试分析。
 */
public class DumpUtil {

    private static final Logger LOGGER = LogManager.getLogger("MyCTMLib-Dump");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * 导出完整诊断信息到 JSON 文件
     * 
     * @param ctx   RenderContext
     * @param error 捕获的异常
     * @param outputDir 输出目录
     * @return 生成的 JSON 文件路径
     */
    public static String dumpDiagnostic(RenderContext ctx, Throwable error, File outputDir) {
        // 确保输出目录存在
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                LOGGER.error("Failed to create output directory: {}", outputDir.getAbsolutePath());
                return null;
            }
        }

        // 生成时间戳文件名
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS").format(new Date());
        String filename = "myctmlib_dump_" + timestamp + ".json";
        File outputFile = new File(outputDir, filename);

        try {
            // 构建 JSON 对象
            JsonObject json = new JsonObject();

            // 1. 时间戳
            json.addProperty("timestamp", timestamp);

            // 2. 异常信息
            json.add("exception", buildExceptionJson(error));

            // 3. RenderInvocationContext 状态
            json.add("renderInvocationContext", buildInvocationContextJson(ctx));

            // 4. RenderContext 状态
            json.add("renderContext", buildRenderContextJson(ctx));

            // 5. Block 详细信息
            json.add("blockDetails", buildBlockDetailsJson(ctx));

            // 6. RenderBlocks 状态
            json.add("renderBlocksState", buildRenderBlocksStateJson(ctx));

            // 7. 线程信息
            json.add("threadInfo", buildThreadInfoJson());

            // 8. Mod 信息
            json.add("modInfo", buildModInfoJson(error));

            // 9. 系统信息
            json.add("systemInfo", buildSystemInfoJson());

            // 写入文件
            try (FileWriter writer = new FileWriter(outputFile)) {
                GSON.toJson(json, writer);
            }

            LOGGER.info("Diagnostic dump written to: {}", outputFile.getAbsolutePath());
            
            // 清理旧文件（保留最近 10 个）
            cleanupOldDumps(outputDir, 10);
            
            return outputFile.getAbsolutePath();
            
        } catch (Exception e) {
            LOGGER.error("Failed to write diagnostic dump", e);
            return null;
        }
    }

    /**
     * 构建异常信息 JSON
     */
    private static JsonObject buildExceptionJson(Throwable error) {
        JsonObject json = new JsonObject();
        json.addProperty("type", error.getClass().getName());
        json.addProperty("message", error.getMessage());

        // 调用栈
        JsonArray stackTrace = new JsonArray();
        for (StackTraceElement element : error.getStackTrace()) {
            stackTrace.add(new com.google.gson.JsonPrimitive(element.toString()));
        }
        json.add("stackTrace", stackTrace);

        // 根本原因
        if (error.getCause() != null) {
            json.add("cause", buildExceptionJson(error.getCause()));
        }

        return json;
    }

    /**
     * 构建 RenderInvocationContext 状态 JSON
     */
    private static JsonObject buildInvocationContextJson(RenderContext ctx) {
        JsonObject json = new JsonObject();
        
        if (ctx == null) {
            json.addProperty("error", "RenderContext is null");
            return json;
        }

        try {
            json.addProperty("renderBlocks", safeStr(ctx.getRenderBlocks()));
            json.addProperty("blockAccess", safeStr(ctx.getBlockAccess()));
            json.addProperty("block", safeStr(ctx.getBlock()));
            json.addProperty("x", ctx.getX());
            json.addProperty("y", ctx.getY());
            json.addProperty("z", ctx.getZ());
            json.addProperty("meta", ctx.getMeta());
            json.addProperty("face", safeStr(ctx.getFace()));
            json.addProperty("originalIcon", safeStr(ctx.getOriginalIcon()));
            json.addProperty("iconName", safeStr(ctx.getIconName()));
            json.addProperty("renderType", safeStr(ctx.getRenderType()));
            
            // 方法堆栈（如果能访问）
            try {
                json.addProperty("stackTop", getStackTop(ctx));
                JsonArray methodStack = new JsonArray();
                Object[] stack = getMethodStack(ctx);
                if (stack != null) {
                    for (int i = 0; i <= getStackTop(ctx) && i < stack.length; i++) {
                        methodStack.add(new com.google.gson.JsonPrimitive(safeStr(stack[i])));
                    }
                }
                json.add("methodStack", methodStack);
            } catch (Exception e) {
                json.addProperty("methodStackError", e.getMessage());
            }
            
        } catch (Exception e) {
            json.addProperty("error", "Failed to read context: " + e.getMessage());
        }

        return json;
    }

    /**
     * 构建 RenderContext 状态 JSON
     */
    private static JsonObject buildRenderContextJson(RenderContext ctx) {
        JsonObject json = new JsonObject();
        
        if (ctx == null) {
            json.addProperty("error", "RenderContext is null");
            return json;
        }

        try {
            json.addProperty("renderBranch", safeStr(ctx.getRenderBranch()));
            json.addProperty("textureData", safeStr(ctx.getTextureData()));
            json.addProperty("drawIcon", safeStr(ctx.getDrawIcon()));
            json.addProperty("connectionMask", ctx.getConnectionMask());
            json.addProperty("tileX", ctx.getTileX());
            json.addProperty("tileY", ctx.getTileY());
            json.addProperty("drewAny", ctx.isDrewAny());
            json.addProperty("pipelineFailed", ctx.isPipelineFailed());
            json.addProperty("failureReason", ctx.getFailureReason());
            
            // 懒加载字段
            json.addProperty("modelId", safeStr(ctx.getModelId()));
            json.addProperty("textureKey", safeStr(ctx.getTextureKey()));
            
        } catch (Exception e) {
            json.addProperty("error", "Failed to read context: " + e.getMessage());
        }

        return json;
    }

    /**
     * 构建 Block 详细信息 JSON
     */
    private static JsonObject buildBlockDetailsJson(RenderContext ctx) {
        JsonObject json = new JsonObject();
        
        try {
            Block block = ctx.getBlock();
            if (block != null) {
                json.addProperty("class", block.getClass().getName());
                json.addProperty("unlocalizedName", block.getUnlocalizedName());
                try {
                    json.addProperty("localizedName", block.getLocalizedName());
                } catch (Exception e) {
                    json.addProperty("localizedName", "Error: " + e.getMessage());
                }
            } else {
                json.addProperty("class", "null");
            }
            
            json.addProperty("position", String.format("(%.1f, %.1f, %.1f)", ctx.getX(), ctx.getY(), ctx.getZ()));
            json.addProperty("meta", ctx.getMeta());
            
            if (block != null) {
                json.addProperty("material", safeStr(block.getMaterial()));
            }
            
        } catch (Exception e) {
            json.addProperty("error", "Failed to read block details: " + e.getMessage());
        }

        return json;
    }

    /**
     * 构建 RenderBlocks 状态 JSON
     */
    private static JsonObject buildRenderBlocksStateJson(RenderContext ctx) {
        JsonObject json = new JsonObject();
        
        try {
            RenderBlocks rb = ctx.getRenderBlocks();
            if (rb != null) {
                json.addProperty("renderMinX", rb.renderMinX);
                json.addProperty("renderMaxX", rb.renderMaxX);
                json.addProperty("renderMinY", rb.renderMinY);
                json.addProperty("renderMaxY", rb.renderMaxY);
                json.addProperty("renderMinZ", rb.renderMinZ);
                json.addProperty("renderMaxZ", rb.renderMaxZ);
                json.addProperty("enableAO", rb.enableAO);
            } else {
                json.addProperty("renderBlocks", "null");
            }
            
        } catch (Exception e) {
            json.addProperty("error", "Failed to read RenderBlocks state: " + e.getMessage());
        }

        return json;
    }

    /**
     * 构建线程信息 JSON
     */
    private static JsonObject buildThreadInfoJson() {
        JsonObject json = new JsonObject();
        Thread currentThread = Thread.currentThread();
        json.addProperty("name", currentThread.getName());
        json.addProperty("id", currentThread.getId());
        json.addProperty("state", currentThread.getState().toString());
        json.addProperty("priority", currentThread.getPriority());
        return json;
    }

    /**
     * 构建 Mod 信息 JSON
     */
    private static JsonObject buildModInfoJson(Throwable error) {
        JsonObject json = new JsonObject();
        
        try {
            // 查找调用方的 Mod
            String callingMod = "Unknown";
            String callingClass = "";
            String callingMethod = "";
            int callingLine = 0;
            
            for (StackTraceElement element : error.getStackTrace()) {
                String className = element.getClassName();
                if (className.contains("renderer") && !className.contains("net.minecraft")) {
                    callingClass = className;
                    callingMethod = element.getMethodName();
                    callingLine = element.getLineNumber();
                    
                    // 提取 Mod 名称
                    if (className.startsWith("bartworks.")) callingMod = "bartworks";
                    else if (className.startsWith("gregtech.")) callingMod = "gregtech";
                    else if (className.startsWith("tconstruct.")) callingMod = "tconstruct";
                    else if (className.startsWith("codechicken.")) callingMod = "codechicken";
                    else callingMod = className.split("\\.")[0];
                    break;
                }
            }
            
            json.addProperty("callingMod", callingMod);
            json.addProperty("callingClass", callingClass);
            json.addProperty("callingMethod", callingMethod);
            json.addProperty("callingLine", callingLine);
            
            // 已加载的 Mod 数量（估计值）
            try {
                json.addProperty("loadedModCount", "unknown (1.7.10)");
            } catch (Exception e) {
                json.addProperty("loadedModCount", "unknown");
            }
            
        } catch (Exception e) {
            json.addProperty("error", "Failed to read mod info: " + e.getMessage());
        }

        return json;
    }

    /**
     * 构建系统信息 JSON
     */
    private static JsonObject buildSystemInfoJson() {
        JsonObject json = new JsonObject();
        
        try {
            // Java 信息
            json.addProperty("javaVersion", System.getProperty("java.version"));
            json.addProperty("javaVendor", System.getProperty("java.vendor"));
            json.addProperty("osName", System.getProperty("os.name"));
            json.addProperty("osArch", System.getProperty("os.arch"));
            
            // Minecraft 信息
            json.addProperty("minecraftVersion", "1.7.10");
            
            // 内存信息
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            json.addProperty("heapUsedMB", memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024));
            json.addProperty("heapMaxMB", memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024));
            
            // 运行时间
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            json.addProperty("uptimeSeconds", runtimeBean.getUptime() / 1000);
            
            // MyCTMLib 版本
            json.addProperty("myctmlibVersion", MyCTMLib.MODID + "-dev");
            
            // CPU 核心数
            json.addProperty("cpuCores", Runtime.getRuntime().availableProcessors());
            
        } catch (Exception e) {
            json.addProperty("error", "Failed to read system info: " + e.getMessage());
        }

        return json;
    }

    /**
     * 安全转换为字符串
     */
    private static String safeStr(Object obj) {
        if (obj == null) return "null";
        try {
            return obj.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 清理旧的 dump 文件
     */
    private static void cleanupOldDumps(File outputDir, int keepCount) {
        try {
            File[] dumpFiles = outputDir.listFiles((dir, name) -> name.startsWith("myctmlib_dump_") && name.endsWith(".json"));
            if (dumpFiles == null || dumpFiles.length <= keepCount) {
                return;
            }

            // 按修改时间排序，删除最旧的文件
            java.util.Arrays.sort(dumpFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
            
            int deleteCount = dumpFiles.length - keepCount;
            for (int i = 0; i < deleteCount; i++) {
                if (dumpFiles[i].delete()) {
                    LOGGER.debug("Deleted old dump: {}", dumpFiles[i].getName());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to cleanup old dump files", e);
        }
    }

    // ========== 反射辅助方法（用于访问 RenderInvocationContext 的包私有字段） ==========
    
    private static int getStackTop(RenderContext ctx) {
        try {
            RenderInvocationContext invCtx = (RenderInvocationContext) getFieldValue(ctx, "invocationContext");
            if (invCtx != null) {
                return (Integer) getFieldValue(invCtx, "stackTop");
            }
        } catch (Exception e) {
            // 忽略
        }
        return -1;
    }
    
    private static Object[] getMethodStack(RenderContext ctx) {
        try {
            RenderInvocationContext invCtx = (RenderInvocationContext) getFieldValue(ctx, "invocationContext");
            if (invCtx != null) {
                return (Object[]) getFieldValue(invCtx, "methodStack");
            }
        } catch (Exception e) {
            // 忽略
        }
        return null;
    }
    
    private static Object getFieldValue(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}
