package net.zhenhuojun.spellweaver.entity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.entity.impl.MagicLightEntity;

//这个工具类装着魔法光源实体的生成方法
public class MagicLightUtils {
    public static void spawnMagicLight(Level level, BlockPos pos) {
        // System.out.println("[DEBUG]spawnMagicLight is used");
        if (!level.isClientSide) {
            // 放宽条件，允许替换一些非固体方块
            BlockState blockState = level.getBlockState(pos);
            if(blockState.isAir() || blockState.canBeReplaced()) {
                // 先放置光源方块
                level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 3);

                // 然后生成实体
                MagicLightEntity light = new MagicLightEntity(ModEntities.MAGIC_LIGHT.get(), level);
                light.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                light.setLightPos(pos); // 添加一个方法记录光源方块位置
                level.addFreshEntity(light);
            }
        }
    }

    public static void spawnMagicLight(Level level, double x, double y, double z) {
        spawnMagicLight(level, new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)));
    }
}