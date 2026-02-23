package com.github.wohaopa.MyCTMLib.render.context;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderBranch;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace;

/**
 * 渲染上下文（简化版：无状态栈）
 * 
 * 职责：
 * - 存储输入数据（由调用方设置）
 * - 存储决策结果（RenderBranch）
 * - 懒加载查询和缓存数据
 * - 管道失败标志
 * - Debug 支持
 */
public class RenderContext {

    private final RenderInvocationContext invocationContext;

    // ========== 输入数据（调用方设置） ==========
    private RenderBlocks renderBlocks;
    private IBlockAccess blockAccess;
    private Block block;
    private double x, y, z;
    private int meta;
    private ForgeDirection face;
    private IIcon originalIcon;
    private String iconName;
    private String textureKey;

    // ========== 决策结果（RenderPipeline 设置） ==========
    private RenderBranch renderBranch;

    // ========== 懒加载数据（查询 + 缓存） ==========
    // 模型相关
    private String modelId;
    private ModelData modelData;
    private List<ModelElement> elements;
    private ModelElement currentElement;
    private int currentElementIndex;

    // 材质相关
    private TextureTypeData textureData;
    private IIcon drawIcon;
    private BaseTextureData baseData;

    // 纹理坐标相关
    private Integer connectionMask;
    private Integer randomIndex;
    private int[] tilePosition;

    // 新数据流字段
    // 原始 UV（从 drawIcon 获取的，只读，不修改）
    private Double iconMinU, iconMaxU, iconMinV, iconMaxV;
    // Tile 位置（独立存储，分开访问）
    private Integer tileX;
    private Integer tileY;
    // Grid 尺寸
    private Integer gridW;
    private Integer gridH;

    // UV 相关
    private Double drawMinU, drawMaxU, drawMinV, drawMaxV;

    // 几何相关
    private Double drawRelMinX, drawRelMaxX;
    private Double drawRelMinY, drawRelMaxY;
    private Double drawRelMinZ, drawRelMaxZ;

    // 世界坐标（PositionDomain 输出）
    private Double worldX, worldY, worldZ;

    // 着色相关
    private Integer drawBrightness;
    private Integer biomeColor;

    // 颜色四角（ColorDomain 输出）
    private Float colorTL_R, colorTL_G, colorTL_B;
    private Float colorTR_R, colorTR_G, colorTR_B;
    private Float colorBL_R, colorBL_G, colorBL_B;
    private Float colorBR_R, colorBR_G, colorBR_B;

    // 亮度四角（BrightnessDomain 输出）
    private Integer brightnessTL, brightnessTR, brightnessBL, brightnessBR;

    // 连接谓词（Model 分支使用）
    private ConnectionPredicate connectionPredicate;

    // Debug
    private PipelineDebugTrace debugTrace;

    // 控制标志
    private boolean drewAny;
    private boolean pipelineFailed;
    private String failureReason;

    private RenderContext() {
        this.invocationContext = RenderInvocationContextHolder.getIfAvailable();
        this.renderBranch = null;
        this.modelId = null;
        this.modelData = null;
        this.elements = null;
        this.currentElement = null;
        this.currentElementIndex = 0;
        this.textureData = null;
        this.drawIcon = null;
        this.baseData = null;
        this.connectionMask = null;
        this.randomIndex = null;
        this.tilePosition = null;
        // 新数据流字段初始化
        this.iconMinU = null;
        this.iconMaxU = null;
        this.iconMinV = null;
        this.iconMaxV = null;
        this.tileX = null;
        this.tileY = null;
        this.gridW = null;
        this.gridH = null;
        // UV 相关
        this.drawMinU = 0.0;
        this.drawMaxU = 1.0;
        this.drawMinV = 0.0;
        this.drawMaxV = 1.0;
        // 几何相关
        this.drawRelMinX = 0.0;
        this.drawRelMaxX = 1.0;
        this.drawRelMinY = 0.0;
        this.drawRelMaxY = 1.0;
        this.drawRelMinZ = 0.0;
        this.drawRelMaxZ = 1.0;
        // 世界坐标
        this.worldX = null;
        this.worldY = null;
        this.worldZ = null;
        // 着色相关
        this.drawBrightness = 0;
        this.biomeColor = null;
        // 颜色四角
        this.colorTL_R = null;
        this.colorTL_G = null;
        this.colorTL_B = null;
        this.colorTR_R = null;
        this.colorTR_G = null;
        this.colorTR_B = null;
        this.colorBL_R = null;
        this.colorBL_G = null;
        this.colorBL_B = null;
        this.colorBR_R = null;
        this.colorBR_G = null;
        this.colorBR_B = null;
        // 亮度四角
        this.brightnessTL = null;
        this.brightnessTR = null;
        this.brightnessBL = null;
        this.brightnessBR = null;
        // 连接谓词
        this.connectionPredicate = null;
        // Debug
        this.debugTrace = null;
        this.drewAny = false;
        this.pipelineFailed = false;
        this.failureReason = null;
    }

