package com.github.wohaopa.MyCTMLib.render;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.github.wohaopa.MyCTMLib.texture.BaseTextureData;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 根据生物群系获取草地/树叶/水颜色。1.7.10 需通过 World.getBiomeGenForCoords 获取 BiomeGenBase。
 */
@SideOnly(Side.CLIENT)
public final class BiomeTintingHelper {

    /** 草地默认色（平原绿） */
    private static final int DEFAULT_GRASS = 0x79C05A;
    /** 树叶默认色 */
    private static final int DEFAULT_FOLIAGE = 0x59AE30;
    /** 水默认色 */
    private static final int DEFAULT_WATER = 0x3F76E4;

    private BiomeTintingHelper() {}

    /**
     * 获取指定位置的生物群系着色。blockAccess 需为 World 才能正确获取，否则返回默认色。
     */
    public static int getBiomeColor(BaseTextureData.QuadTinting tinting, IBlockAccess blockAccess, int x, int y, int z) {
        if (tinting == null || blockAccess == null || !(blockAccess instanceof World)) {
            return getDefaultColor(tinting);
        }
        World world = (World) blockAccess;
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        if (biome == null) {
            return getDefaultColor(tinting);
        }
        switch (tinting) {
            case BIOME_GRASS:
                return biome.getBiomeGrassColor(x, y, z);
            case BIOME_FOLIAGE:
                return biome.getBiomeFoliageColor(x, y, z);
            case BIOME_WATER:
                return biome.waterColorMultiplier;
            default:
                return getDefaultColor(tinting);
        }
    }

    private static int getDefaultColor(BaseTextureData.QuadTinting tinting) {
        if (tinting == null) return DEFAULT_GRASS;
        switch (tinting) {
            case BIOME_GRASS:
                return DEFAULT_GRASS;
            case BIOME_FOLIAGE:
                return DEFAULT_FOLIAGE;
            case BIOME_WATER:
                return DEFAULT_WATER;
            default:
                return DEFAULT_GRASS;
        }
    }
}
