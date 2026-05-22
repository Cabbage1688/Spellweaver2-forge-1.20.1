package net.zhenhuojun.spellweaver.event.mana_event;

import jdk.jfr.Event;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.Nullable;
@Cancelable
public class PlayerManaChangeEvent extends Event { // 实现可取消接口
    private final Player player;
    private final int manaChange; // 魔力变化量（正数为恢复，负数为消耗）
    private final int currentMana; // 变化后的当前魔力值
    //private final int previousMana; // 变化前的魔力值
    private final LogicalSide side; // 触发侧

    public PlayerManaChangeEvent(Player player, int manaChange, int currentMana,  @Nullable LogicalSide side) {
        this.player = player;
        this.manaChange = manaChange;
        this.currentMana = currentMana;
        //this.previousMana = previousMana;
        this.side = side;
    }

    // Getter 方法
    public Player getPlayer() { return player; }
    public int getManaChange() { return manaChange; }
    public int getCurrentMana() { return currentMana; }
    //public int getPreviousMana() { return previousMana; }
    public LogicalSide getSide() { return side; }

    /**
     * 判断此事件是否因“消耗”魔力而触发。
     */
    public boolean isConsumption() {
        return manaChange < 0;
    }

    /**
     * 判断此事件是否因“恢复”魔力而触发。
     */
    public boolean isRestoration() {
        return manaChange > 0;
    }


}