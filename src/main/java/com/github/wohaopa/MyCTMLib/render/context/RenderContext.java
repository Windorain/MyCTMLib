package com.github.wohaopa.MyCTMLib.render.context;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.render.debug.PipelineDebugListener;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderState;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;

public class RenderContext {

    private RenderInvocationContext invocationContext;

    private final RenderState[] stateStack = new RenderState[32];
    private int stateStackTop = -1;

    private PipelineDebugListener debugListener;

    private RenderPipelineBranch branch;
    private BlockRenderMode blockSubBranch;

    private ModelData modelData;
    private List<ModelElement> elements;
    private IIcon drawIcon;
    private TextureTypeData textureData;

    private Integer connectionMask;
    private Integer randomIndex;
    private int[] tilePosition;
    private double minU, maxU, minV, maxV;
    private double relMinX, relMaxX, relMinY, relMaxY, relMinZ, relMaxZ;
    private BaseTextureData baseData;

    private int currentElementIndex;
    private ModelElement currentElement;

    private boolean drewAny;

    private final RenderBlocks renderBlocks;
    private final IBlockAccess blockAccess;
    private final Block block;
    private final double x, y, z;
    private final int meta;
    private final ForgeDirection face;
    private final IIcon originalIcon;
    private final String iconName;
    private final boolean isItemRender;
    private final String modelId;
    private int brightness;
    private String domain;
    private ConnectionPredicate connectionPredicate;

    private RenderContext(RenderContextBuilder builder) {
        this.invocationContext = RenderInvocationContextHolder.getIfAvailable();
        this.stateStackTop = -1;
        this.branch = null;
        this.blockSubBranch = null;
        this.modelData = builder.modelData;
        this.elements = builder.elements;
        this.drawIcon = null;
        this.textureData = null;
        this.connectionMask = null;
        this.randomIndex = null;
        this.tilePosition = null;
        this.minU = 0;
        this.maxU = 1;
        this.minV = 0;
        this.maxV = 1;
        this.relMinX = builder.relMinX;
        this.relMaxX = builder.relMaxX;
        this.relMinY = builder.relMinY;
        this.relMaxY = builder.relMaxY;
        this.relMinZ = builder.relMinZ;
        this.relMaxZ = builder.relMaxZ;
        this.baseData = null;
        this.currentElementIndex = 0;
        this.currentElement = null;
        this.drewAny = false;
        this.debugListener = builder.debugListener;
        this.renderBlocks = builder.renderBlocks;
        this.blockAccess = builder.blockAccess;
        this.block = builder.block;
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
        this.meta = builder.meta;
        this.face = builder.face;
        this.originalIcon = builder.originalIcon;
        this.iconName = builder.iconName;
        this.isItemRender = builder.isItemRender;
        this.modelId = builder.modelId;
        this.brightness = builder.brightness;
        this.domain = builder.domain;
        this.connectionPredicate = builder.connectionPredicate;
    }

