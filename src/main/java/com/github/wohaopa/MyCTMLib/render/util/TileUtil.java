package com.github.wohaopa.MyCTMLib.render.util;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.FastRandom;
import com.github.wohaopa.MyCTMLib.render.ConnectionState;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

/**
 * Tile 位置计算工具类
 * 
 * 职责：计算纹理瓦片位置（中间参数）
 * - Base: 返回 (0, 0)
 * - Random: 根据种子计算随机位置
 * - Connecting: 根据连接掩码查找位置
 * 
 * @author MyCTMLib
 * @since 1.0.0
 */
public final class TileUtil {

    private TileUtil() {}

    /**
     * Base 材质：默认位置 (0, 0)
     * 
     * @return int[] { tileX, tileY }
     */
    public static int[] computeBase() {
        return new int[] { 0, 0 };
    }

    /**
     * Random 材质：计算随机位置
     * 
     * @param blockAccess 世界访问
     * @param blockX 方块 X 坐标
     * @param blockY 方块 Y 坐标
     * @param blockZ 方块 Z 坐标
     * @param count 随机数量
     * @param columns 列数
     * @param rows 行数
     * @return int[] { tileX, tileY }
     */
    public static int[] computeRandom(IBlockAccess blockAccess,
                                       double blockX, double blockY, double blockZ,
                                       int count, int columns, int rows) {
        long worldSeed = 0;
        if (blockAccess instanceof net.minecraft.world.World w) {
            worldSeed = w.getSeed();
        }

        int randomIndex = FastRandom.getRandomIndex(worldSeed,
            (int) blockX, (int) blockY, (int) blockZ, count);

        return new int[] {
            randomIndex % columns,
            randomIndex / columns
        };
    }

    /**
     * Connecting 材质：根据连接掩码计算位置
     * 
     * @param blockAccess 世界访问
     * @param blockX 方块 X 坐标
     * @param blockY 方块 Y 坐标
     * @param blockZ 方块 Z 坐标
     * @param face 面方向
     * @param block 方块
     * @param meta 元数据
     * @param predicate 连接谓词
     * @param layout 布局类型
     * @return int[] { tileX, tileY }
     */
    public static int[] computeConnecting(IBlockAccess blockAccess,
                                           double blockX, double blockY, double blockZ,
                                           ForgeDirection face, Block block, int meta,
                                           ConnectionPredicate predicate,
                                           ConnectingLayout layout) {
        LayoutHandler handler = LayoutHandlers.get(layout);
        int mask = ConnectionState.computeMask(
            blockAccess, (int) blockX, (int) blockY, (int) blockZ,
            face, block, meta, predicate);

        return handler.getTilePosition(mask);
    }
}
