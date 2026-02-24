package com.github.wohaopa.MyCTMLib.texture.layout;

/**
 * 4×4 连接布局：仅 4 邻。连接掩码 bit0=top, bit1=right, bit2=bottom, bit3=left。
 */
public class SimpleLayoutHandler implements LayoutHandler {

    public static final SimpleLayoutHandler INSTANCE = new SimpleLayoutHandler();

    private static final int TOP = 1, RIGHT = 2, BOTTOM = 4, LEFT = 8;

    // 预计算查找表：16 种连接状态 → tile position
    private static final int[][] TILE_TABLE = {
        {0, 0},  // 0b0000: no connections
        {3, 0},  // 0b0001: LEFT
        {3, 1},  // 0b0010: TOP
        {3, 3},  // 0b0011: LEFT | TOP
        {2, 1},  // 0b0100: RIGHT
        {0, 1},  // 0b0101: LEFT | RIGHT
        {1, 1},  // 0b0110: TOP | RIGHT
        {1, 2},  // 0b0111: LEFT | TOP | RIGHT (no bottom)
        {2, 0},  // 0b1000: BOTTOM
        {3, 2},  // 0b1001: LEFT | BOTTOM
        {1, 1},  // 0b1010: TOP | BOTTOM
        {0, 3},  // 0b1011: LEFT | TOP | BOTTOM
        {2, 2},  // 0b1100: RIGHT | BOTTOM
        {1, 3},  // 0b1101: LEFT | RIGHT | BOTTOM
        {2, 3},  // 0b1110: TOP | RIGHT | BOTTOM
        {1, 0}   // 0b1111: all connections
    };

    @Override
    public int getWidth() {
        return 4;
    }

    @Override
    public int getHeight() {
        return 4;
    }

    @Override
    public int[] getTilePosition(int connectionMask) {
        return TILE_TABLE[connectionMask & 0x0F];
    }
}
