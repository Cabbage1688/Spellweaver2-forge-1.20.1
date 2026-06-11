package net.zhenhuojun.spellweaver.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//要混入接口中的方法，自己这个类也要写成接口
//草，不支持Inject
//那只能换ChestMenu了
@Mixin(ChestMenu.class)
public class ChestMenuMixin {
    /*@Inject(at = @At("HEAD"),method = "stillValidBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/player/Player;D)Z", cancellable = true,remap = false)
    private static void stillValidBlockEntity(BlockEntity pBlockEntity, Player pPlayer, double pMaxDistance, CallbackInfoReturnable<Boolean> cir){
        Level level = pBlockEntity.getLevel();
        BlockPos blockpos = pBlockEntity.getBlockPos();
        if (level == null) {
            cir.setReturnValue(false);
        } else if (level.getBlockEntity(blockpos) != pBlockEntity) {
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(true);
        }
    }
     */
    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void onStillValid(Player pPlayer, CallbackInfoReturnable<Boolean> cir) {
            cir.setReturnValue(true);
    }

}


