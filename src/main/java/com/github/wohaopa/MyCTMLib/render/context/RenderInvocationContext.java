package com.github.wohaopa.MyCTMLib.render.context;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class RenderInvocationContext {

    private final RenderMethod[] methodStack = new RenderMethod[64];
    private int stackTop = -1;

    // Debug: 记录栈操作历史（环形队列）
    private final String[] debugLog = new String[256];
    private int debugLogIndex = 0;
    private boolean debugLogFull = false;

    private RenderType renderType;

    private final int[] faceRenderCount = new int[6];
    private final boolean[] faceRendered = new boolean[6];

    private RenderBlocks renderBlocks;
    private IBlockAccess blockAccess;
    private Block block;
    private double x, y, z;
    private int meta;

    private ForgeDirection currentFace;
    private IIcon currentIcon;
    private String iconName;

    private Entity entity;
    private ItemStack itemStack;
    private double renderX, renderY, renderZ;
    private float entityYaw;

    public void reset() {
        Arrays.fill(methodStack, null);
        stackTop = -1;
        renderType = null;
        Arrays.fill(faceRenderCount, 0);
        Arrays.fill(faceRendered, false);
        renderBlocks = null;
        blockAccess = null;
        block = null;
        x = y = z = 0;
        meta = 0;
        currentFace = null;
        currentIcon = null;
        iconName = null;
        entity = null;
        itemStack = null;
        renderX = renderY = renderZ = 0;
        entityYaw = 0;
        // 不清空 debug 日志索引和标志，保留跨 reset() 调用的历史记录
    }

    private void logDebug(String operation) {
        debugLog[debugLogIndex] = operation + " (stackTop=" + stackTop + ")";
        debugLogIndex++;
        if (debugLogIndex >= debugLog.length) {
            debugLogIndex = 0;
            debugLogFull = true;
        }
    }

    public void pushMethod(RenderMethod method) {
        stackTop++;
        methodStack[stackTop] = method;
        logDebug("PUSH " + method);
    }

    public void popMethod() {
        if (stackTop < 0) {
            throw new IllegalStateException(
                "RenderInvocationContext stack underflow! " + "push/pop mismatch detected. "
                    + "This indicates a bug in MixinRenderBlocks injection logic. "
                    + "Current stackTop: "
                    + stackTop
                    + ", renderType: "
                    + renderType
                    + "\n=== Stack Operation History ===\n"
                    + getDebugLogString());
        }
        logDebug("POP " + methodStack[stackTop]);
        methodStack[stackTop] = null;
        stackTop--;
    }

    public String getDebugLogString() {
        StringBuilder sb = new StringBuilder();

        int totalEntries = debugLogFull ? debugLog.length : debugLogIndex;
        int numToShow = Math.min(100, totalEntries);

        for (int i = 0; i < numToShow; i++) {
            int logIndex;
            if (debugLogFull) {
                logIndex = (debugLogIndex + (totalEntries - numToShow) + i) % debugLog.length;
            } else {
                logIndex = (totalEntries - numToShow) + i;
            }
            sb.append("  [")
                .append(i)
                .append("] ")
                .append(debugLog[logIndex])
                .append("\n");
        }
        return sb.toString();
    }

    public int getStackTop() {
        return stackTop;
    }

    public RenderMethod[] getMethodStack() {
        return methodStack;
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public void setRenderType(RenderType renderType) {
        this.renderType = renderType;
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

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public int getMeta() {
        return meta;
    }

    public void setMeta(int meta) {
        this.meta = meta;
    }

    public ForgeDirection getCurrentFace() {
        return currentFace;
    }

    public void setCurrentFace(ForgeDirection currentFace) {
        this.currentFace = currentFace;
    }

    public IIcon getCurrentIcon() {
        return currentIcon;
    }

    public void setCurrentIcon(IIcon currentIcon) {
        this.currentIcon = currentIcon;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public int getFaceRenderCount(ForgeDirection face) {
        return faceRenderCount[face.ordinal()];
    }

    public void incrementFaceRenderCount(ForgeDirection face) {
        faceRenderCount[face.ordinal()]++;
    }

    public boolean isFaceRendered(ForgeDirection face) {
        return faceRendered[face.ordinal()];
    }

    public void setFaceRendered(ForgeDirection face, boolean rendered) {
        faceRendered[face.ordinal()] = rendered;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public double getRenderX() {
        return renderX;
    }

    public double getRenderY() {
        return renderY;
    }

    public double getRenderZ() {
        return renderZ;
    }

    public void setRenderPos(double x, double y, double z) {
        this.renderX = x;
        this.renderY = y;
        this.renderZ = z;
    }

    public float getEntityYaw() {
        return entityYaw;
    }

    public void setEntityYaw(float entityYaw) {
        this.entityYaw = entityYaw;
    }
}
