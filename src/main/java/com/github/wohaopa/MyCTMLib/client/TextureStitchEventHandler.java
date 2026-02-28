package com.github.wohaopa.MyCTMLib.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.event.TextureStitchEvent;

import com.github.wohaopa.MyCTMLib.model.ModelRegistry;
import com.github.wohaopa.MyCTMLib.resource.CTMLibResourceLoader;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 参考 GTNHLib：在 TextureStitchEvent.Pre 中通过 registerIcon 注册模型所需的自定义纹理，
 * 确保纹理正确加入 mapRegisteredSprites，避免 loadTextureAtlas 内部逻辑覆盖。
 */
@SideOnly(Side.CLIENT)
public class TextureStitchEventHandler {

    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        TextureMap map = event.map;
        int textureType = map.getTextureType();
        boolean isBlocks = (textureType == 0);
        boolean isItems = (textureType == 1);
        if (!isBlocks && !isItems) return;

        // 阶段 1: 确保资源已加载
        CTMLibResourceLoader.ensureLoaded(
            Minecraft.getMinecraft()
                .getResourceManager());

        // 阶段 2: 预注册纹理（从 pendingModelData）
        prefillTexturesFromModels(map);

        // 阶段 3: 烘焙（此时所有纹理已就绪）
        ModelRegistry.getInstance()
            .bakeAll();
    }

    /**
     * 从 pendingModelData 预填充纹理
     */
    private void prefillTexturesFromModels(TextureMap textureMap) {
        CTMLibResourceLoader.getInstance()
            .prefillTexturesFromPendingModels(
                Minecraft.getMinecraft()
                    .getResourceManager(),
                textureMap);
    }

}
