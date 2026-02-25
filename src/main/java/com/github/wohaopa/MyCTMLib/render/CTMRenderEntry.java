package com.github.wohaopa.MyCTMLib.render;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.wohaopa.MyCTMLib.Textures;
import com.github.wohaopa.MyCTMLib.blockstate.BlockStateRegistry;
import com.github.wohaopa.MyCTMLib.model.ModelData;
import com.github.wohaopa.MyCTMLib.model.ModelElement;
import com.github.wohaopa.MyCTMLib.model.ModelFace;
import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.predicate.ConnectionPredicate;
import com.github.wohaopa.MyCTMLib.predicate.PredicateRegistry;
import com.github.wohaopa.MyCTMLib.render.context.RenderContext;
import com.github.wohaopa.MyCTMLib.render.debug.DumpUtil;
import com.github.wohaopa.MyCTMLib.render.debug.RenderPipelineDebugCache;
import com.github.wohaopa.MyCTMLib.render.pipeline.RenderPipeline;
import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;
import com.github.wohaopa.MyCTMLib.texture.ConnectingTextureData;
import com.github.wohaopa.MyCTMLib.texture.RandomTextureData;
import com.github.wohaopa.MyCTMLib.texture.TextureKeyNormalizer;
import com.github.wohaopa.MyCTMLib.texture.TextureRegistry;
import com.github.wohaopa.MyCTMLib.texture.TextureTypeData;
import com.github.wohaopa.MyCTMLib.texture.layout.ConnectingLayout;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandler;
import com.github.wohaopa.MyCTMLib.texture.layout.LayoutHandlers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 新管线渲染入口：在 MixinRenderBlocks 六面 HEAD 处调用。
 * 优先查 BlockStateRegistry → ModelRegistry 得该面纹理与谓词；若无匹配，再查 TextureRegistry(iconName)。
 */
@SideOnly(Side.CLIENT)
public final class CTMRenderEntry {

    private static final Logger LOGGER = LogManager.getLogger("MyCTMLib");

