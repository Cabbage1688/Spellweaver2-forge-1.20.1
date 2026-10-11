package net.zhenhuojun.spellweaver.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.PlayerSpellStorage;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerStorageData;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.*;
import org.lwjgl.glfw.GLFW;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class SpellStorageScreen extends Screen {
    //private static final ResourceLocation TEXTURE = new ResourceLocation(Magic.MODID
           // , "textures/gui/spell_storage.png");
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    private static final int SPELL_SLOTS = 9;

    private final Player player;
    //PlayerSpellStorage spellStorage;
    private int leftPos, topPos;//界面定位
    private EditBox searchBox;
    private Button castButton, deleteButton;
    private Button backButton;
    private Button editButton;
    //2025.5.24
    private Button exportButton;
    private Button importButton;

    private StoredSpell selectedSpell;


    private List<StoredSpell> displayedSpells = new ArrayList<>();
    private List<StoredSpell> preSpellList=new ArrayList<>();

    private int selectedIndex = -1;  // 用于跟踪选中项（基于 displayedSpells 的索引）

    // 滑块式列表
    private static final int VISIBLE_ENTRIES = 6; // 列表可见条目数
    private static final int ENTRY_HEIGHT = 20;   // 单条高度
    private int scrollOffset = 0;                 // 当前滚动偏移（条目）
    // 搜索防抖
    private String pendingSearch = null;          // 待应用的搜索文本
    private long searchSetTime = 0;               // 上次输入时间戳（毫秒）
    private static final long SEARCH_DEBOUNCE_MS = 300;

    private final SpellWeavingScreen parentScreen;

    public SpellStorageScreen(Player player,SpellWeavingScreen parentScreen) {
        super(Component.translatable("gui.spellweaver.spell_storage.title"));
        this.player = player;
        this.parentScreen=parentScreen;
    }

    @Override//初始化方法
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        // 列表右上角搜索框：为空显示全部，输入后防抖刷新过滤
        this.searchBox = new EditBox(this.font, leftPos + GUI_WIDTH - 8 - 70, topPos + 6, 70, 12,
                Component.translatable("gui.spellweaver.search"));
        //this.searchBox.setHint(Component.translatable("gui.spellweaver.search"));
        this.searchBox.setResponder(text -> {
            pendingSearch = text;
            searchSetTime = System.currentTimeMillis();
        });
        this.addRenderableWidget(searchBox);

        // 操作按钮,按下调用castSelectedSpell()
        this.castButton = this.addRenderableWidget(
                Button.builder(Component.translatable("gui.cast"), button -> castSelectedSpell())
                        .pos(leftPos + 173, topPos + GUI_HEIGHT - 28)
                        .size(50, 20)
                        .build()
        );
        //删除按钮
        this.deleteButton = this.addRenderableWidget(
                Button.builder(Component.translatable("gui.delete"), button -> deleteSelectedSpell())
                        .pos(leftPos + 118, topPos + GUI_HEIGHT - 28)
                        .size(50, 20)
                        .build()
        );
        // 返回按钮
        this.backButton=this.addRenderableWidget(
                Button.builder(Component.translatable("gui.spellweaver.return"), button -> {
                            Minecraft.getInstance().setScreen(parentScreen);
                        })
                        .pos(leftPos + 63, topPos + GUI_HEIGHT + 2+5)
                        .size(50, 20)
                        .build()
        );
        // 编辑入口
        this.editButton=this.addRenderableWidget(
                Button.builder(Component.translatable("gui.spellweaver.edit_hub"), button -> {
                            if (selectedSpell != null) {
                                Minecraft.getInstance().setScreen(new EditHubScreen(selectedSpell, this));
                            }
                })
                        .pos(leftPos + 63, topPos + GUI_HEIGHT - 28)
                        .size(50, 20)
                        .build()
        );


        // 导出按钮（与编辑按钮同一行）
        this.exportButton = this.addRenderableWidget(
                Button.builder(Component.translatable("gui.spellweaver.export"), button -> {
                            if (selectedSpell != null) {
                                openAuthorSignScreen();
                            }
                        })
                        .pos(leftPos + 8, topPos + GUI_HEIGHT - 28)
                        .size(50, 20)
                        .build()
        );

        // 导入按钮（与编辑按钮同一行）
        this.importButton = this.addRenderableWidget(
                Button.builder(Component.translatable("gui.spellweaver.import"), button -> {
                            openImportScreen();
                        })
                        .pos(leftPos - 47, topPos + GUI_HEIGHT - 28)
                        .size(50, 20)
                        .build()
        );

        preSpellList.clear();
        preSpellList.addAll(ClientPlayerStorageData.getPlayerSpellStorage().getAllSpells());
        refreshSpells();
    }
    // 现在始终从 ClientPlayerStorageData 获取最新数据源，避免数据混乱。
    // 搜索过滤：displayedSpells 只保留名称包含搜索文本的法术（空则全部）。
    // 列表为滑块式，displayedSpells 始终是过滤后的全量列表，渲染时按 scrollOffset 取可见窗口。
    public void refreshSpells() {
        PlayerSpellStorage storage = ClientPlayerStorageData.getPlayerSpellStorage();
        List<StoredSpell> allSpells = new ArrayList<>(storage.getAllSpells());

        // 搜索过滤（当前生效的搜索文本用 searchBox 的值，空则不过滤）
        String searchText = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase();
        displayedSpells.clear();
        for (StoredSpell spell : allSpells) {
            if (searchText.isEmpty() || spell.getName().toLowerCase().contains(searchText)) {
                displayedSpells.add(spell);
            }
        }

        this.preSpellList = allSpells;

        // 选中索引基于 displayedSpells，越界则清空
        if (selectedIndex >= displayedSpells.size()) {
            selectedIndex = -1;
            selectedSpell = null;
        } else if (selectedIndex >= 0 && !displayedSpells.isEmpty()) {
            selectedSpell = displayedSpells.get(selectedIndex);
        }
        clampScroll();
        updateButtonStates();
    }

    private void clampScroll() {
        if (scrollOffset < 0) scrollOffset = 0;
        int max = Math.max(0, displayedSpells.size() - VISIBLE_ENTRIES);
        if (scrollOffset > max) scrollOffset = max;
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 搜索防抖：有未应用的输入且已超过防抖间隔，则应用并刷新列表
        if (pendingSearch != null && System.currentTimeMillis() - searchSetTime >= SEARCH_DEBOUNCE_MS) {
            pendingSearch = null;
            selectedIndex = -1;
            selectedSpell = null;
            refreshSpells();
        }

        this.renderBackground(guiGraphics);

        // 绘制标题和标签
        guiGraphics.drawCenteredString(this.font, this.title,
                this.width / 2-4, topPos + 5, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // 绘制法术列表
        int startY = topPos + 20;
        int listLeft = leftPos + 8;
        int listRight = leftPos + GUI_WIDTH - 8;

        for (int i = 0; i < VISIBLE_ENTRIES; i++) {
            int idx = scrollOffset + i;
            if (idx >= displayedSpells.size()) break;
            StoredSpell spell = displayedSpells.get(idx);
            int yPos = startY + i * ENTRY_HEIGHT;

            // 选中条目半透明背景
            if (idx == selectedIndex) {
                guiGraphics.fill(listLeft, yPos, listRight, yPos + ENTRY_HEIGHT, 0x30FFFFFF);
            }

            // 法术名称：从列表最左边开始
            guiGraphics.drawString(font, Component.literal(spell.getName()),
                    listLeft + 6, yPos + 4, 0xFFFFFF, false);

            // 绑定键位
            Optional<Integer> boundSlot = ClientPlayerStorageData.getPlayerSpellStorage().findBoundSlot(spell.getId());
            if (boundSlot.isPresent()) {
                String keyText = "[" + (boundSlot.get() + 1) + "]";
                guiGraphics.drawString(font, keyText,
                        listRight - 14, yPos + 6, 0x00FF00, false);
            }

            // 独立超载设置：非1（非跟随）时在绑定数字左侧固定位置红字显示
            int overload = spell.getSequenceNode().getOverloadMultiplier();
            if (overload != 1) {
                guiGraphics.drawString(font, "{"+String.valueOf(overload)+"}",
                        listRight - 14 - font.width("[0]") - 6, yPos + 6, 0xFFFF5555, false);
            }
        }

        // 滚动条
        int total = displayedSpells.size();
        if (total > VISIBLE_ENTRIES) {
            int trackHeight = VISIBLE_ENTRIES * ENTRY_HEIGHT;
            int thumbHeight = Math.max(15, trackHeight * VISIBLE_ENTRIES / total);
            int thumbY = startY + (trackHeight - thumbHeight) * scrollOffset / Math.max(1, total - VISIBLE_ENTRIES);
            int barX = listLeft;
            guiGraphics.fill(barX, startY, barX + 3, startY + trackHeight, 0x40FFFFFF);
            guiGraphics.fill(barX, thumbY, barX + 3, thumbY + thumbHeight, 0xFFAAAAAA);
        }
        MutableComponent text= Component.translatable("message.spellweaver.spell_inventory", preSpellList.size(), 27);
        int color=0xFFFFFF;
        MutableComponent text2=Component.translatable("message.spellweaver.binding_tip");
        guiGraphics.drawString(font,text,leftPos + 63, topPos + GUI_HEIGHT - 28+25,color);
        guiGraphics.drawString(font,text2,leftPos + 63-40, topPos + GUI_HEIGHT - 28+25-100-80,color);


        // 右侧详情区域
        if (selectedIndex >= 0 && selectedIndex < displayedSpells.size()) {
            StoredSpell spell = displayedSpells.get(selectedIndex);
            int detailX = leftPos + GUI_WIDTH + 10-15;  // 右侧起始X
            int detailY = topPos + 20;

            // 绘制详情背景
            guiGraphics.fill(detailX - 2, detailY - 2, detailX + 150, detailY + 80, 0x20FFFFFF);

            //作者信息区域
            List<String> authors = spell.getAuthors();
            int authorAreaWidth = 120; // 可用宽度
            int authorX = detailX;
            int authorY = detailY;
            boolean showTooltip = false;

            if(!authors.isEmpty()){

                String displayText;
                if (authors.size() <= 3) {
                    displayText = "作者: " + String.join(", ", "§6"+authors);
                } else {
                    // 取前三个作者，其余缩略
                    String firstThree = String.join(", ", authors.subList(0, 3));
                    displayText = "作者: " + firstThree + "... 等" + authors.size() + "人";
                    showTooltip = true; // 超过3人时启用tooltip
                }

                // 先尝试完整绘制一行，如果超宽则用 font.plainSubstrByWidth 截断
                if (font.width(displayText) > authorAreaWidth) {
                    displayText = font.plainSubstrByWidth(displayText, authorAreaWidth - font.width("...")) + "...";
                    showTooltip = true;
                }

                guiGraphics.drawString(font, Component.literal(displayText), authorX, authorY, 0xAAAAAA, false);


                if (showTooltip) {
                    int textWidth = font.width(displayText);
                    // 检查鼠标是否在文本区域内
                    if (mouseX >= authorX && mouseX <= authorX + textWidth &&
                            mouseY >= authorY && mouseY <= authorY + font.lineHeight) {
                        // 构建完整作者列表的Tooltip
                        List<Component> tooltipLines = new ArrayList<>();
                        tooltipLines.add(Component.translatable("gui.spellweaver.all_authors"));
                        for (String author : authors) {
                            tooltipLines.add(Component.literal("§6"+author));
                        }
                        guiGraphics.renderTooltip(font, tooltipLines, java.util.Optional.empty(), mouseX, mouseY);
                    }
                }
                // 作者行占用一行高度
                detailY += 10;
            }
            // 备注区域
            String note = spell.getNote();
            if (note.isEmpty()) {
                guiGraphics.drawString(font, Component.translatable("gui.spellweaver.no_note"), detailX, detailY, 0x666666, false);
            } else {
                String noteText = "备注: " + note;
                int maxWidth = 120; // 右侧面板可用宽度
                List<FormattedCharSequence> lines = font.split(Component.literal(noteText), maxWidth);
                int yOffset = 0;
                for (FormattedCharSequence line : lines) {
                    guiGraphics.drawString(font, line, detailX, detailY + yOffset, 0xCCCCCC, false);
                    yOffset += 9; // 行高
                }
            }
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 检查是否点击了法术列表区域（可见窗口范围）
        int startY = topPos + 20;
        int listBottom = startY + VISIBLE_ENTRIES * ENTRY_HEIGHT;

        if (mouseX >= leftPos + 8 && mouseX <= leftPos + GUI_WIDTH - 8 &&
                mouseY >= startY && mouseY <= listBottom) {

            // 可见行号 + 滚动偏移 = 实际条目索引
            int visibleRow = (int) ((mouseY - startY) / ENTRY_HEIGHT);
            int index = scrollOffset + visibleRow;

            if (index >= 0 && index < displayedSpells.size()) {
                selectedIndex = index;
                selectedSpell = displayedSpells.get(selectedIndex);
                // 右键绑定法术
                if (button == 1 ) {
                    StoredSpell spell = displayedSpells.get(selectedIndex);
                    openSlotSelectionMenu(mouseX, mouseY, spell);
                    return true;
                }
                updateButtonStates();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // 只在列表区域滚动才翻页，避免误触
        int startY = topPos + 20;
        int listBottom = startY + VISIBLE_ENTRIES * ENTRY_HEIGHT;
        if (mouseX >= leftPos + 8 && mouseX <= leftPos + GUI_WIDTH - 8 &&
                mouseY >= startY && mouseY <= listBottom) {
            if (delta > 0) scrollOffset--;
            else scrollOffset++;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }


    private void openSlotSelectionMenu(double x, double y, StoredSpell spell) {
        Minecraft.getInstance().setScreen(new SlotSelectionScreen(
                this,
                slot -> {
                    ModMessage.sendToServer(new BindSpellC2SPacket(slot, spell.getId()));
                    player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
                    player.displayClientMessage(
                            Component.translatable("gui.spell_bound", spell.getName(), slot + 1).withStyle(ChatFormatting.LIGHT_PURPLE),
                            true
                    );
                },
                spell.getId(),
                spell.getName()
        ));
    }




    private void updateButtonStates() {
        boolean hasSelection = selectedSpell != null;
        castButton.active = hasSelection;
        deleteButton.active = hasSelection;
        backButton.active=true;
        exportButton.active = hasSelection;
        importButton.active = ClientPlayerStorageData.getPlayerSpellStorage().getSpells().size() < 27;
        editButton.active = hasSelection;
    }


    //释放法术
    private void castSelectedSpell() {
        if (selectedIndex >= 0 && selectedIndex < displayedSpells.size()) {
            StoredSpell spell = displayedSpells.get(selectedIndex);//根据索引获取法术
            // 发送施法请求到服务器
            ModMessage.sendToServer(new SpellCastingC2SPacket(spell.getSequenceNode().serializeNBT()));

            this.onClose();//关闭GUI
        }
    }

    //删除法术
    private void deleteSelectedSpell() {
        if (selectedIndex >= 0 && selectedIndex < displayedSpells.size()) {
            StoredSpell spell = displayedSpells.get(selectedIndex);

            // 预测性更新：本地两个列表都按 id 移除（displayedSpells 可能因搜索过滤与 preSpellList 索引不一致）
            displayedSpells.removeIf(s -> s.getId().equals(spell.getId()));
            preSpellList.removeIf(s -> s.getId().equals(spell.getId()));
            selectedIndex = -1;
            selectedSpell = null;
            ClientPlayerStorageData.getPlayerSpellStorage().removeSpell(spell.getId());
            clampScroll();
            updateButtonStates();

            ModMessage.sendToServer(new DeleteSpellC2SPacket(spell.getId()));
        }
    }

    @Override//按ESC键关闭GUI
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

// 法术重命名界面

    class RenameSpellScreen extends Screen {
        private final Screen parent;  // 返回目标：可能是法术库或编辑中心界面
        private final StoredSpell spell;
        private EditBox nameField;//文本输入框
        private Button confirmButton;//确认按钮

        public RenameSpellScreen(Screen parent, StoredSpell spell) {
            super(Component.translatable("gui.rename_spell.title"));
            this.parent = parent;
            this.spell = spell;
        }

        @Override//初始化
        protected void init() {
            super.init();
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            this.nameField = new EditBox(this.font, centerX - 100, centerY - 20, 200, 20,
                    Component.translatable("gui.name"));
            this.nameField.setValue(spell.getName());
            this.nameField.setResponder(text -> updateButtonState());
            this.addRenderableWidget(nameField);

            this.confirmButton = this.addRenderableWidget(
                    Button.builder(Component.translatable("gui.confirm"), button -> confirmRename())
                            .pos(centerX - 50, centerY + 10)
                            .size(100, 20)
                            .build()
            );

            updateButtonState();
        }

        private void updateButtonState() {
            confirmButton.active = !nameField.getValue().trim().isEmpty();
        }

        private void confirmRename() {
            String newName = nameField.getValue().trim();
            if (!newName.isEmpty()) {
                // 直接用构造传入的 spell，而不是按 parent.selectedIndex 取（从编辑中心跳转时索引可能不对应）
                String originalName = spell.getName();
                spell.rename(newName);
                SpellStorageScreen.this.refreshSpells();
                ModMessage.sendToServer(new RenameSpellC2SPacket(spell.getId(), newName, originalName));
            }
            minecraft.setScreen(parent);
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parent);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
            guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 30, 0xFFFFFF);
        }
    }


    class SlotSelectionScreen extends Screen {
        private final Screen parentScreen;
        private final Consumer<Integer> slotConsumer;
        private final UUID spellId;
        private final String spellName;

        public SlotSelectionScreen(Screen parentScreen, Consumer<Integer> slotConsumer, UUID spellId, String spellName) {
            super(Component.translatable("gui.select_slot.title"));
            this.parentScreen = parentScreen;
            this.slotConsumer = slotConsumer;
            this.spellId = spellId;
            this.spellName = spellName;
        }

        @Override
        protected void init() {
            super.init();

            int centerX = this.width / 2;
            int centerY = this.height / 2;

            // 创建 3x3 网格的槽位按钮
            for (int slot = 0; slot < 9; slot++) {
                int row = slot / 3;
                int col = slot % 3;

                int finalSlot = slot;
                this.addRenderableWidget(Button.builder(
                                Component.literal(String.valueOf(slot + 1)),
                                btn -> {
                                    slotConsumer.accept(finalSlot);
                                    Minecraft.getInstance().setScreen(parentScreen);
                                })
                        .pos(centerX - 45 + col * 30, centerY - 30 + row * 25)
                        .size(25, 20)
                        .build());
            }

            // 查询当前法术绑定的槽位
            Optional<Integer> boundSlot = ClientPlayerStorageData.getPlayerSpellStorage().findBoundSlot(spellId);

            // 添加“解绑”按钮（仅当法术已绑定某个槽位时显示）
            if (boundSlot.isPresent()) {
                int currentSlot = boundSlot.get();
                this.addRenderableWidget(Button.builder(
                                Component.translatable("gui.unbind", currentSlot + 1),  // 显示“解绑槽位X”
                                btn -> {
                                    ModMessage.sendToServer(new UnbindSpellC2SPacket(currentSlot));
                                    Minecraft.getInstance().setScreen(parentScreen);
                                })
                        .pos(centerX - 70, centerY + 40)
                        .size(80, 20)
                        .build());
            }


            this.addRenderableWidget(Button.builder(
                            Component.translatable("gui.cancel"),
                            btn -> Minecraft.getInstance().setScreen(parentScreen))
                    .pos(centerX + (boundSlot.isPresent() ? 10 : -40), centerY + 40)  // 动态调整位置
                    .size(80, 20)
                    .build());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);

            guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.select_slot.prompt"), this.width / 2, 40, 0xA0A0A0);
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parentScreen);
        }
    }

    class SpellNamingScreen extends Screen {
        private final SpellStorageScreen parent;
        private final String defaultName;
        private final Consumer<String> onConfirm;
        private EditBox nameField;

        public SpellNamingScreen(SpellStorageScreen parent,
                                 String defaultName,
                                 Consumer<String> onConfirm) {
            super(Component.translatable("gui.spellweaver.name_spell"));
            this.parent = parent;
            this.defaultName = defaultName;
            this.onConfirm = onConfirm;
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            // 名称输入框
            nameField = new EditBox(font, centerX - 100, centerY - 20, 200, 20,
                    Component.translatable("gui.spellweaver.spell_name"));
            nameField.setValue(defaultName);
            nameField.setResponder(text -> updateButtonState());
            addRenderableWidget(nameField);

            // 确认按钮
            addRenderableWidget(Button.builder(Component.translatable("gui.confirm"), button -> {
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
                    Component.translatable("gui.spellweaver.name_your_spell"),
                    width / 2,
                    height / 4,
                    0xFFFFFF
            );
        }
    }

    //2026.5.24，署名界面
    class AuthorSignScreen extends Screen {
        private final SpellStorageScreen parent;
        private final StoredSpell spell;
        private EditBox nameField;

        public AuthorSignScreen(SpellStorageScreen parent, StoredSpell spell) {
            super(Component.translatable("gui.spellweaver.author_sign"));
            this.parent = parent;
            this.spell = spell;
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            String defaultName = "";
            if (Minecraft.getInstance().player != null) {
                defaultName = Minecraft.getInstance().player.getName().getString();
            }

            this.nameField = new EditBox(font, centerX - 100, centerY - 20, 200, 20,
                    Component.translatable("gui.author"));
            this.nameField.setValue(defaultName);
            this.addRenderableWidget(nameField);

            this.addRenderableWidget(Button.builder(Component.translatable("gui.confirm"), button -> {
                String author = nameField.getValue().trim();
                if (author.isEmpty()) {
                    author = Component.translatable("message.spellweaver.anonymous").getString();
                }
                exportSpell(spell, author);
                this.onClose();
            }).pos(centerX - 50, centerY + 20).size(100, 20).build());

            this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> this.onClose())
                    .pos(centerX - 50, centerY + 50).size(100, 20).build());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
            guiGraphics.drawCenteredString(font, this.title, this.width / 2, 40, 0xFFFFFF);
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parent);
        }
    }

    private void openAuthorSignScreen() {
        if (selectedSpell != null) {
            Minecraft.getInstance().setScreen(new AuthorSignScreen(this, selectedSpell));
        }
    }

    //2026.5.24导入界面
    class ImportSpellScreen extends Screen {
        private final SpellStorageScreen parent;
        private EditBox pasteBox;

        public ImportSpellScreen(SpellStorageScreen parent) {
            super(Component.translatable("gui.spellweaver.import"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            this.pasteBox = new EditBox(font, centerX - 150, centerY - 40, 300, 40,
                    Component.translatable("gui.paste_here"));
            this.pasteBox.setMaxLength(4096);
            this.addRenderableWidget(pasteBox);

            this.addRenderableWidget(Button.builder(Component.translatable("gui.confirm"), button -> {
                String input = pasteBox.getValue().trim();
                if (importSpell(input)) {
                    this.onClose();
                }
            }).pos(centerX - 50, centerY + 20).size(100, 20).build());

            this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), btn -> this.onClose())
                    .pos(centerX - 50, centerY + 50).size(100, 20).build());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
            guiGraphics.drawCenteredString(font, this.title, this.width / 2, 20, 0xFFFFFF);
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parent);
        }
    }

    private void openImportScreen() {
        Minecraft.getInstance().setScreen(new ImportSpellScreen(this));
    }

    //2026.5.24覆盖确认界面
    class ConfirmOverwriteScreen extends Screen {
        private final SpellStorageScreen parent;
        private final ImportSpellScreen importScreen;
        private final StoredSpell newSpell;
        private final StoredSpell oldSpell;

        public ConfirmOverwriteScreen(SpellStorageScreen parent, ImportSpellScreen importScreen,
                                      StoredSpell newSpell, StoredSpell oldSpell) {
            super(Component.translatable("message.spellweaver.overwrite_spell"));
            this.parent = parent;
            this.importScreen = importScreen;
            this.newSpell = newSpell;
            this.oldSpell = oldSpell;
        }

        @Override
        protected void init() {
            int cx = this.width / 2;
            int cy = this.height / 2;

            this.addRenderableWidget(Button.builder(Component.translatable("gui.overwrite"), btn -> {
                // 发送覆盖请求
                ModMessage.sendToServer(new ImportSpellC2SPacket(
                        newSpell.getName(),
                        newSpell.getSequenceNode().serializeNBT(),
                        newSpell.getAuthors(),
                        newSpell.getNote(),
                        oldSpell.getId()
                ));
                // 乐观更新
                PlayerSpellStorage storage = ClientPlayerStorageData.getPlayerSpellStorage();
                storage.getSpells().put(oldSpell.getId(), newSpell);
                player.displayClientMessage(Component.translatable("message.spellweaver.overwritten", newSpell.getName()), false);
                this.onClose();
                importScreen.onClose();
                parent.refreshSpells();
            }).pos(cx - 50, cy - 10).size(100, 20).build());

            this.addRenderableWidget(Button.builder(Component.translatable("gui.new"), btban -> {
                // 作为新法术
                if (ClientPlayerStorageData.getPlayerSpellStorage().getSpells().size() >= 27) {
                    player.displayClientMessage(Component.translatable("message.spellweaver.storage_full"), false);
                    return;
                }
                UUID newId = UUID.randomUUID();
                StoredSpell copySpell = new StoredSpell(newId, newSpell.getName(), newSpell.getSequenceNode(), newSpell.getAuthors(),"");
                ModMessage.sendToServer(new ImportSpellC2SPacket(
                        copySpell.getName(),
                        copySpell.getSequenceNode().serializeNBT(),
                        copySpell.getAuthors(),
                        copySpell.getNote(),
                        null
                ));
                ClientPlayerStorageData.getPlayerSpellStorage().getSpells().put(newId, copySpell);
                player.displayClientMessage(Component.translatable("message.spellweaver.imported_new", copySpell.getName()), false);
                this.onClose();
                importScreen.onClose();
                parent.refreshSpells();
            }).pos(cx - 50, cy + 20).size(100, 20).build());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
            guiGraphics.drawCenteredString(font, Component.translatable("message.spellweaver.spell_exists"), this.width / 2, 40, 0xFFFFFF);
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(importScreen); // 返回导入界面
        }
    }


    private void exportSpell(StoredSpell spell, String author) {
        //本地添加作者
        spell.addAuthor(author);
        // 更新作者列表
        ModMessage.sendToServer(new UpdateSpellAuthorsC2SPacket(spell.getId(), new ArrayList<>(spell.getAuthors())));
        // 生成导出字符串
        String exportString = generateExportString(spell);
        //  复制到剪贴板
        Minecraft.getInstance().keyboardHandler.setClipboard(exportString);
        //书页包，如果手上有纸则同步生成书页，2026.8.23添加
        ModMessage.sendToServer(new MagicPageC2SPacket(spell.getNote(),spell.getAuthors(),exportString,spell.getName()));
        // 通知玩家
        player.displayClientMessage(
                Component.translatable("gui.spellweaver.export.success", spell.getName(), author), false);
        refreshSpells();
    }
    //导出方法
    private String generateExportString(StoredSpell spell) {
        CompoundTag exportTag = spell.serialize();
        //版本信息不要
       // exportTag.putInt("exportVersion", 1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            NbtIo.writeCompressed(exportTag, baos);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        byte[] bytes = baos.toByteArray();
        return "SPELLWEAVER_SPELL:" + Base64.getEncoder().encodeToString(bytes);
    }
    //导入方法
    private boolean importSpell(String input) {
        if (!input.startsWith("SPELLWEAVER_SPELL:")) {
            player.displayClientMessage(Component.translatable("message.spellweaver.invalid_spell_data"), false);
            return false;
        }
        String base64 = input.substring("SPELLWEAVER_SPELL:".length());
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            player.displayClientMessage(Component.translatable("message.spellweaver.decode_failed"), false);
            return false;
        }

        CompoundTag importTag;
        try {
            importTag = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            player.displayClientMessage(Component.translatable("message.spellweaver.cannot_read_spell"), false);
            return false;
        }

        StoredSpell importedSpell;
        try {
            importedSpell = StoredSpell.deserialize(importTag);
        } catch (Exception e) {
            player.displayClientMessage(Component.translatable("message.spellweaver.spell_corrupted"), false);
            return false;
        }

        PlayerSpellStorage storage = ClientPlayerStorageData.getPlayerSpellStorage();
        Optional<StoredSpell> existing = storage.getSpell(importedSpell.getId());

        if (existing.isPresent()) {
            // 弹窗询问覆盖
            Minecraft.getInstance().setScreen(new ConfirmOverwriteScreen(this, (ImportSpellScreen) Minecraft.getInstance().screen, importedSpell, existing.get()));
            return false;
        } else {
            // 直接新增
            if (storage.getSpells().size() >=  PlayerSpellStorage.MAX_STORED_SPELLS) {
                player.displayClientMessage(Component.translatable("message.spellweaver.cannot_import"), false);
                return false;
            }
            ModMessage.sendToServer(new ImportSpellC2SPacket(
                    importedSpell.getName(),
                    importedSpell.getSequenceNode().serializeNBT(),
                    importedSpell.getAuthors(),
                    importedSpell.getNote(),
                    null
            ));
            // 本地更新
            storage.getSpells().put(importedSpell.getId(), importedSpell);
            player.displayClientMessage(Component.translatable("message.spellweaver.import_success", importedSpell.getName()), false);
            refreshSpells();
            return true;
        }
    }

    class NoteEditScreen extends Screen {
        private final Screen parent;  // 返回目标：可能是法术库或编辑中心界面
        private final StoredSpell spell;
        private EditBox noteField;

        public NoteEditScreen(Screen parent, StoredSpell spell) {
            super(Component.translatable("gui.spellweaver.edit_note"));
            this.parent = parent;
            this.spell = spell;
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            this.noteField = new EditBox(font, centerX - 100, centerY - 20, 200, 40,
                    Component.translatable("gui.note"));
            this.noteField.setMaxLength(256);
            this.noteField.setValue(spell.getNote());
            this.addRenderableWidget(noteField);

            this.addRenderableWidget(Button.builder(Component.translatable("gui.confirm"), btn -> {
                String newNote = noteField.getValue().trim();
                spell.setNote(newNote);
                PlayerSpellStorage storage = ClientPlayerStorageData.getPlayerSpellStorage();
                storage.getSpell(spell.getId()).ifPresent(s -> s.setNote(newNote));
                ModMessage.sendToServer(new UpdateSpellNoteC2SPacket(spell.getId(), newNote));
                Spellweaver.getLOGGER().debug("[Spellweaver/SpellStorageScreen/NoteEditScreen]备注信息已同步服务端");
                SpellStorageScreen.this.refreshSpells();
                this.onClose();
            }).pos(centerX - 50, centerY + 30).size(100, 20).build());
            this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), btn -> this.onClose())
                    .pos(centerX - 50, centerY + 55).size(100, 20).build());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
            guiGraphics.drawCenteredString(font, this.title, this.width / 2, 40, 0xFFFFFF);
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parent);
        }
    }

    private void openNoteEditScreen() {
        if (selectedSpell != null) {
            Minecraft.getInstance().setScreen(new NoteEditScreen(this, selectedSpell));
        }
    }
}
