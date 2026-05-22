package net.zhenhuojun.spellweaver.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
//这个给卷轴用
public class SpellNamingScreen extends Screen {
    private final String defaultName;
    private final Consumer<String> onConfirm;
    private EditBox nameField;

    public SpellNamingScreen(
                             String defaultName,
                             Consumer<String> onConfirm) {
        super(Component.literal("命名法术"));
        this.defaultName = defaultName;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 名称输入框
        nameField = new EditBox(font, centerX - 100, centerY - 20, 200, 20,
                Component.literal("法术名称"));
        nameField.setValue(defaultName);
        nameField.setResponder(text -> updateButtonState());
        addRenderableWidget(nameField);

        // 确认按钮
        addRenderableWidget(Button.builder(Component.literal("确认"), button -> {
            onConfirm.accept(nameField.getValue().trim());
        }).pos(centerX - 50, centerY + 20).size(100, 20).build());
    }

    private void updateButtonState() {
        // 名称非空验证
        boolean valid = !nameField.getValue().trim().isEmpty();
        // 这里可以添加其他验证逻辑
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // 绘制标题
        guiGraphics.drawCenteredString(
                font,
                Component.literal("为法术命名"),
                width / 2,
                height / 4,
                0xFFFFFF
        );
    }
}