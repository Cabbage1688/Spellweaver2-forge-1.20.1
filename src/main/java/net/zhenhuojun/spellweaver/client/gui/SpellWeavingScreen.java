package net.zhenhuojun.spellweaver.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.long_term_variables.PlayerLongTermVariablesData;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerSpellData;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerVariableData;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.SpellCastingC2SPacket;
import net.zhenhuojun.spellweaver.network.packet.SpellStorageC2SPacket;
import net.zhenhuojun.spellweaver.spell.node.*;
import org.checkerframework.checker.units.qual.C;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;
public class SpellWeavingScreen extends Screen {

    //fromNamespaceAndPath方法已经替代了直接new ResourceLocation对象，因为这个构造在1.21变成私有的了
    private static final ResourceLocation SEQUENCE_TEXTURE = fromNamespaceAndPath(Spellweaver.MODID, "textures/gui/sequence_node.png");
    private static final ResourceLocation NORMAL_TEXTURE = fromNamespaceAndPath(Spellweaver.MODID, "textures/gui/normal_node.png");
    private static final ResourceLocation LOOP_TEXTURE = fromNamespaceAndPath(Spellweaver.MODID, "textures/gui/loop_node.png");
    private static final ResourceLocation WAIT_TEXTURE = fromNamespaceAndPath(Spellweaver.MODID, "textures/gui/wait_node.png");
    private static final ResourceLocation CONDITION_TEXTURE=fromNamespaceAndPath(Spellweaver.MODID,"textures/gui/condition_node.png");

    // 节点当前层级和选中的节点
    public Node currentNode;
    public Node rootNode;
    private List<NodeButton> childButtons = new ArrayList<>();
    private NodeButton centerButton;

    // 右键菜单相关
    private boolean showMenu = false;
    private int menuX, menuY;
    private List<MenuOption> menuOptions = new ArrayList<>();

    // 布局常量
    private static final int CENTER_X = 0;
    private static final int CENTER_Y = 0;
    private static final int CENTER_SIZE = 128;
    private static final int CHILD_SIZE = 32;
    private static final int RADIUS = 100; // 子节点距离中心的半径
    private static final int MAX_CHILDREN = 12; // 最大子节点数,2026.4.2弃用
    //2026.1.25历史栈
    public Deque<Node> historyStack=new ArrayDeque<>();
    private Button backButton;
    protected Button killButton;
    protected Button executeButton;
    protected Button saveButton;
    protected Button spellBoxButton;
    protected Button variableButton;

    protected Button stickButton;//粘贴按钮
    //2026.4.1光标更新
    private int selectedChildIndex; ;
    private long time=0L;


    // 持久化变量显示相关常量
    private static final int PERSISTENT_MAX_DISPLAY = 10;      // 最多显示条目数
    private static final int PERSISTENT_WINDOW_WIDTH = 180;    // 面板宽度
    private static final int PERSISTENT_ITEM_HEIGHT = 12;      // 每行高度
    private static final int PERSISTENT_PADDING = 5;          // 边距
    private boolean displayLongTermVariable=false;



    //2026.1.25历史栈操纵相关
    public Node pop(){
        if(historyStack!=null&&!historyStack.isEmpty()){
            return historyStack.pop();
        }
        return rootNode;
    }

    public void push(Node node){
        if(historyStack!=null){
            historyStack.push(node);
        }
    }
    public Node peek(){
        if(historyStack!=null&&!historyStack.isEmpty()){
            return historyStack.peek();
        }
        return rootNode;
    }
    public void ReturnToParentNode(){
        pop();
        currentNode=peek();
        refreshChildButtons();
    }

    public void killNode(Node centerNode){
        if(centerNode instanceof SequenceNode ){
            ((SequenceNode) centerNode).killChildren();
            refreshChildButtonsWithoutRefreshingSelectedChildIndex();
        } else if (centerNode instanceof LoopNode){
            ((LoopNode) centerNode).killChildren();
            refreshChildButtonsWithoutRefreshingSelectedChildIndex();
        } else if (centerNode instanceof ConditionNode) {
            ((ConditionNode) centerNode).killChildren();;
            refreshChildButtonsWithoutRefreshingSelectedChildIndex();
        }
    }

    public void executeSpell(){
        ModMessage.sendToServer(new SpellCastingC2SPacket(rootNode.serializeNBT()));
        onClose();
    }

