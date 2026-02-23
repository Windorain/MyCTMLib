package com.github.wohaopa.MyCTMLib.render.phasegroups;

import net.minecraft.world.World;

import com.github.wohaopa.MyCTMLib.FastRandom;
import com.github.wohaopa.MyCTMLib.render.ConnectionState;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

/**
 * Tile 位置计算组
 * 
 * <p>本组方法假设输入数据有效，由调用方（Pipeline 层）负责验证前置条件。</p>
 */
public final class TilePositionGroup {

    private TilePositionGroup() {
        // 工具类，禁止实例化
    }

    /**
     * 计算连接掩码（Connecting 材质）
     * 
     * <p>前置条件（由调用方保证）：</p>
     * <ul>
     *   <li>{@code ctx.getBlockAccess() != null}</li>
     *   <li>{@code ctx.getBlock() != null}</li>
     *   <li>{@code ctx.getFace() != null}</li>
     * </ul>
     * 
     * <p>后置条件：</p>
     * <ul>
     *   <li>{@code ctx.getConnectionMask() != null}</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void calcConnectionMask(RenderContext ctx) {
        int mask = ConnectionState.computeMask(
            ctx.getBlockAccess(),
            (int) ctx.getX(),
            (int) ctx.getY(),
            (int) ctx.getZ(),
            ctx.getFace(),
            ctx.getBlock(),
            ctx.getMeta(),
            null);
        ctx.setConnectionMask(mask);
    }

    /**
     * 根据连接掩码查找 tile 位置（Connecting 材质）
     * 
     * <p>前置条件：</p>
     * <ul>
     *   <li>{@code ctx.getConnectionMask() != null}</li>
     *   <li>{@code ctx.getTextureData()} 是 {@link ConnectingTextureData}</li>
     * </ul>
     * 
     * <p>后置条件：</p>
     * <ul>
     *   <li>{@code ctx.getTilePosition() != null}</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void lookupTileFromMask(RenderContext ctx) {
        ConnectingTextureData ctd = (ConnectingTextureData) ctx.getTextureData();
        LayoutHandler handler = LayoutHandlers.get(ctd.getLayout());
        int[] pos = handler.getTilePosition(ctx.getConnectionMask());
        ctx.setTilePosition(pos);
    }

    /**
     * 计算随机索引（Random 材质）
     * 
     * <p>前置条件：</p>
     * <ul>
     *   <li>{@code ctx.getTextureData()} 是 {@link RandomTextureData}</li>
     * </ul>
     * 
     * <p>后置条件：</p>
     * <ul>
     *   <li>{@code ctx.getRandomIndex() != null}</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void calcRandomIndex(RenderContext ctx) {
        RandomTextureData rtd = (RandomTextureData) ctx.getTextureData();

        long worldSeed = 0;
        if (ctx.getBlockAccess() instanceof World w) {
            worldSeed = w.getSeed();
        }

        int randomIndex = FastRandom.getRandomIndex(
            worldSeed,
            (int) ctx.getX(),
            (int) ctx.getY(),
            (int) ctx.getZ(),
            rtd.getCount());
        ctx.setRandomIndex(randomIndex);
    }

    /**
     * 根据随机索引查找 tile 位置（Random 材质）
     * 
     * <p>前置条件：</p>
     * <ul>
     *   <li>{@code ctx.getRandomIndex() != null}</li>
     *   <li>{@code ctx.getTextureData()} 是 {@link RandomTextureData}</li>
     * </ul>
     * 
     * <p>后置条件：</p>
     * <ul>
     *   <li>{@code ctx.getTilePosition() != null}</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void lookupTileFromRandom(RenderContext ctx) {
        RandomTextureData rtd = (RandomTextureData) ctx.getTextureData();
        int randomIndex = ctx.getRandomIndex();

        int tileX = randomIndex % rtd.getColumns();
        int tileY = randomIndex / rtd.getColumns();
        ctx.setTilePosition(new int[]{tileX, tileY});
    }

    /**
     * 设置默认 tile 位置（Base 材质）
     * 
     * <p>后置条件：</p>
     * <ul>
     *   <li>{@code ctx.getTilePosition() = [0, 0]}</li>
     * </ul>
     * 
     * @param ctx 渲染上下文
     */
    public static void setDefaultTile(RenderContext ctx) {
        ctx.setTilePosition(new int[]{0, 0});
    }
}