    public static RenderContext create() {
        return new RenderContext();
    }


    // ========== 决策结果存储 ==========
    public RenderBranch getRenderBranch() {
        return renderBranch;
    }

    public void setRenderBranch(RenderBranch branch) {
        this.renderBranch = branch;
    }

    // ========== 懒加载查询（只缓存，不决策） ==========
    public String getModelId() {
        if (modelId == null) {
            String blockId = getBlockId(getBlock());
            if (blockId != null) {
                modelId = BlockStateRegistry.getInstance().getModelId(blockId, getMeta());
            }
        }
        return modelId;
    }

    public ModelData getModelData() {
        if (modelData == null) {
            String modelId = getModelId();
            if (modelId != null) {
                modelData = ModelRegistry.getInstance().get(modelId);
            }
        }
        return modelData;
    }

    public List<ModelElement> getElements() {
        if (elements == null) {
            ModelData modelData = getModelData();
            if (modelData != null) {
                elements = CTMRenderEntry.getElementsWithFace(modelData, getFace());
            }
        }
        return elements != null ? elements : Collections.emptyList();
    }

    // ========== 输入数据 Getter/Setter ==========
    public RenderBlocks getRenderBlocks() {
        return invocationContext != null ? invocationContext.getRenderBlocks() : null;
    }

    public IBlockAccess getBlockAccess() {
        return invocationContext != null ? invocationContext.getBlockAccess() : null;
    }

    public Block getBlock() {
        return invocationContext != null ? invocationContext.getBlock() : null;
    }

    public double getX() {
        return invocationContext != null ? invocationContext.getX() : 0;
    }

    public double getY() {
        return invocationContext != null ? invocationContext.getY() : 0;
    }

    public double getZ() {
        return invocationContext != null ? invocationContext.getZ() : 0;
    }

    public int getMeta() {
        return invocationContext != null ? invocationContext.getMeta() : 0;
    }

    public ForgeDirection getFace() {
        return invocationContext != null ? invocationContext.getCurrentFace() : null;
    }

    public void setFace(ForgeDirection face) {
        if (invocationContext != null) {
            invocationContext.setCurrentFace(face);
        }
    }

    public IIcon getOriginalIcon() {
        return invocationContext != null ? invocationContext.getCurrentIcon() : null;
    }

    public String getIconName() {
        return invocationContext != null ? invocationContext.getIconName() : null;
    }

    public boolean isItemRender() {
        if (invocationContext == null) return false;
        RenderType type = invocationContext.getRenderType();
        return type == RenderType.ITEM || type == RenderType.BLOCK_AS_ITEM;
    }

    public RenderType getRenderType() {
        return invocationContext != null ? invocationContext.getRenderType() : null;
    }

    // ========== 模型/材质相关 Getter/Setter ==========
    public ModelElement getCurrentElement() {
        return currentElement;
    }

    public void setCurrentElement(ModelElement element) {
        this.currentElement = element;
    }

    public int getCurrentElementIndex() {
        return currentElementIndex;
    }

    public void setCurrentElementIndex(int index) {
        this.currentElementIndex = index;
    }

    public TextureTypeData getTextureData() {
        return textureData;
    }

    public void setTextureData(TextureTypeData data) {
        this.textureData = data;
    }

    public IIcon getDrawIcon() {
        return drawIcon;
    }

    public void setDrawIcon(IIcon icon) {
        this.drawIcon = icon;
    }

    public BaseTextureData getBaseData() {
        return baseData;
    }

