package com.github.wohaopa.MyCTMLib.render.phases;

import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.github.wohaopa.MyCTMLib.FastRandom;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
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
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

public class ModelRenderLoopPhase implements PipelinePhase {

    @Override
    public void process(RenderContext context) {
        context.resetElementIndex();
        context.setDrewAny(false);

        ModelData modelData = context.getModelData();
        if (modelData == null) return;

        String modelId = context.getModelId();
        String domain = modelId != null && modelId.indexOf(':') >= 0 ? modelId.substring(0, modelId.indexOf(':'))
            : "minecraft";

        do {
            ModelElement element = context.getCurrentElement();
            renderElement(element, context, domain);
        } while (context.moveToNextElement());
    }

    private void renderElement(ModelElement element, RenderContext context, String domain) {
        ModelFace faceData = element.getFace(context.getFace());
        if (faceData == null) return;

        String textureKey = faceData.getTextureKey();
        if (textureKey == null) return;

        String texturePath = TextureKeyNormalizer.resolveTexturePath(
            textureKey,
            context.getModelData()
                .getTextures());
        if (texturePath == null) return;

        String textureLookupKey = TextureKeyNormalizer.toCanonicalTextureKey(domain, texturePath);
        TextureTypeData data = CTMRenderEntry.getConnectingData(textureLookupKey);

        IIcon drawIcon = TextureRegistry.getInstance()
            .getIcon(textureLookupKey);
        if (drawIcon == null) drawIcon = context.getOriginalIcon();

        float[] f = element.getFrom(), t = element.getTo();
        double relMinX = Math.min(f[0], t[0]) / 16.0;
        double relMaxX = Math.max(f[0], t[0]) / 16.0;
        double relMinY = Math.min(f[1], t[1]) / 16.0;
        double relMaxY = Math.max(f[1], t[1]) / 16.0;
        double relMinZ = Math.min(f[2], t[2]) / 16.0;
        double relMaxZ = Math.max(f[2], t[2]) / 16.0;

        int brightness = context.getBrightness();
        int x = (int) context.getX();
        int y = (int) context.getY();
        int z = (int) context.getZ();
        IBlockAccess blockAccess = context.getBlockAccess();

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

        if (data instanceof ConnectingTextureData texData) {
            ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
            ModelFace firstFace = element.getFace(context.getFace());
            if (firstFace != null && firstFace.getConnectionKey() != null) {
                ConnectionPredicate p = PredicateRegistry.getPredicate(
                    firstFace.getConnectionKey(),
                    context.getModelData()
                        .getConnections());
                if (p != null) predicate = p;
            }

            ConnectingLayout layout = texData.getLayout();
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
