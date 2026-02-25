package com.github.wohaopa.MyCTMLib.render.domain;

import com.github.wohaopa.MyCTMLib.FastRandom;
import com.github.wohaopa.MyCTMLib.render.ConnectionState;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

/**
 * Tile 位置数据域
 * 
 * 职责：
 * - Base: 返回 (0, 0)
 * - Random: 根据种子计算随机位置
 * - Connecting: 根据连接掩码查找位置
 */
public final class TileDomain {

    private TileDomain() {}

    /**
     * Base 材质：默认位置 (0, 0)
     */
    public static void computeBase(RenderContext ctx) {
        ctx.setTileX(0);
        ctx.setTileY(0);
    }

    /**
     * Random 材质：计算随机位置
     */
    public static void computeRandom(RenderContext ctx) {
        RandomTextureData rtd = (RandomTextureData) ctx.getTextureData();

        long worldSeed = 0;
        if (ctx.getBlockAccess() instanceof net.minecraft.world.World w) {
            worldSeed = w.getSeed();
        }

        int randomIndex = FastRandom
            .getRandomIndex(worldSeed, (int) ctx.getBlockX(), (int) ctx.getBlockY(), (int) ctx.getBlockZ(), rtd.getCount());
        ctx.setRandomIndex(randomIndex);

        int tileX = randomIndex % rtd.getColumns();
        int tileY = randomIndex / rtd.getColumns();
        ctx.setTileX(tileX);
        ctx.setTileY(tileY);
    }

    /**
     * Connecting 材质：根据连接掩码计算位置
     */
    public static void computeConnecting(RenderContext ctx) {
        ConnectingTextureData ctd = (ConnectingTextureData) ctx.getTextureData();
        LayoutHandler handler = LayoutHandlers.get(ctd.getLayout());

        int mask = ConnectionState.computeMask(
            ctx.getBlockAccess(),
            (int) ctx.getBlockX(),
            (int) ctx.getBlockY(),
            (int) ctx.getBlockZ(),
            ctx.getFace(),
            ctx.getBlock(),
            ctx.getMeta(),
            ctx.getConnectionPredicate());
        ctx.setConnectionMask(mask);

        int[] pos = handler.getTilePosition(mask);
        ctx.setTileX(pos[0]);
        ctx.setTileY(pos[1]);
    }
}
