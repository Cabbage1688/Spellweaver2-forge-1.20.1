package net.zhenhuojun.spellweaver.client.gui.item_gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.Spellweaver;
import org.lwjgl.glfw.GLFW;

public class ScrollViewScreen extends ScrollEditScreen {

    public ScrollViewScreen(ItemStack itemStack, Level level) {
        //因为我的史山代码，这个父方法前半截没啥用，还得重写一遍逻辑
        super(itemStack, level);
        CompoundTag tag=itemStack.getOrCreateTag();
        if(tag.contains("scrollSpell")){
            CompoundTag spellData= tag.getCompound("scrollSpell");
            if(spellData.contains("sequence")){
                CompoundTag sequenceData=spellData.getCompound("sequence");
                this.rootNode.deserializeNBT(sequenceData);
                Spellweaver.getLOGGER().debug("[Spellweaver:ScrollViewScreen]rootNode完成反序列化，读取的tag为{}",sequenceData);
            }
        }

    }

    @Override
    protected void init() {
         super.init();
         this.removeWidget(stickButton);
         this.removeWidget(killButton);
    }



    // 禁用右键菜单（添加节点）
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) return true; // 吞掉右键
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return true; // 吞掉中键（禁止编辑节点）
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // 禁用退格键删除节点
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) return true; // 吞掉退格键
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // 关闭时不做任何保存，直接退出
    @Override
    public void onClose() {
        // 不发送 WriteSpellInScrollC2SPacket，不修改卷轴
        Minecraft.getInstance().setScreen(null);
    }
}