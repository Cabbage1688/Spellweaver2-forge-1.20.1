package net.zhenhuojun.spellweaver.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zhenhuojun.spellweaver.client.gui.util.ClientMagicStarData;
import net.zhenhuojun.spellweaver.entity.impl.MagicStarEntity;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.MagicStarActionC2SPacket;

/**
 * 魔法之星主菜单GUI：
 * 第一行：4个行动模式按钮（跟随/定点/待机/巡逻），点击切换模式
 * 第二行：4个法术列表按钮（攻击/自保/保护主人/日常），点击进入对应列表管理
 * 右键实体时由服务器推送数据后打开此界面
 */
public class MagicStarScreen extends Screen {
    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 200;
    private static final int BTN_WIDTH = 100;
    private static final int BTN_HEIGHT = 20;
    private static final int GAP = 5;

    private int leftPos, topPos;

    public MagicStarScreen() {
        super(Component.translatable("gui.spellweaver.magic_star.title"));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        MagicStarEntity.ActMode currentMode = ClientMagicStarData.getActMode();
        int entityId = ClientMagicStarData.getCurrentEntityId();

        // 第一行：4个模式按钮（两两并排）
        int row1Y = topPos + 25;
        int colX1 = leftPos;
        int colX2 = leftPos + BTN_WIDTH + GAP;

        // 跟随
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.mode.follow"),
                        b -> sendModeChange(entityId, MagicStarEntity.ActMode.FOLLOW))
                .pos(colX1, row1Y).size(BTN_WIDTH, BTN_HEIGHT).build());
        // 定点
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.mode.stop"),
                        b -> sendModeChange(entityId, MagicStarEntity.ActMode.STOP))
                .pos(colX2, row1Y).size(BTN_WIDTH, BTN_HEIGHT).build());

        // 待机
        int row2Y = row1Y + BTN_HEIGHT + GAP;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.mode.sleep"),
                        b -> sendModeChange(entityId, MagicStarEntity.ActMode.SLEEP))
                .pos(colX1, row2Y).size(BTN_WIDTH, BTN_HEIGHT).build());
        // 巡逻
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.mode.patrol"),
                        b -> sendModeChange(entityId, MagicStarEntity.ActMode.PATROL))
                .pos(colX2, row2Y).size(BTN_WIDTH, BTN_HEIGHT).build());

        // 第二行：4个列表按钮
        int row3Y = row2Y + BTN_HEIGHT + GAP + 5;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.list.attack"),
                        b -> openListScreen(0))
                .pos(colX1, row3Y).size(BTN_WIDTH, BTN_HEIGHT).build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.list.shield"),
                        b -> openListScreen(1))
                .pos(colX2, row3Y).size(BTN_WIDTH, BTN_HEIGHT).build());

        int row4Y = row3Y + BTN_HEIGHT + GAP;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.list.protect_master"),
                        b -> openListScreen(2))
                .pos(colX1, row4Y).size(BTN_WIDTH, BTN_HEIGHT).build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.list.routine"),
                        b -> openListScreen(3))
                .pos(colX2, row4Y).size(BTN_WIDTH, BTN_HEIGHT).build());

        // 底部居中：物品化按钮
        int itemizeBtnW = 120;
        int row5Y = topPos + GUI_HEIGHT - BTN_HEIGHT - 8;
        int itemizeX = this.width / 2 - itemizeBtnW / 2;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.magic_star.itemize"),
                        b -> sendItemize(entityId))
                .pos(itemizeX, row5Y).size(itemizeBtnW, BTN_HEIGHT).build());

    }

    private void sendModeChange(int entityId, MagicStarEntity.ActMode mode) {
        ModMessage.sendToServer(MagicStarActionC2SPacket.changeMode(entityId, mode.ordinal()));
        // 本地立即更新缓存
        ClientMagicStarData.setActModeOrdinal(mode.ordinal());
        Minecraft.getInstance().setScreen(null);
    }

    private void openListScreen(int listType) {
        Minecraft.getInstance().setScreen(new EntitySpellListScreen(listType, this));
    }

    private void sendItemize(int entityId) {
        ModMessage.sendToServer(MagicStarActionC2SPacket.itemize(entityId));
        // 物品化后实体消失，直接关闭GUI
        ClientMagicStarData.clear();
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        // 半透明背景框
        //guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + GUI_WIDTH + 5, topPos + GUI_HEIGHT + 5, 0x80000000);
        // 标题
        guiGraphics.drawCenteredString(this.font, this.title,
                this.width / 2, topPos + 5, 0xFFFFFF);
        // 当前模式提示
        MagicStarEntity.ActMode mode = ClientMagicStarData.getActMode();
        Component modeText = Component.translatable("gui.spellweaver.magic_star.current_mode",
                Component.translatable("gui.spellweaver.magic_star.mode." + mode.name().toLowerCase()));
        guiGraphics.drawCenteredString(this.font, modeText,
                this.width / 2, topPos + 15, 0xAAAAFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
