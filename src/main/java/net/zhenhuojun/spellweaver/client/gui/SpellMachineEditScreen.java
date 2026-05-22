package net.zhenhuojun.spellweaver.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.SpellMachineWriteC2SPacket;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

public class SpellMachineEditScreen extends SpellWeavingScreen {

    private final BlockPos machinePos;

    public SpellMachineEditScreen(BlockPos machinePos, CompoundTag initialSpell) {
        super(Component.literal("编辑机器法术"));
        this.machinePos = machinePos;

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
        super.init();  // 父类会初始化按钮、菜单等

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
        ModMessage.sendToServer(new SpellMachineWriteC2SPacket(machinePos, spellTag));
        Minecraft.getInstance().setScreen(null);
    }
}