package net.zhenhuojun.spellweaver.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.InscriptionTableWriteC2SPacket;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

public class InscriptionTableEditScreen extends SpellWeavingScreen {

    private final BlockPos tablePos;

    public InscriptionTableEditScreen(BlockPos tablePos, CompoundTag initialSpell) {
        super(Component.translatable("gui.spellweaver.edit_inscription_spell"));
        this.tablePos = tablePos;

        if (initialSpell != null && !initialSpell.isEmpty()) {
            this.rootNode = new SequenceNode();
            this.rootNode.deserializeNBT(initialSpell);
        } else {
            this.rootNode = new SequenceNode();
        }
        this.currentNode = this.rootNode;
        this.historyStack.clear();
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
    }

    @Override
    public void onClose() {
        CompoundTag spellTag = this.rootNode.serializeNBT();
        ModMessage.sendToServer(new InscriptionTableWriteC2SPacket(tablePos, spellTag));
        Minecraft.getInstance().setScreen(null);
    }
}