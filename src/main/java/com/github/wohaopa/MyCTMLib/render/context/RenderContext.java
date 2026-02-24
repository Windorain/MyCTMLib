package com.github.wohaopa.MyCTMLib.render.context;

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

/**
 * 渲染上下文（ThreadLocal 单例）
 * 
 * 职责：
 * - 存储计算结果数据（Domain 方法输出）
 * - 控制标志
 * - Debug 支持
 * - 输入数据委托给 RenderInvocationContext
 */
public class RenderContext {

    // ========== ThreadLocal 单例 ==========
    private static final ThreadLocal<RenderContext> THREAD_LOCAL =
        ThreadLocal.withInitial(RenderContext::new);

    public static RenderContext get() {
        RenderContext ctx = THREAD_LOCAL.get();
        ctx.reset();
        return ctx;
    }

    // ========== 最终字段 ==========
    private final RenderInvocationContext invocationContext;
    private final PipelineDebugTrace debugTrace = new PipelineDebugTrace();

    // ========== 输入字段（委托给 invocationContext，不存储） ==========
    // renderBlocks → invocationContext.getRenderBlocks()
    // blockAccess → invocationContext.getBlockAccess()
    // block → invocationContext.getBlock()
    // x/y/z → invocationContext.getX()/getY()/getZ()
    // meta → invocationContext.getMeta()
    // face → invocationContext.getCurrentFace()
    // originalIcon → invocationContext.getCurrentIcon()

    // ========== 决策结果 ==========
    private RenderBranch renderBranch;

    // ========== 数据字段（Domain 方法设置） ==========
    private String modelId;
    private ModelData modelData;
    private List<ModelElement> elements = Collections.emptyList();
    private ModelElement currentElement;
    private int currentElementIndex;

    // 材质相关
    private TextureTypeData textureData;
    private IIcon drawIcon;
    private BaseTextureData baseData;
    private String textureKey;

    // 连接谓词
    private ConnectionPredicate connectionPredicate;

    // ========== 计算字段（基本类型） ==========
    // Icon 数据域
    private double iconMinU;
    private double iconMaxU;
    private double iconMinV;
    private double iconMaxV;
    private int gridW;
    private int gridH;

    // Tile 数据域
    private int tileX;
    private int tileY;
    private int connectionMask;
    private int randomIndex;
    private int[] tilePosition;

    // 几何数据域
    private double drawRelMinX;
    private double drawRelMaxX;
    private double drawRelMinY;
    private double drawRelMaxY;
    private double drawRelMinZ;
    private double drawRelMaxZ;

    // UV 数据域
    private double drawMinU;
    private double drawMaxU;
    private double drawMinV;
    private double drawMaxV;

    // 世界坐标
    private double worldX;
    private double worldY;
    private double worldZ;

    // 着色相关
    private int drawBrightness;
    private int biomeColor;

    // 颜色四角
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

    // 亮度四角
    private int brightnessTL;
    private int brightnessTR;
    private int brightnessBL;
    private int brightnessBR;

    // ========== 控制标志 ==========
    private boolean drewAny;
    private boolean pipelineFailed;
    private String failureReason;

    private RenderContext() {
        this.invocationContext = RenderInvocationContextHolder.getIfAvailable();
    }

    public void reset() {
        this.drewAny = false;
        this.pipelineFailed = false;
        this.failureReason = null;
        this.debugTrace.clear();
    }

    // ========== 决策结果存储 ==========
    public RenderBranch getRenderBranch() {
        return renderBranch;
    }

    public void setRenderBranch(RenderBranch branch) {
        this.renderBranch = branch;
    }

    // ========== Model 数据 Getter/Setter ==========
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

    // ========== 输入数据 Getter（委托给 invocationContext） ==========
    public RenderBlocks getRenderBlocks() {
        return invocationContext.getRenderBlocks();
    }

    public IBlockAccess getBlockAccess() {
        return invocationContext.getBlockAccess();
    }

    public Block getBlock() {
        return invocationContext.getBlock();
    }

    public double getX() {
        return invocationContext.getX();
    }

    public double getY() {
        return invocationContext.getY();
    }

    public double getZ() {
        return invocationContext.getZ();
    }

    public int getMeta() {
        return invocationContext.getMeta();
    }

    public ForgeDirection getFace() {
        return invocationContext.getCurrentFace();
    }

    public IIcon getOriginalIcon() {
        return invocationContext.getCurrentIcon();
    }

    public String getIconName() {
        return invocationContext.getIconName();
    }

    public boolean isItemRender() {
        RenderType type = invocationContext.getRenderType();
        return type == RenderType.ITEM || type == RenderType.BLOCK_AS_ITEM;
    }

