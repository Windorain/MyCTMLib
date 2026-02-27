package com.github.wohaopa.MyCTMLib.render.domain;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.wohaopa.MyCTMLib.FastRandom;
import com.github.wohaopa.MyCTMLib.render.ConnectionState;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.texture.CTMTextureAtlasSprite;
import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;
import com.github.wohaopa.MyCTMLib.texture.layout.FullLayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.SimpleLayoutHandler;

public final class TileDomain {

    private TileDomain() {}

    public static void computeBase(RenderContext ctx) {
        ctx.setTileX(0);
        ctx.setTileY(0);
        ctx.info("TILE: Using base tile (0, 0)");
    }

    public static void computeRandom(RenderContext ctx) {
        assert ctx.getCtmSprite() != null;
        assert ctx.getBlockAccess() != null;

        CTMTextureAtlasSprite ctmSprite = ctx.getCtmSprite();
        IBlockAccess blockAccess = ctx.getBlockAccess();
        int blockX = (int) ctx.getBlockX();
        int blockY = (int) ctx.getBlockY();
        int blockZ = (int) ctx.getBlockZ();
        int count = ctmSprite.getRandomCount();
        int columns = ctmSprite.getGridWidth();
        int rows = ctmSprite.getGridHeight();

        long worldSeed = 0;
        if (blockAccess instanceof World w) {
            worldSeed = w.getSeed();
        }

        int randomIndex = FastRandom.getRandomIndex(worldSeed, blockX, blockY, blockZ, count);

        ctx.setTileX(randomIndex % columns);
        ctx.setTileY(randomIndex / columns);
        ctx.info("TILE: Using random tile (" + ctx.getTileX() + ", " + ctx.getTileY() + ")");
    }

    public static void computeConnecting(RenderContext ctx) {
        assert ctx.getCtmSprite() != null;
        assert ctx.getBlockAccess() != null;
        assert ctx.getFace() != null;
        assert ctx.getBlock() != null;
        assert ctx.getConnectionPredicate() != null;

        CTMTextureAtlasSprite ctmSprite = ctx.getCtmSprite();
        ConnectingLayout layout = ctmSprite.getLayoutStyle();

        int mask = ConnectionState.computeMask(
            ctx.getBlockAccess(),
            (int) ctx.getBlockX(),
            (int) ctx.getBlockY(),
            (int) ctx.getBlockZ(),
            ctx.getFace(),
            ctx.getBlock(),
            ctx.getMeta(),
            ctx.getConnectionPredicate()
        );
        ctx.setConnectionMask(mask);
        ctx.info("TILE: Connection mask = 0x" + Integer.toHexString(mask));

        if (layout == ConnectingLayout.SIMPLE) {
            int[] pos = SimpleLayoutHandler.TILE_TABLE[mask & 0x0F];
            ctx.setTileX(pos[0]);
            ctx.setTileY(pos[1]);
        } else if (layout == ConnectingLayout.FULL) {
            byte[] pos = FullLayoutHandler.TILE_TABLE[mask & 0xFF];
            ctx.setTileX(pos[0]);
            ctx.setTileY(pos[1]);
        }
        ctx.info("TILE: Using tile (" + ctx.getTileX() + ", " + ctx.getTileY() + ") for layout " + layout);
    }
}