    /**
     * 尝试用新管线渲染该面。若应由新管线处理则绘制并返回 true，否则返回 false。
     */
    public static boolean tryRender(RenderBlocks renderBlocks, IBlockAccess blockAccess, Block block, double x,
        double y, double z, IIcon icon, ForgeDirection face) {
        if (blockAccess == null || icon == null) return false;
        String iconName = TextureKeyNormalizer.normalizeIconName(icon.getIconName());
        int meta = blockAccess.getBlockMetadata((int) x, (int) y, (int) z);
        String blockId = getBlockId(block);

        // 优先 Model 分支：blockId + meta → BlockStateRegistry → ModelRegistry
        if (blockId != null) {
            String modelId = BlockStateRegistry.getInstance()
                .getModelId(blockId, meta);
            if (modelId != null) {
                ModelData modelData = ModelRegistry.getInstance()
                    .get(modelId);
                if (modelData != null) {
                    List<ModelElement> elements = getElementsWithFace(modelData, face);
                    if (!elements.isEmpty()) {
                        ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
                        ModelFace firstFace = elements.get(0)
                            .getFace(face);
                        if (firstFace != null && firstFace.getConnectionKey() != null) {
                            ConnectionPredicate p = PredicateRegistry
                                .getPredicate(firstFace.getConnectionKey(), modelData.getConnections());
                            if (p != null) predicate = p;
                        }
                        int mask = ConnectionState
                            .computeMask(blockAccess, (int) x, (int) y, (int) z, face, block, meta, predicate);
                        int brightness = block.getMixedBrightnessForBlock(blockAccess, (int) x, (int) y, (int) z);
                        String domain = modelId.indexOf(':') >= 0 ? modelId.substring(0, modelId.indexOf(':'))
                            : "minecraft";
                        boolean drewAny = false;
                        for (ModelElement el : elements) {
                            ModelFace faceData = el.getFace(face);
                            if (faceData == null) continue;
                            String textureKey = faceData.getTextureKey();
                            if (textureKey == null) continue;
                            String texturePath = TextureKeyNormalizer
                                .resolveTexturePath(textureKey, modelData.getTextures());
                            if (texturePath == null) continue;
                            String textureLookupKey = TextureKeyNormalizer.toCanonicalTextureKey(domain, texturePath);
                            TextureTypeData data = getConnectingData(textureLookupKey);

                            IIcon drawIcon = TextureRegistry.getInstance()
                                .getIcon(textureLookupKey);
                            if (drawIcon == null) drawIcon = icon;

                            float[] f = el.getFrom(), t = el.getTo();
                            double relMinX = Math.min(f[0], t[0]) / 16.0;
                            double relMaxX = Math.max(f[0], t[0]) / 16.0;
                            double relMinY = Math.min(f[1], t[1]) / 16.0;
                            double relMaxY = Math.max(f[1], t[1]) / 16.0;
                            double relMinZ = Math.min(f[2], t[2]) / 16.0;
                            double relMaxZ = Math.max(f[2], t[2]) / 16.0;

                            // 处理 BaseTextureData
                            if (data instanceof BaseTextureData baseData) {
                                FaceRenderer.drawFace(
                                    renderBlocks,
                                    x,
                                    y,
                                    z,
                                    face,
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
                                    (int) x,
                                    (int) y,
                                    (int) z);
                                drewAny = true;
                                continue;
                            }

                            // 处理 RandomTextureData
                            if (data instanceof RandomTextureData randomData) {
                                long worldSeed = 0;
                                if (blockAccess instanceof net.minecraft.world.World) {
                                    worldSeed = ((net.minecraft.world.World) blockAccess).getSeed();
                                }
                                int randomIndex = com.github.wohaopa.MyCTMLib.FastRandom
                                    .getRandomIndex(worldSeed, (int) x, (int) y, (int) z, randomData.getCount());
                                int tileX = randomIndex % randomData.getColumns();
                                int tileY = randomIndex / randomData.getColumns();

                                FaceRenderer.drawFace(
                                    renderBlocks,
                                    x,
                                    y,
                                    z,
                                    face,
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
                                    (int) x,
                                    (int) y,
                                    (int) z);
                                drewAny = true;
                                continue;
                            }

                            // 处理 ConnectingTextureData
                            if (!(data instanceof ConnectingTextureData texData)) continue;
                            ConnectingLayout layout = texData.getLayout();
                            LayoutHandler handler = LayoutHandlers.get(layout);
                            int[] pos = handler.getTilePosition(mask);

                            FaceRenderer.drawFace(
                                renderBlocks,
                                x,
                                y,
                                z,
                                face,
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
                                (int) x,
                                (int) y,
                                (int) z);
                            drewAny = true;
                        }
                        if (drewAny) return true;
                    }
                }
            }
        }

        // 回退到 TextureRegistry(iconName)：用 TexReg 查到的 sprite 绘制，无则用 block icon
        TextureTypeData data = getConnectingData(iconName);
        if (!(data instanceof ConnectingTextureData) && !(data instanceof RandomTextureData)
            && !(data instanceof BaseTextureData)) return false;
        IIcon drawIcon = TextureRegistry.getInstance()
            .getIcon(iconName);
        if (drawIcon == null) drawIcon = icon;
        int brightness = block.getMixedBrightnessForBlock(blockAccess, (int) x, (int) y, (int) z);

        // 使用 RenderBlocks 的 bounds，支持台阶、楼梯等非标准方块
        double relMinX = renderBlocks.renderMinX;
        double relMaxX = renderBlocks.renderMaxX;
        double relMinY = renderBlocks.renderMinY;
        double relMaxY = renderBlocks.renderMaxY;
        double relMinZ = renderBlocks.renderMinZ;
        double relMaxZ = renderBlocks.renderMaxZ;

        // 处理 BaseTextureData
        if (data instanceof BaseTextureData baseData) {
            FaceRenderer.drawFace(
                renderBlocks,
                x,
                y,
                z,
                face,
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
                (int) x,
                (int) y,
                (int) z);
            return true;
        }

        // 处理 RandomTextureData
        if (data instanceof RandomTextureData randomData) {
            int tileX, tileY;
            long worldSeed = 0;
            if (blockAccess instanceof net.minecraft.world.World) {
                worldSeed = ((net.minecraft.world.World) blockAccess).getSeed();
            }
            int randomIndex = com.github.wohaopa.MyCTMLib.FastRandom
                .getRandomIndex(worldSeed, (int) x, (int) y, (int) z, randomData.getCount());
            tileX = randomIndex % randomData.getColumns();
            tileY = randomIndex / randomData.getColumns();

            FaceRenderer.drawFace(
                renderBlocks,
                x,
                y,
                z,
                face,
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
                (int) x,
                (int) y,
                (int) z);
            return true;
        }

        // 处理 ConnectingTextureData
        ConnectingTextureData ctd = (ConnectingTextureData) data;
        ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
        ConnectingLayout layout = ctd.getLayout();
        LayoutHandler handler = LayoutHandlers.get(layout);
        int mask = ConnectionState.computeMask(blockAccess, (int) x, (int) y, (int) z, face, block, meta, predicate);
        int[] pos = handler.getTilePosition(mask);

        FaceRenderer.drawFace(
            renderBlocks,
            x,
            y,
            z,
            face,
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
            (int) x,
            (int) y,
            (int) z);
        return true;
    }

