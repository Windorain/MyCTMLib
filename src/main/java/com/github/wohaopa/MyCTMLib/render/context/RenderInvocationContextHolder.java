package com.github.wohaopa.MyCTMLib.render.context;

import java.util.Arrays;

public class RenderInvocationContextHolder {

    private static final ThreadLocal<RenderInvocationContext> HOLDER = ThreadLocal
        .withInitial(() -> new RenderInvocationContext());

    public static RenderInvocationContext get() {
        RenderInvocationContext ctx = HOLDER.get();

        if (ctx.getStackTop() >= 0) {
            throw new IllegalStateException(
                "RenderInvocationContext re-entry detected! " + "Current stack: "
                    + Arrays.toString(ctx.getMethodStack()));
        }

        return ctx;
    }

    public static RenderInvocationContext getIfAvailable() {
        return HOLDER.get();
    }

    public static void clear() {
        RenderInvocationContext ctx = HOLDER.get();
        ctx.reset();
    }
}
