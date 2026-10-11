package net.zhenhuojun.spellweaver.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.client.data_util.ClientPlayerOverloadData;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.SetSpellOverloadC2SPacket;

/**
 * 法术编辑中心界面：整合 重命名 / 编辑法术 / 编辑备注 / 设置超载 四个入口。
 * 从法术库选中某个法术后点击"编辑"进入，再由此跳转到对应子界面。
 */
public class EditHubScreen extends Screen {
    private final StoredSpell spell;
    private final SpellStorageScreen parent;

    public EditHubScreen(StoredSpell spell, SpellStorageScreen parent) {
        super(Component.translatable("gui.spellweaver.edit_hub"));
        this.spell = spell;
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 重命名 → 复用 SpellStorageScreen 内部的 RenameSpellScreen，确认/取消后返回本界面
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.rename"),
                        btn -> Minecraft.getInstance().setScreen(parent.new RenameSpellScreen(this, spell)))
                .pos(centerX - 60, centerY - 50)
                .size(120, 20)
                .build());

        // 编辑法术 → 跳到 SpellEditScreen（法术编织界面）
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.edit"),
                        btn -> Minecraft.getInstance().setScreen(new SpellEditScreen(spell, this)))
                .pos(centerX - 60, centerY - 25)
                .size(120, 20)
                .build());

        // 编辑备注 → 复用 SpellStorageScreen 内部的 NoteEditScreen，确认/取消后返回本界面
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.edit_note"),
                        btn -> Minecraft.getInstance().setScreen(parent.new NoteEditScreen(this, spell)))
                .pos(centerX - 60, centerY)
                .size(120, 20)
                .build());

        // 设置超载 → 超载输入界面
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.set_overload"),
                        btn -> Minecraft.getInstance().setScreen(new OverloadInputScreen(this, spell)))
                .pos(centerX - 60, centerY + 25)
                .size(120, 20)
                .build());

        // 返回法术库
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.spellweaver.return"),
                        btn -> Minecraft.getInstance().setScreen(parent))
                .pos(centerX - 60, centerY + 50)
                .size(120, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 30, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
                Component.literal(spell.getName()), this.width / 2, 45, 0xAAAAAA);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    /**
     * 库存法术独立超载输入界面：一个输入框 + 确认/取消。
     * 语义：1=跟随超载能力（默认），0=固定1倍，>1=固定N倍（超过上限取上限）。
     */
    class OverloadInputScreen extends Screen {
        private final EditHubScreen parent;
        private final StoredSpell spell;
        private EditBox valueField;

        public OverloadInputScreen(EditHubScreen parent, StoredSpell spell) {
            super(Component.translatable("gui.spellweaver.set_overload"));
            this.parent = parent;
            this.spell = spell;
        }

        @Override
        protected void init() {
            super.init();
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            this.valueField = new EditBox(this.font, centerX - 60, centerY - 20, 120, 20,
                    Component.translatable("gui.spellweaver.set_overload"));
            // 只允许非负整数
            this.valueField.setFilter(s -> s.matches("[0-9]*"));
            this.valueField.setValue(String.valueOf(spell.getSequenceNode().getOverloadMultiplier()));
            this.addRenderableWidget(valueField);

            this.addRenderableWidget(Button.builder(
                            Component.translatable("gui.confirm"),
                            btn -> confirmOverload())
                    .pos(centerX - 60, centerY + 10)
                    .size(58, 20)
                    .build());

            this.addRenderableWidget(Button.builder(
                            Component.translatable("gui.cancel"),
                            btn -> Minecraft.getInstance().setScreen(parent))
                    .pos(centerX + 2, centerY + 10)
                    .size(58, 20)
                    .build());
        }

        private void confirmOverload() {
            String text = valueField.getValue().trim();
            // 空输入视为 1（跟随）
            int v = text.isEmpty() ? 1 : Integer.parseInt(text);
            // 客户端钳制只是 UX，服务端包处理里会重新钳制
            v = Math.max(0, v);
            if (v > 1) {
                v = Math.min(v, ClientPlayerOverloadData.getMaxMultiplier());
            }
            // 本地预测 + 服务端同步
            spell.getSequenceNode().setOverloadMultiplier(v);
            ModMessage.sendToServer(new SetSpellOverloadC2SPacket(spell.getId(), v));
            Minecraft.getInstance().setScreen(parent);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
            guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 30, 0xFFFFFF);
            guiGraphics.drawCenteredString(this.font,
                    Component.literal(spell.getName()), this.width / 2, 45, 0xAAAAAA);
            // 语义提示：1=跟随，0=固定1倍，N=固定N倍（当前上限）
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("gui.spellweaver.overload_input_hint"),
                    this.width / 2, this.height / 2 - 35, 0xAAAAAA);
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("gui.spellweaver.overload_current_cap",
                            ClientPlayerOverloadData.getMaxMultiplier()),
                    this.width / 2, this.height / 2 + 38, 0xAAAAAA);
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parent);
        }
    }
}
