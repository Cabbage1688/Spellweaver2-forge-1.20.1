package net.zhenhuojun.spellweaver.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.item.custom.ManaBottleItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManaPedestalBlock extends BaseEntityBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);

    public ManaPedestalBlock(Properties pProperties) {
        super(pProperties);
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ManaPedestalBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        boolean isCrouching = player.isCrouching();

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ManaPedestalBlockEntity machine)) {
            return InteractionResult.PASS;
        }

        // 空手潜行右键取出魔力瓶
        if (held.isEmpty() && isCrouching) {
            machine.extractManaBottles(player);
            return InteractionResult.SUCCESS;
        }

        // 手持魔力瓶右键放入魔力瓶
        if (held.getItem() instanceof ManaBottleItem) {
            machine.insertManaBottle(player, hand);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }
    public boolean useShapeForLightOcclusion(BlockState pState) {
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ManaPedestalBlockEntity pedestal) {
                int count = pedestal.getCurrentManaBottle();
                double totalMana = pedestal.getMana();
                if(count>0){
                    double perMana=totalMana/count;
                    for(int i=0;i<count;i++){
                        ItemStack bottle=new ItemStack(ModItems.MANA_BOTTLE.get());
                        CompoundTag tag = bottle.getOrCreateTag();
                        tag.putDouble("mana", perMana);
                        Block.popResource(level, pos, bottle);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

}
