package com.github.wohaopa.MyCTMLib.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PipelineDebugTrace {

    public enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    public static class LogEntry {

        public final LogLevel level;
        public final String message;

        public LogEntry(LogLevel level, String message) {
            this.level = level;
            this.message = message;
        }
    }

    private final List<String> steps = new ArrayList<>();
    private final List<LogEntry> logs = new ArrayList<>();
    private String degradationReason;
    private String predicateUsed;
    private int[] tilePos;
    private int[] connectionBits;
    private Boolean texRegTexMapSynced;
    private String texRegGetIconLookupKey;
    private String drawSpriteName;
    private String drawSpriteLoaded;

    // UV 调试信息
    private String iconUV; // icon 原始 UV: [minU,maxU,minV,maxV]
    private String drawUV; // 最终绘制 UV: [minU,maxU,minV,maxV]
    private String gridInfo; // grid 尺寸：WxH
    private String textureKey; // 纹理键

    public PipelineDebugTrace() {}

    public void clear() {
        steps.clear();
        logs.clear();
        degradationReason = null;
        predicateUsed = null;
        tilePos = null;
        connectionBits = null;
        texRegTexMapSynced = null;
        texRegGetIconLookupKey = null;
        drawSpriteName = null;
        drawSpriteLoaded = null;
        iconUV = null;
        drawUV = null;
        gridInfo = null;
        textureKey = null;
    }

    // ========== 决策步骤（保持原有逻辑） ==========
    public void addStep(String step) {
        if (step != null && !step.isEmpty()) {
            steps.add(step);
        }
    }

    public List<String> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    // ========== 分级日志 ==========
    public void log(LogLevel level, String message) {
        if (message != null && !message.isEmpty()) {
            logs.add(new LogEntry(level, message));
        }
    }

    public void trace(String msg) {
        log(LogLevel.TRACE, msg);
    }

    public void debug(String msg) {
        log(LogLevel.DEBUG, msg);
    }

    public void info(String msg) {
        log(LogLevel.INFO, msg);
    }

    public void warn(String msg) {
        log(LogLevel.WARN, msg);
    }

    public void error(String msg) {
        log(LogLevel.ERROR, msg);
    }

    public List<LogEntry> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    public List<LogEntry> getLogsByLevel(LogLevel level) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry entry : logs) {
            if (entry.level == level) {
                result.add(entry);
            }
        }
        return result;
    }

    // ========== 元数据 ==========
    public void setDegradationReason(String reason) {
        this.degradationReason = reason;
    }

    public void setPredicateUsed(String predicate) {
        this.predicateUsed = predicate;
    }

    public void setTilePos(int tileX, int tileY) {
        this.tilePos = new int[] { tileX, tileY };
    }

    public void setConnectionBits(int mask) {
        this.connectionBits = new int[8];
        for (int d = 0; d < 8; d++) {
            connectionBits[d] = (mask & (1 << d)) != 0 ? 1 : 0;
        }
    }

    public void setTexRegTexMapSync(boolean synced, String lookupKey) {
        this.texRegTexMapSynced = synced;
        this.texRegGetIconLookupKey = lookupKey;
    }

    public Boolean getTexRegTexMapSynced() {
        return texRegTexMapSynced;
    }

    public String getTexRegGetIconLookupKey() {
        return texRegGetIconLookupKey;
    }

    public void setDrawSpriteInfo(String iconName, int width, int height) {
        this.drawSpriteName = iconName;
        this.drawSpriteLoaded = (width > 0 && height > 0) ? (width + "x" + height) : "0x0(unloaded)";
    }

    public String getDrawSpriteName() {
        return drawSpriteName;
    }

    public String getDrawSpriteLoaded() {
        return drawSpriteLoaded;
    }

    public String getDegradationReason() {
        return degradationReason;
    }

    public String getPredicateUsed() {
        return predicateUsed;
    }

    public int[] getTilePos() {
        return tilePos;
    }

    public int[] getConnectionBits() {
        return connectionBits;
    }

    // ========== UV 调试信息 ==========
    public void setIconUV(double minU, double maxU, double minV, double maxV) {
        this.iconUV = String.format("[%.6f,%.6f,%.6f,%.6f]", minU, maxU, minV, maxV);
    }

    public void setDrawUV(double minU, double maxU, double minV, double maxV) {
        this.drawUV = String.format("[%.6f,%.6f,%.6f,%.6f]", minU, maxU, minV, maxV);
    }

    public void setGridInfo(int w, int h) {
        this.gridInfo = w + "x" + h;
    }

    public void setTextureKey(String key) {
        this.textureKey = key;
    }

    public String getIconUV() {
        return iconUV;
    }

    public String getDrawUV() {
        return drawUV;
    }

    public String getGridInfo() {
        return gridInfo;
    }

    public String getTextureKey() {
        return textureKey;
    }
}