    // 菜单选项类
    private class MenuOption {
        String text;
        Runnable action;

        public MenuOption(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }
    }

    // 节点按钮类
    private class NodeButton {
        Node node;
        int x, y;
        int width, height;
        boolean isCenter;

        public NodeButton(Node node, int x, int y, int width, int height, boolean isCenter) {
            this.node = node;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isCenter = isCenter;
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public void onClick() {
            if (isCenter) {
                // 右键点击中心节点显示菜单
                showMenu = true;
                menuX = x + width / 2;
                menuY = y + height / 2;
                updateMenuOptions();
            } else {
                // 点击子节点，切换当前节点
                if (node instanceof SequenceNode || node instanceof LoopNode||node instanceof ConditionNode) {
                    currentNode = node;
                    //2026.1.25将节点压入历史栈
                    push(node);

                    refreshChildButtons();
                } else {
                    // Normal和Wait节点没有子节点，但保留点击方法
                    onNormalOrWaitNodeClick(node);
                }
            }
        }
        //中建点击时调用
        public void onClickCenterNodeByCenter(SpellWeavingScreen screen){
            if(isCenter){
                Spellweaver.getLOGGER().debug("[Spellweaver:SpellWeavingScreen/NodeButton/onClickCenterNodeByCenter:]isCenter条件通过");
                if(currentNode instanceof LoopNode){
                    Minecraft minecraft = Minecraft.getInstance();
                    LoopEditScreen loopEditScreen=new LoopEditScreen(screen,(LoopNode) currentNode);
                    Spellweaver.getLOGGER().debug("[Spellweaver:SpellWeavingScreen/NodeButton/onClickCenterNodeByCenter:]尝试打开loopEditScreen");
                    minecraft.setScreen(loopEditScreen);
                } else if (currentNode instanceof WaitNode) {
                    Minecraft minecraft = Minecraft.getInstance();
                    WaitEditScreen waitEditScreen=new WaitEditScreen(screen,(WaitNode) currentNode);
                    minecraft.setScreen(waitEditScreen);
                }else if(currentNode instanceof ConditionNode){
                    Minecraft minecraft = Minecraft.getInstance();
                    ConditionEditScreen conditionEditScreen=new ConditionEditScreen(screen,(ConditionNode)currentNode);
                    minecraft.setScreen(conditionEditScreen);
                }
            }
        }
    }