    /**
     * 获取单面渲染管线信息（仅查询，不绘制）。用于 Debug HUD。
     */
    public static PipelineInfo getPipelineInfo(IBlockAccess blockAccess, Block block, int x, int y, int z,
        ForgeDirection face) {
        return getPipelineInfo(blockAccess, block, x, y, z, face, null);
    }

    /**
     * 获取单面渲染管线信息，可选填充 trace 用于 Debug HUD 展示决策流程。
     *
     * @param out 当非 null 时填充决策步骤、退化原因、谓词、连接状态、瓦片坐标
     */
    public static PipelineInfo getPipelineInfo(IBlockAccess blockAccess, Block block, int x, int y, int z,
        ForgeDirection face, PipelineDebugTrace out) {
        if (blockAccess == null || block == null) {
            if (out != null) out.addStep("blockAccess=null or block=null");
            return PipelineInfo.vanilla("", "blockAccess=null");
        }
        int meta = blockAccess.getBlockMetadata(x, y, z);
        IIcon icon;
        try {
            icon = block.getIcon(face.ordinal(), meta);
        } catch (Throwable t) {
            if (out != null) out.addStep("getIcon failed: " + t.getMessage());
            return PipelineInfo.vanilla("", "getIcon failed");
        }
        if (icon == null) {
            if (out != null) out.addStep("icon=null");
            return PipelineInfo.vanilla("", "icon=null");
        }
        String iconName = TextureKeyNormalizer.normalizeIconName(icon.getIconName());
        String blockId = getBlockId(block);

        // 用于构建 Vanilla 时的 skipReason 与 trace
        String skipModelId = null;
        String skipTexKey = null;
        String skipDetail = null;
        List<String> failedTexKeys = new ArrayList<>();

        if (out != null) {
            out.addStep("1. blockId=" + (blockId != null ? blockId : "null (unregistered)"));
            out.addStep("2. icon=" + iconName + ", meta=" + meta);
        }

        // Model 分支
        if (blockId != null) {
            String modelId = BlockStateRegistry.getInstance()
                .getModelId(blockId, meta);
            if (out != null) {
                out.addStep(
                    "3. BlockStateRegistry(" + blockId
                        + ","
                        + meta
                        + ") -> "
                        + (modelId != null ? "modelId=" + modelId : "no modelId"));
            }
            if (modelId == null) {
                skipDetail = "BlockStateRegistry(blockId,meta) -> no modelId";
                if (out != null) out.setDegradationReason(skipDetail);
            } else {
                ModelData modelData = ModelRegistry.getInstance()
                    .get(modelId);
                if (out != null) {
                    out.addStep(
                        "4. ModelRegistry.get(" + modelId
                            + ") -> "
                            + (modelData != null ? "modelData found" : "no modelData"));
                }
                if (modelData == null) {
                    skipModelId = modelId;
                    skipDetail = "ModelRegistry(modelId) -> no modelData";
                    if (out != null) out.setDegradationReason(skipDetail);
                } else {
                    List<ModelElement> elements = getElementsWithFace(modelData, face);
                    if (out != null) out.addStep("5. elements with face: " + elements.size());
                    if (elements.isEmpty()) {
                        skipModelId = modelId;
                        skipDetail = "model has no elements for face";
                        if (out != null) out.setDegradationReason(skipDetail);
                    } else {
                        ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
                        String connectionKey = null;
                        ModelFace firstFace = elements.get(0)
                            .getFace(face);
                        if (firstFace != null && firstFace.getConnectionKey() != null) {
                            connectionKey = firstFace.getConnectionKey();
                            ConnectionPredicate p = PredicateRegistry
                                .getPredicate(connectionKey, modelData.getConnections());
                            if (p != null) predicate = p;
                        }
                        if (out != null)
                            out.setPredicateUsed(PredicateRegistry.getPredicateDebugName(predicate, connectionKey));

                        int mask = ConnectionState.computeMask(blockAccess, x, y, z, face, block, meta, predicate);
                        String domain = modelId.indexOf(':') >= 0 ? modelId.substring(0, modelId.indexOf(':'))
                            : "minecraft";
                        for (ModelElement el : elements) {
                            ModelFace faceData = el.getFace(face);
                            if (faceData == null) continue;
                            String textureKey = faceData.getTextureKey();
                            if (textureKey == null) continue;
                            String texturePath = TextureKeyNormalizer
                                .resolveTexturePath(textureKey, modelData.getTextures());
                            if (texturePath == null) continue;
                            String textureLookupKey = TextureKeyNormalizer.toCanonicalTextureKey(domain, texturePath);
                            skipModelId = modelId;
                            skipTexKey = textureLookupKey;
                            TextureTypeData data = getConnectingData(textureLookupKey);
                            if (out != null) {
                                String dataTypeName = data != null ? data.getClass()
                                    .getSimpleName() : "null";
                                out.addStep(
                                    "6. element texKey " + textureKey
                                        + " -> lookup "
                                        + textureLookupKey
                                        + " -> "
                                        + dataTypeName);
                            }
                            // 处理 RandomTextureData
                            if (data instanceof RandomTextureData randomData) {
                                IIcon modelIcon = TextureRegistry.getInstance()
                                    .getIcon(textureLookupKey);
                                if (out != null) {
                                    if (modelIcon == null) {
                                        out.addStep(
                                            "7. TexReg.getIcon(" + textureLookupKey
                                                + ") -> null (TexReg/TexMap OUT OF SYNC, fallback to block icon)");
                                        out.setTexRegTexMapSync(false, textureLookupKey);
                                    } else {
                                        out.addStep(
                                            "7. TexReg.getIcon(" + textureLookupKey
                                                + ") -> HIT (TexReg/TexMap synced)");
                                        out.setTexRegTexMapSync(true, textureLookupKey);
                                        out.setDrawSpriteInfo(
                                            modelIcon.getIconName(),
                                            modelIcon.getIconWidth(),
                                            modelIcon.getIconHeight());
                                    }
                                }
                                long worldSeed = 0;
                                if (blockAccess instanceof net.minecraft.world.World) {
                                    worldSeed = ((net.minecraft.world.World) blockAccess).getSeed();
                                }
                                int randomIndex = com.github.wohaopa.MyCTMLib.FastRandom
                                    .getRandomIndex(worldSeed, x, y, z, randomData.getCount());
                                if (out != null) {
                                    out.setTilePos(
                                        randomIndex % randomData.getColumns(),
                                        randomIndex / randomData.getColumns());
                                }
                                return PipelineInfo.modelRandom(textureLookupKey, modelId, textureKey, randomIndex);
                            }
                            // 处理 ConnectingTextureData
                            if (data instanceof ConnectingTextureData texData) {
                                IIcon modelIcon = TextureRegistry.getInstance()
                                    .getIcon(textureLookupKey);
                                if (out != null) {
                                    if (modelIcon == null) {
                                        out.addStep(
                                            "7. TexReg.getIcon(" + textureLookupKey
                                                + ") -> null (TexReg/TexMap OUT OF SYNC, fallback to block icon)");
                                        out.setTexRegTexMapSync(false, textureLookupKey);
                                    } else {
                                        out.addStep(
                                            "7. TexReg.getIcon(" + textureLookupKey
                                                + ") -> HIT (TexReg/TexMap synced)");
                                        out.setTexRegTexMapSync(true, textureLookupKey);
                                        out.setDrawSpriteInfo(
                                            modelIcon.getIconName(),
                                            modelIcon.getIconWidth(),
                                            modelIcon.getIconHeight());
                                    }
                                }
                                ConnectingLayout layout = texData.getLayout();
                                LayoutHandler handler = LayoutHandlers.get(layout);
                                int[] pos = handler.getTilePosition(mask);
                                if (out != null) {
                                    out.setTilePos(pos[0], pos[1]);
                                    out.setConnectionBits(mask);
                                }
                                return PipelineInfo.model(textureLookupKey, modelId, textureKey, layout, mask);
                            }
                            failedTexKeys.add(textureLookupKey);
                        }
                        if (out != null) {
                            out.setDegradationReason(
                                "model texKeys " + failedTexKeys
                                    + " not in TexReg; icon="
                                    + iconName
                                    + " not in TexReg/Legacy");
                        }
                        // fall-through：不 return，继续检查 TextureRegistry 与 Legacy
                    }
                }
            }
        } else {
            if (out != null) out.setDegradationReason("blockId=null (unregistered block)");
        }

        // TextureRegistry 分支
        TextureTypeData data = getConnectingData(iconName);
        List<String> texRegCandidates = TextureKeyNormalizer.getLookupCandidates(iconName);
        if (out != null) {
            String dataTypeName = data != null ? data.getClass()
                .getSimpleName() : "null";
            out.addStep("8. TexReg(iconName) candidates " + texRegCandidates + " -> " + dataTypeName);
        }
        // 处理 RandomTextureData
        if (data instanceof RandomTextureData randomData) {
            long worldSeed = 0;
            if (blockAccess instanceof net.minecraft.world.World) {
                worldSeed = ((net.minecraft.world.World) blockAccess).getSeed();
            }
            int randomIndex = com.github.wohaopa.MyCTMLib.FastRandom
                .getRandomIndex(worldSeed, x, y, z, randomData.getCount());
            if (out != null) {
                out.setTilePos(randomIndex % randomData.getColumns(), randomIndex / randomData.getColumns());
                IIcon regIcon = TextureRegistry.getInstance()
                    .getIcon(iconName);
                if (regIcon != null) {
                    out.setDrawSpriteInfo(regIcon.getIconName(), regIcon.getIconWidth(), regIcon.getIconHeight());
                } else {
                    out.setDrawSpriteInfo(icon.getIconName(), icon.getIconWidth(), icon.getIconHeight());
                }
            }
            return PipelineInfo.textureRegistryRandom(iconName, randomIndex);
        }
        // 处理 ConnectingTextureData
        if (data instanceof ConnectingTextureData ctd) {
            ConnectionPredicate predicate = PredicateRegistry.defaultPredicate();
            int mask = ConnectionState.computeMask(blockAccess, x, y, z, face, block, meta, predicate);
            if (out != null) {
                int[] pos = LayoutHandlers.get(ctd.getLayout())
                    .getTilePosition(mask);
                out.setPredicateUsed(PredicateRegistry.getPredicateDebugName(predicate, null));
                out.setTilePos(pos[0], pos[1]);
                out.setConnectionBits(mask);
                IIcon regIcon = TextureRegistry.getInstance()
                    .getIcon(iconName);
                if (regIcon != null) {
                    out.setDrawSpriteInfo(regIcon.getIconName(), regIcon.getIconWidth(), regIcon.getIconHeight());
                } else {
                    out.setDrawSpriteInfo(icon.getIconName(), icon.getIconWidth(), icon.getIconHeight());
                }
            }
            return PipelineInfo.textureRegistry(iconName, ctd.getLayout(), mask);
        }
        if (out != null && out.getDegradationReason() == null) {
            out.setDegradationReason("TexReg(iconName) + candidates " + texRegCandidates + " -> miss");
        }

        // Legacy
        boolean legacyHit = Textures.contain(iconName);
        if (out != null) out.addStep("9. Legacy ctmIconMap(" + iconName + ") -> " + (legacyHit ? "HIT" : "miss"));
        if (legacyHit) {
            return PipelineInfo.legacy(iconName);
        }
        if (out != null && !legacyHit) {
            String dr = out.getDegradationReason();
            if (dr == null) out.setDegradationReason("Legacy ctmIconMap(iconName) -> miss");
        }

        // Vanilla：根据上下文构建 skipReason
        String skipReason = buildVanillaSkipReason(skipModelId, skipTexKey, skipDetail, iconName);
        return PipelineInfo.vanilla(iconName, skipReason);
    }

