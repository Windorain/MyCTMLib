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
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;

public class RenderContext {

    private static final ThreadLocal<RenderContext> THREAD_LOCAL = ThreadLocal.withInitial(RenderContext::new);

    public static RenderContext get() {
        RenderContext ctx = THREAD_LOCAL.get();
        ctx.reset();
        return ctx;
    }

    private final int[] faceRenderCount = new int[6];

    private IIcon originalIcon;
    private RenderBlocks renderBlocks;
    private IBlockAccess blockAccess;
    private Block block;
    private double blockX, blockY, blockZ;
    private int meta;
    private ForgeDirection face;

    private RenderBranch renderBranch;

    private String modelId;
    private ModelData modelData;
    private List<ModelElement> elements = Collections.emptyList();
    private ModelElement currentElement;
    private int currentElementIndex;

    private TextureTypeData textureData;
    private IIcon drawIcon;
    private BaseTextureData baseData;
    private String textureKey;
    private ConnectionPredicate connectionPredicate;

    private double iconMinU;
    private double iconMaxU;
    private double iconMinV;
    private double iconMaxV;
    private int gridW;
    private int gridH;

    private int tileX;
    private int tileY;
    private int connectionMask;
    private int randomIndex;
    private int[] tilePosition;

    private double drawRelMinX;
    private double drawRelMaxX;
    private double drawRelMinY;
    private double drawRelMaxY;
    private double drawRelMinZ;
    private double drawRelMaxZ;

    private double drawMinU;
    private double drawMaxU;
    private double drawMinV;
    private double drawMaxV;

    private double worldX;
    private double worldY;
    private double worldZ;

    private int drawBrightness;
    private int biomeColor;

    private float colorTL_R;
    private float colorTL_G;
    private float colorTL_B;
    private float colorTR_R;
    private float colorTR_G;
    private float colorTR_B;
    private float colorBL_R;
    private float colorBL_G;
    private float colorBL_B;
    private float colorBR_R;
    private float colorBR_G;
    private float colorBR_B;

    private int brightnessTL;
    private int brightnessTR;
    private int brightnessBL;
    private int brightnessBR;

    private boolean drewAny;
    private boolean pipelineFailed;
    private String failureReason;
    private final PipelineDebugTrace debugTrace = new PipelineDebugTrace();

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
        this.textureData = null;
        this.drawIcon = null;
        this.baseData = null;
        this.textureKey = null;
        this.connectionPredicate = null;
        this.iconMinU = 0;
        this.iconMaxU = 0;
        this.iconMinV = 0;
        this.iconMaxV = 0;
        this.gridW = 0;
        this.gridH = 0;
        this.tileX = 0;
        this.tileY = 0;
        this.connectionMask = 0;
        this.randomIndex = 0;
        this.tilePosition = null;
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

    public IIcon getOriginalIcon() {
        return originalIcon;
    }

    public void setOriginalIcon(IIcon originalIcon) {
        this.originalIcon = originalIcon;
    }

    public RenderBlocks getRenderBlocks() {
        return renderBlocks;
    }

    public void setRenderBlocks(RenderBlocks renderBlocks) {
        this.renderBlocks = renderBlocks;
    }

    public IBlockAccess getBlockAccess() {
        return blockAccess;
    }

