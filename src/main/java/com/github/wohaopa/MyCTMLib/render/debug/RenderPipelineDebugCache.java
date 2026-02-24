package com.github.wohaopa.MyCTMLib.render.debug;

import java.util.EnumMap;
import java.util.Map;

import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace;

/**
 * 渲染管线调试缓存
 * 
 * <p>
 * <strong>优化策略：</strong>只保存当前光标指向的方块的调试信息，大幅减少内存和 GC 开销。
 * </p>
 * 
 * <p>
 * <strong>工作原理：</strong>
 * <ul>
 * <li>只缓存 1 个方块 × 6 个面的 trace 数据</li>
 * <li>当光标移动到新方块时，自动丢弃旧数据</li>
 * <li>无需清理逻辑，每帧自动替换</li>
 * <li>{@code record()} 方法会检查坐标，只有当前光标方块的数据才会被保存</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>性能优势：</strong>
 * <ul>
 * <li>内存占用恒定：~1KB（1 个方块 × 6 个面）</li>
 * <li>零 GC 压力：无 HashMap 增长</li>
 * <li>O(1) 查询速度：直接访问，无需 hash 计算</li>
 * </ul>
 * </p>
 */
public class RenderPipelineDebugCache {

    /**
     * 当前缓存的方块坐标 key
     * -1 表示无缓存
     */
    private static volatile long currentKey = -1;

    /**
     * 当前方块的 6 个面的 trace 数据
     */
    private static volatile Map<ForgeDirection, PipelineDebugTrace> currentTraces;

    private RenderPipelineDebugCache() {}

    /**
     * 记录调试信息到缓存
     * 
     * <p>
     * <strong>优化逻辑：</strong>
     * </p>
     * <ul>
     * <li>如果坐标与当前缓存的方块不同，自动替换为新方块的数据</li>
     * <li>只保存最新光标方块的 trace，其他方块的 trace 自动丢弃</li>
     * </ul>
     * 
     * @param x     方块 X 坐标
     * @param y     方块 Y 坐标
     * @param z     方块 Z 坐标
     * @param face  面方向
     * @param trace 调试轨迹
     */
    public static void record(int x, int y, int z, ForgeDirection face, PipelineDebugTrace trace) {
        long key = getKey(x, y, z);

        // 检查是否需要更新当前方块
        if (key != currentKey || currentTraces == null) {
            currentKey = key;
            currentTraces = new EnumMap<>(ForgeDirection.class);
        }

        currentTraces.put(face, trace);
    }

    /**
     * 获取指定方块的调试信息
     * 
     * <p>
     * <strong>注意：</strong>只返回当前光标方块的 trace，其他方块返回 null
     * </p>
     * 
     * @param x    方块 X 坐标
     * @param y    方块 Y 坐标
     * @param z    方块 Z 坐标
     * @param face 面方向
     * @return 调试轨迹，如果坐标不匹配或无缓存则返回 null
     */
    public static PipelineDebugTrace get(int x, int y, int z, ForgeDirection face) {
        long key = getKey(x, y, z);

        // 只返回当前光标方块的 trace
        if (key != currentKey) {
            return null;
        }

        return currentTraces != null ? currentTraces.get(face) : null;
    }

    /**
     * 清空缓存
     */
    public static void clear() {
        currentKey = -1;
        currentTraces = null;
    }

    /**
     * 获取当前缓存的方块坐标
     * 
     * @return int[]{x, y, z}，如果无缓存则返回 null
     */
    public static int[] getCurrentTarget() {
        if (currentKey == -1) {
            return null;
        }

        int x = (int) ((currentKey >>> 38) & 0x7FFFFFFFL);
        int y = (int) ((currentKey >>> 26) & 0xFFFL);
        int z = (int) (currentKey & 0x3FFFFFFL);

        // 处理符号扩展
        if ((x & 0x40000000) != 0) x |= 0x80000000;
        if ((y & 0x2000) != 0) y |= 0xFFFFF000;
        if ((z & 0x20000000) != 0) z |= 0xC0000000;

        return new int[] { x, y, z };
    }

    /**
     * 坐标编码为 long key
     * 
     * <p>
     * 位运算布局：
     * </p>
     * 
     * <pre>
     * bit 63-38: X 坐标 (26 bits, signed)
     * bit 37-26: Y 坐标 (12 bits, signed)
     * bit 25-0:  Z 坐标 (26 bits, signed)
     * </pre>
     */
    private static long getKey(int x, int y, int z) {
        return (((long) x & 0x7FFFFFFFL) << 38) | (((long) y & 0xFFFL) << 26) | ((long) z & 0x3FFFFFFL);
    }
}
