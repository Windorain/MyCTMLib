package com.github.wohaopa.MyCTMLib.render.context;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderBranch;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;

import lombok.Getter;
import lombok.Setter;

public class RenderContext {

    private static final ThreadLocal<RenderContext> THREAD_LOCAL = ThreadLocal.withInitial(RenderContext::new);

    public static RenderContext get() {
        RenderContext ctx = THREAD_LOCAL.get();
        ctx.reset();
        return ctx;
    }

    // ========== 输入参数 ==========
    @Getter @Setter private IIcon originalIcon;
    @Getter @Setter private RenderBlocks renderBlocks;
    @Getter @Setter private IBlockAccess blockAccess;
    @Getter @Setter private Block block;
    @Getter @Setter private double blockX, blockY, blockZ;
    @Getter @Setter private int meta;
    @Getter @Setter private ForgeDirection face;

    // ========== 分支决策 ==========
    @Getter @Setter private RenderBranch renderBranch;

    // ========== Model 分支 ==========
    @Getter @Setter private String modelId;
    @Getter @Setter private ModelData modelData;
    @Getter @Setter private List<ModelElement> elements = Collections.emptyList();
    @Getter @Setter private ModelElement currentElement;
    @Getter @Setter private int currentElementIndex;
    @Getter @Setter private ConnectionPredicate connectionPredicate;

    // ========== CTM 重定向 ==========
    @Getter @Setter private CTMTextureAtlasSprite ctmSprite;

    // ========== UV 缓存 ==========
    @Getter @Setter private double iconMinU;
    @Getter @Setter private double iconMaxU;
    @Getter @Setter private double iconMinV;
    @Getter @Setter private double iconMaxV;

    // ========== 几何缓存（最终参数）==========
    @Getter @Setter private double drawRelMinX, drawRelMaxX;
    @Getter @Setter private double drawRelMinY, drawRelMaxY;
    @Getter @Setter private double drawRelMinZ, drawRelMaxZ;

    // ========== 绘制 UV（最终参数）==========
    @Getter @Setter private double drawMinU, drawMaxU;
    @Getter @Setter private double drawMinV, drawMaxV;

    // ========== 世界坐标（最终参数）==========
    @Getter @Setter private double worldX, worldY, worldZ;

    // ========== 亮度（最终参数）==========
    @Getter @Setter private int drawBrightness;
    @Getter @Setter private int brightnessTL, brightnessTR, brightnessBL, brightnessBR;

    // ========== 颜色（最终参数）==========
    @Getter @Setter private float colorTL_R, colorTL_G, colorTL_B;
    @Getter @Setter private float colorTR_R, colorTR_G, colorTR_B;
    @Getter @Setter private float colorBL_R, colorBL_G, colorBL_B;
    @Getter @Setter private float colorBR_R, colorBR_G, colorBR_B;

    // ========== 生物群系着色 ==========
    @Getter @Setter private int biomeColor;

    // ========== 状态 ==========
    @Getter @Setter private boolean drewAny;
    @Getter @Setter private boolean pipelineFailed;
    @Getter @Setter private String failureReason;
    @Getter private final PipelineDebugTrace debugTrace = new PipelineDebugTrace();

    // ========== 面计数 ==========
    private final int[] faceRenderCount = new int[6];

    private RenderContext() {}

    public void reset() {
        this.drewAny = false;
        this.pipelineFailed = false;
        this.failureReason = null;
        this.debugTrace.clear();
        this.renderBranch = null;
        this.modelId = null;
        this.modelData = null;
        this.elements = Collections.emptyList();
        this.currentElement = null;
        this.currentElementIndex = 0;
        this.ctmSprite = null;
        this.connectionPredicate = null;
        this.iconMinU = 0;
        this.iconMaxU = 0;
        this.iconMinV = 0;
        this.iconMaxV = 0;
        this.drawRelMinX = 0;
        this.drawRelMaxX = 0;
        this.drawRelMinY = 0;
        this.drawRelMaxY = 0;
        this.drawRelMinZ = 0;
        this.drawRelMaxZ = 0;
        this.drawMinU = 0;
        this.drawMaxU = 0;
        this.drawMinV = 0;
        this.drawMaxV = 0;
        this.worldX = 0;
        this.worldY = 0;
        this.worldZ = 0;
        this.drawBrightness = 0;
        this.biomeColor = 0;
        this.colorTL_R = 0;
        this.colorTL_G = 0;
        this.colorTL_B = 0;
        this.colorTR_R = 0;
        this.colorTR_G = 0;
        this.colorTR_B = 0;
        this.colorBL_R = 0;
        this.colorBL_G = 0;
        this.colorBL_B = 0;
        this.colorBR_R = 0;
        this.colorBR_G = 0;
        this.colorBR_B = 0;
        this.brightnessTL = 0;
        this.brightnessTR = 0;
        this.brightnessBL = 0;
        this.brightnessBR = 0;
    }

    public int getFaceRenderCount(ForgeDirection face) {
        return faceRenderCount[face.ordinal()];
    }

    public void incrementFaceRenderCount(ForgeDirection face) {
        faceRenderCount[face.ordinal()]++;
    }

    public void resetFaceRenderCount() {
        Arrays.fill(faceRenderCount, 0);
    }

    public boolean isDebug() {
        if (!com.github.wohaopa.MyCTMLib.MyCTMLib.debugMode) {
            return false;
        }

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.objectMouseOver == null
            || mc.objectMouseOver.typeOfHit != net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK) {
            return false;
        }

        int focusX = mc.objectMouseOver.blockX;
        int focusY = mc.objectMouseOver.blockY;
        int focusZ = mc.objectMouseOver.blockZ;

        return (int) getBlockX() == focusX && (int) getBlockY() == focusY && (int) getBlockZ() == focusZ;
    }

    public void trace(String msg) {
        if (!isDebug()) return;
        debugTrace.trace(msg);
    }

    public void trace(Supplier<String> msgSupplier) {
        if (!isDebug()) return;
        debugTrace.trace(msgSupplier.get());
    }

    public void debug(String msg) {
        if (!isDebug()) return;
        debugTrace.debug(msg);
    }

    public void debug(Supplier<String> msgSupplier) {
        if (!isDebug()) return;
        debugTrace.debug(msgSupplier.get());
    }

    public void info(String msg) {
        if (!isDebug()) return;
        debugTrace.info(msg);
    }

    public void info(Supplier<String> msgSupplier) {
        if (!isDebug()) return;
        debugTrace.info(msgSupplier.get());
    }

    public void warn(String msg) {
        if (!isDebug()) return;
        debugTrace.warn(msg);
    }

    public void warn(Supplier<String> msgSupplier) {
        if (!isDebug()) return;
        debugTrace.warn(msgSupplier.get());
    }

    public void error(String msg) {
        if (isDebug()) {
            debugTrace.error(msg);
        }
        failPipeline(msg);
    }

    public void error(Supplier<String> msgSupplier) {
        if (isDebug()) {
            debugTrace.error(msgSupplier.get());
        }
        failPipeline(msgSupplier.get());
    }

    public void failPipeline(String reason) {
        this.pipelineFailed = true;
        this.failureReason = reason;
        debug("PIPELINE FAILED: " + reason);
    }

    public void resetPipelineFailed() {
        this.pipelineFailed = false;
        this.failureReason = null;
    }

    public boolean needsBiomeTinting() {
        return ctmSprite != null && ctmSprite.getTinting() != null;
    }
}
