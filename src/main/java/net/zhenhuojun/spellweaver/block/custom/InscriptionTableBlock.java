package net.zhenhuojun.spellweaver.block.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.block.ModBlockEntities;
import net.zhenhuojun.spellweaver.client.gui.InscriptionTableEditScreen;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.InscriptionTableClearC2SPacket;
import net.zhenhuojun.spellweaver.network.packet.InscriptionTableWriteC2SPacket;
import net.zhenhuojun.spellweaver.network.packet.MagicBrushClearC2SPacket;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

public class InscriptionTableBlock extends BaseEntityBlock {
    public InscriptionTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InscriptionTableBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Spellweaver.getLOGGER().debug("[Spellweaver:InscriptionTableBlock]use方法被调用");
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide) {
            //空手右键编辑法术
            if (held.isEmpty()) {
                BlockEntity be = level.getBlockEntity(pos);
                CompoundTag existingSpell = null;
                if (be instanceof InscriptionTableBlockEntity table) {
                    if (table.getSpellRoot() != null) {
                        existingSpell = table.getSpellRoot().serializeNBT();
                    }
                }
                Minecraft.getInstance().setScreen(new InscriptionTableEditScreen(pos, existingSpell));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof InscriptionTableBlockEntity table)) {
            return InteractionResult.PASS;
        }
        boolean crouching=player.isCrouching();
        Spellweaver.getLOGGER().debug("[Spellweaver:InscriptionTableBlock]玩家是否蹲下{}",crouching);
        //我tm到底为什么在服务端发包？牢逍你在干什么
        //写都写了用吧，不用不就白写了吗
        if (!held.isEmpty()) {
            if(!crouching){//2026.7.12新增蹲下消除法术
                SequenceNode spellRoot = table.getSpellRoot();
                if (spellRoot != null && !spellRoot.getChildrenNodeList().isEmpty()) {
                    ModMessage.sendToServer(new InscriptionTableWriteC2SPacket(pos, spellRoot.serializeNBT(), hand));
                    return InteractionResult.SUCCESS;
                }
            }else if(player.isCrouching()&&!held.isEmpty()){
                Spellweaver.getLOGGER().debug("[Spellweaver:InscriptionTableBlock]玩家手持物品蹲下右键，应该删除物品上的法术");
                ModMessage.sendToServer(new InscriptionTableClearC2SPacket(hand));
                Spellweaver.getLOGGER().debug("[Spellweaver:InscriptionTableBlock]已发送法术删除包");


                /*if (held.hasTag() && held.getTag().contains("SpellData")) {
                    held.getTag().remove("SpellData");
                    held.getTag().remove("TriggerType");
                    if (held.getTag().isEmpty()) {
                        held.setTag(null);
                    }
                    player.sendSystemMessage(Component.literal("§a已清除 " + held.getDisplayName().getString() + " 上的法术"));
                }

                 */
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
}