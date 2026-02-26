package com.github.wohaopa.MyCTMLib.render;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.debug.DumpUtil;
import com.github.wohaopa.MyCTMLib.render.debug.RenderPipelineDebugCache;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderPipeline;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 新管线渲染入口：在 MixinRenderBlocks 六面 HEAD 处调用。
 * 优先查 BlockStateRegistry → ModelRegistry 得该面纹理与谓词；若无匹配，再查 TextureRegistry(iconName)。
 */
@SideOnly(Side.CLIENT)
public final class CTMRenderEntry {

    private static final Logger LOGGER = LogManager.getLogger("MyCTMLib");


    public static String getBlockId(Block block) {
        return block != null ? (String) Block.blockRegistry.getNameForObject(block) : null;
    }

    public static List<ModelElement> getElementsWithFace(ModelData modelData, ForgeDirection face) {
        List<ModelElement> out = new ArrayList<>();
        for (ModelElement el : modelData.getElements()) {
            if (el.getFace(face) != null) out.add(el);
        }
        return out;
    }

    /**
     * 从 TextureRegistry 查找 ConnectingTextureData。使用 TextureKeyNormalizer 多键回退，
     * 兼容 1.7.10 短名（cobblestone）、模型路径（minecraft:block/cobblestone）等格式。
     */
    public static TextureTypeData getConnectingData(String key) {
        return TextureRegistry.getInstance()
            .get(key);
    }

    /**
     * 物品渲染通道：blockAccess 为 null 时使用。以无连接（默认格 0,0）渲染连接纹理。
     * 用于手持、背包 GUI、物品栏等场景，避免显示整张连接图。
     */
    public static boolean tryRenderItemFace(RenderBlocks renderBlocks, Block block, double x, double y, double z,
        IIcon icon, ForgeDirection face) {
        if (icon == null) return false;
        String iconName = TextureKeyNormalizer.normalizeIconName(icon.getIconName());
        TextureTypeData data = getConnectingData(iconName);
        if (!(data instanceof ConnectingTextureData ctd)) return false;

        ConnectingLayout layout = ctd.getLayout();
        LayoutHandler handler = LayoutHandlers.get(layout);
        int tileX = 0;
        int tileY = 0;
        int brightness = 15728880;

        // 使用 RenderBlocks 的 bounds，支持台阶、楼梯等非标准方块的物品渲染
        double relMinX = renderBlocks.renderMinX;
        double relMaxX = renderBlocks.renderMaxX;
        double relMinY = renderBlocks.renderMinY;
        double relMaxY = renderBlocks.renderMaxY;
        double relMinZ = renderBlocks.renderMinZ;
        double relMaxZ = renderBlocks.renderMaxZ;

        FaceRenderer.drawFace(
            renderBlocks,
            x,
            y,
            z,
            face,
            icon,
            tileX,
            tileY,
            handler.getWidth(),
            handler.getHeight(),
            brightness,
            relMinX,
            relMaxX,
            relMinY,
            relMaxY,
            relMinZ,
            relMaxZ,
            null,
            null,
            0,
            0,
            0);
        return true;
    }

    // ========== 新管线：RenderPipeline 状态机版本 ==========

    private static final RenderPipeline PIPELINE = new RenderPipeline();