    private static String buildVanillaSkipReason(String skipModelId, String skipTexKey, String skipDetail,
        String iconName) {
        if (skipDetail != null) {
            return skipDetail;
        }
        if (skipModelId != null && skipTexKey != null) {
            return "model tex key=" + skipTexKey + " not in TexReg; icon=" + iconName + " not in TexReg/Legacy";
        }
        return "no CTM in Model/TexReg, not in Legacy";
    }

    public static String getBlockId(Block block) {
        return block != null ? (String) Block.blockRegistry.getNameForObject(block) : null;
    }

    public static List<ModelElement> getElementsWithFace(ModelData modelData, ForgeDirection face) {
        List<ModelElement> out = new ArrayList<>();
        for (ModelElement el : modelData.getElements()) {
            if (el.getFace(face) != null) out.add(el);
        }
        return out;
    }

    /**
     * 从 TextureRegistry 查找 ConnectingTextureData。使用 TextureKeyNormalizer 多键回退，
     * 兼容 1.7.10 短名（cobblestone）、模型路径（minecraft:block/cobblestone）等格式。
     */
    public static TextureTypeData getConnectingData(String key) {
        return TextureRegistry.getInstance()
            .get(key);
    }

    /**
     * 物品渲染通道：blockAccess 为 null 时使用。以无连接（默认格 0,0）渲染连接纹理。
     * 用于手持、背包 GUI、物品栏等场景，避免显示整张连接图。
     */
    public static boolean tryRenderItemFace(RenderBlocks renderBlocks, Block block, double x, double y, double z,
        IIcon icon, ForgeDirection face) {
        if (icon == null) return false;
        String iconName = TextureKeyNormalizer.normalizeIconName(icon.getIconName());
        TextureTypeData data = getConnectingData(iconName);
        if (!(data instanceof ConnectingTextureData ctd)) return false;

        ConnectingLayout layout = ctd.getLayout();
        LayoutHandler handler = LayoutHandlers.get(layout);
        int tileX = 0;
        int tileY = 0;
        int brightness = 15728880;

        // 使用 RenderBlocks 的 bounds，支持台阶、楼梯等非标准方块的物品渲染
        double relMinX = renderBlocks.renderMinX;
        double relMaxX = renderBlocks.renderMaxX;
        double relMinY = renderBlocks.renderMinY;
        double relMaxY = renderBlocks.renderMaxY;
        double relMinZ = renderBlocks.renderMinZ;
        double relMaxZ = renderBlocks.renderMaxZ;

        FaceRenderer.drawFace(
            renderBlocks,
            x,
            y,
            z,
            face,
            icon,
            tileX,
            tileY,
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
            null,
            0,
            0,
            0);
        return true;
    }