    public void setBaseData(BaseTextureData data) {
        this.baseData = data;
    }

    public String getTextureKey() {
        return textureKey;
    }

    public void setTextureKey(String key) {
        this.textureKey = key;
    }

    // ========== 纹理坐标相关 Getter/Setter ==========
    public Integer getConnectionMask() {
        return connectionMask;
    }

    public void setConnectionMask(Integer mask) {
        this.connectionMask = mask;
    }

    public boolean hasConnectionMask() {
        return connectionMask != null;
    }

    public Integer getRandomIndex() {
        return randomIndex;
    }

    public void setRandomIndex(Integer index) {
        this.randomIndex = index;
    }

    public int[] getTilePosition() {
        return tilePosition;
    }

    public void setTilePosition(int[] pos) {
        this.tilePosition = pos;
    }

    // ========== 新数据流字段 Getter/Setter ==========
    // 原始 UV（只读）
    public Double getIconMinU() { return iconMinU; }
    public Double getIconMaxU() { return iconMaxU; }
    public Double getIconMinV() { return iconMinV; }
    public Double getIconMaxV() { return iconMaxV; }
    
    public void setIconMinU(double u) { this.iconMinU = u; }
    public void setIconMaxU(double u) { this.iconMaxU = u; }
    public void setIconMinV(double v) { this.iconMinV = v; }
    public void setIconMaxV(double v) { this.iconMaxV = v; }
    
    // TileX, TileY（独立存储）
    public Integer getTileX() { return tileX; }
    public Integer getTileY() { return tileY; }
    
    public void setTileX(int x) { this.tileX = x; }
    public void setTileY(int y) { this.tileY = y; }
    
    // Grid 尺寸
    public Integer getGridW() { return gridW; }
    public Integer getGridH() { return gridH; }
    
    public void setGridW(int w) { this.gridW = w; }
    public void setGridH(int h) { this.gridH = h; }

    // ========== UV 相关 Getter/Setter ==========
    public double getDrawMinU() {
        return drawMinU;
    }

    public void setDrawMinU(double u) {
        this.drawMinU = u;
    }

    public double getDrawMaxU() {
        return drawMaxU;
    }

    public void setDrawMaxU(double u) {
        this.drawMaxU = u;
    }

    public double getDrawMinV() {
        return drawMinV;
    }

    public void setDrawMinV(double v) {
        this.drawMinV = v;
    }

    public double getDrawMaxV() {
        return drawMaxV;
    }

    public void setDrawMaxV(double v) {
        this.drawMaxV = v;
    }

    // ========== 几何相关 Getter/Setter ==========
    public double getDrawRelMinX() {
        return drawRelMinX;
    }

    public void setDrawRelMinX(double x) {
        this.drawRelMinX = x;
    }

    public double getDrawRelMaxX() {
        return drawRelMaxX;
    }

    public void setDrawRelMaxX(double x) {
        this.drawRelMaxX = x;
    }

    public double getDrawRelMinY() {
        return drawRelMinY;
    }

    public void setDrawRelMinY(double y) {
        this.drawRelMinY = y;
    }

    public void setDrawRelMaxY(double y) {
        this.drawRelMaxY = y;
    }

    public double getDrawRelMaxY() {
        return drawRelMaxY;
    }

    public double getDrawRelMinZ() {
        return drawRelMinZ;
    }

    public void setDrawRelMinZ(double z) {
        this.drawRelMinZ = z;
    }

    public double getDrawRelMaxZ() {
        return drawRelMaxZ;
    }

    public void setDrawRelMaxZ(double z) {
        this.drawRelMaxZ = z;
    }

    // ========== 世界坐标 Getter/Setter ==========
    public double getWorldX() {
        return worldX != null ? worldX : 0.0;
    }

    public void setWorldX(double x) {
        this.worldX = x;
    }

    public double getWorldY() {
        return worldY != null ? worldY : 0.0;
    }

    public void setWorldY(double y) {
        this.worldY = y;
    }

    public double getWorldZ() {
        return worldZ != null ? worldZ : 0.0;
    }

    public void setWorldZ(double z) {
        this.worldZ = z;
    }

    // ========== 着色相关 Getter/Setter ==========
    public Integer getDrawBrightness() {
        return drawBrightness;
    }