    /**
     * 新管线渲染入口
     * 
     * <p>
     * <strong>性能优化：</strong>只在 {@code context.isDebug()=true} 时创建和填充 debug trace，
     * 即同时满足以下条件：
     * </p>
     * <ul>
     * <li>{@code MyCTMLib.debugMode = true}</li>
     * <li>当前渲染的方块是光标指向的方块</li>
     * </ul>
     * <p>
     * 避免在正常运行或渲染非光标方块时产生不必要的对象分配和 HashMap 操作。
     * </p>
     */
    public static boolean renderPipeline() {
        RenderContext context = RenderContext.get();

        try {
            if (context.isDebug()) {
                PipelineDebugTrace trace = context.getDebugTrace();
                trace.addStep(
                    "Position: " + (int) context.getBlockX()
                        + ", "
                        + (int) context.getBlockY()
                        + ", "
                        + (int) context.getBlockZ());
                trace.addStep("Face: " + context.getFace());
                trace.addStep("Icon: " + context.getOriginalIcon());

                boolean result = PIPELINE.execute(context);

                trace.addStep("Branch: " + context.getRenderBranch());
                trace.addStep("New pipeline drewAny: " + context.isDrewAny());

                trace.setConnectionBits(0);
                trace.setTilePos(0, 0);
                CTMTextureAtlasSprite ctmSprite = context.getCtmSprite();
                if (ctmSprite != null) {
                    trace.setDrawSpriteInfo(
                        ctmSprite.getIconName(),
                        ctmSprite.getIconWidth(),
                        ctmSprite.getIconHeight());
                    trace.setTexRegTexMapSync(true, ctmSprite.getIconName());
                    trace.setTextureKey(ctmSprite.getIconName());
                    trace.setGridInfo(ctmSprite.getGridWidth(), ctmSprite.getGridHeight());
                }

                ForgeDirection face = context.getFace();
                RenderPipelineDebugCache.record(
                    (int) context.getBlockX(),
                    (int) context.getBlockY(),
                    (int) context.getBlockZ(),
                    face,
                    trace);

                return result;
            } else {
                // debugMode=false 或 非光标方块：直接执行，不创建任何 debug 对象
                return PIPELINE.execute(context);
            }
        } catch (Throwable t) {
            // 诊断日志：打印两个 Context 的完整状态
            logContextState(context, t);

            // 生成 JSON 诊断文件
            File dumpDir = new File("run/client/diagnostic_dumps");
            String dumpPath = DumpUtil.dumpDiagnostic(context, t, dumpDir);
            if (dumpPath != null) {
                LOGGER.error("Diagnostic dump written to: {}", dumpPath);
            }

            throw t;
        }
    }

    private static void logContextState(RenderContext ctx, Throwable error) {
        LOGGER.error("============================================================");
        LOGGER.error("MyCTMLib RenderPipeline Exception Caught");
        LOGGER.error("============================================================");
        LOGGER.error(
            "Exception Type: {}",
            error.getClass()
                .getName());
        LOGGER.error("Exception Message: {}", error.getMessage());
        LOGGER.error("");

        // 打印完整调用栈
        LOGGER.error("=== Stack Trace ===");
        for (StackTraceElement element : error.getStackTrace()) {
            LOGGER.error("  at {}", element);
        }
        LOGGER.error("");

        LOGGER.error("=== RenderContext State ===");
        if (ctx != null) {
            try {
                LOGGER.error("  renderBlocks: {}", safeStr(ctx.getRenderBlocks()));
                LOGGER.error("  blockAccess: {}", safeStr(ctx.getBlockAccess()));
                LOGGER.error("  block: {}", safeStr(ctx.getBlock()));
                LOGGER.error("  blockX: {}", ctx.getBlockX());
                LOGGER.error("  blockY: {}", ctx.getBlockY());
                LOGGER.error("  blockZ: {}", ctx.getBlockZ());
                LOGGER.error("  meta: {}", ctx.getMeta());
                LOGGER.error("  face: {}", ctx.getFace());
                LOGGER.error("  originalIcon: {}", safeStr(ctx.getOriginalIcon()));
            } catch (Exception e) {
                LOGGER.error("  Error reading context: {}", e.getMessage());
            }
        } else {
            LOGGER.error("  RenderContext is NULL!");
        }
        LOGGER.error("");

        // 打印 RenderContext 计算字段状态
        LOGGER.error("=== RenderContext Computed Fields ===");
        if (ctx != null) {
            try {
                LOGGER.error("  renderBranch: {}", ctx.getRenderBranch());
                LOGGER.error("  ctmSprite: {}", safeStr(ctx.getCtmSprite()));
                LOGGER.error("  drewAny: {}", ctx.isDrewAny());
                LOGGER.error("  pipelineFailed: {}", ctx.isPipelineFailed());
                LOGGER.error("  failureReason: {}", ctx.getFailureReason());
            } catch (Exception e) {
                LOGGER.error("  Error reading computed fields: {}", e.getMessage());
            }
        }
        LOGGER.error("");

        // 线程信息
        LOGGER.error("=== Thread Info ===");
        LOGGER.error(
            "  Thread Name: {}",
            Thread.currentThread()
                .getName());
        LOGGER.error(
            "  Thread ID: {}",
            Thread.currentThread()
                .getId());
        LOGGER.error("");

        LOGGER.error("============================================================");
        LOGGER.error("End of Diagnostic Info");
        LOGGER.error("============================================================");
    }

    private static String safeStr(Object obj) {
        return obj != null ? obj.toString() : "null";
    }

}
