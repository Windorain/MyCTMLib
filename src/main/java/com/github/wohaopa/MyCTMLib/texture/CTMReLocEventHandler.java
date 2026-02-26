package com.github.wohaopa.MyCTMLib.texture;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.TextureStitchEvent;

/**
 * CTMReLoc 事件处理器
 * 
 * 订阅 TextureStitchEvent.Pre 事件，触发 CTMReLoc 表的构建
 * 
 * @author MyCTMLib
 * @since 1.0.0
 */
@SideOnly(Side.CLIENT)
public class CTMReLocEventHandler {
    
    /**
     * 在纹理拼接前构建 CTM 重定向表
     */
    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        CTMReLoc.build(event.map);
    }
}
