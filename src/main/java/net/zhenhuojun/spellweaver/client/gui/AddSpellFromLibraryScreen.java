package net.zhenhuojun.spellweaver.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.PlayerSpellStorage;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.SpellListEntry;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.client.gui.util.ClientMagicStarData;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerStorageData;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.MagicStarActionC2SPacket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 从法术库添加法术到实体列表的界面。
 * 展示玩家法术库（ClientPlayerStorageData），点击法术条目即将该法术添加到当前实体列表，
 * 已添加的法术以暗色/打勾标记，玩家可继续选多个，按返回手动结束。
 */
public class AddSpellFromLibraryScreen extends Screen {
    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 180;
    private static final int LIST_X_OFFSET = 8;
    private static final int LIST_WIDTH = 204;
    private static final int ENTRY_HEIGHT = 18;
    private static final int VISIBLE_ENTRIES = 6;

    private final int listType;
    private final EntitySpellListScreen parentScreen;

    private int leftPos, topPos;
    private int scrollOffset = 0;

    /** 当前实体列表中已存在的法术UUID（用于标记已添加） */
    private final Set<UUID> alreadyInList = new HashSet<>();

    private Button backButton;

    public AddSpellFromLibraryScreen(int listType, EntitySpellListScreen parentScreen) {
        super(Component.translatable("gui.spellweaver.magic_star.add.title." + listType));
        this.listType = listType;
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        // 刷新已添加集合
        alreadyInList.clear();
        for (SpellListEntry entry : ClientMagicStarData.getListByType(listType)) {
            if (entry.isSpell()) {
                alreadyInList.add(entry.getSpell().getId());
            }
        }

        this.backButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.return"),
                        b -> Minecraft.getInstance().setScreen(parentScreen))
                .pos(leftPos + GUI_WIDTH - 60, topPos + GUI_HEIGHT - 25).size(56, 18).build());

        clampScroll();
    }

    private PlayerSpellStorage getStorage() {
        return ClientPlayerStorageData.getPlayerSpellStorage();
    }

    private List<StoredSpell> getLibrarySpells() {
        if (getStorage() == null) return new ArrayList<>();
        return new ArrayList<>(getStorage().getAllSpells());
    }

    private void clampScroll() {
        int total = getLibrarySpells().size();
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > Math.max(0, total - VISIBLE_ENTRIES)) {
            scrollOffset = Math.max(0, total - VISIBLE_ENTRIES);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        //guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + GUI_WIDTH + 5, topPos + GUI_HEIGHT + 5, 0x80000000);
        guiGraphics.drawCenteredString(this.font, this.title,
                this.width / 2, topPos + 5, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.spellweaver.magic_star.add.tip"),
                this.width / 2, topPos + 14, 0xAAAAFF);

        int listTop = topPos + 28;
        int listBottom = listTop + VISIBLE_ENTRIES * ENTRY_HEIGHT;

        guiGraphics.fill(leftPos + LIST_X_OFFSET, listTop,
                leftPos + LIST_X_OFFSET + LIST_WIDTH, listBottom, 0x40000000);

        List<StoredSpell> spells = getLibrarySpells();
        for (int i = 0; i < VISIBLE_ENTRIES; i++) {
            int idx = scrollOffset + i;
            if (idx >= spells.size()) break;
            int yPos = listTop + i * ENTRY_HEIGHT;
            StoredSpell spell = spells.get(idx);
            boolean added = alreadyInList.contains(spell.getId());

            // 已添加条目用绿色色调
            if (added) {
                guiGraphics.fill(leftPos + LIST_X_OFFSET, yPos,
                        leftPos + LIST_X_OFFSET + LIST_WIDTH, yPos + ENTRY_HEIGHT, 0x4000AA00);
            }
            // 鼠标悬停背景
            if (mouseX >= leftPos + LIST_X_OFFSET && mouseX < leftPos + LIST_X_OFFSET + LIST_WIDTH
                    && mouseY >= yPos && mouseY < yPos + ENTRY_HEIGHT) {
                guiGraphics.fill(leftPos + LIST_X_OFFSET, yPos,
                        leftPos + LIST_X_OFFSET + LIST_WIDTH, yPos + ENTRY_HEIGHT, 0x30FFFFFF);
            }
            // 标记符号
            String mark = added ? "✓" : "+";
            int markColor = added ? 0x55FF55 : 0xFFFF55;
            guiGraphics.drawString(this.font, mark, leftPos + LIST_X_OFFSET + 4, yPos + 5, markColor, false);
            // 法术名称
            String name = spell.getName();
            int nameX = leftPos + LIST_X_OFFSET + 18;
            int nameMaxWidth = LIST_WIDTH - 26;
            if (this.font.width(name) > nameMaxWidth) {
                name = this.font.plainSubstrByWidth(name, nameMaxWidth - this.font.width("...")) + "...";
            }
            guiGraphics.drawString(this.font, name, nameX, yPos + 5,
                    added ? 0xAAAAAA : 0xFFFFFF, false);
        }

        // 滚动条
        int total = spells.size();
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

        // 右侧详情：备注
        int hoverIdx = getHoveredIndex(mouseX, mouseY);
        if (hoverIdx >= 0 && hoverIdx < spells.size()) {
            StoredSpell spell = spells.get(hoverIdx);
            int detailX = leftPos + GUI_WIDTH + 6-12;
            int detailY = topPos + 28;
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

    private int getHoveredIndex(double mouseX, double mouseY) {
        int listTop = topPos + 28;
        int listBottom = listTop + VISIBLE_ENTRIES * ENTRY_HEIGHT;
        if (mouseX >= leftPos + LIST_X_OFFSET && mouseX < leftPos + LIST_X_OFFSET + LIST_WIDTH
                && mouseY >= listTop && mouseY < listBottom) {
            int clickedVisible = (int) ((mouseY - listTop) / ENTRY_HEIGHT);
            int idx = scrollOffset + clickedVisible;
            if (idx >= 0 && idx < getLibrarySpells().size()) return idx;
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int idx = getHoveredIndex(mouseX, mouseY);
        if (idx >= 0) {
            StoredSpell spell = getLibrarySpells().get(idx);
            if (alreadyInList.contains(spell.getId())) {
                // 已添加，提示
                Minecraft.getInstance().player.displayClientMessage(
                        Component.translatable("gui.spellweaver.magic_star.add.already_added", spell.getName()),
                        true
                );
            } else {
                // 发送添加包
                ModMessage.sendToServer(MagicStarActionC2SPacket.addSpell(
                        ClientMagicStarData.getCurrentEntityId(), listType, spell.getId()));
                // 乐观更新
                alreadyInList.add(spell.getId());
                ClientMagicStarData.getListByType(listType).add(
                        SpellListEntry.ofSpell(StoredSpell.deserialize(spell.serialize()))
                );
                Minecraft.getInstance().player.displayClientMessage(
                        Component.translatable("gui.spellweaver.magic_star.add.added", spell.getName()),
                        true
                );
            }
            return true;
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
