package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;


public class MagicStarItem extends Item {

    public static final String ENTITY_DATA_TAG = "EntityData";

    public MagicStarItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        if (level.isClientSide) return InteractionResult.sidedSuccess(true);
        Player player = pContext.getPlayer();
        if (player == null) return InteractionResult.PASS;
        InteractionHand hand = pContext.getHand();
        ItemStack stack = player.getItemInHand(hand);
        // 在点击面相对方块位置的上方生成
        BlockPos clickedPos = pContext.getClickedPos();
        Vec3 spawnPos = Vec3.atCenterOf(clickedPos.relative(pContext.getClickedFace()));
        if (spawnEntityFromStack(level, stack, spawnPos, player)) {
            consumeItem(player, hand, stack);
            return InteractionResult.sidedSuccess(false);
        }
        return InteractionResult.PASS;
    }



    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (pLevel.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        // 在玩家头顶上方生成
        Vec3 spawnPos = pPlayer.position().add(0.0, pPlayer.getEyeHeight() + 0.5, 0.0);
        if (spawnEntityFromStack(pLevel, stack, spawnPos, pPlayer)) {
            consumeItem(pPlayer, pUsedHand, stack);
            return InteractionResultHolder.sidedSuccess(pPlayer.getItemInHand(pUsedHand), false);
        }
        return InteractionResultHolder.pass(stack);
    }

    /**
     * 从物品NBT中取出EntityData并生成/恢复实体，成功返回true
     */
    private boolean spawnEntityFromStack(Level level, ItemStack stack, Vec3 spawnPos, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return false;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ENTITY_DATA_TAG, CompoundTag.TAG_COMPOUND)) return false;
        CompoundTag entityData = tag.getCompound(ENTITY_DATA_TAG);

        // 只允许原主人（或无主情况下任何创造玩家）重生实体
        if (entityData.hasUUID("OwnerUUID")) {
            if (!entityData.getUUID("OwnerUUID").equals(player.getUUID())) {
                return false;
            }
        }

        MagicStarEntity entity = new MagicStarEntity(ModEntities.MAGIC_STAR.get(), level);
        entity.load(entityData);
        // load之后再覆盖为新位置，避免实体在旧位置生成
        entity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        // 清除旧速度，避免生成时被保存时的Motion推动
        entity.setDeltaMovement(Vec3.ZERO);
        serverLevel.addFreshEntity(entity);
        return true;
    }

    private static void consumeItem(Player player, InteractionHand hand, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            player.setItemInHand(hand, stack);
        }
    }
}
