package net.zhenhuojun.spellweaver.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zhenhuojun.spellweaver.spell.node.LoopNode;

import static net.zhenhuojun.spellweaver.client.gui.SpellWeavingScreen.STARS;

public class LoopEditScreen extends Screen {
    private  SpellWeavingScreen parent;
    private EditBox time;
    private LoopNode editNode;
    private int currentTime=0;

    private double offsetY = 0;
    protected LoopEditScreen(Component pTitle) {
        super(pTitle);
    }
   public LoopEditScreen(SpellWeavingScreen parent, LoopNode editNode){
        super(Component.translatable("gui.spellweaver.loop_count"));
       this.parent = parent;
       this.editNode=editNode;
   }
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;


        time = new EditBox(font, centerX - 100, centerY - 20, 200, 20,
                Component.translatable("gui.spellweaver.loop_count"));
        time.setResponder(text -> updateButtonState());
        addRenderableWidget(time);

        // 确认按钮
        addRenderableWidget(Button.builder(Component.translatable("gui.confirm"), button -> {
            if(!time.getValue().trim().isEmpty()){
                currentTime= Integer.parseInt(time.getValue());
                saveAndReturn();
            }
            saveAndReturn();
        }).pos(centerX - 50, centerY + 20).size(100, 20).build());
    }

    private void updateButtonState() {
        // 名称非空验证
        boolean valid = !time.getValue().trim().isEmpty();
        // 这里可以添加其他验证逻辑
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
        /*if (offsetY >=this.height) {
            offsetY = 0;
        } else {
            offsetY += 0.01;
        }
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, offsetY, 0);
        guiGraphics.blit(STARS, 0, 0, 0, 0, this.width, this.height, 256, 256);
        guiGraphics.blit(STARS, 0, -this.height, 0, 0, this.width, this.height, 256, 256);
        pose.popPose();

         */
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // 绘制标题
        guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.spellweaver.spell_loop_count"),
                width / 2,
                height / 4,
                0xFFFFFF
        );
    }

    private void saveAndReturn() {

        editNode.setCurrentTime(currentTime);

        // 返回到父节点（使用父屏幕的历史栈）
        if (parent != null) {
            Minecraft.getInstance().setScreen(parent);
        } else {
            this.onClose();
        }
    }
}

