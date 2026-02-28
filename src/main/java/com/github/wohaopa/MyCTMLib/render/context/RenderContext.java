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
import com.github.wohaopa.MyCTMLib.model.baked.BakedModel;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.render.RenderLog;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderBranch;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderLevel;
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
    @Getter
    @Setter
    private IIcon originalIcon;
    @Getter
    @Setter
    private RenderBlocks renderBlocks;
    @Getter
    @Setter
    private IBlockAccess blockAccess;
    @Getter
    @Setter
    private Block block;
    @Getter
    @Setter
    private double blockX, blockY, blockZ;
    @Getter
    @Setter
    private int meta;
    @Getter
    @Setter
    private ForgeDirection face;

    // ========== 分支决策 ==========
    @Getter
    @Setter
    private RenderBranch renderBranch;
    @Getter
    @Setter
    private RenderLevel renderLevel;
    @Getter
    @Setter
    private BakedModel bakedModel;

    // ========== Model 分支 ==========
    @Getter
    @Setter
    private String modelId;
    @Getter
    @Setter
    private ModelData modelData;
    @Getter
    @Setter
    private List<ModelElement> elements = Collections.emptyList();
    @Getter
    @Setter
    private ModelElement currentElement;
    @Getter
    @Setter
    private int currentElementIndex;
    @Getter
    @Setter
    private ConnectionPredicate connectionPredicate;

    // ========== CTM 重定向 ==========
    @Getter
    @Setter
    private CTMTextureAtlasSprite ctmSprite;

    // ========== UV 缓存 ==========
    @Getter
    @Setter
    private double iconMinU;
    @Getter
    @Setter
    private double iconMaxU;
    @Getter
    @Setter
    private double iconMinV;
    @Getter
    @Setter
    private double iconMaxV;

    // ========== Tile 缓存 ==========
    @Getter
    @Setter
    private int connectionMask;
    @Getter
    @Setter
    private int tileX, tileY;

    // ========== 几何缓存（最终参数）==========
    @Getter
    @Setter
    private double drawRelMinX, drawRelMaxX;
    @Getter
    @Setter
    private double drawRelMinY, drawRelMaxY;
    @Getter
    @Setter
    private double drawRelMinZ, drawRelMaxZ;

    // ========== 绘制 UV（最终参数）==========
    @Getter
    @Setter
    private double drawMinU, drawMaxU;
    @Getter
    @Setter
    private double drawMinV, drawMaxV;

    // ========== 世界坐标（最终参数）==========
    @Getter
    @Setter
    private double worldX, worldY, worldZ;

    // ========== 亮度（最终参数）==========
    @Getter
    @Setter
    private int drawBrightness;
    @Getter
    @Setter
    private int brightnessTL, brightnessTR, brightnessBL, brightnessBR;

    // ========== 颜色（最终参数）==========
    @Getter
    @Setter
    private float colorTL_R, colorTL_G, colorTL_B;
    @Getter
    @Setter
    private float colorTR_R, colorTR_G, colorTR_B;
    @Getter
    @Setter
    private float colorBL_R, colorBL_G, colorBL_B;
    @Getter
    @Setter
    private float colorBR_R, colorBR_G, colorBR_B;

    // ========== 生物群系着色 ==========
    @Getter
    @Setter
    private int biomeColor;

    // ========== enableAO ==========
    @Getter
    @Setter
    private boolean enableAO;

    // ========== 状态 ==========
    @Getter
    @Setter
    private boolean drewAny;
    @Getter
    @Setter
    private boolean pipelineFailed;
    @Getter
    @Setter
    private boolean dryRun;
    @Getter
    private final RenderLog log = new RenderLog();

    // ========== 面计数 ==========
    private final int[] faceRenderCount = new int[6];

    public RenderContext() {}

    public void reset() {
        this.drewAny = false;
        this.pipelineFailed = false;
        this.dryRun = false;
        this.log.clear();
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

    public void trace(String msg) {
        if (!isDryRun()) return;
        log.debug(msg);
    }

    public void trace(Supplier<String> msgSupplier) {
        if (!isDryRun()) return;
        log.debug(msgSupplier.get());
    }

    public void debug(String msg) {
        if (!isDryRun()) return;
        log.debug(msg);
    }

    public void debug(Supplier<String> msgSupplier) {
        if (!isDryRun()) return;
        log.debug(msgSupplier.get());
    }

    public void info(String msg) {
        if (!isDryRun()) return;
        log.info(msg);
    }

    public void info(Supplier<String> msgSupplier) {
        if (!isDryRun()) return;
        log.info(msgSupplier.get());
    }

    public void warn(String msg) {
        if (!isDryRun()) return;
        log.warn(msg);
    }

    public void warn(Supplier<String> msgSupplier) {
        if (!isDryRun()) return;
        log.warn(msgSupplier.get());
    }

    public void error(String msg) {
        if (isDryRun()) {
            log.error(msg);
        }
        failPipeline(msg);
    }

    public void error(Supplier<String> msgSupplier) {
        if (isDryRun()) {
            log.error(msgSupplier.get());
        }
        failPipeline(msgSupplier.get());
    }

    public void failPipeline(String reason) {
        this.pipelineFailed = true;
        log.debug("PIPELINE FAILED: " + reason);
    }

    public void resetPipelineFailed() {
        this.pipelineFailed = false;
    }

    public boolean needsBiomeTinting() {
        return ctmSprite != null && ctmSprite.getTinting() != null;
    }
}
