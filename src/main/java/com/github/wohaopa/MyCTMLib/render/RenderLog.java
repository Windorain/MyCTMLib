package com.github.wohaopa.MyCTMLib.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RenderLog {

    private final List<String> lines = new ArrayList<>();

    public RenderLog() {}

    public void clear() {
        lines.clear();
    }

    public void info(String msg) {
        if (msg != null && !msg.isEmpty()) {
            lines.add("§a[INFO] " + msg);
        }
    }

    public void debug(String msg) {
        if (msg != null && !msg.isEmpty()) {
            lines.add("§7[DEBUG] " + msg);
        }
    }

    public void warn(String msg) {
        if (msg != null && !msg.isEmpty()) {
            lines.add("§e[WARN] " + msg);
        }
    }

    public void error(String msg) {
        if (msg != null && !msg.isEmpty()) {
            lines.add("§4[ERROR] " + msg);
        }
    }

    public void add(String line) {
        if (line != null && !line.isEmpty()) {
            lines.add(line);
        }
    }

    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }
}
