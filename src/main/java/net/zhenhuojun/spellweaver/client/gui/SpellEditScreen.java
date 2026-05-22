package net.zhenhuojun.spellweaver.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerSpellData;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.UpdateSpellC2SPacket;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

public class SpellEditScreen extends SpellWeavingScreen {

    private final StoredSpell originalSpell;
    private final Screen parentScreen;

    public SpellEditScreen(StoredSpell spell, Screen parent) {
        super(Component.literal("编辑法术"));
        this.originalSpell = spell;
        this.parentScreen = parent;

        //避免直接修改原法术而导致取消按钮失效
        CompoundTag tag = spell.getSequenceNode().serializeNBT();
        this.rootNode = new SequenceNode();
        this.rootNode.deserializeNBT(tag);
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

        // 保存按钮
        this.addRenderableWidget(Button.builder(Component.literal("保存更改"), button -> saveAndClose())
                .bounds(width - 60, height / 4, 50, 20).build());

        // 取消按钮
        this.addRenderableWidget(Button.builder(Component.literal("取消更改"), button -> onClose())
                .bounds(width - 60, height / 4 - 30, 50, 20).build());

        // 复制按钮
        /*this.addRenderableWidget(Button.builder(Component.literal("复制"), button -> {
                    if(rootNode!=null&&Minecraft.getInstance().player != null){
                        ClientPlayerSpellData playerData = ClientPlayerSpellData.get(Minecraft.getInstance().player);
                        if(playerData!=null){
                            playerData.setCopyTag(rootNode.serializeNBT());
                        }
                    }
                })
                .bounds(width - 60, height / 4 - 30, 50, 20).build());

         */
    }

    /**
     * 保存编辑：将修改后的节点树写回原始法术，并同步服务器
     */
    private void saveAndClose() {
        // 将编辑后的节点树设置回原始法术
        originalSpell.setSequenceNode((SequenceNode) this.rootNode);
        ModMessage.sendToServer(new UpdateSpellC2SPacket(originalSpell.getId(), rootNode.serializeNBT()));
        if (parentScreen instanceof SpellStorageScreen) {
            ((SpellStorageScreen) parentScreen).refreshSpells();
        }

        Minecraft.getInstance().setScreen(parentScreen);
    }

    @Override
    public void onClose() {
        // 直接返回不做任何保存
        Minecraft.getInstance().setScreen(parentScreen);
    }
}