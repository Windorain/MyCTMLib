package com.github.wohaopa.MyCTMLib.render.context;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.debug.PipelineDebugListener;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;

public class RenderContext {

    private final RenderInvocationContext invocationContext;

    private final RenderState[] stateStack = new RenderState[32];
    private int stateStackTop = -1;

    private PipelineDebugListener debugListener;

    private RenderPipelineBranch branch;
    private BlockRenderMode blockSubBranch;
    private String modelId;

    private ModelData modelData;
    private List<ModelElement> elements;
    private boolean modelDataLoaded = false;

    private IIcon drawIcon;
    private TextureTypeData textureData;

    private Integer connectionMask;
    private Integer randomIndex;
    private int[] tilePosition;
    private double drawMinU, drawMaxU, drawMinV, drawMaxV;
    private double drawRelMinX, drawRelMaxX, drawRelMinY, drawRelMaxY, drawRelMinZ, drawRelMaxZ;
    private BaseTextureData baseData;
    private int drawBrightness;

    private int currentElementIndex;
    private ModelElement currentElement;

    private boolean drewAny;

    private RenderContext() {
        this.invocationContext = RenderInvocationContextHolder.getIfAvailable();
        this.stateStackTop = -1;
        this.branch = null;
        this.blockSubBranch = null;
        this.modelId = null;
        this.modelData = null;
        this.elements = null;
        this.modelDataLoaded = false;
        this.drawIcon = null;
        this.textureData = null;
        this.connectionMask = null;
        this.randomIndex = null;
        this.tilePosition = null;
        this.drawMinU = 0;
        this.drawMaxU = 1;
        this.drawMinV = 0;
        this.drawMaxV = 1;
        this.drawRelMinX = 0;
        this.drawRelMaxX = 1;
        this.drawRelMinY = 0;
        this.drawRelMaxY = 1;
        this.drawRelMinZ = 0;
        this.drawRelMaxZ = 1;
        this.baseData = null;
        this.drawBrightness = 0;
        this.currentElementIndex = 0;
        this.currentElement = null;
        this.drewAny = false;
        this.debugListener = null;
    }

    public static RenderContext create() {
        return new RenderContext();
    }

    public void pushState(RenderState state) {
        stateStackTop++;
        stateStack[stateStackTop] = state;
    }

    public RenderState popState() {
        RenderState state = stateStack[stateStackTop];
        stateStack[stateStackTop] = null;
        stateStackTop--;
        return state;
    }

    public RenderState getCurrentState() {
        if (stateStackTop < 0) {
            return null;
        }
        return stateStack[stateStackTop];
    }

    // ========= 委托给 invocationContext 的 getter =========

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

    // ========= 本地字段 getter/setter =========

    public PipelineDebugListener getDebugListener() {
        return debugListener;
    }

    public void setDebugListener(PipelineDebugListener debugListener) {
        this.debugListener = debugListener;
    }

    public RenderPipelineBranch getBranch() {
        return branch;
    }

    public void setBranch(RenderPipelineBranch branch) {
        this.branch = branch;
    }

    public BlockRenderMode getBlockSubBranch() {
        return blockSubBranch;
    }

    public void setBlockSubBranch(BlockRenderMode blockSubBranch) {
        this.blockSubBranch = blockSubBranch;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public boolean hasElements() {
        ensureModelDataLoaded();
        return elements != null && !elements.isEmpty();
    }

    public ConnectionPredicate getConnectionPredicate() {
        ensureModelDataLoaded();
        if (modelData == null) {
            return PredicateRegistry.defaultPredicate();
        }
        
        ModelFace firstFace = getFirstElementFace();
        if (firstFace != null && firstFace.getConnectionKey() != null) {
            ConnectionPredicate p = PredicateRegistry.getPredicate(
                firstFace.getConnectionKey(), modelData.getConnections());
            if (p != null) return p;
        }
        return PredicateRegistry.defaultPredicate();
    }
    
    private ModelFace getFirstElementFace() {
        if (elements == null || elements.isEmpty()) return null;
        ModelElement first = elements.get(0);
        if (first == null) return null;
        return first.getFace(getFace());
    }
    
    public void ensureModelDataLoaded() {
        if (modelDataLoaded) return;
        
        String blockId = CTMRenderEntry.getBlockId(getBlock());
        if (blockId != null) {
            int meta = getMeta();
            String modelId = com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry.getInstance()
                .getModelId(blockId, meta);
            if (modelId != null) {
                this.modelId = modelId;
                this.modelData = com.github.wohaopa.MyCTMLib.model.ModelRegistry.getInstance().get(modelId);
                if (this.modelData != null) {
                    this.elements = CTMRenderEntry.getElementsWithFace(this.modelData, getFace());
                }
            }
        }
        modelDataLoaded = true;
    }

    public ModelData getModelData() {
        ensureModelDataLoaded();
        return modelData;
    }

    public void setModelData(ModelData modelData) {
        this.modelData = modelData;
        this.modelDataLoaded = true;
    }

    public List<ModelElement> getElements() {
        ensureModelDataLoaded();
        return elements;
    }

    public void setElements(List<ModelElement> elements) {
        this.elements = elements;
        this.modelDataLoaded = true;
    }

    public ModelElement getCurrentElement() {
        return elements != null && currentElementIndex < elements.size() 
            ? elements.get(currentElementIndex) 
            : null;
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

    public boolean moveToNextElement() {
        currentElementIndex++;
        return elements != null && currentElementIndex < elements.size();
    }

    public void resetElementIndex() {
        currentElementIndex = 0;
    }

    public IIcon getDrawIcon() {
        return drawIcon;
    }

    public void setDrawIcon(IIcon drawIcon) {
        this.drawIcon = drawIcon;
    }

    public TextureTypeData getTextureData() {
        return textureData;
    }

    public void setTextureData(TextureTypeData textureData) {
        this.textureData = textureData;
    }

    public Integer getConnectionMask() {
        return connectionMask;
    }

    public void setConnectionMask(Integer connectionMask) {
        this.connectionMask = connectionMask;
    }

    public Integer getRandomIndex() {
        return randomIndex;
    }

    public void setRandomIndex(Integer randomIndex) {
        this.randomIndex = randomIndex;
    }

    public int[] getTilePosition() {
        return tilePosition;
    }

    public void setTilePosition(int[] tilePosition) {
        this.tilePosition = tilePosition;
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

    public BaseTextureData getBaseData() {
        return baseData;
    }

    public void setBaseData(BaseTextureData baseData) {
        this.baseData = baseData;
    }

    public int getDrawBrightness() {
        return drawBrightness;
    }

    public void setDrawBrightness(int drawBrightness) {
        this.drawBrightness = drawBrightness;
    }

    public boolean isDrewAny() {
        return drewAny;
    }

    public void setDrewAny(boolean drewAny) {
        this.drewAny = drewAny;
    }
}
