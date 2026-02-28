package com.github.wohaopa.MyCTMLib.predicate;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.gson.JsonObject;

/**
 * condition: "match_block", "block": "modid:block_id" — 邻格为指定方块即连接。
 */
public class MatchBlockPredicate implements ConnectionPredicate {

    private final Block targetBlock;

    public MatchBlockPredicate(Block targetBlock) {
        this.targetBlock = targetBlock;
    }

    @Override
    public String getDebugName() {
        return "match_block";
    }

    @Override
    public boolean connect(IBlockAccess world, int x, int y, int z, ForgeDirection face, Block block, int meta, int dx,
        int dy, int dz) {
        int nx = x + dx, ny = y + dy, nz = z + dz;
        Block neighbor = world.getBlock(nx, ny, nz);
        return neighbor != null && neighbor == targetBlock;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "match_block");
        if (targetBlock != null) {
            String blockName = Block.blockRegistry.getNameForObject(targetBlock);
            json.addProperty("block", blockName != null ? blockName.toString() : "unknown");
        }
        return json;
    }
}