    // ========== 新管线：RenderPipeline 状态机版本 ==========

    private static final RenderPipeline PIPELINE = new RenderPipeline();

    /**
     * 新管线渲染入口
     * 
     * <p>
     * <strong>性能优化：</strong>只在 {@code context.isDebug()=true} 时创建和填充 debug trace，
     * 即同时满足以下条件：
     * </p>
     * <ul>
     * <li>{@code MyCTMLib.debugMode = true}</li>
     * <li>当前渲染的方块是光标指向的方块</li>
     * </ul>
     * <p>
     * 避免在正常运行或渲染非光标方块时产生不必要的对象分配和 HashMap 操作。
     * </p>
     */
    public static boolean renderPipeline() {
        RenderContext context = RenderContext.get();

        try {
            if (context.isDebug()) {
                PipelineDebugTrace trace = context.getDebugTrace();
                trace.addStep(
                    "Position: " + (int) context.getBlockX()
                        + ", "
                        + (int) context.getBlockY()
                        + ", "
                        + (int) context.getBlockZ());
                trace.addStep("Face: " + context.getFace());
                trace.addStep("Icon: " + context.getOriginalIcon());

                boolean result = PIPELINE.execute(context);

                trace.addStep("Branch: " + context.getRenderBranch());
                trace.addStep("New pipeline drewAny: " + context.isDrewAny());

                trace.setConnectionBits(context.getConnectionMask());
                trace.setTilePos(context.getTileX(), context.getTileY());
                if (context.getDrawIcon() != null) {
                    trace.setDrawSpriteInfo(
                        context.getDrawIcon()
                            .getIconName(),
                        context.getDrawIcon()
                            .getIconWidth(),
                        context.getDrawIcon()
                            .getIconHeight());
                }
                if (context.getTextureKey() != null) {
                    trace.setTexRegTexMapSync(true, context.getTextureKey());
                    trace.setTextureKey(context.getTextureKey());
                }
                trace.setIconUV(
                    context.getIconMinU(),
                    context.getIconMaxU(),
                    context.getIconMinV(),
                    context.getIconMaxV());
                trace.setDrawUV(
                    context.getDrawMinU(),
                    context.getDrawMaxU(),
                    context.getDrawMinV(),
                    context.getDrawMaxV());
                trace.setGridInfo(context.getGridW(), context.getGridH());

                ForgeDirection face = context.getFace();
                RenderPipelineDebugCache.record(
                    (int) context.getBlockX(),
                    (int) context.getBlockY(),
                    (int) context.getBlockZ(),
                    face,
                    trace);

                return result;
            } else {
                // debugMode=false 或 非光标方块：直接执行，不创建任何 debug 对象
                return PIPELINE.execute(context);
            }
        } catch (Throwable t) {
            // 诊断日志：打印两个 Context 的完整状态
            logContextState(context, t);

            // 生成 JSON 诊断文件
            File dumpDir = new File("run/client/diagnostic_dumps");
            String dumpPath = DumpUtil.dumpDiagnostic(context, t, dumpDir);
            if (dumpPath != null) {
                LOGGER.error("Diagnostic dump written to: {}", dumpPath);
            }

            throw t;
        }
    }

