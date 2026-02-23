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
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;

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

    // UV 相关
    private Double drawMinU, drawMaxU, drawMinV, drawMaxV;

    // 几何相关
    private Double drawRelMinX, drawRelMaxX;
    private Double drawRelMinY, drawRelMaxY;
    private Double drawRelMinZ, drawRelMaxZ;

    // 着色相关
    private Integer drawBrightness;
    private Integer biomeColor;

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
        this.drawMinU = 0.0;
        this.drawMaxU = 1.0;
        this.drawMinV = 0.0;
        this.drawMaxV = 1.0;
        this.drawRelMinX = 0.0;
        this.drawRelMaxX = 1.0;
        this.drawRelMinY = 0.0;
        this.drawRelMaxY = 1.0;
        this.drawRelMinZ = 0.0;
        this.drawRelMaxZ = 1.0;
        this.drawBrightness = 0;
        this.biomeColor = null;
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
        logDebug("PIPELINE FAILED: " + reason);
    }

    public boolean isPipelineFailed() {
        return pipelineFailed;
    }

    public void resetPipelineFailed() {
        pipelineFailed = false;
        failureReason = null;
    }


    public void logDebug(String message) {
        if (MyCTMLib.debugMode) {
            // 简化调试日志，直接输出
            System.out.println("[CTMLib Debug] " + message);
        }
    }

    // ========== 工具方法 ==========
    private String getBlockId(Block block) {
        if (block == null) return null;
        return CTMRenderEntry.getBlockId(block);
    }
}
