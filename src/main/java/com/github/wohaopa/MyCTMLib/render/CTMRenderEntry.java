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

    public static TextureTypeData getConnectingData(String key) {
        return TextureRegistry.getInstance()
            .get(key);
    }

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

    private static final RenderPipeline PIPELINE = new RenderPipeline();

    public static boolean renderPipeline() {
        RenderContext context = RenderContext.get();
        context.getLog().clear();

        try {
            if (context.isDebug()) {
                boolean result = PIPELINE.execute(context);
                return result;
            } else {
                return PIPELINE.execute(context);
            }
        } catch (Throwable t) {
            logContextState(context, t);
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

        LOGGER.error("=== RenderContext Computed Fields ===");
        if (ctx != null) {
            try {
                LOGGER.error("  renderBranch: {}", ctx.getRenderBranch());
                LOGGER.error("  ctmSprite: {}", safeStr(ctx.getCtmSprite()));
                LOGGER.error("  drewAny: {}", ctx.isDrewAny());
                LOGGER.error("  pipelineFailed: {}", ctx.isPipelineFailed());
            } catch (Exception e) {
                LOGGER.error("  Error reading computed fields: {}", e.getMessage());
            }
        }
        LOGGER.error("");

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
