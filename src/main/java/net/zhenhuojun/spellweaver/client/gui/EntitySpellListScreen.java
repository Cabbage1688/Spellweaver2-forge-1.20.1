package net.zhenhuojun.spellweaver.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.SpellListEntry;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.client.gui.util.ClientMagicStarData;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.MagicStarActionC2SPacket;

import java.util.List;
import java.util.UUID;

/**
 * 实体法术列表管理界面：
 * 展示指定列表（attack/shield/protectMaster/routine）的法术与延迟条目，含名称与备注。
 * 功能：上移、下移、删除、禁用、添加延迟、添加法术（跳转到法术库界面）。
 * 数据源为 ClientMagicStarData，所有修改通过C2S包发送至服务器，服务器处理后回推同步。
 */
public class EntitySpellListScreen extends Screen {
    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 180;
    private static final int LIST_X_OFFSET = 8;
    private static final int LIST_WIDTH = 204;   // GUI_WIDTH - 2*LIST_X_OFFSET
    private static final int ENTRY_HEIGHT = 18;
    private static final int VISIBLE_ENTRIES = 6;

    private final int listType;            // 0=attack,1=shield,2=protectMaster,3=routine
    private final MagicStarScreen parentScreen;

    private int leftPos, topPos;
    private int selectedIndex = -1;
    private int scrollOffset = 0;

    private Button upButton, downButton, deleteButton, addButton, backButton, toggleDisableButton, delayButton;

