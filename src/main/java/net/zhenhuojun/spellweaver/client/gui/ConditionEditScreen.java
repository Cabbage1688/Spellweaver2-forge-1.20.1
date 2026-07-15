package net.zhenhuojun.spellweaver.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zhenhuojun.spellweaver.spell.node.ConditionNode;

import static net.zhenhuojun.spellweaver.client.gui.SpellWeavingScreen.STARS;

public class ConditionEditScreen extends Screen {
    private final SpellWeavingScreen parent;
    private final ConditionNode editNode;
    private boolean currentCondition;

    private double offsetY = 0;

    public ConditionEditScreen(SpellWeavingScreen parent, ConditionNode editNode) {
        super(Component.translatable("gui.spellweaver.condition_settings"));
        this.parent = parent;
        this.editNode = editNode;
        this.currentCondition = editNode.getCondition();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonWidth = 80;
        int buttonHeight = 20;

        // “是”按钮
        Button trueButton = Button.builder(Component.literal("true"), btn -> {
            currentCondition = true;
            updateButtonStyles();
        }).pos(centerX - buttonWidth - 10, centerY).size(buttonWidth, buttonHeight).build();
        addRenderableWidget(trueButton);

        // “否”按钮
        Button falseButton = Button.builder(Component.literal("false"), btn -> {
            currentCondition = false;
            updateButtonStyles();
        }).pos(centerX + 10, centerY).size(buttonWidth, buttonHeight).build();
        addRenderableWidget(falseButton);

        // 确认按钮
        addRenderableWidget(Button.builder(Component.translatable("gui.confirm"), btn -> {
            editNode.setCondition(currentCondition);
            saveAndReturn();
        }).pos(centerX - 50, centerY + 30).size(100, 20).build());

        updateButtonStyles();
    }

    private void updateButtonStyles() {
        // 此处可以根据 currentCondition 改变按钮颜色以指示当前选中状态
        // 但 Minecraft 的 Button 默认样式修改较麻烦，可略过或使用 Narration 提示
        // 若需视觉反馈，可重写渲染或改用其他控件，这里保持简洁。
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



        guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.spellweaver.condition_true"),
                width / 2,
                height / 4,
                0xFFFFFF
        );

        // 绘制当前选中状态文字
        String status = currentCondition ? "true" : "false";
        guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.spellweaver.current_state", status),
                width / 2,
                height / 2 - 20,
                0xAAAAAA
        );
    }

    private void saveAndReturn() {
        if (parent != null) {
            Minecraft.getInstance().setScreen(parent);
        } else {
            this.onClose();
        }
    }

    @Override
    public void onClose() {
        // 关闭时不保存修改
        super.onClose();
    }
}