    public RenderType getRenderType() {
        return invocationContext.getRenderType();
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
    public int getConnectionMask() {
        return connectionMask;
    }

    public void setConnectionMask(int mask) {
        this.connectionMask = mask;
    }

    public int getRandomIndex() {
        return randomIndex;
    }

    public void setRandomIndex(int index) {
        this.randomIndex = index;
    }

    public int[] getTilePosition() {
        return tilePosition;
    }

    public void setTilePosition(int[] pos) {
        this.tilePosition = pos;
    }

    // ========== 原始 UV（只读） ==========
    public double getIconMinU() {
        return iconMinU;
    }

    public double getIconMaxU() {
        return iconMaxU;
    }

    public double getIconMinV() {
        return iconMinV;
    }

    public double getIconMaxV() {
        return iconMaxV;
    }

    public void setIconMinU(double u) {
        this.iconMinU = u;
    }

    public void setIconMaxU(double u) {
        this.iconMaxU = u;
    }

    public void setIconMinV(double v) {
        this.iconMinV = v;
    }

    public void setIconMaxV(double v) {
        this.iconMaxV = v;
    }

    // ========== Tile 位置 ==========
    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public void setTileX(int x) {
        this.tileX = x;
    }

    public void setTileY(int y) {
        this.tileY = y;
    }

    // ========== Grid 尺寸 ==========
    public int getGridW() {
        return gridW;
    }

    public int getGridH() {
        return gridH;
    }

    public void setGridW(int w) {
        this.gridW = w;
    }

    public void setGridH(int h) {
        this.gridH = h;
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

    // ========== 世界坐标 Getter/Setter ==========
    public double getWorldX() {
        return worldX;
    }

    public void setWorldX(double x) {
        this.worldX = x;
    }

    public double getWorldY() {
        return worldY;
    }

    public void setWorldY(double y) {
        this.worldY = y;
    }

    public double getWorldZ() {
        return worldZ;
    }

    public void setWorldZ(double z) {
        this.worldZ = z;
    }

    // ========== 着色相关 Getter/Setter ==========
    public int getDrawBrightness() {
        return drawBrightness;
    }

    public void setDrawBrightness(int brightness) {
        this.drawBrightness = brightness;
    }

    public int getBiomeColor() {
        return biomeColor;
    }

    public void setBiomeColor(int color) {
        this.biomeColor = color;
    }

    public boolean needsBiomeTinting() {
        return baseData != null && baseData.getTinting() != null;
    }

    // ========== 颜色四角 Getter/Setter ==========
    public float getColorTL_R() {
        return colorTL_R;
    }

    public void setColorTL_R(float r) {
        this.colorTL_R = r;
    }

    public float getColorTL_G() {
        return colorTL_G;
    }

    public void setColorTL_G(float g) {
        this.colorTL_G = g;
    }

    public float getColorTL_B() {
        return colorTL_B;
    }

    public void setColorTL_B(float b) {
        this.colorTL_B = b;
    }

    public float getColorTR_R() {
        return colorTR_R;
    }

    public void setColorTR_R(float r) {
        this.colorTR_R = r;
    }

    public float getColorTR_G() {
        return colorTR_G;
    }

    public void setColorTR_G(float g) {
        this.colorTR_G = g;
    }

    public float getColorTR_B() {
        return colorTR_B;
    }

    public void setColorTR_B(float b) {
        this.colorTR_B = b;
    }

    public float getColorBL_R() {
        return colorBL_R;
    }

    public void setColorBL_R(float r) {
        this.colorBL_R = r;
    }

    public float getColorBL_G() {
        return colorBL_G;
    }

    public void setColorBL_G(float g) {
        this.colorBL_G = g;
    }

    public float getColorBL_B() {
        return colorBL_B;
    }

    public void setColorBL_B(float b) {
        this.colorBL_B = b;
    }

    public float getColorBR_R() {
        return colorBR_R;
    }

    public void setColorBR_R(float r) {
        this.colorBR_R = r;
    }

    public float getColorBR_G() {
        return colorBR_G;
    }

    public void setColorBR_G(float g) {
        this.colorBR_G = g;
    }

    public float getColorBR_B() {
        return colorBR_B;
    }

    public void setColorBR_B(float b) {
        this.colorBR_B = b;
    }

    // ========== 亮度四角 Getter/Setter ==========
    public int getBrightnessTL() {
        return brightnessTL;
    }

    public void setBrightnessTL(int b) {
        this.brightnessTL = b;
    }

    public int getBrightnessTR() {
        return brightnessTR;
    }

    public void setBrightnessTR(int b) {
        this.brightnessTR = b;
    }

    public int getBrightnessBL() {
        return brightnessBL;
    }

    public void setBrightnessBL(int b) {
        this.brightnessBL = b;
    }

    public int getBrightnessBR() {
        return brightnessBR;
    }

    public void setBrightnessBR(int b) {
        this.brightnessBR = b;
    }

    public ConnectionPredicate getConnectionPredicate() {
        return connectionPredicate;
    }

    public void setConnectionPredicate(ConnectionPredicate predicate) {
        this.connectionPredicate = predicate;
    }

    // ========== Debug ==========
    public PipelineDebugTrace getDebugTrace() {
        return debugTrace;
    }

    /**
     * 检查当前渲染的方块是否为调试目标（光标指向的方块）
     * 
     * <p>
     * <strong>判断条件：</strong>
     * </p>
     * <ul>
     * <li>{@code MyCTMLib.debugMode = true}</li>
     * <li>当前渲染坐标与光标指向的方块坐标一致</li>
     * </ul>
     * 
     * <p>
     * <strong>用途：</strong>只在渲染光标方块时创建 debug trace，大幅减少性能开销。
     * 即使 {@code debugMode=true}，其他方块也不会创建 trace。
     * </p>
     * 
     * @return true 当且仅当 debugMode=true 且当前方块是光标指向的方块
     */
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

        return (int) getX() == focusX
            && (int) getY() == focusY
            && (int) getZ() == focusZ;
    }

    // ========== Debug 日志（支持懒加载） ==========
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

    public String getFailureReason() {
        return failureReason;
    }

    public void resetPipelineFailed() {
        pipelineFailed = false;
        failureReason = null;
    }
}
