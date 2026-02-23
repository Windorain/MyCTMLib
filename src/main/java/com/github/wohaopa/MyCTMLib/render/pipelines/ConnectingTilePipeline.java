package com.github.wohaopa.MyCTMLib.render.pipelines;

import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.domain.BrightnessDomain;
import com.github.wohaopa.MyCTMLib.render.domain.ColorDomain;
import com.github.wohaopa.MyCTMLib.render.domain.GeometryDomain;
import com.github.wohaopa.MyCTMLib.render.domain.IconDomain;
import com.github.wohaopa.MyCTMLib.render.domain.PositionDomain;
import com.github.wohaopa.MyCTMLib.render.domain.TileDomain;
import com.github.wohaopa.MyCTMLib.render.domain.UVDomain;
import com.github.wohaopa.MyCTMLib.render.phasegroups.RenderGroup;

/**
 * Connecting 材质渲染管道
 */
public final class ConnectingTilePipeline {

    private ConnectingTilePipeline() {}

    public static void execute(RenderContext ctx) {
        ctx.trace("=== ConnectingTilePipeline START ===");
        ctx.trace("Texture: key=" + ctx.getTextureKey());

        IconDomain.resolve(ctx);
        ctx.trace(
            "IconDomain: icon=[" + formatDouble(ctx.getIconMinU())
                + ","
                + formatDouble(ctx.getIconMaxU())
                + ","
                + formatDouble(ctx.getIconMinV())
                + ","
                + formatDouble(ctx.getIconMaxV())
                + "]"
                + ", grid="
                + ctx.getGridW()
                + "x"
                + ctx.getGridH());

        TileDomain.computeConnecting(ctx);
        ctx.trace(
            "TileDomain: mask=0x" + String
                .format("%02X", ctx.getConnectionMask()) + ", tile=(" + ctx.getTileX() + "," + ctx.getTileY() + ")");

        GeometryDomain.fromElement(ctx);
        ctx.trace(
            "GeometryDomain: relMin=[" + formatDouble(ctx.getDrawRelMinX())
                + ","
                + formatDouble(ctx.getDrawRelMinY())
                + ","
                + formatDouble(ctx.getDrawRelMinZ())
                + "]"
                + ", relMax=["
                + formatDouble(ctx.getDrawRelMaxX())
                + ","
                + formatDouble(ctx.getDrawRelMaxY())
                + ","
                + formatDouble(ctx.getDrawRelMaxZ())
                + "]");

        UVDomain.calc(ctx);
        ctx.trace(
            "UVDomain: drawUV=[" + formatDouble(ctx.getDrawMinU())
                + ","
                + formatDouble(ctx.getDrawMaxU())
                + ","
                + formatDouble(ctx.getDrawMinV())
                + ","
                + formatDouble(ctx.getDrawMaxV())
                + "]");

        PositionDomain.calc(ctx);
        ctx.trace(
            "PositionDomain: world=[" + formatDouble(
                ctx.getWorldX()) + "," + formatDouble(ctx.getWorldY()) + "," + formatDouble(ctx.getWorldZ()) + "]");

        if (ctx.isItemRender()) {
            ctx.trace("Shading: item render (full brightness, uniform color)");
            BrightnessDomain.setFullAO(ctx);
            ColorDomain.computeUniform(ctx);
        } else if (ctx.getRenderBlocks().enableAO) {
            ctx.trace("Shading: AO enabled");
            BrightnessDomain.computeAO(ctx);
            ColorDomain.computeAO(ctx);
        } else {
            ctx.trace("Shading: uniform brightness/color");
            BrightnessDomain.computeUniform(ctx);
            ColorDomain.computeUniform(ctx);
        }

        RenderGroup.renderFace(ctx);
        ctx.trace("RenderGroup: face rendered");

        if (!ctx.isPipelineFailed()) {
            ctx.setDrewAny(true);
            ctx.trace("=== ConnectingTilePipeline COMPLETE (drewAny=true) ===");
        } else {
            ctx.trace("=== ConnectingTilePipeline COMPLETE (failed) ===");
        }
    }

    private static String formatDouble(double value) {
        return String.format("%.6f", value);
    }
}
