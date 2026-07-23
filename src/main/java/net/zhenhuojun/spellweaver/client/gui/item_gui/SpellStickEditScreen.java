package net.zhenhuojun.spellweaver.client.gui.item_gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.zhenhuojun.spellweaver.client.gui.SpellStorageScreen;
import net.zhenhuojun.spellweaver.client.gui.SpellWeavingScreen;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.WriteSpellStickC2SPacket;
import net.zhenhuojun.spellweaver.client.gui.SpellNamingScreen;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

public class SpellStickEditScreen extends SpellWeavingScreen {

    public SpellStickEditScreen() {
        super(Component.translatable("gui.spellweaver.infuse_spell_staff"));
        //2026.5.21修复会读取父屏幕缓存的bug
        if(this.rootNode instanceof SequenceNode sequenceNode){
            sequenceNode.clearChildren();
        }
    }

    @Override
    protected void init() {
        super.init();
        this.removeWidget(executeButton);
        this.removeWidget(saveButton);
        this.removeWidget(spellBoxButton);
        this.removeWidget(variableButton);
        this.historyStack.clear();
        this.push(this.rootNode);

        this.spellBoxButton=this.addRenderableWidget(Button.builder(Component.translatable("gui.spellweaver.spell_library"), button->{
            Minecraft.getInstance().setScreen(new SpellStorageScreen(Minecraft.getInstance().player,this));
        }).bounds(width - 60, height / 4, 50, 20).build());
    }

    @Override
    public void onClose() {
        // 关闭编辑界面时，打开命名界面
        String defaultName = "法术_" + System.currentTimeMillis();
        Minecraft.getInstance().setScreen(new SpellNamingScreen(
                defaultName,
                name -> {
                    // 命名完成，发送写入包并彻底关闭所有界面
                    ModMessage.sendToServer(new WriteSpellStickC2SPacket(
                            name, rootNode.serializeNBT()));
                    Minecraft.getInstance().setScreen(null);
                }
        ));
    }
}