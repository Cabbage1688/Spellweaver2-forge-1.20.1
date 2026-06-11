package net.zhenhuojun.spellweaver.capability.impl.mana_shield;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public class ManaShield {
    //private LivingEntity protectedEntity;
    private boolean active;
    private double shieldAmount;

    public ManaShield(){
        this.active=false;
        shieldAmount=0;
    }

    public boolean isActive() {
        return active;
    }
    public double getShieldAmount() {
        return shieldAmount;
    }
    public void addShieldAmount(double amount){
        this.shieldAmount=shieldAmount+amount;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    public void setShieldAmount(double shieldAmount) {
        this.shieldAmount = shieldAmount;
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("active", this.active);
        tag.putDouble("shieldAmount", this.shieldAmount);
        return tag;
    }

    public void deserialize(CompoundTag tag) {
        this.active = tag.getBoolean("active");
        this.shieldAmount = tag.getDouble("shieldAmount");
    }
}
