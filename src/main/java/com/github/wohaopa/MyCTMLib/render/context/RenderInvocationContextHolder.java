package com.github.wohaopa.MyCTMLib.render.context;

import java.util.Arrays;

public class RenderInvocationContextHolder {

    private static final ThreadLocal<RenderInvocationContext> HOLDER = ThreadLocal
        .withInitial(() -> new RenderInvocationContext());

    public static RenderInvocationContext get() {
        RenderInvocationContext ctx = HOLDER.get();

        RenderMethod[] methodStack = ctx.getMethodStack();
        int stackTop = ctx.getStackTop();
        
        for (int i = 0; i <= stackTop; i++) {
            for (int j = i + 1; j <= stackTop; j++) {
                if (methodStack[i] == methodStack[j] && methodStack[i] != null) {
                    throw new IllegalStateException(
                        "RenderInvocationContext re-entry detected! Duplicate method: " + methodStack[i]
                            + ", Current stack: " + Arrays.toString(methodStack));
                }
            }
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
