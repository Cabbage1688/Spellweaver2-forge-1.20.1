package net.zhenhuojun.spellweaver.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.PlayerSpellStorage;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerSpellStorageProvider;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerStorageData;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.*;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    //private SpellList spellList;
    private EditBox searchBox;
    private Button castButton, renameButton, deleteButton;
    private Button Up,Down;
    private Button backButton;
    private Button editButton;

    private Button WriteScroll;
    private StoredSpell selectedSpell;


    private List<StoredSpell> displayedSpells = new ArrayList<>();
    private List<StoredSpell> preSpellList=new ArrayList<>();

    private int selectedIndex = -1;  // 用于跟踪选中项

    private int page;

    private final SpellWeavingScreen parentScreen;

    public SpellStorageScreen(Player player,SpellWeavingScreen parentScreen) {
        super(Component.translatable("gui.spellweaver.spell_storage.title"));
        this.player = player;
        //2026.2.7为适应新版本打的补丁,客户端调用这玩意不会炸吧卧槽
         //player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(playerSpellStorage->{
            // this.spellStorage=playerSpellStorage;
        // });

        //2025.11.9
        this.page=1;
        this.parentScreen=parentScreen;
    }

    @Override//初始化方法
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;


        // 法术列表组件
        // this.spellList = new SpellList(minecraft, GUI_WIDTH - 24, GUI_HEIGHT - 60,
        //        topPos + 20, topPos + GUI_HEIGHT - 40);
        // this.addWidget(spellList);

        //创建搜索框（`EditBox`），并设置响应器（输入时刷新法术列表）。
        /*this.searchBox = new EditBox(this.font, leftPos + 82, topPos + 6, 86, 12,
                Component.translatable("gui.search"));
        this.searchBox.setResponder(text -> refreshSpells());
        this.addRenderableWidget(searchBox);

         */

        // 操作按钮,按下调用castSelectedSpell()
        this.castButton = this.addRenderableWidget(
                Button.builder(Component.translatable("gui.cast"), button -> castSelectedSpell())
                        //.pos(leftPos + 8, topPos + GUI_HEIGHT - 28)
                        .pos(leftPos + 173, topPos + GUI_HEIGHT - 28)
                        .size(50, 20)
                        .build()
        );
        //重命名按钮
        this.renameButton = this.addRenderableWidget(
                Button.builder(Component.translatable("gui.rename"), button -> renameSelectedSpell())
                        //.pos(leftPos + 63, topPos + GUI_HEIGHT - 28)
                        .pos(leftPos +145, topPos + GUI_HEIGHT +2)
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
        //这个和释放交换位置
        this.Up= this.addRenderableWidget(
                Button.builder(Component.literal("➡"), button -> {
                            upPage();
                            refreshSpells();
                        })
                        //.pos(leftPos + 173, topPos + GUI_HEIGHT - 28)
                        .pos(leftPos + 8, topPos + GUI_HEIGHT - 28)
                        .size(50, 20)
                        .build()
        );

        this.Down= this.addRenderableWidget(
                Button.builder(Component.literal("⬅"), button -> {
                            downPage();
                            refreshSpells();
                        })
                        .pos(leftPos -47, topPos + GUI_HEIGHT - 28)
                        .size(50, 20)
                        .build()
        );
        this.backButton=this.addRenderableWidget(
                Button.builder(Component.literal("返回"), button -> {
                            Minecraft.getInstance().setScreen(parentScreen);
                        })
                       // .pos(leftPos +63, topPos + GUI_HEIGHT +12)
                        .pos(leftPos -19, topPos + GUI_HEIGHT +2)
                        .size(50, 20)
                        .build()
        );
        this.editButton=this.addRenderableWidget(
                Button.builder(Component.literal("编辑"), button -> {
                            if (selectedSpell != null) {
                                Minecraft.getInstance().setScreen(new SpellEditScreen(selectedSpell, this));
                            }
                })
                        .pos(leftPos + 63, topPos + GUI_HEIGHT - 28)
                        .size(50, 20)
                        .build()
        );
        preSpellList.clear();
        preSpellList.addAll(ClientPlayerStorageData.getPlayerSpellStorage().getAllSpells());
        refreshSpells();
    }
    //根据搜索框内容过滤并显示法术列表。
    public void refreshSpells() {
        displayedSpells.clear(); //清空当前显示列表`displayedSpells`。
        // String searchText = searchBox.getValue().toLowerCase();// 获取搜索文本（转换为小写）



        for (StoredSpell spell : ClientPlayerStorageData.getPlayerSpellStorage().getAllSpells()) {//遍历玩家存储的所有法术，将名称匹配（或搜索框为空）的法术加入显示列表
            //if (searchText.isEmpty() || spell.getName().toLowerCase().contains(searchText)) {
            Spellweaver.getLOGGER().debug("[Spellweaver:SpellStorageScreen/refreshSpells()]展示前检查" +
                    "ClientPlayerStorageData.getPlayerSpellStorage(){}",ClientPlayerStorageData.getPlayerSpellStorage().serialize());
            displayedSpells.add(spell);
            // }
        }
        //Magic.getLOGGER().debug("[MAGIC_DEBUG]预处理法术列表中的法术数量为{}", preSpellList.size());
        if (preSpellList.size() > 6) {
            //Magic.getLOGGER().debug("[MAGIC_DEBUG]法术数量大于6，需要裁剪展示列表");

            int pageSize = 6; // 每页显示6个
            int startIndex = (this.page - 1) * pageSize;
            int endIndex = Math.min(preSpellList.size(), startIndex + pageSize);

           // Magic.getLOGGER().debug("[MAGIC_DEBUG]展示法术存储中的{}到{}", startIndex, endIndex);
            displayedSpells = new ArrayList<>(preSpellList.subList(startIndex, endIndex));
            //Magic.getLOGGER().debug("[MAGIC_DEBUG]展示法术列表中法术数量为{}", displayedSpells.size());
        } else {
            // 如果法术数量不超过6个，直接显示全部
            displayedSpells = new ArrayList<>(preSpellList);
            //Magic.getLOGGER().debug("[MAGIC_DEBUG]法术数量不超过6，直接显示全部，数量为{}", displayedSpells.size());
        }

        // 重置选择状态,调整选中索引（如果超出范围则重置为-1）
        if (selectedIndex >= displayedSpells.size()) {
            selectedIndex = -1;
        }
        updateButtonStates();//更新按钮状态（根据是否有选中项启用/禁用按钮）
    }
    private void upPage(){
        if((this.page+1)*6-7<preSpellList.size())
            this.page++;
    }
    private void downPage(){
        if(this.page>1){
            page--;
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        // 绘制背景
        //guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        // 绘制标题和标签
        guiGraphics.drawCenteredString(this.font, this.title,
                this.width / 2, topPos + 5, 0xFFFFFF);
        //guiGraphics.drawString(this.font, Component.translatable("gui.search"),
        // leftPos + 8, topPos + 8, 0x404040, false);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // 绘制法术列表
        int startY = topPos + 20;
        int entryHeight = 20;

        for (int i = 0; i < displayedSpells.size(); i++) {//遍历`displayedSpells`，计算每个条目的位置。
            StoredSpell spell = displayedSpells.get(i);
            int yPos = startY + i * entryHeight;

            // 为选中的条目绘制半透明背景（通过`selectedIndex`判断）
            if (i == selectedIndex) {
                guiGraphics.fill(leftPos + 8, yPos, leftPos + GUI_WIDTH - 8, yPos + entryHeight, 0x30FFFFFF);
            }

            // 绘制法术名称
            guiGraphics.drawString(font, Component.literal(spell.getName()),
                    leftPos + 60, yPos + 4, 0xFFFFFF, false);

        }

        //在底部区域绘制选中法术的详情（目前仅显示名称）
        /*if (selectedIndex >= 0 && selectedIndex < displayedSpells.size()) {
            StoredSpell selectedSpell = displayedSpells.get(selectedIndex);
            int detailY = topPos + GUI_HEIGHT - 60;

            guiGraphics.drawString(font, Component.literal(selectedSpell.getName()),
                    leftPos + 8, detailY, 0xFFFFFF, false);
        }

         */


        // 在法术旁显示绑定键位(新增功能）
        for (int i = 0; i < displayedSpells.size(); i++) {
            StoredSpell spell = displayedSpells.get(i);
            int yPos = startY + i * entryHeight;

            // 检查法术是否绑定
            Optional<Integer> boundSlot = ClientPlayerStorageData.getPlayerSpellStorage().findBoundSlot(spell.getId());
            if (boundSlot.isPresent()) {
                String keyText = "[" + (boundSlot.get() + 1) + "]";
                guiGraphics.drawString(font, keyText,
                        leftPos + GUI_WIDTH - 20, yPos + 6,
                        0x00FF00, false);
            }
        }
        //法术库存量
        Font font= Minecraft.getInstance().font;
        MutableComponent text= net.minecraft.network.chat.Component.literal("法术库存"+preSpellList.size()+"/27");
        int color=0xFFFFFF;//白色
        MutableComponent text2=net.minecraft.network.chat.Component.literal("右键法术条目以打开法术绑定界面");
        guiGraphics.drawString(font,text,leftPos + 63, topPos + GUI_HEIGHT - 28+25,color);
        guiGraphics.drawString(font,text2,leftPos + 63-40, topPos + GUI_HEIGHT - 28+25-100-80,color);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //Magic.getLOGGER().debug("[MAGIC_DEBUG]SpellStorageScreen's mouseClicked() is used");
        // 检查是否点击了法术列表区域
        int startY = topPos + 20;
        int endY = topPos + GUI_HEIGHT - 20;
        int entryHeight = 20;

        if (mouseX >= leftPos + 8 && mouseX <= leftPos + GUI_WIDTH - 8 &&
                mouseY >= startY && mouseY <= endY) {

            // 计算点击的条目索引
            int index = (int) ((mouseY - startY) / entryHeight);

            if (index >= 0 && index < displayedSpells.size()) {
                selectedIndex = index;
                //同步更新 selectedSpell
                selectedSpell = displayedSpells.get(selectedIndex);
                // 右键绑定法术（新增功能）
                if (button == 1 ) {
                    //Magic.getLOGGER().debug("[MAGIC_DEBUG] Player pressed the right mouse button ");
                    StoredSpell spell = displayedSpells.get(selectedIndex);
                    openSlotSelectionMenu(mouseX, mouseY, spell);//打开绑定界面
                    return true;
                }
                updateButtonStates();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }



    //打开绑定界面
    /*private void openSlotSelectionMenu(double x, double y, StoredSpell spell) {
        Minecraft.getInstance().setScreen(new SlotSelectionScreen(
                this, // 传入当前屏幕作为父屏幕
                slot -> {
                    ModMessage.sendToServer(new BindSpellC2SPacket(slot,spell.getId()));
                    //ClientPlayerStorageData.getPlayerSpellStorage().bindSpellToSlot(slot,spell.getId());
                    // 视觉反馈
                    player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
                    player.displayClientMessage(
                            Component.translatable("gui.spell_bound",
                                    spell.getName(),
                                    slot + 1),
                            true
                    );
                }
        ));
    }

     */
    private void openSlotSelectionMenu(double x, double y, StoredSpell spell) {
        Minecraft.getInstance().setScreen(new SlotSelectionScreen(
                this,
                slot -> {
                    ModMessage.sendToServer(new BindSpellC2SPacket(slot, spell.getId()));
                    player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
                    player.displayClientMessage(
                            Component.translatable("gui.spell_bound", spell.getName(), slot + 1),
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
        renameButton.active = hasSelection;
        deleteButton.active = hasSelection;
        //2025.11.9新增
        Up.active=true;
        Down.active=true;
        backButton.active=true;
    }


    //释放法术
    private void castSelectedSpell() {
        if (selectedIndex >= 0 && selectedIndex < displayedSpells.size()) {
            StoredSpell spell = displayedSpells.get(selectedIndex);//根据索引获取法术
            // 发送施法请求到服务器
            //NetworkHandler.sendToServer(new CastSpellPacket(spell.getId()));
            ModMessage.sendToServer(new SpellCastingC2SPacket(spell.getSequenceNode().serializeNBT()));

            this.onClose();//关闭GUI
        }
    }

    //删除法术
    private void deleteSelectedSpell() {
        if (selectedIndex >= 0 && selectedIndex < displayedSpells.size()) {
            StoredSpell spell = displayedSpells.get(selectedIndex);

            // 预测性更新：立即移除本地副本
            displayedSpells.remove(selectedIndex);
            preSpellList.remove(selectedIndex);
            selectedIndex = -1;
            updateButtonStates();
            ClientPlayerStorageData.getPlayerSpellStorage().removeSpell(spell.getId());

            ModMessage.sendToServer(new DeleteSpellC2SPacket(spell.getId()));


        }
    }

    private void renameSelectedSpell() {
        if (selectedIndex >= 0 && selectedIndex < displayedSpells.size()) {
            StoredSpell spell = displayedSpells.get(selectedIndex);
            minecraft.setScreen(new RenameSpellScreen(this, spell));
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
        private final SpellStorageScreen parent;
        private final StoredSpell spell;
        private EditBox nameField;//文本输入框
        private Button confirmButton;//确认按钮

        public RenameSpellScreen(SpellStorageScreen parent, StoredSpell spell) {
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
            if (!newName.isEmpty() && selectedIndex >= 0) {
                StoredSpell spell = parent.displayedSpells.get(parent.selectedIndex);

                // 预测性更新：立即显示新名称
                String originalName = spell.getName();
                spell.rename(newName);
                parent.refreshSpells();

                spell.rename(newName);

                // 发送重命名请求
              //  PacketDistributor.SERVER.noArg().send((new RenameSpellPacket(spell.getId(),newName,originalName)));
                ModMessage.sendToServer(new RenameSpellC2SPacket(spell.getId(),newName,originalName));

            }
            minecraft.setScreen(parent);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
            guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 30, 0xFFFFFF);
        }
    }




    //槽位选择弹出菜单(新增界面
  /*  class SlotSelectionScreen extends Screen {
        private final Consumer<Integer> slotConsumer=null;

        protected SlotSelectionScreen(Component title) {
            super(title);
        }

        protected void init() {
            for (int i = 0; i < 9; i++) {
                int finalI = i;
                this.addRenderableWidget(Button.builder(Component.literal(String.valueOf(i + 1)), btn -> {
                            slotConsumer.accept(finalI);
                            this.onClose();
                        })
                        .pos((int) (width * 0.5f - 50 + (i % 3) * 35),
                                (int) (height * 0.5f - 30 + (i / 3) * 25))
                        .size(30, 20)
                        .build());
            }
        }
    }

   */

    /*class SlotSelectionScreen extends Screen {
        private final Screen parentScreen;
        private final Consumer<Integer> slotConsumer;

        public SlotSelectionScreen(Screen parentScreen, Consumer<Integer> slotConsumer) {
            super(Component.translatable("gui.select_slot.title"));
            this.parentScreen = parentScreen;
            this.slotConsumer = slotConsumer;
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

            // 添加取消按钮
            this.addRenderableWidget(Button.builder(
                            Component.translatable("gui.cancel"),
                            btn -> Minecraft.getInstance().setScreen(parentScreen))
                    .pos(centerX - 40, centerY + 40)
                    .size(80, 20)
                    .build());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTicks);

            // 绘制标题
            guiGraphics.drawCenteredString(
                    this.font,
                    this.title,
                    this.width / 2,
                    20,
                    0xFFFFFF
            );

            // 绘制提示
            guiGraphics.drawCenteredString(
                    this.font,
                    Component.translatable("gui.select_slot.prompt"),
                    this.width / 2,
                    40,
                    0xA0A0A0
            );
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parentScreen);
        }
    }

     */

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
            super(Component.literal("命名法术"));
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

}