    public SpellWeavingScreen(Component pTitle) {
        super(pTitle);
        // 初始化根节点
        this.rootNode = new SequenceNode();
        this.currentNode = rootNode;

        //数据恢复
        Player player=Minecraft.getInstance().player;
        if(player!=null){
            ClientPlayerSpellData playerData = ClientPlayerSpellData.get(player);
            if (playerData != null&&playerData.getSpellTag()!=null) {
                this.rootNode.deserializeNBT(playerData.getSpellTag());
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        // 计算中心位置（屏幕中心）
        int screenWidth = this.width;
        int screenHeight = this.height;
        int centerX = screenWidth / 2 - CENTER_SIZE / 2;
        int centerY = screenHeight / 2 - CENTER_SIZE / 2;

        // 创建中心按钮
        centerButton = new NodeButton(currentNode, centerX, centerY, CENTER_SIZE, CENTER_SIZE, true);

        backButton=this.addRenderableWidget(Button.builder(Component.literal("返回上层"),button -> {
            ReturnToParentNode();
        }).bounds(width - 60, height / 4 + 60, 50, 20).build());

        killButton=this.addRenderableWidget(Button.builder(Component.literal("删除"),button -> {
            //killNode(currentNode);
            //2026.4.1光标更新
            if (selectedChildIndex >= 0 && selectedChildIndex < childButtons.size()) {
                // 删除选中的子节点
                Node target = childButtons.get(selectedChildIndex).node;
                if (currentNode instanceof SequenceNode) {
                    ((SequenceNode) currentNode).getChildrenNodeList().remove(target);
                } else if (currentNode instanceof LoopNode) {
                    ((LoopNode) currentNode).getChildrenNodeList().remove(target);
                } else if (currentNode instanceof ConditionNode) {
                    ((ConditionNode) currentNode).getChildrenNodeList().remove(target);
                }
                //refreshChildButtons();
                refreshChildButtonsAfterKill();
            } else {
                killNode(currentNode);
            }
        }).bounds(width - 60, height / 4 + 30, 50, 20).build());

        executeButton=this.addRenderableWidget(Button.builder(Component.literal("执行"),button->{
           executeSpell();
        }).bounds(width - 60, height / 4, 50, 20).build());

        saveButton=this.addRenderableWidget(Button.builder(Component.literal("保存"),button->{
                storeCurrentSpell();
                }).bounds(width - 60, height / 4-30, 50, 20).build());

        spellBoxButton=this.addRenderableWidget(Button.builder(Component.literal("法术库"),button->{
            Minecraft.getInstance().setScreen(new SpellStorageScreen(Minecraft.getInstance().player,this));

        }).bounds(width - 60, height / 4-60, 50, 20).build());


        stickButton=this.addRenderableWidget(Button.builder(Component.literal("粘贴"),button -> {
            Player player=Minecraft.getInstance().player;
            if(player!=null){
                ClientPlayerSpellData playerData = ClientPlayerSpellData.get(player);
                if (playerData != null&&playerData.getCopyTag()!=null) {
                    //是为完整法术
                    if(playerData.getCopyTag().getString("type").equals("sequence")){
                        this.rootNode.deserializeNBT(playerData.getCopyTag());
                        Spellweaver.getLOGGER().debug("[Spellweaver/SpellWeavingScreen/stickButton]是完整法术");
                        Minecraft.getInstance().player.sendSystemMessage(Component.literal("完整法术已粘贴！"));
                        /// 不是完整法术，将法术片段作为节点存储于可包含子节点的节点
                    } else if (currentNode instanceof SequenceNode||currentNode instanceof ConditionNode||currentNode instanceof LoopNode) {
                        //复制的法术片段类型
                        String type=playerData.getCopyTag().getString("type");
                        CompoundTag tag=playerData.getCopyTag();

                        /// 这一坨是使山，但是写完了才发现有现成的添加逻辑不需要用节点的添加方法，懒得改了
                        switch (type){
                            case "condition"-> {
                                ConditionNode node=new ConditionNode();
                                node.deserializeNBT(tag);
                                if(currentNode instanceof SequenceNode ){
                                    //((SequenceNode) currentNode).addChildren(node);
                                    addNodeToCurrent(node);
                                } else if (currentNode instanceof ConditionNode) {
                                    //((ConditionNode) currentNode).addChildren(node);
                                    addNodeToCurrent(node);
                                } else if (currentNode instanceof LoopNode) {
                                   // ((LoopNode) currentNode).addChildren(node);
                                    addNodeToCurrent(node);
                                }
                            }
                            case "loop"->{
                                LoopNode node=new LoopNode();
                                node.deserializeNBT(tag);
                                if(currentNode instanceof SequenceNode ){
                                    //((SequenceNode) currentNode).addChildren(node);
                                    addNodeToCurrent(node);
                                } else if (currentNode instanceof ConditionNode) {
                                    //((ConditionNode) currentNode).addChildren(node);
                                    addNodeToCurrent(node);
                                } else if (currentNode instanceof LoopNode) {
                                    //((LoopNode) currentNode).addChildren(node);
                                    addNodeToCurrent(node);
                                }
                            }
                            case "wait"->{
                                WaitNode node=new WaitNode();
                                node.deserializeNBT(tag);
                                if(currentNode instanceof SequenceNode ){
                                    //((SequenceNode) currentNode).addChildren(node);
                                    addNodeToCurrent(node);
                                } else if (currentNode instanceof ConditionNode) {
                                    //((ConditionNode) currentNode).addChildren(node);
                                    addNodeToCurrent(node);
                                } else if (currentNode instanceof LoopNode) {
                                    //((LoopNode) currentNode).addChildren(node);
                                    addNodeToCurrent(node);
                                }
                            }
                            case "normal"->{
                                NormalNode node=new NormalNode();
                                node.deserializeNBT(tag);
                                if(currentNode instanceof SequenceNode ){
                                    addNodeToCurrent(node);
                                   // ((SequenceNode) currentNode).addChildren(node);
                                } else if (currentNode instanceof ConditionNode) {
                                    addNodeToCurrent(node);
                                   // ((ConditionNode) currentNode).addChildren(node);
                                } else if (currentNode instanceof LoopNode) {
                                    addNodeToCurrent(node);
                                   // ((LoopNode) currentNode).addChildren(node);
                                }
                            }
                        }
                        Minecraft.getInstance().player.sendSystemMessage(Component.literal("法术片段已加入当前节点！"));
                        Spellweaver.getLOGGER().debug("[Spellweaver/SpellWeavingScreen/stickButton]法术片段已存储到current节点");
                    }
                }
            }
            refreshChildButtons();
        }).bounds(width - 60, height / 4 + 120, 50, 20).build());

        // 复制按钮
        this.addRenderableWidget(Button.builder(Component.literal("复制"), button -> {
                    if(rootNode!=null&&Minecraft.getInstance().player != null){
                        ClientPlayerSpellData playerData = ClientPlayerSpellData.get(Minecraft.getInstance().player);
                        if(playerData!=null){
                            //当前节点是跟节点，复制完整法术
                            if(currentNode instanceof SequenceNode){
                                playerData.setCopyTag(rootNode.serializeNBT());
                                Minecraft.getInstance().player.sendSystemMessage(Component.literal("完整法术已复制！"));
                            }else{
                                //否则只复制片段
                                Minecraft.getInstance().player.sendSystemMessage(Component.literal("法术片段已复制！"));
                                playerData.setCopyTag(currentNode.serializeNBT());
                            }
                        }
                    }
                })
                .bounds(width - 60, height / 4 +90, 50, 20).build());

        variableButton=this.addRenderableWidget(Button.builder(Component.literal("持久变量"),button -> {
            displayLongTermVariable= !displayLongTermVariable;
        }).bounds(width - 60, height / 4 + 150, 50, 20).build());

        // 刷新子节点按钮
        refreshChildButtons();

        // 初始化菜单选项
        updateMenuOptions();
    }
    // 法术存储方法
    private void storeCurrentSpell() {

        // 获取法术名称（可扩展为自定义命名）
        String spellName = "法术_" + System.currentTimeMillis();

        // 打开命名对话框
        this.minecraft.setScreen(new SpellNamingScreen(
                this,
                spellName,
                name -> confirmSpellStorage(name, rootNode.serializeNBT())
        ));
    }
    // 确认存储回调
    private void confirmSpellStorage(String spellName, CompoundTag spellTag) {
        ModMessage.sendToServer(new SpellStorageC2SPacket(spellName,spellTag));
        // 返回绘制界面
        this.minecraft.setScreen(this);
    }


    // 刷新子节点按钮
    private void refreshChildButtons() {
        childButtons.clear();
        // 重置光标索引，2026.4.1光标更新
        //selectedChildIndex = -2;

        if (currentNode instanceof SequenceNode) {
            SequenceNode seqNode = (SequenceNode) currentNode;
            createChildButtons(seqNode.getChildrenNodeList());
        } else if (currentNode instanceof LoopNode) {
            LoopNode loopNode = (LoopNode) currentNode;
            createChildButtons(loopNode.getChildrenNodeList());
        } else if (currentNode instanceof ConditionNode) {
            ConditionNode conditionNode=(ConditionNode) currentNode;
            createChildButtons(conditionNode.getChildrenNodeList());
        }
        selectedChildIndex=childButtons.size()-1;
    }

    private void refreshChildButtonsAfterKill(){
        selectedChildIndex--;
        refreshChildButtonsWithoutRefreshingSelectedChildIndex();
    }

    private void refreshChildButtonsWithoutRefreshingSelectedChildIndex(){
        childButtons.clear();

        if (currentNode instanceof SequenceNode) {
            SequenceNode seqNode = (SequenceNode) currentNode;
            createChildButtons(seqNode.getChildrenNodeList());
        } else if (currentNode instanceof LoopNode) {
            LoopNode loopNode = (LoopNode) currentNode;
            createChildButtons(loopNode.getChildrenNodeList());
        } else if (currentNode instanceof ConditionNode) {
            ConditionNode conditionNode=(ConditionNode) currentNode;
            createChildButtons(conditionNode.getChildrenNodeList());
        }
    }


    private void createChildButtons(List<Node> children) {
        if (children == null || children.isEmpty()) {
            return;
        }

        int screenWidth = this.width;
        int screenHeight = this.height;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int n = children.size();

        // 限制最大有效节点数（用于大小计算，避免过小）
        int effectiveN = Math.min(n, 12);

        // 动态大小：n=1 -> 2*CHILD_SIZE；n=12 -> CHILD_SIZE
        // 线性公式：size = CHILD_SIZE * (23 - effectiveN) / 11
        int size = CHILD_SIZE * (23 - effectiveN) / 11;

        // 确保最小值（当 n>12 时保持为 CHILD_SIZE）
        if (effectiveN == 12 && n > 12) {
            size = CHILD_SIZE;
        } else if (effectiveN < 1) {
            size = CHILD_SIZE;  // 防御性
        }

        for (int i = 0; i < n; i++) {
            // 角度：从12点方向（-π/2）开始均匀分布
            double angle = 2 * Math.PI * i / n - Math.PI / 2;

            // 计算节点左上角坐标，使几何中心位于圆周上
            int x = centerX + (int)(RADIUS * Math.cos(angle)) - size / 2;
            int y = centerY + (int)(RADIUS * Math.sin(angle)) - size / 2;

            NodeButton button = new NodeButton(children.get(i), x, y, size, size, false);
            childButtons.add(button);
        }
    }

    // 更新菜单选项
    private void updateMenuOptions() {
        menuOptions.clear();

        // 只有Sequence和Loop节点还有Condition可以添加子节点
        if (currentNode instanceof SequenceNode || currentNode instanceof LoopNode||currentNode instanceof ConditionNode) {
            menuOptions.add(new MenuOption("添加普通节点", () -> {
                addNodeToCurrent(new NormalNode());
                showMenu = false;
            }));

            menuOptions.add(new MenuOption("添加循环节点", () -> {
                addNodeToCurrent(new LoopNode());
                showMenu = false;
            }));

            menuOptions.add(new MenuOption("添加等待节点", () -> {
                addNodeToCurrent(new WaitNode());
                showMenu = false;
            }));
            menuOptions.add(new MenuOption("添加条件节点", () -> {
                addNodeToCurrent(new ConditionNode());
                showMenu = false;
            }));
        }
    }

    // 添加节点到当前节点
    private void addNodeToCurrent(Node node) {
        if (currentNode instanceof SequenceNode) {
            if(selectedChildIndex>=-1&&selectedChildIndex< ((SequenceNode) currentNode).getChildrenNodeList().size()){
                ((SequenceNode) currentNode).getChildrenNodeList().add(selectedChildIndex+1,node);
                selectedChildIndex++;
            }else {
                ((SequenceNode) currentNode).addChildren(node);
            }
        } else if (currentNode instanceof LoopNode) {
            if (selectedChildIndex >= -1 && selectedChildIndex < ((LoopNode) currentNode).getChildrenNodeList().size()) {
                ((LoopNode) currentNode).getChildrenNodeList().add(selectedChildIndex + 1, node);
                selectedChildIndex++;
            } else {
                ((LoopNode) currentNode).addChildren(node);
            }
        } else if (currentNode instanceof ConditionNode) {
            if (selectedChildIndex >= -1 && selectedChildIndex < ((ConditionNode) currentNode).getChildrenNodeList().size()) {
                ((ConditionNode) currentNode).getChildrenNodeList().add(selectedChildIndex + 1, node);
                selectedChildIndex++;
            } else {
                ((ConditionNode) currentNode).addChildren(node);
            }
        }
        refreshChildButtonsWithoutRefreshingSelectedChildIndex();
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        // 渲染背景
        this.renderBackground(pGuiGraphics);

        // 渲染中心节点
        if(currentNode instanceof SequenceNode sequenceNode){
            renderNode(pGuiGraphics, centerButton, SEQUENCE_TEXTURE);
        } else if (currentNode instanceof LoopNode loopNode) {
            renderNode(pGuiGraphics, centerButton, LOOP_TEXTURE);
            //2026.1.27循环次数渲染
            Font font=Minecraft.getInstance().font;
            pGuiGraphics.drawString(font,""+loopNode.getCurrentTime(),width / 2 - CENTER_SIZE / 2+56,width / 2 - CENTER_SIZE / 2-24,0xFFFFFF);
        } else if (currentNode instanceof WaitNode waitNode) {
            renderNode(pGuiGraphics,centerButton,WAIT_TEXTURE);
            Font font=Minecraft.getInstance().font;
            pGuiGraphics.drawString(font,""+waitNode.getWaitingTime(),width / 2 - CENTER_SIZE / 2+60,width / 2 - CENTER_SIZE / 2-24,0xFFFFFF);
        } else if (currentNode instanceof ConditionNode conditionNode) {
            renderNode(pGuiGraphics,centerButton,CONDITION_TEXTURE);
        }



        //2026.4.1光标更新
        for (int i = 0; i < childButtons.size(); i++) {
            NodeButton button = childButtons.get(i);
            ResourceLocation texture = getTextureForNode(button.node);
            if (i == selectedChildIndex) {
                // 选中的子节点
                renderLightingNode(pGuiGraphics, button.x, button.y, button.width, button.height, texture);
            } else {
                // 普通子节点
                //pGuiGraphics.blit(texture, button.x, button.y, 0, 0, button.width, button.height, button.width, button.height);
                renderRotingNode(pGuiGraphics, button.x, button.y, button.width, button.height, texture);
            }
        }

        // 渲染右键菜单
        if (showMenu && !menuOptions.isEmpty()) {
            renderMenu(pGuiGraphics, pMouseX, pMouseY);
        }

        if(displayLongTermVariable){
            drawPersistentVariables(pGuiGraphics);
        }

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    // 2026.4.1光标更新，渲染闪烁子节点（被光标选中
    private void renderLightingNode(GuiGraphics pGuiGraphics, int x, int y, int width, int height, ResourceLocation texture) {
        PoseStack poseStack = pGuiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x + width / 2f, y + height / 2f, 0);
        float angle = (System.currentTimeMillis()% 360000) * 0.001f * 10;
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        poseStack.translate(-(x + width / 2f), -(y + height / 2f), 0);

        float pulse = 0.5f + 0.5f * (float)Math.sin((System.currentTimeMillis()-time) * 0.003);
         //设置颜色调制（R,G,B,A）
        pGuiGraphics.setColor(pulse, pulse, pulse, 1.0f);

        pGuiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
        poseStack.popPose();

        pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderRotingNode(GuiGraphics pGuiGraphics, int x, int y, int width, int height, ResourceLocation texture) {
        PoseStack poseStack = pGuiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x + width / 2f, y + height / 2f, 0);
        float angle = (System.currentTimeMillis()% 360000) * 0.001f * 10;
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        poseStack.translate(-(x + width / 2f), -(y + height / 2f), 0);
        pGuiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
        poseStack.popPose();
    }


    // 渲染节点
    private void renderNode(GuiGraphics pGuiGraphics, NodeButton button, ResourceLocation texture) {

        PoseStack poseStack = pGuiGraphics.pose();
        // 保存当前矩阵状态
        poseStack.pushPose();
        // 移动到纹理中心点
        poseStack.translate(button.x + button.width/2f, button.y + button.height/2f, 0);
        // 根据时间旋转（毫秒转角度）
        float angle = (System.currentTimeMillis() % 360000) * 0.001f*10;
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        // 移回绘制位置（旋转后需要偏移回去）
        poseStack.translate(-(button.x + button.width/2f), -(button.y + button.height/2f), 0);

        //float pulse = 0.5f + 0.5f * (float)Math.sin(System.currentTimeMillis() * 0.003);
        // 设置颜色调制（R,G,B,A）
        //pGuiGraphics.setColor(pulse, pulse, pulse, 1.0f);

        pGuiGraphics.blit(texture, button.x, button.y, 0, 0,
                button.width, button.height, button.width, button.height);

        // 恢复默认颜色
        //pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        // 恢复矩阵状态
        poseStack.popPose();
    }

    // 根据节点类型获取纹理
    private ResourceLocation getTextureForNode(Node node) {
        if (node instanceof NormalNode) {
            return NORMAL_TEXTURE;
        } else if (node instanceof LoopNode) {
            return LOOP_TEXTURE;
        } else if (node instanceof WaitNode) {
            return WAIT_TEXTURE;
        } else if (node instanceof SequenceNode) {
            return SEQUENCE_TEXTURE;
        } else if (node instanceof ConditionNode) {
            return CONDITION_TEXTURE;
        }
        return SEQUENCE_TEXTURE; // 默认
    }

    // 渲染右键菜单
    private void renderMenu(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        int menuWidth = 100;
        int menuHeight = menuOptions.size() * 20 + 5;
        int menuX = this.menuX - menuWidth / 2;
        int menuY = this.menuY;

        // 绘制菜单背景
        pGuiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xAA000000);
        pGuiGraphics.renderOutline(menuX, menuY, menuWidth, menuHeight, 0xFFFFFFFF);

        // 绘制菜单选项
        for (int i = 0; i < menuOptions.size(); i++) {
            MenuOption option = menuOptions.get(i);
            int optionY = menuY + 5 + i * 20;

            // 检查鼠标是否悬停在该选项上
            boolean isHovered = mouseX >= menuX && mouseX <= menuX + menuWidth &&
                    mouseY >= optionY && mouseY <= optionY + 20;

            // 绘制选项背景
            if (isHovered) {
                pGuiGraphics.fill(menuX + 2, optionY, menuX + menuWidth - 2, optionY + 20, 0xAA555555);
            }

            // 绘制选项文本
            Component text = Component.literal(option.text);
            pGuiGraphics.drawString(this.font, text,
                    menuX + 5, optionY + 6,
                    isHovered ? 0xFFFFFF00 : 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        // 检查是否点击了中心节点
        if (centerButton.isMouseOver(pMouseX, pMouseY)) {
            if (pButton == 1) { // 右键
                centerButton.onClick();
                return true;
            } else if (pButton==2) {//中键
                Spellweaver.getLOGGER().debug("[Spellweaver:SpellWeavingScreen/mouseClicked]检测到鼠标中键点击");
                centerButton.onClickCenterNodeByCenter(this);
                return true;
            }
        }

        // 检查是否点击了子节点
        /*for (NodeButton button : childButtons) {
            if (button.isMouseOver(pMouseX, pMouseY)) {
                button.onClick();
                return true;
            }
        }
         */
        for (int i = 0; i < childButtons.size(); i++) {
            NodeButton button = childButtons.get(i);
            if (button.isMouseOver(pMouseX, pMouseY)) {
               if(pButton==0){
                   //selectedChildIndex = i;           // 更新光标
                   button.onClick();
                   return true;
               } else if (pButton==1) {
                   selectedChildIndex = i;
               }
            }
        }

        // 检查是否点击了菜单选项
        if (showMenu) {
            for (int i = 0; i < menuOptions.size(); i++) {
                MenuOption option = menuOptions.get(i);
                int menuWidth = 100;
                int menuHeight = menuOptions.size() * 20 + 5;
                int menuX = this.menuX - menuWidth / 2;
                int optionY = menuY + 5 + i * 20;

                if (pMouseX >= menuX && pMouseX <= menuX + menuWidth &&
                        pMouseY >= optionY && pMouseY <= optionY + 20) {
                    option.action.run();
                    return true;
                }
            }
        }

        // 点击其他地方隐藏菜单
        showMenu = false;
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    // 键盘左右键移动光标
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT||keyCode == GLFW.GLFW_KEY_A) {
            if (selectedChildIndex >= -1) {
                selectedChildIndex--;
            } else if (selectedChildIndex == -2 && !childButtons.isEmpty()) {
                selectedChildIndex = childButtons.size() - 1;
            }
            time=System.currentTimeMillis();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT||keyCode == GLFW.GLFW_KEY_D) {
            if (selectedChildIndex < childButtons.size() - 1) {
                selectedChildIndex++;
            }
            time=System.currentTimeMillis();
            return true;
        } /*else if (keyCode == GLFW.GLFW_KEY_UP||keyCode == GLFW.GLFW_KEY_DOWN||keyCode == GLFW.GLFW_KEY_W||keyCode == GLFW.GLFW_KEY_S) {
            selectedChildIndex=-1;
            time=System.currentTimeMillis();
            return true;
        } /*else if (keyCode==GLFW.GLFW_KEY_BACKSPACE) {
            if (selectedChildIndex >= 0 && selectedChildIndex < childButtons.size()) {
                // 删除选中的子节点
                Node target = childButtons.get(selectedChildIndex).node;
                if (currentNode instanceof SequenceNode) {
                    ((SequenceNode) currentNode).getChildrenNodeList().remove(target);
                } else if (currentNode instanceof LoopNode) {
                    ((LoopNode) currentNode).getChildrenNodeList().remove(target);
                } else if (currentNode instanceof ConditionNode) {
                    ((ConditionNode) currentNode).getChildrenNodeList().remove(target);
                }
                refreshChildButtonsWithoutRefreshingSelectedChildIndex();
            } else {
                killNode(currentNode);
            }

        }
        */

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // Normal和Wait节点的点击方法（留空，供后续添加内容）
    private void onNormalOrWaitNodeClick(Node node) {
        if (node instanceof NormalNode) {
            // 打开符文编辑界面
            Minecraft minecraft = Minecraft.getInstance();
            NormalNodeEditScreen editScreen = new NormalNodeEditScreen(this, (NormalNode) node, minecraft.player);
            minecraft.setScreen(editScreen);
        } else if (node instanceof WaitNode) {
            currentNode = node;
            push(node);
            refreshChildButtons();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void onClose(){
        if(rootNode!=null&&Minecraft.getInstance().player != null){
            ClientPlayerSpellData playerData = ClientPlayerSpellData.get(Minecraft.getInstance().player);
            if(playerData!=null){
                playerData.setSpellTag(rootNode.serializeNBT());
            }
        }
        super.onClose();
    }


    class SpellNamingScreen extends Screen {
        private final SpellWeavingScreen parent;
        private final String defaultName;
        private final Consumer<String> onConfirm;
        private EditBox nameField;

        public SpellNamingScreen(SpellWeavingScreen parent,
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


    // 格式化持久化变量值
    private String formatPersistentValue(Object value) {
        if (value == null) return "null";
        if (value instanceof EntityType<?> type) {
            ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
            return key != null ? key.toString() : "unknown";
        } else if (value instanceof Vec3 vec) {
            return String.format("向量(%.1f, %.1f, %.1f)", vec.x, vec.y, vec.z);
        } else if (value instanceof UUID uuid) {
            return uuid.toString();
        } else if (value instanceof Double d) {
            return String.format("%.2f", d);
        } else if (value instanceof Float f) {
            return String.format("%.2f", f);
        } else if (value instanceof Integer i) {
            return i.toString();
        } else if (value instanceof Boolean b) {
            return b ? "真" : "假";
        } else if (value instanceof String s) {
            return "\"" + s + "\"";
        } else if (value instanceof List<?> list) {
            return "列表[" + list.size() + "]";
        } else {
            return value.getClass().getSimpleName();
        }
    }



     // 绘制左下角持久化变量面板
    private void drawPersistentVariables(GuiGraphics guiGraphics) {
        PlayerLongTermVariablesData data = ClientPlayerVariableData.getPlayerLongTermVariablesData();
        if (data == null){
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("你还没有定义持久化变量！"));
            return;
        }

        Map<String, Object> vars = data.getPersistentVariables();
        if (vars.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int screenHeight = this.height;

        // 条目列表
        List<Map.Entry<String, Object>> entries = new ArrayList<>(vars.entrySet());
        int totalCount = entries.size();
        int displayCount = Math.min(totalCount, PERSISTENT_MAX_DISPLAY);

        // 计算面板高度：标题行 + 内容行数 + 上下边距
        int windowHeight = PERSISTENT_ITEM_HEIGHT + displayCount * PERSISTENT_ITEM_HEIGHT + PERSISTENT_PADDING * 2;
        int yBase = screenHeight - PERSISTENT_PADDING; // 最下方一行的 y 坐标

        // 面板背景
        int panelX = PERSISTENT_PADDING;
        int panelY = screenHeight - windowHeight - PERSISTENT_PADDING;
        guiGraphics.fill(panelX, panelY, panelX + PERSISTENT_WINDOW_WIDTH, panelY + windowHeight, 0x80000000);

        // 标题
        String title = "持久化变量 (" + totalCount + ")";
        guiGraphics.drawString(font, title, panelX + 2, panelY + 2, 0xFFFFFF);

        // 绘制内容（从上到下，标题下面开始）
        int y = panelY + PERSISTENT_ITEM_HEIGHT + 2; // 标题下方第一行
        for (int i = 0; i < displayCount; i++) {
            Map.Entry<String, Object> entry = entries.get(i);
            String key = entry.getKey();
            String valueStr = formatPersistentValue(entry.getValue());
            String line = key + ": " + valueStr;

            // 超长截断
            if (font.width(line) > PERSISTENT_WINDOW_WIDTH - 6) {
                line = font.plainSubstrByWidth(line, PERSISTENT_WINDOW_WIDTH - 6) + "…";
            }
            guiGraphics.drawString(font, line, panelX + 2, y, 0xFFFFFF);
            y += PERSISTENT_ITEM_HEIGHT;
        }

        // 超出限制提示
        if (totalCount > PERSISTENT_MAX_DISPLAY) {
            String more = "… 还有 " + (totalCount - PERSISTENT_MAX_DISPLAY) + " 个变量";
            guiGraphics.drawString(font, more, panelX + 2, y, 0xAAAAAA);
        }
    }
}

