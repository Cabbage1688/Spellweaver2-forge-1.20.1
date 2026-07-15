package net.zhenhuojun.spellweaver.block.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.zhenhuojun.spellweaver.block.ModBlockEntities;
import net.zhenhuojun.spellweaver.client.gui.SpellMachineEditScreen;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.item.custom.ManaBottleItem;


//方块
public class SpellMachineBlock extends BaseEntityBlock {
    public SpellMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpellMachineBlockEntity(pos, state);
    }

   @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.SPELL_MACHINE_BLOCK_ENTITY.get(),
                SpellMachineBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        boolean isCrouching = player.isCrouching();

        if (level.isClientSide) {
            if (held.isEmpty() && !isCrouching) {
                //尝试获取方块实体，检查是否有魔力瓶
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof SpellMachineBlockEntity machine) {
                    if (machine.getCurrentManaBottle() > 0) {
                        return InteractionResult.SUCCESS;
                    }
                }
                CompoundTag existingSpell = null;
                if (be instanceof SpellMachineBlockEntity machine) {
                    if (machine.getSpellRoot() != null) {
                        // SpellMachineBlockEntity 应当提供一个获取序列 NBT 的方法
                        existingSpell = machine.getSpellRoot().serializeNBT();
                    }
                }
                Minecraft.getInstance().setScreen(new SpellMachineEditScreen(pos, existingSpell));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        }

        //服务端处理
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SpellMachineBlockEntity machine)) {
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

        // 空手普通右键释放法术
        if (held.isEmpty() && !isCrouching) {
            if (machine.getSpellRoot() != null && machine.getCurrentManaBottle() > 0) {
                if (machine.getPlayer() == null) {
                    machine.setPlayer(player);
                }
                machine.spellLogic(pos);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
    /// BaseEntityBlock默认是null，不重写看不到模型
    /// 也可能是我的写法比较歪门邪道？一般会直接在方块实体中处理这一块？
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }


    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SpellMachineBlockEntity pedestal) {
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
