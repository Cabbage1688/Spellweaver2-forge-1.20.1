package net.zhenhuojun.spellweaver.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.zhenhuojun.spellweaver.entity.impl.SpellEntity;
import net.zhenhuojun.spellweaver.spell.node.SpellEntityNode;

/**
 * 法术实体节点的中键配置界面：
 * - 触发模式：延时（输入tick数）/ 命中（命中实体或方块时）
 * - 触发后运动：原地停止 / 继续飞行
 */
public class SpellEntityScreen extends Screen {
    private final SpellWeavingScreen parent;
    private final SpellEntityNode editNode;

    private SpellEntity.TriggerMode triggerMode;
    private boolean keepFlying;

    private EditBox delayInput;
    private Button delayModeButton;
    private Button hitModeButton;
    private Button stopButton;
    private Button flyButton;

    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;

    public SpellEntityScreen(SpellWeavingScreen parent, SpellEntityNode editNode) {
        super(Component.translatable("gui.spellweaver.spell_entity_settings"));
        this.parent = parent;
        this.editNode = editNode;
        this.triggerMode = editNode.getTriggerMode();
        this.keepFlying = editNode.isKeepFlying();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        //触发模式：延时
        delayModeButton = Button.builder(buildModeLabel(SpellEntity.TriggerMode.DELAY), btn -> {
            triggerMode = SpellEntity.TriggerMode.DELAY;
            refreshWidgets();
        }).bounds(centerX - BUTTON_WIDTH - 10, centerY - 65, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addRenderableWidget(delayModeButton);

        //触发模式：命中
        hitModeButton = Button.builder(buildModeLabel(SpellEntity.TriggerMode.HIT), btn -> {
            triggerMode = SpellEntity.TriggerMode.HIT;
            refreshWidgets();
        }).bounds(centerX + 10, centerY - 65, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addRenderableWidget(hitModeButton);

        //延时tick输入
        delayInput = new EditBox(font, centerX - 100, centerY - 20, 200, 20,
                Component.translatable("gui.spellweaver.spell_entity_delay_ticks"));
        delayInput.setValue(String.valueOf(editNode.getDelayTicks()));
        delayInput.setFilter(text -> text.isEmpty() || text.chars().allMatch(Character::isDigit));
        addRenderableWidget(delayInput);

        //触发后运动：原地停止
        stopButton = Button.builder(buildMoveLabel(false), btn -> {
            keepFlying = false;
            refreshWidgets();
        }).bounds(centerX - BUTTON_WIDTH - 10, centerY + 30, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addRenderableWidget(stopButton);

        //触发后运动：继续飞行
        flyButton = Button.builder(buildMoveLabel(true), btn -> {
            keepFlying = true;
            refreshWidgets();
        }).bounds(centerX + 10, centerY + 30, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addRenderableWidget(flyButton);

        //确认
        addRenderableWidget(Button.builder(Component.translatable("gui.confirm"), btn -> {
            editNode.setTriggerMode(triggerMode);
            editNode.setKeepFlying(keepFlying);
            editNode.setDelayTicks(parseDelayTicks());
            saveAndReturn();
        }).bounds(centerX - 50, centerY + 65, 100, 20).build());

        refreshWidgets();
    }

    private int parseDelayTicks() {
        String text = delayInput.getValue().trim();
        if (text.isEmpty()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(text));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Component buildModeLabel(SpellEntity.TriggerMode mode) {
        Component base = (mode == SpellEntity.TriggerMode.DELAY)
                ? Component.translatable("gui.spellweaver.spell_entity_mode_delay")
                : Component.translatable("gui.spellweaver.spell_entity_mode_hit");
        return triggerMode == mode ? base.copy().append(" ✔") : base;
    }

    private Component buildMoveLabel(boolean fly) {
        Component base = fly
                ? Component.translatable("gui.spellweaver.spell_entity_move_fly")
                : Component.translatable("gui.spellweaver.spell_entity_move_stop");
        return keepFlying == fly ? base.copy().append(" ✔") : base;
    }

    private void refreshWidgets() {
        if (delayModeButton != null) delayModeButton.setMessage(buildModeLabel(SpellEntity.TriggerMode.DELAY));
        if (hitModeButton != null) hitModeButton.setMessage(buildModeLabel(SpellEntity.TriggerMode.HIT));
        if (stopButton != null) stopButton.setMessage(buildMoveLabel(false));
        if (flyButton != null) flyButton.setMessage(buildMoveLabel(true));
        if (delayInput != null) {
            //延时输入框仅在延时模式下可用可见
            delayInput.visible = (triggerMode == SpellEntity.TriggerMode.DELAY);
            delayInput.setEditable(triggerMode == SpellEntity.TriggerMode.DELAY);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.drawCenteredString(font,
                Component.translatable("gui.spellweaver.spell_entity_settings"),
                width / 2, height / 7, 0xFFFFFF);

        int centerX = width / 2;
        int centerY = height / 2;
        guiGraphics.drawCenteredString(font,
                Component.translatable("gui.spellweaver.spell_entity_trigger_mode"),
                centerX, centerY - 80, 0xAAAAAA);

        if (triggerMode == SpellEntity.TriggerMode.DELAY) {
            guiGraphics.drawCenteredString(font,
                    Component.translatable("gui.spellweaver.spell_entity_delay_ticks"),
                    centerX, centerY - 32, 0xAAAAAA);
        }

        guiGraphics.drawCenteredString(font,
                Component.translatable("gui.spellweaver.spell_entity_after_trigger"),
                centerX, centerY + 15, 0xAAAAAA);
    }

    private void saveAndReturn() {
        if (parent != null) {
            Minecraft.getInstance().setScreen(parent);
        } else {
            this.onClose();
        }
    }
}
