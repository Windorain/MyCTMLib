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
import com.github.wohaopa.MyCTMLib.render.pipeline.MainRenderState;
import com.github.wohaopa.MyCTMLib.render.pipeline.SubRenderState;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;

public class RenderContext {

    private final RenderBlocks renderBlocks;
    private final IBlockAccess blockAccess;
    private final Block block;
    private final double x, y, z;
    private final int meta;
    private final ForgeDirection face;
    private final IIcon originalIcon;
    private final String iconName;
    private PipelineDebugListener debugListener;
    private final ModelData modelData;
    private final List<ModelElement> elements;
    private final boolean isItemRender;
    private final String modelId;

    private MainRenderState mainState;
    private SubRenderState subState;

    private TextureTypeData textureData;
    private Integer connectionMask;
    private int[] tilePosition;
    private Integer randomIndex;
    private int currentElementIndex;
    private TextureTypeData currentElementTextureData;
    private RenderLayer renderLayer;

    private int brightness;
    private boolean drewAny;
    private String domain;
    private ConnectionPredicate connectionPredicate;
    private double relMinX, relMaxX, relMinY, relMaxY, relMinZ, relMaxZ;

    private RenderContext(RenderContextBuilder builder) {
        this.renderBlocks = builder.renderBlocks;
        this.blockAccess = builder.blockAccess;
        this.block = builder.block;
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
        this.meta = builder.meta;
        this.face = builder.face;
        this.originalIcon = builder.originalIcon;
        this.iconName = builder.originalIcon != null
            ? TextureKeyNormalizer.normalizeIconName(builder.originalIcon.getIconName())
            : null;
        this.debugListener = builder.debugListener;
        this.modelData = builder.modelData;
        this.elements = builder.elements;
        this.isItemRender = builder.isItemRender;
        this.modelId = builder.modelId;
        this.brightness = builder.brightness;
        this.domain = builder.domain;
        this.connectionPredicate = builder.connectionPredicate;
        this.relMinX = builder.relMinX;
        this.relMaxX = builder.relMaxX;
        this.relMinY = builder.relMinY;
        this.relMaxY = builder.relMaxY;
        this.relMinZ = builder.relMinZ;
        this.relMaxZ = builder.relMaxZ;
        this.mainState = MainRenderState.INITIAL;
        this.subState = SubRenderState.NONE;
        this.currentElementIndex = 0;
    }

    public static RenderContextBuilder builder() {
        return new RenderContextBuilder();
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

    public PipelineDebugListener getDebugListener() {
        return debugListener;
    }

    public void setDebugListener(PipelineDebugListener listener) {
        this.debugListener = listener;
    }

    public ModelData getModelData() {
        return modelData;
    }

    public String getModelId() {
        return modelId;
    }

    public List<ModelElement> getElements() {
        return elements;
    }

    public boolean isItemRender() {
        return isItemRender;
    }

    public MainRenderState getMainState() {
        return mainState;
    }

    public void setMainState(MainRenderState state) {
        this.mainState = state;
    }

    public SubRenderState getSubState() {
        return subState;
    }

    public void setSubState(SubRenderState state) {
        this.subState = state;
    }

    public TextureTypeData getTextureData() {
        if (textureData == null) textureData = computeTextureData();
        return textureData;
    }

    public void setTextureData(TextureTypeData data) {
        this.textureData = data;
    }

    public Integer getConnectionMask() {
        return connectionMask;
    }

    public void setConnectionMask(Integer mask) {
        this.connectionMask = mask;
    }

    public int[] getTilePosition() {
        return tilePosition;
    }

    public void setTilePosition(int[] pos) {
        this.tilePosition = pos;
    }

    public Integer getRandomIndex() {
        return randomIndex;
    }

    public void setRandomIndex(Integer index) {
        this.randomIndex = index;
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

    public TextureTypeData getCurrentElementTextureData() {
        return currentElementTextureData;
    }

    public void setCurrentElementTextureData(TextureTypeData data) {
        this.currentElementTextureData = data;
    }

    public RenderLayer getRenderLayer() {
        return renderLayer;
    }

    public void setRenderLayer(RenderLayer layer) {
        this.renderLayer = layer;
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

    public void setConnectionPredicate(ConnectionPredicate predicate) {
        this.connectionPredicate = predicate;
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

    public boolean isDrewAny() {
        return drewAny;
    }

    public void setDrewAny(boolean drewAny) {
        this.drewAny = drewAny;
    }

    private TextureTypeData computeTextureData() {
        if (iconName != null) {
            return TextureRegistry.getInstance()
                .get(iconName);
        }
        return null;
    }

    public RenderInvocationContext getInvocationContext() {
        return RenderInvocationContextHolder.getIfAvailable();
    }

    public static class RenderContextBuilder {

        private RenderBlocks renderBlocks;
        private IBlockAccess blockAccess;
        private Block block;
        private double x, y, z;
        private int meta;
        private ForgeDirection face;
        private IIcon originalIcon;
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

        public RenderContextBuilder connectionPredicate(ConnectionPredicate predicate) {
            this.connectionPredicate = predicate;
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