    public static RenderContextBuilder builder() {
        return new RenderContextBuilder();
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

    public RenderInvocationContext getInvocationContext() {
        return invocationContext;
    }

    public void setInvocationContext(RenderInvocationContext invocationContext) {
        this.invocationContext = invocationContext;
    }

    public RenderBlocks getRenderBlocks() {
        return renderBlocks;
    }

    public IBlockAccess getBlockAccess() {
        return blockAccess;
    }

    public Block getBlock() {
        return block;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getMeta() {
        return meta;
    }

    public ForgeDirection getFace() {
        return face;
    }

    public IIcon getOriginalIcon() {
        return originalIcon;
    }

    public String getIconName() {
        return iconName;
    }

    public boolean isItemRender() {
        return isItemRender;
    }

    public String getModelId() {
        return modelId;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public ConnectionPredicate getConnectionPredicate() {
        return connectionPredicate;
    }

    public void setConnectionPredicate(ConnectionPredicate connectionPredicate) {
        this.connectionPredicate = connectionPredicate;
    }

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

    public boolean hasElements() {
        return elements != null && !elements.isEmpty();
    }

    public ModelElement getCurrentElement() {
        return elements.get(currentElementIndex);
    }

    public boolean moveToNextElement() {
        currentElementIndex++;
        return currentElementIndex < elements.size();
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

    public double getMinU() {
        return minU;
    }

    public void setMinU(double minU) {
        this.minU = minU;
    }

    public double getMaxU() {
        return maxU;
    }

    public void setMaxU(double maxU) {
        this.maxU = maxU;
    }

    public double getMinV() {
        return minV;
    }

    public void setMinV(double minV) {
        this.minV = minV;
    }

    public double getMaxV() {
        return maxV;
    }

    public void setMaxV(double maxV) {
        this.maxV = maxV;
    }

    public double getRelMinX() {
        return relMinX;
    }

    public void setRelMinX(double relMinX) {
        this.relMinX = relMinX;
    }

    public double getRelMaxX() {
        return relMaxX;
    }

    public void setRelMaxX(double relMaxX) {
        this.relMaxX = relMaxX;
    }

    public double getRelMinY() {
        return relMinY;
    }

    public void setRelMinY(double relMinY) {
        this.relMinY = relMinY;
    }

    public double getRelMaxY() {
        return relMaxY;
    }

    public void setRelMaxY(double relMaxY) {
        this.relMaxY = relMaxY;
    }

    public double getRelMinZ() {
        return relMinZ;
    }

    public void setRelMinZ(double relMinZ) {
        this.relMinZ = relMinZ;
    }

    public double getRelMaxZ() {
        return relMaxZ;
    }

    public void setRelMaxZ(double relMaxZ) {
        this.relMaxZ = relMaxZ;
    }

    public BaseTextureData getBaseData() {
        return baseData;
    }

    public void setBaseData(BaseTextureData baseData) {
        this.baseData = baseData;
    }

    public int getCurrentElementIndex() {
        return currentElementIndex;
    }

    public void setCurrentElementIndex(int currentElementIndex) {
        this.currentElementIndex = currentElementIndex;
    }

    public void setCurrentElement(ModelElement currentElement) {
        this.currentElement = currentElement;
    }

    public boolean isDrewAny() {
        return drewAny;
    }

    public void setDrewAny(boolean drewAny) {
        this.drewAny = drewAny;
    }

    public static class RenderContextBuilder {

        private RenderBlocks renderBlocks;
        private IBlockAccess blockAccess;
        private Block block;
        private double x, y, z;
        private int meta;
        private ForgeDirection face;
        private IIcon originalIcon;
        private String iconName;
        private PipelineDebugListener debugListener;
        private ModelData modelData;
        private List<ModelElement> elements;
        private boolean isItemRender = false;
        private String modelId;
        private int brightness;
        private String domain;
        private ConnectionPredicate connectionPredicate;
        private double relMinX, relMaxX, relMinY, relMaxY, relMinZ, relMaxZ;

        public RenderContextBuilder renderBlocks(RenderBlocks renderBlocks) {
            this.renderBlocks = renderBlocks;
            return this;
        }

        public RenderContextBuilder blockAccess(IBlockAccess blockAccess) {
            this.blockAccess = blockAccess;
            return this;
        }

        public RenderContextBuilder block(Block block) {
            this.block = block;
            return this;
        }

        public RenderContextBuilder x(double x) {
            this.x = x;
            return this;
        }

        public RenderContextBuilder y(double y) {
            this.y = y;
            return this;
        }

        public RenderContextBuilder z(double z) {
            this.z = z;
            return this;
        }

        public RenderContextBuilder meta(int meta) {
            this.meta = meta;
            return this;
        }

        public RenderContextBuilder face(ForgeDirection face) {
            this.face = face;
            return this;
        }

        public RenderContextBuilder originalIcon(IIcon originalIcon) {
            this.originalIcon = originalIcon;
            return this;
        }

        public RenderContextBuilder iconName(String iconName) {
            this.iconName = iconName;
            return this;
        }

        public RenderContextBuilder debugListener(PipelineDebugListener debugListener) {
            this.debugListener = debugListener;
            return this;
        }

        public RenderContextBuilder modelData(ModelData modelData) {
            this.modelData = modelData;
            return this;
        }

        public RenderContextBuilder elements(List<ModelElement> elements) {
            this.elements = elements;
            return this;
        }

        public RenderContextBuilder modelId(String modelId) {
            this.modelId = modelId;
            return this;
        }

        public RenderContextBuilder isItemRender(boolean isItemRender) {
            this.isItemRender = isItemRender;
            return this;
        }

        public RenderContextBuilder brightness(int brightness) {
            this.brightness = brightness;
            return this;
        }

        public RenderContextBuilder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public RenderContextBuilder connectionPredicate(ConnectionPredicate connectionPredicate) {
            this.connectionPredicate = connectionPredicate;
            return this;
        }

        public RenderContextBuilder relMinX(double relMinX) {
            this.relMinX = relMinX;
            return this;
        }

        public RenderContextBuilder relMaxX(double relMaxX) {
            this.relMaxX = relMaxX;
            return this;
        }

        public RenderContextBuilder relMinY(double relMinY) {
            this.relMinY = relMinY;
            return this;
        }

        public RenderContextBuilder relMaxY(double relMaxY) {
            this.relMaxY = relMaxY;
            return this;
        }

        public RenderContextBuilder relMinZ(double relMinZ) {
            this.relMinZ = relMinZ;
            return this;
        }

        public RenderContextBuilder relMaxZ(double relMaxZ) {
            this.relMaxZ = relMaxZ;
            return this;
        }

        public RenderContext build() {
            return new RenderContext(this);
        }
    }
}
