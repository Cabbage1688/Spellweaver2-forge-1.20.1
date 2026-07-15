package net.zhenhuojun.spellweaver.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
//注入法术的物品会显示附魔效果
@Mixin(ItemStack.class)
public class ItemFoilMixin {
    @Shadow
    @Nullable
    private CompoundTag tag;
    @Inject(method = "isEnchanted",at = @At("HEAD"), cancellable = true)
    private void foilEffect(CallbackInfoReturnable<Boolean> cir){
        if (this.tag != null) {
            if(this.tag.contains("SpellData")){
                cir.setReturnValue(true);
            }
        }
    }

}