    public EntitySpellListScreen(int listType, MagicStarScreen parentScreen) {
        super(Component.translatable("gui.spellweaver.magic_star.list.title." + listType));
        this.listType = listType;
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        int btnY = topPos + GUI_HEIGHT - 25;
        int btnW = 28;
        int btnH = 18;
        int gap = 2;

        // 上移 / 下移 / 删除 / 禁用 / 延迟 / 添加 / 返回
        this.upButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.up"), b -> moveSelected(-1))
                .pos(leftPos + 4, btnY).size(btnW, btnH).build());
        this.downButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.down"), b -> moveSelected(+1))
                .pos(leftPos + 4 + (btnW + gap), btnY).size(btnW, btnH).build());
        this.deleteButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.delete"), b -> deleteSelected())
                .pos(leftPos + 4 + 2 * (btnW + gap), btnY).size(btnW, btnH).build());
        this.toggleDisableButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.disable"), b -> toggleDisableSelected())
                .pos(leftPos + 4 + 3 * (btnW + gap), btnY).size(btnW, btnH).build());
        this.delayButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.add_delay"), b -> openDelayInputScreen())
                .pos(leftPos + 4 + 4 * (btnW + gap), btnY).size(btnW, btnH).build());
        this.addButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.add"), b -> openAddScreen())
                .pos(leftPos + 4 + 5 * (btnW + gap), btnY).size(btnW, btnH).build());
        this.backButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.return"), b -> Minecraft.getInstance().setScreen(parentScreen))
                .pos(leftPos + 4 + 6 * (btnW + gap), btnY).size(btnW, btnH).build());

        clampScroll();
        updateButtonStates();
    }

    private List<SpellListEntry> getCurrentList() {
        return ClientMagicStarData.getListByType(listType);
    }

    private int getEntityId() {
        return ClientMagicStarData.getCurrentEntityId();
    }

    private void clampScroll() {
        int total = getCurrentList().size();
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > Math.max(0, total - VISIBLE_ENTRIES)) {
            scrollOffset = Math.max(0, total - VISIBLE_ENTRIES);
        }
        if (selectedIndex >= 0 && selectedIndex < total) {
            if (selectedIndex < scrollOffset) scrollOffset = selectedIndex;
            if (selectedIndex >= scrollOffset + VISIBLE_ENTRIES) {
                scrollOffset = selectedIndex - VISIBLE_ENTRIES + 1;
            }
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedIndex >= 0 && selectedIndex < getCurrentList().size();
        upButton.active = hasSelection && selectedIndex > 0;
        downButton.active = hasSelection && selectedIndex < getCurrentList().size() - 1;
        deleteButton.active = hasSelection;
        toggleDisableButton.active = hasSelection;
    }

    private void toggleDisableSelected() {
        if (selectedIndex < 0 || selectedIndex >= getCurrentList().size()) return;
        UUID id = getCurrentList().get(selectedIndex).getId();
        ModMessage.sendToServer(MagicStarActionC2SPacket.toggleDisable(getEntityId(), listType, id));
        // 乐观更新本地缓存
        if (ClientMagicStarData.isSpellDisabled(listType, id)) {
            ClientMagicStarData.getDisabledSpells(listType).remove(id);
        } else {
            ClientMagicStarData.getDisabledSpells(listType).add(id);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        // 标题
        guiGraphics.drawCenteredString(this.font, this.title,
                this.width / 2, topPos + 5, 0xFFFFFF);

        // 列表区域
        int listTop = topPos + 20;
        int listBottom = listTop + VISIBLE_ENTRIES * ENTRY_HEIGHT;

        // 列表背景
        guiGraphics.fill(leftPos + LIST_X_OFFSET, listTop,
                leftPos + LIST_X_OFFSET + LIST_WIDTH, listBottom, 0x40000000);

        List<SpellListEntry> entries = getCurrentList();
        for (int i = 0; i < VISIBLE_ENTRIES; i++) {
            int idx = scrollOffset + i;
            if (idx >= entries.size()) break;
            int yPos = listTop + i * ENTRY_HEIGHT;
            SpellListEntry entry = entries.get(idx);

            // 选中背景
            if (idx == selectedIndex) {
                guiGraphics.fill(leftPos + LIST_X_OFFSET, yPos,
                        leftPos + LIST_X_OFFSET + LIST_WIDTH, yPos + ENTRY_HEIGHT, 0x60FFFFFF);
            }
            // 鼠标悬停背景
            if (mouseX >= leftPos + LIST_X_OFFSET && mouseX < leftPos + LIST_X_OFFSET + LIST_WIDTH
                    && mouseY >= yPos && mouseY < yPos + ENTRY_HEIGHT && idx != selectedIndex) {
                guiGraphics.fill(leftPos + LIST_X_OFFSET, yPos,
                        leftPos + LIST_X_OFFSET + LIST_WIDTH, yPos + ENTRY_HEIGHT, 0x30FFFFFF);
            }
            // 序号
            guiGraphics.drawString(this.font, String.valueOf(idx + 1),
                    leftPos + LIST_X_OFFSET + 2, yPos + 5, 0xAAAAAA, false);
            // 禁用标记
            boolean isDisabled = ClientMagicStarData.isSpellDisabled(listType, entry.getId());
            if (isDisabled) {
                //guiGraphics.drawString(this.font, "[X]",
                //leftPos + LIST_X_OFFSET + 14, yPos + 5, 0xFF6666, false);
                guiGraphics.drawString(this.font, "❌",
                        leftPos + LIST_X_OFFSET + 14-4, yPos + 5, 0xFF6666, false);

            }
            // 条目名称
            int nameX = leftPos + LIST_X_OFFSET + 20;
            int nameMaxWidth = LIST_WIDTH - 28;
            String name;
            int nameColor;
            if (entry.isDelay()) {
                name = Component.translatable("gui.spellweaver.magic_star.delay_display", entry.getDelayTicks()).getString();
                nameColor = isDisabled ? 0x664444 : 0xFFAA00;
            } else {
                StoredSpell spell = entry.getSpell();
                name = spell.getName();
                nameColor = isDisabled ? 0x888888 : 0xFFFFFF;
            }
            if (this.font.width(name) > nameMaxWidth) {
                name = this.font.plainSubstrByWidth(name, nameMaxWidth - this.font.width("...")) + "...";
            }
            guiGraphics.drawString(this.font, name, nameX, yPos + 5, nameColor, false);
        }

        // 滚动条
        int total = entries.size();
        if (total > VISIBLE_ENTRIES) {
            int trackHeight = VISIBLE_ENTRIES * ENTRY_HEIGHT;
            int thumbHeight = Math.max(15, trackHeight * VISIBLE_ENTRIES / total);
            int trackY = listTop;
            int thumbY = trackY + (trackHeight - thumbHeight) * scrollOffset / Math.max(1, total - VISIBLE_ENTRIES);
            int barX = leftPos + LIST_X_OFFSET + LIST_WIDTH - 3;
            guiGraphics.fill(barX, trackY, barX + 3, trackY + trackHeight, 0x40FFFFFF);
            guiGraphics.fill(barX, thumbY, barX + 3, thumbY + thumbHeight, 0xFFAAAAAA);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // 右侧详情：备注（仅法术条目显示）
        if (selectedIndex >= 0 && selectedIndex < entries.size()) {
            SpellListEntry entry = entries.get(selectedIndex);
            if (entry.isSpell()) {
                StoredSpell spell = entry.getSpell();
                int detailX = leftPos + GUI_WIDTH + 6-12;
                int detailY = topPos + 20;
                int detailWidth = 130;
                guiGraphics.fill(detailX - 2, detailY - 2, detailX + detailWidth, detailY + 90, 0x60000000);
                guiGraphics.drawString(this.font,
                        Component.translatable("gui.spellweaver.magic_star.note_label"),
                        detailX, detailY, 0xAAAAFF, false);
                String note = spell.getNote();
                if (note.isEmpty()) {
                    guiGraphics.drawString(this.font,
                            Component.translatable("gui.spellweaver.no_note"),
                            detailX, detailY + 12, 0x666666, false);
                } else {
                    List<FormattedCharSequence> lines = this.font.split(Component.literal(note), detailWidth - 4-20);
                    int y = detailY + 12;
                    for (FormattedCharSequence line : lines) {
                        guiGraphics.drawString(this.font, line, detailX, y, 0xCCCCCC, false);
                        y += 9;
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listTop = topPos + 20;
        int listBottom = listTop + VISIBLE_ENTRIES * ENTRY_HEIGHT;
        if (mouseX >= leftPos + LIST_X_OFFSET && mouseX < leftPos + LIST_X_OFFSET + LIST_WIDTH
                && mouseY >= listTop && mouseY < listBottom) {
            int clickedVisible = (int) ((mouseY - listTop) / ENTRY_HEIGHT);
            int idx = scrollOffset + clickedVisible;
            if (idx >= 0 && idx < getCurrentList().size()) {
                selectedIndex = idx;
                updateButtonStates();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) scrollOffset--;
        else scrollOffset++;
        clampScroll();
        return true;
    }

    private void moveSelected(int delta) {
        if (selectedIndex < 0 || selectedIndex >= getCurrentList().size()) return;
        int newIdx = selectedIndex + delta;
        if (newIdx < 0 || newIdx >= getCurrentList().size()) return;
        UUID id = getCurrentList().get(selectedIndex).getId();
        ModMessage.sendToServer(delta < 0
                ? MagicStarActionC2SPacket.moveUp(getEntityId(), listType, id)
                : MagicStarActionC2SPacket.moveDown(getEntityId(), listType, id));
        // 乐观更新本地缓存
        List<SpellListEntry> list = getCurrentList();
        SpellListEntry tmp = list.get(selectedIndex);
        list.set(selectedIndex, list.get(newIdx));
        list.set(newIdx, tmp);
        selectedIndex = newIdx;
        clampScroll();
        updateButtonStates();
    }

    private void deleteSelected() {
        if (selectedIndex < 0 || selectedIndex >= getCurrentList().size()) return;
        UUID id = getCurrentList().get(selectedIndex).getId();
        ModMessage.sendToServer(MagicStarActionC2SPacket.removeSpell(getEntityId(), listType, id));
        // 乐观更新本地缓存
        getCurrentList().remove(selectedIndex);
        if (selectedIndex >= getCurrentList().size()) {
            selectedIndex = getCurrentList().size() - 1;
        }
        clampScroll();
        updateButtonStates();
    }

    private void openAddScreen() {
        Minecraft.getInstance().setScreen(new AddSpellFromLibraryScreen(listType, this));
    }

    private void openDelayInputScreen() {
        Minecraft.getInstance().setScreen(new DelayInputScreen(listType, this));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
