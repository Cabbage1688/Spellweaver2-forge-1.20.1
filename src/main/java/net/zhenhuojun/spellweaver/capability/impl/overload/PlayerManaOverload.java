package net.zhenhuojun.spellweaver.capability.impl.overload;

import net.minecraft.nbt.CompoundTag;

public class PlayerManaOverload {
    private boolean enabled = false;
    private int currentMultiplier = 1;       // 当前倍率，1 表示不加速
    private int maxMultiplier = 1;           // 上限（未解锁为 1）

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getCurrentMultiplier() { return currentMultiplier; }
    public void setCurrentMultiplier(int mult) {this.currentMultiplier = Math.min(mult, maxMultiplier);
    }

    public int getMaxMultiplier() { return maxMultiplier; }
    public void setMaxMultiplier(int maxMultiplier){this.maxMultiplier=maxMultiplier;}

    public void updateMaxMultiplier(int manaLevel) {
        if (manaLevel < 10) {
            maxMultiplier = 1;
        } else {
            maxMultiplier = 2 + 2*(manaLevel - 10) / 5;

        }
        // 若当前倍率超过新上限，自动削减
        if (currentMultiplier > maxMultiplier) {
            currentMultiplier = maxMultiplier;
        }
    }
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("enabled", isEnabled());
        tag.putInt("currentMult", getCurrentMultiplier());
        tag.putInt("maxMult",getMaxMultiplier());
        return tag;
    }
    public void deserializeNBT(CompoundTag tag) {
        setEnabled(tag.getBoolean("enabled"));
        setMaxMultiplier(tag.getInt("maxMult"));
        //currentMult的赋值依赖于maxMult（Math.min(mult, maxMultiplier)），因此应该在它之后反序列化
        setCurrentMultiplier(tag.getInt("currentMult"));
    }
}
