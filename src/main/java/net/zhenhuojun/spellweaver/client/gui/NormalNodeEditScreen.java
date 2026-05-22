package net.zhenhuojun.spellweaver.client.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ScreenEvent;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerSpellData;
import net.zhenhuojun.spellweaver.client.gui.util.GuiUtil;
import net.zhenhuojun.spellweaver.spell.node.NormalNode;
import net.zhenhuojun.spellweaver.spell.runes.HexPoint;
import net.zhenhuojun.spellweaver.spell.runes.SpellPattern;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class NormalNodeEditScreen extends Screen {
    // 网格参数
    private static final int POINT_COUNT = 37;
    private static final int POINT_RADIUS = 1;
    private static final int SNAP_DISTANCE = //20;
    4;
    private static final int BACKGROUND_COLOR = 0x80000000;

    // 界面组件
    private final List<HexPoint> hexPoints = new ArrayList<>();
    private final List<HexPoint> currentPath = new ArrayList<>();
    private final List<SpellPattern> matchedPatterns = new ArrayList<>();

    private Button deleteButton;
    private Button clearButton;
    private Button returnButton;

    private int gridCenterX, gridCenterY;
    private int gridRadius = 40;

    // 滚动相关
    private int scrollOffset = 0;
    private boolean isDraggingScroll = false;
    private int lastMouseX;
    private int totalPatternsWidth = 0;

    // 预设图案
    private final List<SpellPattern> predefinedPatterns = new ArrayList<>();

    // 与父屏幕的关联
    private final SpellWeavingScreen parentScreen;
    private final NormalNode editingNode;
    private final Player player;

    // 模拟栈相关
    private List<String> currentRuneSequence = new ArrayList<>();//这两个其实已经没用了，但是一删就会有问题，不能动。
    private Deque<Object> lastSimulatedStack = new ArrayDeque<>();

    private int  lastMouseXX,lastMouseYY;

    private final List<RuneEntry> entries = new ArrayList<>();
    //2026.3.29光标更新
    private int selectedIndex = -1;

    EditBox constantInput; // 1.20.1 是 EditBox，需导入
    Button addConstantButton;



    private static final ResourceLocation NORMAL_TEXTURE = fromNamespaceAndPath(Spellweaver.MODID, "textures/gui/normal_node.png");

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


    public NormalNodeEditScreen(SpellWeavingScreen parentScreen, NormalNode editingNode, Player player) {
        super(Component.literal("符文编辑"));
        this.parentScreen = parentScreen;
        this.editingNode = editingNode;
        this.player = player;

        // 初始化预设图案（从一代代码继承）
        initPredefinedPatterns();

        // 加载当前节点的符文序列
        if (editingNode.getSpellList() != null) {
            // 将符文字符串转换为SpellPattern对象
            for (String rune : editingNode.getSpellList()) {
                predefinedPatterns.stream()
                        .filter(p -> p.getName().equals(rune))
                        .findFirst()
                        .ifPresent(matchedPatterns::add);
            }
        }
        if (editingNode.getSpellList() != null) {
            for (String rune : editingNode.getSpellList()) {
                // 尝试匹配预设图案
                Optional<SpellPattern> matched = predefinedPatterns.stream()
                        .filter(p -> p.getName().equals(rune))
                        .findFirst();
                if (matched.isPresent()) {
                    entries.add(new RuneEntry(matched.get()));  // 图案符文
                } else {
                    entries.add(new RuneEntry(rune));           // 常量符文
                }
            }
        }
        //2026.3.29光标更新
        selectedIndex = entries.isEmpty() ? -1 : entries.size() - 1;
    }

    // 初始化预设图案（从一代代码复制）
    private void initPredefinedPatterns() {
        GuiUtil.initPredefinedPatterns(predefinedPatterns);
    }

    @Override
    protected void init() {
        super.init();
        createButtons();
        generateHexGrid();
    }

    private void createButtons() {

        this.addRenderableWidget(Button.builder(Component.literal("复制"), button -> {
                 if(Minecraft.getInstance().player != null){
                     ClientPlayerSpellData playerData = ClientPlayerSpellData.get(Minecraft.getInstance().player);
                     if(playerData!=null){
                         Minecraft.getInstance().player.sendSystemMessage(Component.literal("法术片段已复制！"));
                         playerData.setCopyTag(editingNode.serializeNBT());
                     }
                 }

                }).bounds(width - 60, height / 4-30, 50, 20).build());


        // 删除按钮
        deleteButton = this.addRenderableWidget(Button.builder(Component.literal("⌫"), button -> {
        /*if (!entries.isEmpty()) {
                entries.remove(entries.size() - 1);
                updateRuneSequence();
            }
         */  //2026.3.29光标更新
            if (selectedIndex >= 0 && selectedIndex < entries.size()) {
                entries.remove(selectedIndex);
                if(entries.size()>1){
                    selectedIndex--;
                }
                adjustSelectedIndexAfterModification();
                updateRuneSequence();
            }
        }).bounds(width - 60, height / 4, 50, 20).build());

        // 清除全部按钮
        clearButton = this.addRenderableWidget(Button.builder(Component.literal("清除全部"), button -> {
            //2026.3.29光标更新
            entries.clear();
            selectedIndex = -1;
            updateRuneSequence();

        }).bounds(width - 60, height / 4 + 30, 50, 20).build());

        // 返回按钮（保存并返回）
        returnButton = this.addRenderableWidget(Button.builder(Component.literal("返回"), button -> {
            saveAndReturn();
        }).bounds(width - 60, height / 4 + 60, 50, 20).build());


        constantInput = new EditBox(font, width - 200+25+50+50, height / 4 + 90+30, 120, 20, Component.literal("常量"));
        constantInput.setMaxLength(50-25);
        addRenderableWidget(constantInput);

        addConstantButton = Button.builder(Component.literal("添加常量"), button -> {
            String text = constantInput.getValue().trim();
            if (!text.isEmpty()) {
                String entryText;

                // 情况1：如果文本已经被双引号包围，直接保留（允许玩家手动指定字符串）
                if (text.startsWith("\"") && text.endsWith("\"")) {
                    entryText = text; // 保持原样，例如 "123" 会作为字符串常量
                } else {
                    // 情况2：尝试解析为数字（整数或浮点数）
                    try {
                        Double.parseDouble(text); // 测试是否可解析
                        entryText = text; // 是有效数字，不加引号，作为数字常量
                    } catch (NumberFormatException e) {
                        // 情况3：不是数字，自动添加双引号，作为字符串常量
                        entryText = "\"" + text + "\"";
                    }
                }

                // 将处理后的符文字符串添加到条目列表
                /*entries.add(new RuneEntry(entryText));
                updateRuneSequence();
                constantInput.setValue(""); // 清空输入框

                 */
                //2026.3.29光标更新
                RuneEntry newEntry = new RuneEntry(entryText);
                int insertPos = (selectedIndex == -1) ? 0 : selectedIndex + 1;
                entries.add(insertPos, newEntry);
                selectedIndex = insertPos;  // 光标跟随新条目
                updateRuneSequence();
                constantInput.setValue("");
            }
        }).bounds(width - 70, height / 4 + 90, 60, 20).build();
        addRenderableWidget(addConstantButton);
    }

    // 保存符文序列并返回
    private void saveAndReturn() {
        // 将匹配的图案转换为符文字符串列表
        /*List<String> runeSequence = matchedPatterns.stream()
                .map(SpellPattern::getName)
                .collect(Collectors.toList());

         */
        List<String> runeSequence = entries.stream()
                .map(RuneEntry::getDisplayName)
                .collect(Collectors.toList());

        // 保存到NormalNode
        editingNode.setSpellList(runeSequence);

        // 返回
        if (parentScreen != null) {
            //parentScreen.ReturnToParentNode();
            Minecraft.getInstance().setScreen(parentScreen);
        } else {
            this.onClose();
        }
    }

    //2026.3.29光标更新，用于修正索引
    private void adjustSelectedIndexAfterModification() {
        if (entries.isEmpty()) {
            selectedIndex = -1;
        } else if (selectedIndex >= entries.size()) {
            selectedIndex = entries.size() - 1;
        } else if (selectedIndex < -1) {
            selectedIndex = -1;
        }
    }



    /** 二次贝塞尔曲线计算 */
    private float bezier(float p0, float p1, float p2, float t) {
        float oneMinusT = 1.0f - t;
        return oneMinusT * oneMinusT * p0 + 2 * oneMinusT * t * p1 + t * t * p2;
    }


    private void drawLineSegment(PoseStack poseStack,
                                 float x1, float y1,
                                 float x2, float y2,
                                 float thickness, int color) {
        // 提前计算长度，不满足条件就直接返回
        float dx = x2 - x1, dy = y2 - y1;
        float len = Mth.sqrt(dx * dx + dy * dy);
        if (len < 0.001f) return;

        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        dx /= len; dy /= len;
        float nx = -dy * (thickness * 0.5f);
        float ny =  dx * (thickness * 0.5f);

        buffer.vertex(matrix, x1 - nx, y1 - ny, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1 + nx, y1 + ny, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2 - nx, y2 - ny, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2 + nx, y2 + ny, 0).color(r, g, b, a).endVertex();

        Tesselator.getInstance().end();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void drawEnergyLine_Bezier(PoseStack poseStack,
                                       float x1, float y1, float x2, float y2,
                                       float baseThickness, int color, boolean wiggle) {
        float time = (System.currentTimeMillis() % 10000) / 10000.0f * Mth.TWO_PI;
        float offsetX = 0, offsetY = 0;
        if (wiggle) {
            offsetX = Mth.sin(time * 0.5f) * 1.2f;
            offsetY = Mth.cos(time * 0.3f) * 1.2f;
        }
        float ctrlX = (x1 + x2) / 2 + offsetX * 2;
        float ctrlY = (y1 + y2) / 2 + offsetY * 2;

        float step = 0.06f; // 采样步长
        for (float t = 0; t < 1.0f; t += step) {
            float t2 = Math.min(t + step, 1.0f);
            float bx1 = bezier(x1, ctrlX, x2, t);
            float by1 = bezier(y1, ctrlY, y2, t);
            float bx2 = bezier(x1, ctrlX, x2, t2);
            float by2 = bezier(y1, ctrlY, y2, t2);


            float wave = 0.7f + 0.1f * Mth.sin(t * 15 + time * 2);
            float thickness = baseThickness * wave;


            // === 全局闪烁因子：基于时间的正弦波，范围 [0.3, 1.0]（避免完全消失） ===
            float blink = 0.5f + 0.5f * Mth.sin(time * 2.0f); // 调整乘数控制闪烁速度
            // 确保 alpha 因子在合理范围（比如 0.3 ~ 1.0）
            float alphaFactor = 0.5f + 0.5f * blink; // 最终范围 0.3~1.0
            // 将全局闪烁应用到原始颜色上
            int originalAlpha = (color >> 24) & 0xFF;
            int newAlpha = (int)(originalAlpha * alphaFactor);
            int finalColor = (newAlpha << 24) | (color & 0x00FFFFFF);

            drawLineSegment(poseStack, bx1, by1, bx2, by2, thickness, finalColor);
        }
    }





    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        PoseStack poseStack = guiGraphics.pose();


        // 渲染背景
        this.renderBackground(guiGraphics);
        this.renderRotingNode(guiGraphics,gridCenterX-85,gridCenterY-85,170,170,NORMAL_TEXTURE);

        // 渲染六边形点阵
        renderHexGrid(guiGraphics);
        //renderHexGrid(guiGraphics,mouseX,mouseY);



        renderRuneEntries(poseStack,guiGraphics);

        // 5. 绘制当前绘制路径
        renderCurrentPath(poseStack, partialTicks,mouseX,mouseY);

        poseStack.popPose();


        // 显示当前符文数量
        String status = "符文: " +
                entries.size();
        guiGraphics.drawString(
                font,
                status,
                width - font.width(status) - 10,
                height - 20,
                0xFFFFFF
        );

        // 滚动提示
        if (matchedPatterns.size() > 6 && scrollOffset == 0) {
            String hint = "使用鼠标滚轮或中键拖动可查看更多符文，右键或左右方向键可切换光标位置";
            guiGraphics.drawString(
                    font,
                    hint,
                    width - font.width(hint) - 10,
                    height - 40,
                    0xAAFFFFFF
            );
        }
    }


    // 生成六边形网格（从一代代码复制）
    private void generateHexGrid() {
        hexPoints.clear();
        gridCenterX = this.width / 2;
        gridCenterY = this.height * 2 / 3-20;

        int maxRadius = Math.min(this.width, this.height) / 3;
        if (gridRadius > maxRadius) {
            gridRadius = maxRadius;
        }

        double hexSize = gridRadius / 3.5;
        double hexWidth = 2 * hexSize;
        double hexHeight = Math.sqrt(3) * hexSize;

        int layers = 3;

        for (int q = -layers; q <= layers; q++) {
            int r1 = Math.max(-layers, -q - layers);
            int r2 = Math.min(layers, -q + layers);

            for (int r = r1; r <= r2; r++) {
                int s = -q - r;

                if (Math.abs(q) <= layers &&
                        Math.abs(r) <= layers &&
                        Math.abs(s) <= layers) {

                    double x = hexWidth * (q + r * 0.5);
                    double y = hexHeight * r;

                    HexPoint point = new HexPoint(q, r);
                    point.screenX = gridCenterX + (int) x;
                    point.screenY = gridCenterY + (int) y;

                    hexPoints.add(point);
                }
            }
        }
    }

    // 渲染六边形点阵
    private void renderHexGrid(GuiGraphics guiGraphics) {
        for (HexPoint point : hexPoints) {
            guiGraphics.fill(
                    point.screenX - POINT_RADIUS, point.screenY - POINT_RADIUS,
                    point.screenX + POINT_RADIUS, point.screenY + POINT_RADIUS,
                    0xFFFFFFFF
            );
        }
    }


    //用坐标点和箭头展示图案绘制顺序，测试逻辑
    private String pathToString(List<HexPoint> path) {
        return path.stream()
                .map(p -> "(" + p.q + "," + p.r + ")")
                .collect(Collectors.joining(" -> "));
    }
    private void renderCurrentPath(PoseStack poseStack, float partialTicks,int mouseX,int mouseY) {


        //测试逻辑
       // if (currentPath.size()> 2){
           //List<HexPoint> normalizedPath = normalizePath(currentPath);
           // System.out.println("[DEBUG]:player's path"+pathToString(normalizedPath));
       // }

        // 绘制已经固定的线段（完整显示）
        //Spellweaver.getLOGGER().debug("[Spellweaver:NormalNodeEditScreen/renderCurrentPath方法]尝试绘制当前路径");
        for (int i = 0; i < currentPath.size() - 1; i++) {
            HexPoint start = currentPath.get(i);
            HexPoint end = currentPath.get(i + 1);
            //Spellweaver.getLOGGER().debug("[Spellweaver:NormalNodeEditScreen/renderCurrentPath方法]开始使用drawEnergyLine");
            drawEnergyLine_Bezier(poseStack,
                    start.screenX, start.screenY,
                    end.screenX, end.screenY,
                    2.2f,               // 基础线宽
                    0xFF88FF88,         // 亮绿色
                    // 完整进度
                    false);              // 不启用扰动
        }

        // 绘制从最后一个点到鼠标的预览线
       if (!currentPath.isEmpty()) {
            HexPoint last = currentPath.get(currentPath.size() - 1);
            drawEnergyLine_Bezier(poseStack,last.screenX, last.screenY,mouseX,mouseY,2.2f,0xFF88FF88,false);
       }
    }


    //2026.2.13
     private void renderMatchedPatterns(PoseStack poseStack,GuiGraphics guiGraphics) {
        int startX = 50 - scrollOffset;
        int startY = 30;
        float scale = 0.5f;
        int horizontalSpacing = 20;

        double hexSize = gridRadius / 3.5;
        double hexWidth = 2 * hexSize;
        double hexHeight = Math.sqrt(3) * hexSize;

        totalPatternsWidth = 50;

        for (SpellPattern pattern : matchedPatterns) {
            // 计算图案边界（这部分保留不变）
            int minQ = Integer.MAX_VALUE, maxQ = Integer.MIN_VALUE;
            int minR = Integer.MAX_VALUE, maxR = Integer.MIN_VALUE;
            for (HexPoint p : pattern.getPath()) {
                minQ = Math.min(minQ, p.q);
                maxQ = Math.max(maxQ, p.q);
                minR = Math.min(minR, p.r);
                maxR = Math.max(maxR, p.r);
            }
            int patternWidth = (int)((maxQ - minQ + 1) * hexWidth * scale);
            int patternHeight = (int)((maxR - minR + 1) * hexHeight * scale);

            if (startX + patternWidth > 0 && startX < width) {
                // 绘制图案内的连线
                for (int j = 0; j < pattern.getPath().size() - 1; j++) {
                    HexPoint p1 = pattern.getPath().get(j);
                    HexPoint p2 = pattern.getPath().get(j + 1);

                    float x1 = startX + (float)((p1.q + p1.r * 0.5 - minQ) * hexWidth * scale);
                    float y1 = startY  + (float)((p1.r - minR) * hexHeight * scale);
                    float x2 = startX + (float)((p2.q + p2.r * 0.5 - minQ) * hexWidth * scale);
                    float y2 = startY  + (float)((p2.r - minR) * hexHeight * scale);

                    // 使用动态能量线，颜色从pattern获取，添加扰动和脉动
                    drawEnergyLine_Bezier(poseStack, x1, y1, x2, y2,
                            1.8f,                    // 稍细
                            pattern.getColor(),
                            // 完整显示
                            false);                   // 不启用扰动，这玩意效果不行
                    //Spellweaver.getLOGGER().debug("[Spellweaver:NormalNodeEditScreen/renderMatchedPatterns方法]绘制已匹配图案");
                }
            }

            totalPatternsWidth += patternWidth + horizontalSpacing;
            startX += patternWidth + horizontalSpacing;
        }

        if (totalPatternsWidth > width) {
            renderScrollIndicator(guiGraphics); // 这个保持用GuiGraphics
        }
    }





    // 渲染滚动指示器
    private void renderScrollIndicator(GuiGraphics guiGraphics) {
        int scrollBarHeight = 6;
        int scrollBarY = 10;
        float visibleRatio = (float) width / totalPatternsWidth;
        int scrollBarWidth = (int) (width * 0.3f);
        int scrollBarX = (width - scrollBarWidth) / 2;

        float scrollProgress = (float) scrollOffset / (totalPatternsWidth - width);
        int sliderX = scrollBarX + (int) (scrollProgress * (scrollBarWidth - 20));

        guiGraphics.fill(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, 0x80000000);
        guiGraphics.fill(sliderX, scrollBarY, sliderX + 20, scrollBarY + scrollBarHeight, 0x80FFFFFF);

        if (scrollOffset > 0) {
            guiGraphics.drawString(font, "◀", 10, scrollBarY - 2, 0xFFFFFF);
        }

        if (scrollOffset < totalPatternsWidth - width) {
            guiGraphics.drawString(font, "▶", width - 15, scrollBarY - 2, 0xFFFFFF);
        }
    }


    private void renderRuneEntries(PoseStack poseStack, GuiGraphics guiGraphics) {
        int startX = 50 - scrollOffset;
        int startY = 30;
        float scale = 0.5f;
        int horizontalSpacing = 20;

        double hexSize = gridRadius / 3.5;
        double hexWidth = 2 * hexSize;
        double hexHeight = Math.sqrt(3) * hexSize;

        totalPatternsWidth = 50;

        for (int idx = 0; idx < entries.size(); idx++) {
            RuneEntry entry = entries.get(idx);
            if (entry.type == RuneEntry.Type.PATTERN) {
                SpellPattern pattern = entry.pattern;
                int minQ = Integer.MAX_VALUE, maxQ = Integer.MIN_VALUE;
                int minR = Integer.MAX_VALUE, maxR = Integer.MIN_VALUE;
                for (HexPoint p : pattern.getPath()) {
                    minQ = Math.min(minQ, p.q);
                    maxQ = Math.max(maxQ, p.q);
                    minR = Math.min(minR, p.r);
                    maxR = Math.max(maxR, p.r);
                }
                int patternWidth = (int) ((maxQ - minQ + 1) * hexWidth * scale);
                int patternHeight = (int) ((maxR - minR + 1) * hexHeight * scale);

                if (startX + patternWidth > 0 && startX < width) {
                    // 绘制图案连线
                    for (int j = 0; j < pattern.getPath().size() - 1; j++) {
                        HexPoint p1 = pattern.getPath().get(j);
                        HexPoint p2 = pattern.getPath().get(j + 1);
                        float x1 = startX + (float) ((p1.q + p1.r * 0.5 - minQ) * hexWidth * scale);
                        float y1 = startY + (float) ((p1.r - minR) * hexHeight * scale);
                        float x2 = startX + (float) ((p2.q + p2.r * 0.5 - minQ) * hexWidth * scale);
                        float y2 = startY + (float) ((p2.r - minR) * hexHeight * scale);
                        // 关键：选中时绿色，否则原色
                        int lineColor = (idx == selectedIndex) ? 0xFF88FF88: pattern.getColor();
                        drawEnergyLine_Bezier(poseStack, x1, y1, x2, y2, 1.8f, lineColor, false);
                    }
                }
                totalPatternsWidth += patternWidth + horizontalSpacing;
                startX += patternWidth + horizontalSpacing;
            } else {
                // 常量
                String text = entry.constant;
                int textWidth = font.width(text);
                int boxWidth = textWidth + 10;
                int boxHeight = 20;
                // 文本颜色：选中绿色，否则蓝色
                int textColor = (idx == selectedIndex) ? 0xFF88FF88: 0xFF0000FF;
                guiGraphics.drawString(font, text, startX + 5, startY + 6, textColor);
                totalPatternsWidth += boxWidth + horizontalSpacing;
                startX += boxWidth + horizontalSpacing;
            }
        }

        if (totalPatternsWidth > width) {
            renderScrollIndicator(guiGraphics);
        }
    }

    // 鼠标滚轮事件
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double pDelta) {
        scrollOffset -= pDelta * 30;
        scrollOffset = Math.max(0, Math.min(scrollOffset, totalPatternsWidth - width + 100));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (button == 2) {
            isDraggingScroll = true;
            lastMouseX = (int) mouseX;
            return true;
        }

        if(button==1){
            // 检查是否点击到符文条目
            int clickedIndex = getClickedEntryIndex(mouseX, mouseY);
            if (clickedIndex != -1) {
                selectedIndex = clickedIndex;
                return true;
            }
        }

        if (button == 0) {
            HexPoint point = getNearestPoint(mouseX, mouseY);
            if (point != null) {
                currentPath.clear();
                currentPath.add(point);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingScroll && button == 2) {
            int deltaX = (int) mouseX - lastMouseX;
            scrollOffset -= deltaX;
            scrollOffset = Math.max(0, Math.min(scrollOffset, totalPatternsWidth - width + 100));
            lastMouseX = (int) mouseX;
            return true;
        }

        if (button == 0 && !currentPath.isEmpty()) {
            HexPoint lastPoint = currentPath.get(currentPath.size() - 1);
            HexPoint newPoint = getNearestPoint(mouseX, mouseY);

            if (newPoint != null && !newPoint.equals(lastPoint) && isAdjacent(lastPoint, newPoint)) {
                currentPath.add(newPoint);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && currentPath.size() > 1) {
            matchPattern(currentPath);
            currentPath.clear();
            return true;
        }

        if (button == 2) {
            isDraggingScroll = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    // 获取最近的网格点
    private HexPoint getNearestPoint(double x, double y) {
        HexPoint nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (HexPoint point : hexPoints) {
            double distance = Math.sqrt(
                    Math.pow(point.screenX - x, 2) +
                            Math.pow(point.screenY - y, 2)
            );

            if (distance < SNAP_DISTANCE && distance < minDistance) {
                minDistance = distance;
                nearest = point;
            }
        }
        return nearest;
    }

    // 判断点是否相邻
    private boolean isAdjacent(HexPoint a, HexPoint b) {
        int dq = Math.abs(a.q - b.q);
        int dr = Math.abs(a.r - b.r);
        int ds = Math.abs(a.s() - b.s());
        return (dq + dr + ds) == 2;
    }

    //2026.3.29光标更新
    private boolean matchPattern(List<HexPoint> path) {
        if (path.size() < 2) return false;
        List<HexPoint> normalizedPath = normalizePath(path);
        for (SpellPattern pattern : predefinedPatterns) {
            if (pattern.matches(normalizedPath)) {
                RuneEntry newEntry = new RuneEntry(pattern);
                int insertPos = (selectedIndex == -1) ? 0 : selectedIndex + 1;
                entries.add(insertPos, newEntry);
                selectedIndex = insertPos;  // 光标跟随新图案
                updateRuneSequence();
                return true;
            }
        }
        return false;
    }

    @Override//不返回true会调动父逻辑
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (selectedIndex > -1) {
                selectedIndex--;
            } else if (selectedIndex == -1 && !entries.isEmpty()) {
                selectedIndex = entries.size() - 1; // 从无选中变为选中最后一个
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (selectedIndex < entries.size() - 1) {
                selectedIndex++;
            }
            return true;
        }/* else if (keyCode==GLFW.GLFW_KEY_BACKSPACE) {
            if (selectedIndex >= 0 && selectedIndex < entries.size()) {
                entries.remove(selectedIndex);
                if(entries.size()>1){
                    selectedIndex--;
                }
                adjustSelectedIndexAfterModification();
                updateRuneSequence();
            }
            return true;
        }*/else if(keyCode==GLFW.GLFW_KEY_UP){
            selectedIndex=entries.size() - 1;
            return true;
        }else if (keyCode==GLFW.GLFW_KEY_DOWN) {
            if(!entries.isEmpty()){
                selectedIndex=-1;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // 路径归一化
    private List<HexPoint> normalizePath(List<HexPoint> path) {
        List<HexPoint> normalized = new ArrayList<>();
        if (path.isEmpty()) return normalized;

        HexPoint first = path.get(0);
        int offsetQ = -first.q;
        int offsetR = -first.r;

        for (HexPoint point : path) {
            normalized.add(new HexPoint(point.q + offsetQ, point.r + offsetR));
        }
        return normalized;
    }

    // 更新符文序列
    private void updateRuneSequence(/*List<String> newSequence*/) {
        //this.currentRuneSequence = new ArrayList<>(newSequence);
        // 可以在这里添加模拟栈逻辑
        // simulateStack();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        // 可选：在关闭时自动保存
        // saveAndReturn();
        super.onClose();
    }

    private static class RuneEntry {
        enum Type { PATTERN, CONSTANT }
        final Type type;
        final SpellPattern pattern;  // null if CONSTANT
        final String constant;       // null if PATTERN

        RuneEntry(SpellPattern pattern) {
            this.type = Type.PATTERN;
            this.pattern = pattern;
            this.constant = null;
        }

        RuneEntry(String constant) {
            this.type = Type.CONSTANT;
            this.pattern = null;
            this.constant = constant;
        }

        String getDisplayName() {
            return type == Type.PATTERN ? pattern.getName() : constant;
        }
    }



    private int getClickedEntryIndex(double mouseX, double mouseY) {
        if (entries.isEmpty()) return -1;

        double hexSize = gridRadius / 3.5;
        double hexWidth = 2 * hexSize;
        double hexHeight = Math.sqrt(3) * hexSize;
        float scale = 0.5f;
        int spacing = 20;
        int startX = 50 - scrollOffset;
        int startY = 30;

        int currentStartX = startX;
        for (int idx = 0; idx < entries.size(); idx++) {
            RuneEntry entry = entries.get(idx);
            int width, height;
            float left, top;

            if (entry.type == RuneEntry.Type.PATTERN) {
                SpellPattern pattern = entry.pattern;
                int minQ = Integer.MAX_VALUE, maxQ = Integer.MIN_VALUE;
                int minR = Integer.MAX_VALUE, maxR = Integer.MIN_VALUE;
                for (HexPoint p : pattern.getPath()) {
                    minQ = Math.min(minQ, p.q);
                    maxQ = Math.max(maxQ, p.q);
                    minR = Math.min(minR, p.r);
                    maxR = Math.max(maxR, p.r);
                }
                float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
                float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
                for (HexPoint p : pattern.getPath()) {
                    float px = currentStartX + (float) ((p.q + p.r * 0.5 - minQ) * hexWidth * scale);
                    float py = startY + (float) ((p.r - minR) * hexHeight * scale);
                    minX = Math.min(minX, px);
                    maxX = Math.max(maxX, px);
                    minY = Math.min(minY, py);
                    maxY = Math.max(maxY, py);
                }
                left = minX - 5;   // 增加点击热区边距
                top = minY - 5;
                width = (int)(maxX - minX + 10);
                height = (int)(maxY - minY + 10);
            } else { // CONSTANT
                String text = entry.constant;
                width = font.width(text) + 10;
                height = 20;
                left = currentStartX;
                top = startY;
            }

            if (mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height) {
                return idx;
            }

            // 更新下一个条目的起始X
            if (entry.type == RuneEntry.Type.PATTERN) {
                int minQ = Integer.MAX_VALUE, maxQ = Integer.MIN_VALUE;
                for (HexPoint p : entry.pattern.getPath()) {
                    minQ = Math.min(minQ, p.q);
                    maxQ = Math.max(maxQ, p.q);
                }
                int patternWidth = (int) ((maxQ - minQ + 1) * hexWidth * scale);
                currentStartX += patternWidth + spacing;
            } else {
                currentStartX += width + spacing;
            }
        }
        return -1;
    }

}