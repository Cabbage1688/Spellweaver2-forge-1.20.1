package net.zhenhuojun.spellweaver.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.block.ModBlockEntities;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaSource;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.item.custom.ManaBottleItem;
import net.zhenhuojun.spellweaver.spell.SpellContext;
import net.zhenhuojun.spellweaver.spell.node.NodeResult;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;
import net.zhenhuojun.spellweaver.spell.util.RunesExecuteMethod;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

//方块实体
public class SpellMachineBlockEntity extends BlockEntity {
    public static final int MAX_MANA_BOTTLES = 3;

    private SequenceNode spellRoot;
    private double mana;
    private int currentManaBottle;
    private Player player;
    private UUID playerUUID;

    private boolean isCasting=false;

    public SpellMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPELL_MACHINE_BLOCK_ENTITY.get(), pos, state);
        this.spellRoot = null;
        this.mana = 0.0;
        this.currentManaBottle = 0;
    }

    // ---------- Getter/Setter ----------
    public SequenceNode getSpellRoot() { return spellRoot; }
    public int getCurrentManaBottle() { return currentManaBottle; }

    public void setMana(double mana) {
        this.mana = mana;
    }

    public double getMana() {
        return mana;
    }

    public Player getPlayer() {
        if (player == null && level != null && !level.isClientSide && playerUUID != null) {
            // 服务端尝试通过 UUID 查找玩家
            ServerLevel serverLevel = (ServerLevel) level;
            player = serverLevel.getPlayerByUUID(playerUUID);
        }
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        this.playerUUID = player != null ? player.getUUID() : null;
        setChanged();
    }

    public void setSpellRoot(SequenceNode root) {
        this.spellRoot = root;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public boolean isCasting() {
        return isCasting;
    }

    public void setCasting(boolean casting) {
        isCasting = casting;
    }

    // ---------- 魔力瓶操作 ----------
    public void insertManaBottle(Player player, InteractionHand hand) {
        if (currentManaBottle >= MAX_MANA_BOTTLES) return;

        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ManaBottleItem)) return;

        CompoundTag tag = stack.getTag();
        double bottleMana = tag != null && tag.contains("mana") ? tag.getDouble("mana") : 0.0;

        // 增加方块魔力
        mana += bottleMana;

        // 消耗玩家手中的魔力瓶（减少1个）
        stack.shrink(1);
        if (stack.isEmpty()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        //客户端同步
        currentManaBottle++;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void extractManaBottles(Player player) {
        if (currentManaBottle <= 0) return;

        double manaPerBottle = mana / currentManaBottle;
        //int bottlesToGive = currentManaBottle;
        int bottlesToGive =1;

        // 生成魔力瓶物品
        ItemStack bottleStack = new ItemStack(ModItems.MANA_BOTTLE.get(), bottlesToGive);
        CompoundTag tag = bottleStack.getOrCreateTag();
        tag.putDouble("mana", manaPerBottle);

        // 尝试给予玩家，若背包满则掉落
        if (!player.getInventory().add(bottleStack)) {
            player.drop(bottleStack, false);
        }

        mana = mana-manaPerBottle;
        --currentManaBottle;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }


    public void spellLogic(BlockPos pos){
        Player user = getPlayer();          // 懒加载，找不到就返回 null
        if (user == null || spellRoot == null) {
            return;                         // 安全退出
        }
        Spellweaver.getLOGGER().debug("[Spellweaver:SpellMachineBlockEntity/spellLogic]准备调用施法逻辑，检查存储的根节点内容{}",spellRoot.serializeNBT());
        SequenceNode freshRoot = new SequenceNode();
        freshRoot.deserializeNBT(spellRoot.serializeNBT());
        RunesExecuteMethod.spellLogic(freshRoot,user.level(),user, ManaSource.MACHINE,pos);
        this.setCasting(true);
        setChanged();
    }


    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("isCasting",isCasting());
        tag.putDouble("mana", mana);
        tag.putInt("currentManaBottle", currentManaBottle);
        if (spellRoot != null) {
            tag.put("spellRoot", spellRoot.serializeNBT());
            Spellweaver.getLOGGER().debug("[Spellweaver:SpellMachineBlockEntity/saveAdditional]尝试存储法术根节点数据{}",spellRoot.serializeNBT());
        }
        // 保存玩家 UUID
        if (playerUUID != null) {
            tag.putUUID("playerUUID", playerUUID);
            Spellweaver.getLOGGER().debug("[Spellweaver:SpellMachineBlockEntity/saveAdditional]尝试存储玩家UUID{}",playerUUID);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        setCasting(tag.getBoolean("isCasting"));
        mana = tag.getDouble("mana");
        currentManaBottle = tag.getInt("currentManaBottle");
        if (tag.contains("spellRoot")) {
            spellRoot = new SequenceNode();
            spellRoot.deserializeNBT(tag.getCompound("spellRoot"));
            Spellweaver.getLOGGER().debug("[Spellweaver:SpellMachineBlockEntity/load]已执行法术根节点加载逻辑，加载数据{}",tag.getCompound("spellRoot"));
        } else {
            spellRoot = null;
            Spellweaver.getLOGGER().debug("[Spellweaver:SpellMachineBlockEntity/load]法术根节点没有数据或者数据加载失败");
        }
        // 读取玩家 UUID
        if (tag.hasUUID("playerUUID")) {
            playerUUID = tag.getUUID("playerUUID");
            player = null; // 清空缓存，等待下次 getPlayer 时重新查找
        } else {
            playerUUID = null;
            player = null;
        }
    }

    // 在方块实体被移除或卸载时清除玩家引用
    @Override
    public void setRemoved() {
        super.setRemoved();
        this.player = null;
    }

    // 这三个用于同步数据
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    public static void tick(Level level, BlockPos pos, BlockState state, SpellMachineBlockEntity machine) {
        if (!level.isClientSide) return;

        if (machine.isCasting()&&machine.getMana()>0) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.2;
            double z = pos.getZ() + 0.5;

            level.addParticle(ParticleTypes.INSTANT_EFFECT, x, y, z, 0, 0, 0);

        }
    }



}
