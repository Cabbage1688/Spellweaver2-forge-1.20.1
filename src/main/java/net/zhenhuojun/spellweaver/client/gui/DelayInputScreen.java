package net.zhenhuojun.spellweaver.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zhenhuojun.spellweaver.client.gui.util.ClientMagicStarData;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.SpellListEntry;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.MagicStarActionC2SPacket;

/**
 * 延迟输入弹窗：输入tick数后添加延迟条目到法术列表。
 */
public class DelayInputScreen extends Screen {
    private static final int GUI_WIDTH = 200;
    private static final int GUI_HEIGHT = 80;

    private final int listType;
    private final EntitySpellListScreen parentScreen;

    private EditBox inputBox;
    private Button confirmButton, cancelButton;

    public DelayInputScreen(int listType, EntitySpellListScreen parentScreen) {
        super(Component.translatable("gui.spellweaver.magic_star.delay_input.title"));
        this.listType = listType;
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        int leftPos = (this.width - GUI_WIDTH) / 2;
        int topPos = (this.height - GUI_HEIGHT) / 2;

        // 输入框
        this.inputBox = new EditBox(this.font, leftPos + 10, topPos + 30, GUI_WIDTH - 20, 18,
                Component.translatable("gui.spellweaver.magic_star.delay_input.placeholder"));
        this.inputBox.setMaxLength(6);
        this.inputBox.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
        this.inputBox.setValue("20");
        this.addRenderableWidget(this.inputBox);

        // 确认按钮
        this.confirmButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.confirm"), b -> confirm())
                .pos(leftPos + 20, topPos + 55).size(70, 18).build());

        // 取消按钮
        this.cancelButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.return"), b -> cancel())
                .pos(leftPos + 110, topPos + 55).size(70, 18).build());
    }

    private void confirm() {
        String text = inputBox.getValue().trim();
        if (text.isEmpty()) return;
        try {
            int delayTicks = Integer.parseInt(text);
            if (delayTicks <= 0) return;
            // 发送添加延迟包
            ModMessage.sendToServer(MagicStarActionC2SPacket.addDelay(
                    ClientMagicStarData.getCurrentEntityId(), listType, delayTicks));
            // 乐观更新本地缓存
            ClientMagicStarData.getListByType(listType).add(SpellListEntry.ofDelay(delayTicks));
        } catch (NumberFormatException ignored) {
        }
        Minecraft.getInstance().setScreen(parentScreen);
    }

    private void cancel() {
        Minecraft.getInstance().setScreen(parentScreen);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        int leftPos = (this.width - GUI_WIDTH) / 2;
        int topPos = (this.height - GUI_HEIGHT) / 2;
        // 背景
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0xC0000000);
        // 标题
        guiGraphics.drawCenteredString(this.font, this.title,
                this.width / 2, topPos + 8, 0xFFFFFF);
        // 提示
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.spellweaver.magic_star.delay_input.tip"),
                this.width / 2, topPos + 20, 0xAAAAFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
