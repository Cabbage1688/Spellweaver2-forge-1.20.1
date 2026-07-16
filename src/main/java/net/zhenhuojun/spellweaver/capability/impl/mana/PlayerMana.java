package net.zhenhuojun.spellweaver.capability.impl.mana;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerManaOverloadProvider;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerManaProvider;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.ManaChangeS2CPacket;
import net.zhenhuojun.spellweaver.network.packet.OverloadDataS2CPacket;

//移植到二代的一些修改：
// 魔力从int改为double。自定义事件先不写了，实现基本功能先
public class PlayerMana {
    private final Player player;
    private double mana;//玩家当前魔力值
    private int mana_level;//魔力等级，用于限制MAX_MANA
    private Long mana_exp;//提升魔力等级需要的魔力经验值,数据较大用长整型
    private Long present_exp;//当前经验值
    private final int MIN_MANA = 0;
    private int MAX_MANA;//=100+20*(mana_level);

    public PlayerMana(Player player) {
        this.player = player;
        // 初始化默认值
        this.mana = 0;
        this.mana_level = 0;
        this.present_exp = 0L;
        this.mana_exp = calculateManaExp();
        this.MAX_MANA = calculateMaxMana();
    }

    public void setMana_level(int level){
        this.mana_level=(level<=120&&level>=0)?level:120;
    }

    public int getMana_level() { return mana_level; }

    public int getMaxMana() {
        MAX_MANA = calculateMaxMana();
        return MAX_MANA;
    }

    public double getMana() { return mana; }//获取当前魔力值

    public Long getMana_exp() {
        mana_exp = calculateManaExp();
        return mana_exp;
    }

    public Long getPresent_exp() {
        return present_exp;
    }

    public void addMana(double add) {
        this.mana = Math.min(mana + add, getMaxMana());
    }//增加魔力，但不超过最大值

    public void subMana(double sub) {
        this.mana = Math.max(mana - sub, MIN_MANA);//消耗魔力，不低于最小值
    }
    //理论上来说现在应该不需要再额外发包了，直接用这个
    public void subManaAndAddExp(double count){
        subMana(count);
        addExp((int)count);
    }

    public void clearMana() { this.mana = 0; }//清空魔力值

    // 修改 addExp 方法，确保经验值可以正确累积并触发升级
    public void addExp(int add) {
        this.present_exp += add;
        checkLevelUp();
    }

    // 修改 checkLevelUp 方法，正确处理多级连升
    public void checkLevelUp() {
        boolean leveledUp;
        do {
            leveledUp = false;
            if (present_exp >= getMana_exp()) {
                present_exp -= getMana_exp();
                //TODO 当前99满级，本来不应该限制，但我后面要改魔力体系
                if(mana_level>=100) return;
                mana_level++;
                leveledUp = true;
                // 玩家升级提示

                // 同步更新超载上限2026.4.13更新
                player.getCapability(PlayerManaOverloadProvider.PLAYER_MANA_OVERLOAD).ifPresent(playerManaOverload -> {
                    playerManaOverload.updateMaxMultiplier(mana_level);
                    ModMessage.sendToPlayer(new OverloadDataS2CPacket(playerManaOverload.isEnabled(), playerManaOverload.getCurrentMultiplier(), playerManaOverload.getMaxMultiplier()),(ServerPlayer) player);
                    Spellweaver.getLOGGER().debug("[Spellweaver:PlayerMana/checkLevelUp]已更新超载上限并发送魔力超载同步包，是否启用{}，" +
                                    "当前超载倍数{}，最大超载倍数{}",playerManaOverload.isEnabled(),
                            playerManaOverload.getCurrentMultiplier(), playerManaOverload.getMaxMultiplier());
                });

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendSystemMessage(Component.literal("§a魔力等级提升至 " + mana_level + "!"));
                }
            }
        } while (leveledUp);
    }

    // 修改 addLevel 方法，移除不必要的条件判断
    public void addLevel() {
        mana_level++;
        this.present_exp = 0L;
    }


    public void copyFrom(PlayerMana source) {
        this.mana = source.mana;
        this.mana_level = source.mana_level;
        this.present_exp = source.present_exp;
    }//从另一个PlayerMana对象复制魔力值

    public void saveNBTData(CompoundTag nbt) {
        nbt.putDouble("mana", mana);
        nbt.putInt("mana_level", mana_level);
        nbt.putLong("present_exp", present_exp);
    }//将魔力值保存于NBT数据

    public void loadNBTData(CompoundTag nbt) {
        mana = nbt.getDouble("mana");
        mana_level = nbt.getInt("mana_level");
        present_exp = nbt.getLong("present_exp");
        // 重新计算相关值
        mana_exp = calculateManaExp();
        MAX_MANA = calculateMaxMana();
    }//从NBT数据中加载魔力值

    //TODO魔力体系下次改
    private int calculateMaxMana() {
        if (mana_level <= 0) return 0;

        int maxMana = 100;   // 1级基础

        for (int level = 2; level <= mana_level; level++) {
            int tier = (level - 1) / 10 + 1;
            if (tier > 10) tier = 10;          // 100级后增量不再增加
            int increment = 20 + 10 * (tier - 1);
            maxMana += increment;
        }
        return maxMana;

    }


/*
    private int calculateMaxMana() {
        if (mana_level <= 0) return 0;
        // 先计算到 99 级（保留原线性增长）
        int maxMana = 100;   // 1级基础
        int levelLimit = Math.min(mana_level, 99);
        for (int level = 2; level <= levelLimit; level++) {
            int tier = (level - 1) / 10 + 1;
            if (tier > 10) tier = 10;
            int increment = 20 + 10 * (tier - 1);
            maxMana += increment;
        }
        if (mana_level <= 99) return maxMana;
        long current = maxMana;
        for (int level = 100; level <= mana_level; level++) {
            current *= 2;
            if (current > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) current;
    }

 */

    private Long calculateManaExp() {
        //return (long) (100 + Math.pow(mana_level, 1.5));
        return (long) (100 + Math.pow(mana_level, 2.5));//2025.9.26削弱魔力成长速度
    }


    /*private Long calculateManaExp() {
        if (mana_level <= 99) {
            return (long) (100 + Math.pow(mana_level, 2.5));
        }
        long exp = (long) (100 + Math.pow(99, 2.5));
        for (int level = 100; level <= mana_level; level++) {
            exp *= 2;
            if (exp > Long.MAX_VALUE / 2) return Long.MAX_VALUE;
        }
        return exp;
    }

     */

    public CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        saveNBTData(tag);
        return tag;
    }

    public void deserialize(CompoundTag tag){
        loadNBTData(tag);
    }
}