    private static void logContextState(RenderContext ctx, Throwable error) {
        LOGGER.error("============================================================");
        LOGGER.error("MyCTMLib RenderPipeline Exception Caught");
        LOGGER.error("============================================================");
        LOGGER.error(
            "Exception Type: {}",
            error.getClass()
                .getName());
        LOGGER.error("Exception Message: {}", error.getMessage());
        LOGGER.error("");

        // 打印完整调用栈
        LOGGER.error("=== Stack Trace ===");
        for (StackTraceElement element : error.getStackTrace()) {
            LOGGER.error("  at {}", element);
        }
        LOGGER.error("");

        LOGGER.error("=== RenderContext State ===");
        if (ctx != null) {
            try {
                LOGGER.error("  renderBlocks: {}", safeStr(ctx.getRenderBlocks()));
                LOGGER.error("  blockAccess: {}", safeStr(ctx.getBlockAccess()));
                LOGGER.error("  block: {}", safeStr(ctx.getBlock()));
                LOGGER.error("  blockX: {}", ctx.getBlockX());
                LOGGER.error("  blockY: {}", ctx.getBlockY());
                LOGGER.error("  blockZ: {}", ctx.getBlockZ());
                LOGGER.error("  meta: {}", ctx.getMeta());
                LOGGER.error("  face: {}", ctx.getFace());
                LOGGER.error("  originalIcon: {}", safeStr(ctx.getOriginalIcon()));
            } catch (Exception e) {
                LOGGER.error("  Error reading context: {}", e.getMessage());
            }
        } else {
            LOGGER.error("  RenderContext is NULL!");
        }
        LOGGER.error("");

        // 打印 RenderContext 计算字段状态
        LOGGER.error("=== RenderContext Computed Fields ===");
        if (ctx != null) {
            try {
                LOGGER.error("  renderBranch: {}", ctx.getRenderBranch());
                LOGGER.error("  textureData: {}", safeStr(ctx.getTextureData()));
                LOGGER.error("  drawIcon: {}", safeStr(ctx.getDrawIcon()));
                LOGGER.error("  connectionMask: {}", ctx.getConnectionMask());
                LOGGER.error("  tileX: {}", ctx.getTileX());
                LOGGER.error("  tileY: {}", ctx.getTileY());
                LOGGER.error("  drewAny: {}", ctx.isDrewAny());
                LOGGER.error("  pipelineFailed: {}", ctx.isPipelineFailed());
                LOGGER.error("  failureReason: {}", ctx.getFailureReason());
            } catch (Exception e) {
                LOGGER.error("  Error reading computed fields: {}", e.getMessage());
            }
        }
        LOGGER.error("");

        // 线程信息
        LOGGER.error("=== Thread Info ===");
        LOGGER.error(
            "  Thread Name: {}",
            Thread.currentThread()
                .getName());
        LOGGER.error(
            "  Thread ID: {}",
            Thread.currentThread()
                .getId());
        LOGGER.error("");

        LOGGER.error("============================================================");
        LOGGER.error("End of Diagnostic Info");
        LOGGER.error("============================================================");
    }

    private static String safeStr(Object obj) {
        return obj != null ? obj.toString() : "null";
    }

}
