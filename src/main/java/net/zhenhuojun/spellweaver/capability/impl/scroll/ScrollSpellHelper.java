package net.zhenhuojun.spellweaver.capability.impl.scroll;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

public class ScrollSpellHelper {

    // 设置卷轴法术（直接操作NBT）
    public static void setSpell(ItemStack scroll, SequenceNode sequenceNode, String spellName) {
        CompoundTag tag = scroll.getOrCreateTag();
        ScrollSpell spell = new ScrollSpell(sequenceNode, spellName,0.0);
        tag.put("scrollSpell", spell.serialize());//这里加一个0是为了能在scrollSpell这个命名空间下面有一个mana,否则mana不在这儿文本会显示不出来
    }

    // 获取卷轴法术（通过能力接口）
    /*public static Optional<ScrollSpell> getSpell(ItemStack scroll) {
        return Optional.ofNullable(scroll.getCapability(ModCapabilities.SCROLL_SPELL_HANDLER));
    }

     */

    // 直接从NBT获取卷轴法术（备选方案）
    /*public static ScrollSpell getSpellDirect(ItemStack scroll) {
        CompoundTag tag = scroll.getOrCreateTag();
        if (tag.contains("scrollSpell")) {
            return ScrollSpell.deserialize(tag.getCompound("scrollSpell"));
        }
        return new ScrollSpell(new SequenceNode(), "空卷轴");
    }

     */

    // 检查卷轴是否有法术
    /*public static boolean hasSpell(ItemStack scroll) {
        ScrollSpell spell = getSpellDirect(scroll);
        return spell != null && !spell.getRuneSequence().isEmpty();
    }

     */

    public static int getScrollMana(ItemStack scroll){
        CompoundTag tag = scroll.getOrCreateTag();
        if(tag.getCompound("scrollSpell").contains("mana")){
            return tag.getCompound("scrollSpell").getInt("mana");
        }
        return 0;
    }
    public static void addScrollMana(ItemStack scroll,int add){

        CompoundTag tag = scroll.getOrCreateTag();
        if(tag.getCompound("scrollSpell").contains("mana")){
            int newMana=tag.getCompound("scrollSpell").getInt("mana")+add;
            tag.getCompound("scrollSpell").putInt("mana",newMana);
        }
    }
    public static void subScrollMana(ItemStack scroll,int sub){
        CompoundTag tag = scroll.getOrCreateTag();
        if(tag.getCompound("scrollSpell").contains("mana")){
            int newMana=tag.getCompound("scrollSpell").getInt("mana")-sub;
            tag.getCompound("scrollSpell").putInt("mana",newMana);
        }
    }
}