    public void setDrawBrightness(int brightness) {
        this.drawBrightness = brightness;
    }

    public Integer getBiomeColor() {
        return biomeColor;
    }

    public void setBiomeColor(int color) {
        this.biomeColor = color;
    }

    public boolean needsBiomeTinting() {
        return baseData != null && baseData.getTinting() != null;
    }

    // ========== 颜色四角 Getter/Setter ==========
    public Float getColorTL_R() { return colorTL_R; }
    public void setColorTL_R(float r) { this.colorTL_R = r; }
    public Float getColorTL_G() { return colorTL_G; }
    public void setColorTL_G(float g) { this.colorTL_G = g; }
    public Float getColorTL_B() { return colorTL_B; }
    public void setColorTL_B(float b) { this.colorTL_B = b; }

    public Float getColorTR_R() { return colorTR_R; }
    public void setColorTR_R(float r) { this.colorTR_R = r; }
    public Float getColorTR_G() { return colorTR_G; }
    public void setColorTR_G(float g) { this.colorTR_G = g; }
    public Float getColorTR_B() { return colorTR_B; }
    public void setColorTR_B(float b) { this.colorTR_B = b; }

    public Float getColorBL_R() { return colorBL_R; }
    public void setColorBL_R(float r) { this.colorBL_R = r; }
    public Float getColorBL_G() { return colorBL_G; }
    public void setColorBL_G(float g) { this.colorBL_G = g; }
    public Float getColorBL_B() { return colorBL_B; }
    public void setColorBL_B(float b) { this.colorBL_B = b; }

    public Float getColorBR_R() { return colorBR_R; }
    public void setColorBR_R(float r) { this.colorBR_R = r; }
    public Float getColorBR_G() { return colorBR_G; }
    public void setColorBR_G(float g) { this.colorBR_G = g; }
    public Float getColorBR_B() { return colorBR_B; }
    public void setColorBR_B(float b) { this.colorBR_B = b; }

    // ========== 亮度四角 Getter/Setter ==========
    public Integer getBrightnessTL() { return brightnessTL; }
    public void setBrightnessTL(int b) { this.brightnessTL = b; }
    public Integer getBrightnessTR() { return brightnessTR; }
    public void setBrightnessTR(int b) { this.brightnessTR = b; }
    public Integer getBrightnessBL() { return brightnessBL; }
    public void setBrightnessBL(int b) { this.brightnessBL = b; }
    public Integer getBrightnessBR() { return brightnessBR; }
    public void setBrightnessBR(int b) { this.brightnessBR = b; }

    public ConnectionPredicate getConnectionPredicate() { return connectionPredicate; }
    public void setConnectionPredicate(ConnectionPredicate predicate) { this.connectionPredicate = predicate; }

    // ========== Debug ==========
    public PipelineDebugTrace getDebugTrace() {
        return debugTrace;
    }

    public void setDebugTrace(PipelineDebugTrace trace) {
        this.debugTrace = trace;
    }

    public void trace(String msg) {
        if (debugTrace != null) debugTrace.trace(msg);
    }

    public void debug(String msg) {
        if (debugTrace != null) debugTrace.debug(msg);
    }

    public void info(String msg) {
        if (debugTrace != null) debugTrace.info(msg);
    }

    public void warn(String msg) {
        if (debugTrace != null) debugTrace.warn(msg);
    }

    public void error(String msg) {
        if (debugTrace != null) debugTrace.error(msg);
        failPipeline(msg);
    }

    @Deprecated
    public void logDebug(String message) {
        debug(message);
    }

    // ========== 控制标志 ==========
    public boolean isDrewAny() {
        return drewAny;
    }

    public void setDrewAny(boolean drew) {
        this.drewAny = drew;
    }

    // ========== 管道失败标志 ==========
    public void failPipeline(String reason) {
        pipelineFailed = true;
        failureReason = reason;
        debug("PIPELINE FAILED: " + reason);
    }

    public boolean isPipelineFailed() {
        return pipelineFailed;
    }

    public void resetPipelineFailed() {
        pipelineFailed = false;
        failureReason = null;
    }

    // ========== 工具方法 ==========
    private String getBlockId(Block block) {
        if (block == null) return null;
        return CTMRenderEntry.getBlockId(block);
    }
}
