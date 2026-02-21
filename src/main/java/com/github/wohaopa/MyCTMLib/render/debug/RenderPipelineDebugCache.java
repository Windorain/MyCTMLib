package com.github.wohaopa.MyCTMLib.render.debug;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.render.PipelineDebugTrace;

public class RenderPipelineDebugCache {

    private static final Map<Long, Map<ForgeDirection, PipelineDebugTrace>> CACHE = new HashMap<>();
    private static final long MAX_CACHE_AGE_MS = 60000; // 1分钟
    private static long lastCleanupTime = System.currentTimeMillis();

    private RenderPipelineDebugCache() {}

    public static void record(int x, int y, int z, ForgeDirection face, PipelineDebugTrace trace) {
        long key = getKey(x, y, z);
        CACHE.computeIfAbsent(key, k -> new EnumMap<>(ForgeDirection.class))
            .put(face, trace);

        maybeCleanup();
    }

    public static PipelineDebugTrace get(int x, int y, int z, ForgeDirection face) {
        long key = getKey(x, y, z);
        Map<ForgeDirection, PipelineDebugTrace> faceTraces = CACHE.get(key);
        return faceTraces != null ? faceTraces.get(face) : null;
    }

    public static void clear() {
        CACHE.clear();
    }

    private static long getKey(int x, int y, int z) {
        return (((long) x & 0x7FFFFFFFL) << 38) | (((long) y & 0xFFFL) << 26) | ((long) z & 0x3FFFFFFL);
    }

    private static void maybeCleanup() {
        long now = System.currentTimeMillis();
        if (now - lastCleanupTime > MAX_CACHE_AGE_MS) {
            clear();
            lastCleanupTime = now;
        }
    }
}