    public void setBlockAccess(IBlockAccess blockAccess) {
        this.blockAccess = blockAccess;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public double getBlockX() {
        return blockX;
    }

    public void setBlockX(double blockX) {
        this.blockX = blockX;
    }

    public double getBlockY() {
        return blockY;
    }

    public void setBlockY(double blockY) {
        this.blockY = blockY;
    }

    public double getBlockZ() {
        return blockZ;
    }

    public void setBlockZ(double blockZ) {
        this.blockZ = blockZ;
    }

    public int getMeta() {
        return meta;
    }

    public void setMeta(int meta) {
        this.meta = meta;
    }

    public ForgeDirection getFace() {
        return face;
    }

    public void setFace(ForgeDirection face) {
        this.face = face;
    }

    public RenderBranch getRenderBranch() {
        return renderBranch;
    }

    public void setRenderBranch(RenderBranch renderBranch) {
        this.renderBranch = renderBranch;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public ModelData getModelData() {
        return modelData;
    }

    public void setModelData(ModelData modelData) {
        this.modelData = modelData;
    }

    public List<ModelElement> getElements() {
        return elements;
    }

    public void setElements(List<ModelElement> elements) {
        this.elements = elements;
    }

    public ModelElement getCurrentElement() {
        return currentElement;
    }

    public void setCurrentElement(ModelElement currentElement) {
        this.currentElement = currentElement;
    }

    public int getCurrentElementIndex() {
        return currentElementIndex;
    }

    public void setCurrentElementIndex(int currentElementIndex) {
        this.currentElementIndex = currentElementIndex;
    }

    public TextureTypeData getTextureData() {
        return textureData;
    }

    public void setTextureData(TextureTypeData textureData) {
        this.textureData = textureData;
    }

    public IIcon getDrawIcon() {
        return drawIcon;
    }

    public void setDrawIcon(IIcon drawIcon) {
        this.drawIcon = drawIcon;
    }

    public BaseTextureData getBaseData() {
        return baseData;
    }

    public void setBaseData(BaseTextureData baseData) {
        this.baseData = baseData;
    }

    public String getTextureKey() {
        return textureKey;
    }

    public void setTextureKey(String textureKey) {
        this.textureKey = textureKey;
    }

    public ConnectionPredicate getConnectionPredicate() {
        return connectionPredicate;
    }

    public void setConnectionPredicate(ConnectionPredicate connectionPredicate) {
        this.connectionPredicate = connectionPredicate;
    }

    public double getIconMinU() {
        return iconMinU;
    }

    public void setIconMinU(double iconMinU) {
        this.iconMinU = iconMinU;
    }

    public double getIconMaxU() {
        return iconMaxU;
    }

    public void setIconMaxU(double iconMaxU) {
        this.iconMaxU = iconMaxU;
    }

    public double getIconMinV() {
        return iconMinV;
    }

    public void setIconMinV(double iconMinV) {
        this.iconMinV = iconMinV;
    }

    public double getIconMaxV() {
        return iconMaxV;
    }

    public void setIconMaxV(double iconMaxV) {
        this.iconMaxV = iconMaxV;
    }

    public int getGridW() {
        return gridW;
    }

    public void setGridW(int gridW) {
        this.gridW = gridW;
    }

    public int getGridH() {
        return gridH;
    }

    public void setGridH(int gridH) {
        this.gridH = gridH;
    }

    public int getTileX() {
        return tileX;
    }

    public void setTileX(int tileX) {
        this.tileX = tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public void setTileY(int tileY) {
        this.tileY = tileY;
    }

    public int getConnectionMask() {
        return connectionMask;
    }

    public void setConnectionMask(int connectionMask) {
        this.connectionMask = connectionMask;
    }

    public int getRandomIndex() {
        return randomIndex;
    }

    public void setRandomIndex(int randomIndex) {
        this.randomIndex = randomIndex;
    }

    public int[] getTilePosition() {
        return tilePosition;
    }

    public void setTilePosition(int[] tilePosition) {
        this.tilePosition = tilePosition;
    }

    public double getDrawRelMinX() {
        return drawRelMinX;
    }

    public void setDrawRelMinX(double drawRelMinX) {
        this.drawRelMinX = drawRelMinX;
    }

    public double getDrawRelMaxX() {
        return drawRelMaxX;
    }

    public void setDrawRelMaxX(double drawRelMaxX) {
        this.drawRelMaxX = drawRelMaxX;
    }

    public double getDrawRelMinY() {
        return drawRelMinY;
    }

    public void setDrawRelMinY(double drawRelMinY) {
        this.drawRelMinY = drawRelMinY;
    }

    public double getDrawRelMaxY() {
        return drawRelMaxY;
    }

    public void setDrawRelMaxY(double drawRelMaxY) {
        this.drawRelMaxY = drawRelMaxY;
    }

    public double getDrawRelMinZ() {
        return drawRelMinZ;
    }

    public void setDrawRelMinZ(double drawRelMinZ) {
        this.drawRelMinZ = drawRelMinZ;
    }

    public double getDrawRelMaxZ() {
        return drawRelMaxZ;
    }

    public void setDrawRelMaxZ(double drawRelMaxZ) {
        this.drawRelMaxZ = drawRelMaxZ;
    }

    public double getDrawMinU() {
        return drawMinU;
    }

    public void setDrawMinU(double drawMinU) {
        this.drawMinU = drawMinU;
    }

    public double getDrawMaxU() {
        return drawMaxU;
    }

    public void setDrawMaxU(double drawMaxU) {
        this.drawMaxU = drawMaxU;
    }

    public double getDrawMinV() {
        return drawMinV;
    }

    public void setDrawMinV(double drawMinV) {
        this.drawMinV = drawMinV;
    }

    public double getDrawMaxV() {
        return drawMaxV;
    }

    public void setDrawMaxV(double drawMaxV) {
        this.drawMaxV = drawMaxV;
    }

    public double getWorldX() {
        return worldX;
    }

    public void setWorldX(double worldX) {
        this.worldX = worldX;
    }

    public double getWorldY() {
        return worldY;
    }

    public void setWorldY(double worldY) {
        this.worldY = worldY;
    }

    public double getWorldZ() {
        return worldZ;
    }

    public void setWorldZ(double worldZ) {
        this.worldZ = worldZ;
    }

    public int getDrawBrightness() {
        return drawBrightness;
    }

    public void setDrawBrightness(int drawBrightness) {
        this.drawBrightness = drawBrightness;
    }

    public int getBiomeColor() {
        return biomeColor;
    }

    public void setBiomeColor(int biomeColor) {
        this.biomeColor = biomeColor;
    }

    public float getColorTL_R() {
        return colorTL_R;
    }

    public void setColorTL_R(float colorTL_R) {
        this.colorTL_R = colorTL_R;
    }

    public float getColorTL_G() {
        return colorTL_G;
    }

    public void setColorTL_G(float colorTL_G) {
        this.colorTL_G = colorTL_G;
    }

    public float getColorTL_B() {
        return colorTL_B;
    }

    public void setColorTL_B(float colorTL_B) {
        this.colorTL_B = colorTL_B;
    }

    public float getColorTR_R() {
        return colorTR_R;
    }

    public void setColorTR_R(float colorTR_R) {
        this.colorTR_R = colorTR_R;
    }

    public float getColorTR_G() {
        return colorTR_G;
    }

    public void setColorTR_G(float colorTR_G) {
        this.colorTR_G = colorTR_G;
    }

    public float getColorTR_B() {
        return colorTR_B;
    }

    public void setColorTR_B(float colorTR_B) {
        this.colorTR_B = colorTR_B;
    }

    public float getColorBL_R() {
        return colorBL_R;
    }

    public void setColorBL_R(float colorBL_R) {
        this.colorBL_R = colorBL_R;
    }

    public float getColorBL_G() {
        return colorBL_G;
    }

    public void setColorBL_G(float colorBL_G) {
        this.colorBL_G = colorBL_G;
    }

    public float getColorBL_B() {
        return colorBL_B;
    }

    public void setColorBL_B(float colorBL_B) {
        this.colorBL_B = colorBL_B;
    }

    public float getColorBR_R() {
        return colorBR_R;
    }

    public void setColorBR_R(float colorBR_R) {
        this.colorBR_R = colorBR_R;
    }

    public float getColorBR_G() {
        return colorBR_G;
    }

    public void setColorBR_G(float colorBR_G) {
        this.colorBR_G = colorBR_G;
    }

    public float getColorBR_B() {
        return colorBR_B;
    }

    public void setColorBR_B(float colorBR_B) {
        this.colorBR_B = colorBR_B;
    }

    public int getBrightnessTL() {
        return brightnessTL;
    }

    public void setBrightnessTL(int brightnessTL) {
        this.brightnessTL = brightnessTL;
    }

    public int getBrightnessTR() {
        return brightnessTR;
    }

    public void setBrightnessTR(int brightnessTR) {
        this.brightnessTR = brightnessTR;
    }

    public int getBrightnessBL() {
        return brightnessBL;
    }

    public void setBrightnessBL(int brightnessBL) {
        this.brightnessBL = brightnessBL;
    }

    public int getBrightnessBR() {
        return brightnessBR;
    }

    public void setBrightnessBR(int brightnessBR) {
        this.brightnessBR = brightnessBR;
    }

    public boolean isDrewAny() {
        return drewAny;
    }

    public void setDrewAny(boolean drewAny) {
        this.drewAny = drewAny;
    }

    public boolean isPipelineFailed() {
        return pipelineFailed;
    }

    public void failPipeline(String reason) {
        this.pipelineFailed = true;
        this.failureReason = reason;
        debug("PIPELINE FAILED: " + reason);
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void resetPipelineFailed() {
        this.pipelineFailed = false;
        this.failureReason = null;
    }

    public PipelineDebugTrace getDebugTrace() {
        return debugTrace;
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

    @Deprecated
    public void logDebug(String message) {
        debug(message);
    }

    public boolean needsBiomeTinting() {
        return baseData != null && baseData.getTinting() != null;
    }
}
