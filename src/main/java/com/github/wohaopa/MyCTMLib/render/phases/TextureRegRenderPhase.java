package com.github.wohaopa.MyCTMLib.render.phases;

import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.github.wohaopa.MyCTMLib.FastRandom;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.render.CTMRenderEntry;
import com.github.wohaopa.MyCTMLib.render.ConnectionState;
import com.github.wohaopa.MyCTMLib.render.FaceRenderer;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.pipeline.PipelinePhase;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

public class TextureRegRenderPhase implements PipelinePhase {

    @Override
    public void process(RenderContext context) {
        context.setDrewAny(false);

        String iconName = context.getIconName();
        TextureTypeData data = CTMRenderEntry.getConnectingData(iconName);

        if (!(data instanceof ConnectingTextureData) && !(data instanceof RandomTextureData)
            && !(data instanceof BaseTextureData)) {
            return;
        }

        IIcon drawIcon = TextureRegistry.getInstance()
            .getIcon(iconName);
        if (drawIcon == null) drawIcon = context.getOriginalIcon();

        int brightness = context.getBrightness();
        int x = (int) context.getX();
        int y = (int) context.getY();
        int z = (int) context.getZ();
        IBlockAccess blockAccess = context.getBlockAccess();

        double relMinX = context.getRelMinX();
        double relMaxX = context.getRelMaxX();
        double relMinY = context.getRelMinY();
        double relMaxY = context.getRelMaxY();
        double relMinZ = context.getRelMinZ();
        double relMaxZ = context.getRelMaxZ();

        if (data instanceof BaseTextureData baseData) {
            FaceRenderer.drawFace(
                context.getRenderBlocks(),
                context.getX(),
                context.getY(),
                context.getZ(),
                context.getFace(),
                drawIcon,
                0,
                0,
                1,
                1,
                brightness,
                relMinX,
                relMaxX,
                relMinY,
                relMaxY,
                relMinZ,
                relMaxZ,
                baseData,
                blockAccess,
                x,
                y,
                z);
            context.setDrewAny(true);
            return;
        }

        if (data instanceof RandomTextureData randomData) {
            long worldSeed = 0;
            if (blockAccess instanceof World) {
                worldSeed = ((World) blockAccess).getSeed();
            }
            int randomIndex = FastRandom.getRandomIndex(worldSeed, x, y, z, randomData.getCount());
            int tileX = randomIndex % randomData.getColumns();
            int tileY = randomIndex / randomData.getColumns();

            FaceRenderer.drawFace(
                context.getRenderBlocks(),
                context.getX(),
                context.getY(),
                context.getZ(),
                context.getFace(),
                drawIcon,
                tileX,
                tileY,
                randomData.getColumns(),
                randomData.getRows(),
                brightness,
                relMinX,
                relMaxX,
                relMinY,
                relMaxY,
                relMinZ,
                relMaxZ,
                null,
                blockAccess,
                x,
                y,
                z);
            context.setDrewAny(true);
            return;
        }

        if (data instanceof ConnectingTextureData ctd) {
            ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
            ConnectingLayout layout = ctd.getLayout();
            LayoutHandler handler = LayoutHandlers.get(layout);
            int mask = ConnectionState
                .computeMask(blockAccess, x, y, z, context.getFace(), context.getBlock(), context.getMeta(), predicate);
            int[] pos = handler.getTilePosition(mask);

            FaceRenderer.drawFace(
                context.getRenderBlocks(),
                context.getX(),
                context.getY(),
                context.getZ(),
                context.getFace(),
                drawIcon,
                pos[0],
                pos[1],
                handler.getWidth(),
                handler.getHeight(),
                brightness,
                relMinX,
                relMaxX,
                relMinY,
                relMaxY,
                relMinZ,
                relMaxZ,
                null,
                blockAccess,
                x,
                y,
                z);
            context.setDrewAny(true);
        }
    }
}
