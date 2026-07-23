package net.zhenhuojun.spellweaver.client.gui.item_gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.client.gui.SpellStorageScreen;
import net.zhenhuojun.spellweaver.client.gui.SpellWeavingScreen;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.WriteSpellInScrollC2SPacket;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

public class ScrollEditScreen extends SpellWeavingScreen {

    public ScrollEditScreen(ItemStack itemStack, Level level) {
        super(Component.translatable("gui.spellweaver.edit_spell"));
        CompoundTag tag=itemStack.getOrCreateTag();
        if(tag.contains("sequence")){
            this.rootNode = new SequenceNode();
            this.rootNode.deserializeNBT(tag.getCompound("sequence"));
        }else{
            this.rootNode = new SequenceNode();
        }
        this.currentNode = this.rootNode;
        this.historyStack.clear();
        this.push(this.rootNode);
    }
    @Override
    protected void init() {
        super.init();
        // 移除不需要的按钮
        this.removeWidget(executeButton);
        this.removeWidget(saveButton);
        this.removeWidget(spellBoxButton);
        this.removeWidget(variableButton);

        this.spellBoxButton=this.addRenderableWidget(Button.builder(Component.translatable("gui.spellweaver.spell_library"), button->{
            Minecraft.getInstance().setScreen(new SpellStorageScreen(Minecraft.getInstance().player,this));
        }).bounds(width - 60, height / 4, 50, 20).build());
    }

    @Override
    public void onClose() {
        CompoundTag spell = rootNode.serializeNBT();
        ModMessage.sendToServer(new WriteSpellInScrollC2SPacket(spell));
        Minecraft.getInstance().setScreen(null);
    }
}
