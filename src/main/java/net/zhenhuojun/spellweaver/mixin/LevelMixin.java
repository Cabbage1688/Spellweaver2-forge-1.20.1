package net.zhenhuojun.spellweaver.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.item.util.SpellBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.zhenhuojun.spellweaver.spell.util.RunesExecuteMethod.triggerSpell;

@Mixin(Level.class)
public class LevelMixin {
    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD")
    )
    private void onSetBlock(BlockPos pos, BlockState newState, int flags, int recursionLeft,
                            CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level)(Object)this;
        if (level.isClientSide()) return;
        BlockState oldState = level.getBlockState(pos);
        Spellweaver.getLOGGER().debug("[Spellweaver:Mixin] setBlock at {}, old: {}, new: {}", pos, oldState, newState);
        if (oldState == newState){
            Spellweaver.getLOGGER().debug("[Spellweaver:Mixin] 状态无变化，跳过");
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        SpellBlockStorage storage = SpellBlockStorage.get(serverLevel);
        CompoundTag spellData = storage.get(pos);
        if (spellData == null) return;
        Spellweaver.getLOGGER().debug("[Spellweaver:Mixin] 方块位置 {} 状态变更，触发法术", pos);
        double radius = 64;
        Player player = serverLevel.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), radius, false);
        if (player == null) {
            Spellweaver.getLOGGER().debug("[Spellweaver:Mixin] 方块 {} 有法术但无附近玩家，跳过触发", pos);
            return;
        }
        //防冲突
        if(player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.ERASING_KNIFE.get())) return;
        triggerSpell((ServerPlayer) player, serverLevel, spellData, context->{
            context.push(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
        });
    }
}
