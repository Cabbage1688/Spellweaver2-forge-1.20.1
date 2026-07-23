package net.zhenhuojun.spellweaver.client.gui.util;

import net.zhenhuojun.spellweaver.spell.runes.HexPoint;
import net.zhenhuojun.spellweaver.spell.runes.SpellPattern;

import java.util.List;

public class GuiUtil {
    public static void initPredefinedPatterns(List<SpellPattern> predefinedPatterns){
        //自我图案，返回施法者
        predefinedPatterns.add(new SpellPattern("自我", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0)
        ));
        // 三角形图案，返回实体脚下方块坐标
        predefinedPatterns.add(new SpellPattern("脚下坐标", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(1, -1),
                new HexPoint(0, 0)
        ));
        // 直线图案，破坏指定坐标的方块
        predefinedPatterns.add(new SpellPattern("破坏", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(0, 2)
        ));
        predefinedPatterns.add(new SpellPattern("丢弃", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(0, 2),
                new HexPoint(1, 1),
                new HexPoint(0, 1),
                new HexPoint(-1, 1)
        ));
        predefinedPatterns.add(new SpellPattern("交换", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(-1, 2),
                new HexPoint(-1, 1),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("是", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(1, 0),
                new HexPoint(2, -1)
        ));
        predefinedPatterns.add(new SpellPattern("非", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(-1, 0),
                new HexPoint(-1, -1)
        ));
        predefinedPatterns.add(new SpellPattern("随机数", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(2, -1)
        ));
        //应该算是箭头图案，返回实体所看的方向向量
        predefinedPatterns.add(new SpellPattern("视线方向", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(2, -1),
                new HexPoint(1, 0),
                new HexPoint(1, 1),
                new HexPoint(2, 0)
        ));
        predefinedPatterns.add(new SpellPattern("向量归一化", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(2, -2),
                new HexPoint(2, -1),
                new HexPoint(1, -1),
                new HexPoint(2, -2)
        ));
        //倒三角图案，返回实体的眼部坐标
        predefinedPatterns.add(new SpellPattern("眼坐标", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(0, 1),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("检测生命值", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(0, 2),
                new HexPoint(0, 1),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("检测魔力值", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(2, 0),
                new HexPoint(3, -1),
                new HexPoint(3, 0),
                new HexPoint(2, 1),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(1, 0),
                new HexPoint(0, 1),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("检测速度向量", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 0),
                new HexPoint(-2, 0),
                new HexPoint(-1, -1),
                new HexPoint(0, -1),
                new HexPoint(-1, 0),
                new HexPoint(-1, 1),
                new HexPoint(-2, 1),
                new HexPoint(-2, 0)
        ));
        predefinedPatterns.add(new SpellPattern("距离", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(3, -1),
                new HexPoint(3, -2),
                new HexPoint(2, -1),
                new HexPoint(2, 0)
        ));
        predefinedPatterns.add(new SpellPattern("比较器", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(2, -1),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(0, 1),
                new HexPoint(0, 0),
                new HexPoint(0, -1),
                new HexPoint(-1, -1),
                new HexPoint(-2, 0),
                new HexPoint(-2, 1),
                new HexPoint(-1, 1),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("复制", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(1, 0),
                new HexPoint(1, -1),
                new HexPoint(0, 0),
                new HexPoint(1, 0)
        ));
        predefinedPatterns.add(new SpellPattern(">", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(1, -1)
        ));
        predefinedPatterns.add(new SpellPattern("<", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 0),
                new HexPoint(0, -1)
        ));
        predefinedPatterns.add(new SpellPattern("+", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(2, -2),
                new HexPoint(1, -1),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("-", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(2, -2)
        ));
        predefinedPatterns.add(new SpellPattern("*", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(2, -2),
                new HexPoint(1, -1),
                new HexPoint(0, 0),
                new HexPoint(-1, 0),
                new HexPoint(0, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("÷", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(2, -2),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(-1, 0)
        ));
        predefinedPatterns.add(new SpellPattern("sin", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0)
        ));
        predefinedPatterns.add(new SpellPattern("cos", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(1, 0)
        ));
        predefinedPatterns.add(new SpellPattern("组合向量", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(0, 1),
                new HexPoint(1, 1),
                new HexPoint(1, 0),
                new HexPoint(1, -1),
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(0, 1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("存储变量", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(0, 1),
                new HexPoint(-1, 2),
                new HexPoint(-1, 3),
                new HexPoint(0, 3),
                new HexPoint(1, 2),
                new HexPoint(1, 1),
                new HexPoint(0, 1)
        ));
        predefinedPatterns.add(new SpellPattern("读取变量", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(1, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(2, -3),
                new HexPoint(3, -3),
                new HexPoint(3, -2),
                new HexPoint(2, -1),
                new HexPoint(1, -1)
        ));

        predefinedPatterns.add(new SpellPattern("坐标实体", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(-1, 1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("存储持久变量", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(-1, 2),
                new HexPoint(0, 1),
                new HexPoint(0, 2),
                new HexPoint(-1, 3),
                new HexPoint(-2, 4),
                new HexPoint(-2, 5),
                new HexPoint(-1, 5),
                new HexPoint(0, 4),
                new HexPoint(0, 3),
                new HexPoint(-1, 3)
        ));

        predefinedPatterns.add(new SpellPattern("读取持久变量", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(0, -1),
                new HexPoint(1, -2),
                new HexPoint(1, -1),
                new HexPoint(2, -2),
                new HexPoint(2, -3),
                new HexPoint(2, -4),
                new HexPoint(3, -5),
                new HexPoint(4, -5),
                new HexPoint(4, -4),
                new HexPoint(3, -3),
                new HexPoint(2, -3)
        ));
        predefinedPatterns.add(new SpellPattern("实体类型", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(0, 1)
        ));

        predefinedPatterns.add(new SpellPattern("实体列表", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(2, -2),
                new HexPoint(2, -3),
                new HexPoint(1, -3),
                new HexPoint(0, -2),
                new HexPoint(-1, -1),
                new HexPoint(-1, 0),
                new HexPoint(-1, 1),
                new HexPoint(0, 1)
        ));
        predefinedPatterns.add(new SpellPattern("弹出", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(0, 1)
        ));

        predefinedPatterns.add(new SpellPattern("栈清空", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(0, 2),
                new HexPoint(1, 1),
                new HexPoint(0, 1),
                new HexPoint(-1, 1),
                new HexPoint(-1, 2),
                new HexPoint(0, 1),
                new HexPoint(1, 0)
        ));

        predefinedPatterns.add(new SpellPattern("真名", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(0, 1),
                new HexPoint(-1, 2),
                new HexPoint(-2, 3),
                new HexPoint(-2, 4),
                new HexPoint(-1, 3),
                new HexPoint(-1, 2)
        ));

        predefinedPatterns.add(new SpellPattern("真名", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(-1, 2),
                new HexPoint(0, 1),
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(0, -1),
                new HexPoint(1, -2)
        ));
        predefinedPatterns.add(new SpellPattern("生物判断器", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(0, 1),
                new HexPoint(0, 0),
                new HexPoint(0, -1),
                new HexPoint(-1, 0),
                new HexPoint(-1, 1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("自我判断器", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(0, 1),
                new HexPoint(-1, 2),
                new HexPoint(-1, 3),
                new HexPoint(0, 2),
                new HexPoint(0, 1)
        ));
        predefinedPatterns.add(new SpellPattern("掉落物判断器", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(0, 1),
                new HexPoint(-1, 2),
                new HexPoint(0, 2),
                new HexPoint(0, 1)
        ));

        predefinedPatterns.add(new SpellPattern("列表判断器", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(0, 1),
                new HexPoint(-1, 1),
                new HexPoint(-2, 2),
                new HexPoint(-2, 3),
                new HexPoint(-1, 3),
                new HexPoint(0, 3),
                new HexPoint(1, 2),
                new HexPoint(1, 1),
                new HexPoint(0, 1)
        ));

        predefinedPatterns.add(new SpellPattern("滞空判断器", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(0, 1),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("向量分解", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(0, -1),
                new HexPoint(1, -1),
                new HexPoint(2, -1),
                new HexPoint(1, 0),
                new HexPoint(0, 1),
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(1, -1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("栈状态", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(0, 1),
                new HexPoint(1, 1),
                new HexPoint(1, 0),
                new HexPoint(0, 0),
                new HexPoint(0, -1),
                new HexPoint(1, -1),
                new HexPoint(1, 0)
        ));

        predefinedPatterns.add(new SpellPattern("驱动", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(2, -1),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(0, 1),
                new HexPoint(1, 1),
                new HexPoint(2, 0)

        ));
        predefinedPatterns.add(new SpellPattern("水", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(-1, 2),
                new HexPoint(-2, 3),
                new HexPoint(-1, 3),
                new HexPoint(0, 2),
                new HexPoint(0, 1),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(1, 2),
                new HexPoint(0, 3)
        ));

        predefinedPatterns.add(new SpellPattern("闪电", 0xFFFFFF00,
                new HexPoint(0, 0),
                new HexPoint(-1, 0),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(0, 1),
                new HexPoint(-1, 2)
        ));

        predefinedPatterns.add(new SpellPattern("音爆", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(0, -1),
                new HexPoint(1, -2),
                new HexPoint(2, -2),
                new HexPoint(2, -1),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(3, -1),
                new HexPoint(3, -2),
                new HexPoint(3, -3),
                new HexPoint(2, -3),
                new HexPoint(1, -3),
                new HexPoint(0, -2),
                new HexPoint(-1, -1)
        ));
        predefinedPatterns.add(new SpellPattern("魔法光源", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(2, -2),
                new HexPoint(1, -2),
                new HexPoint(1, -1)
        ));
        predefinedPatterns.add(new SpellPattern("传送", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(2, 1),
                new HexPoint(1, 2),
                new HexPoint(0, 2),
                new HexPoint(-1, 2),
                new HexPoint(0, 1),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("生长", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(2, 0),
                new HexPoint(3, -1),
                new HexPoint(3, 0)
        ));
        predefinedPatterns.add(new SpellPattern("治疗", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(2, 0),
                new HexPoint(3, -1),
                new HexPoint(3, 0),
                new HexPoint(2, 1),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(0, 2),
                new HexPoint(0, 1),
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(-1, 0),
                new HexPoint(0, -1),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("缓降", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(1, -1),
                new HexPoint(0, 0),
                new HexPoint(-1, 0),
                new HexPoint(-1, -1),
                new HexPoint(0, -1),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("泥土", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(2, -1),
                new HexPoint(1, -1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("沙", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(0, 2),
                new HexPoint(0, 1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("细雪", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 0),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0),
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(-2, 2),
                new HexPoint(-1, 2),
                new HexPoint(0, 2),
                new HexPoint(0, 1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("岩浆", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(-1, 2),
                new HexPoint(0, 2),
                new HexPoint(0, 1),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(1, 2)
        ));
        predefinedPatterns.add(new SpellPattern("[",0XFF0000FF,
                new HexPoint(0,0),
                new HexPoint(-1,0),
                new HexPoint(-1,-1),
                new HexPoint(0,-2),
                new HexPoint(1,-2)
        ));
        predefinedPatterns.add(new SpellPattern("]", 0XFF0000FF,
                new HexPoint(0,0),
                new HexPoint(1,0),
                new HexPoint(2,-1),
                new HexPoint(2,-2),
                new HexPoint(1,-2)
        ));
        predefinedPatterns.add(new SpellPattern("符文加载器", 0XFF0000FF,
                new HexPoint(0,0),
                new HexPoint(0,1),
                new HexPoint(1,0),
                new HexPoint(1,-1)
        ));
        predefinedPatterns.add(new SpellPattern("魔法飞弹", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(2, -1),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(0, 1),
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, -1)
        ));

        predefinedPatterns.add(new SpellPattern("火元素", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 0),
                new HexPoint(-1, 1),
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(1, 0),
                new HexPoint(0, 0),
                new HexPoint(0, -1),
                new HexPoint(1, -2),
                new HexPoint(1, -1),
                new HexPoint(0, 0)
        ));
        //重载
        predefinedPatterns.add(new SpellPattern("火元素", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(-1, 0),
                new HexPoint(-1, 1),
                new HexPoint(0, 1),
                new HexPoint(1, 0),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("水元素", 0xFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(-1, 2),
                new HexPoint(-2, 3)
        ));

        predefinedPatterns.add(new SpellPattern("冰元素", 0xFF00FFFF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(-1, 2),
                new HexPoint(-1, 1),
                new HexPoint(0, 0),
                new HexPoint(-1, 0),
                new HexPoint(-1, -1),
                new HexPoint(-2, 0),
                new HexPoint(-2, 1),
                new HexPoint(-1, 0),
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(1, 0)
        ));
        //重载
        predefinedPatterns.add(new SpellPattern("冰元素", 0xFF00FFFF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(1, -1),
                new HexPoint(0, 0),
                new HexPoint(-1, 0),
                new HexPoint(-1, -1),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(-1, 2),
                new HexPoint(-1, 1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("风元素", 0xFF00FF00,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(1, -1),
                new HexPoint(0, -1),
                new HexPoint(-1, 0),
                new HexPoint(-1, 1)
        ));

        predefinedPatterns.add(new SpellPattern("末影元素", 0xFF800080,
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(-1, 2),
                new HexPoint(0, 2),
                new HexPoint(1, 1),
                new HexPoint(1, 0),
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(-1, 2),
                new HexPoint(-2, 2),
                new HexPoint(-2, 1),
                new HexPoint(-1, 0),
                new HexPoint(0, 0)
        ));
        //末影元素的重载画法
        predefinedPatterns.add(new SpellPattern("末影元素", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(2, -2),
                new HexPoint(1, -2),
                new HexPoint(0, -2),
                new HexPoint(-1, -1),
                new HexPoint(-1, 0),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("雷元素", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(0, 1),
                new HexPoint(-1, 2)
        ));
        predefinedPatterns.add(new SpellPattern("元素伤害", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(2, -1),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(0, 1),
                new HexPoint(0, 0)
        ));
        predefinedPatterns.add(new SpellPattern("幻化之剑", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(3, -2),
                new HexPoint(3, -3),
                new HexPoint(2, -2),
                new HexPoint(1, -1),
                new HexPoint(0, 0),
                new HexPoint(-1, 1)
        ));
        predefinedPatterns.add(new SpellPattern("幻化之剑", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(2, -2),
                new HexPoint(3, -3),
                new HexPoint(3, -2),
                new HexPoint(2, -1),
                new HexPoint(1, -1)
        ));
        predefinedPatterns.add(new SpellPattern("幻化之弓", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(2, -1),
                new HexPoint(1, 0),
                new HexPoint(0, 1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("充能", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(2, -1),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(0, 1),
                new HexPoint(0, 0),
                new HexPoint(0, -1),
                new HexPoint(1, -1),
                new HexPoint(2, -2),
                new HexPoint(2, -1),
                new HexPoint(3, -1),
                new HexPoint(2, 0),
                new HexPoint(2, 1),
                new HexPoint(1, 1),
                new HexPoint(0, 2),
                new HexPoint(0, 1),
                new HexPoint(-1, 1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("方块射线", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(2, -1),
                new HexPoint(1, 0)
        ));

        predefinedPatterns.add(new SpellPattern("实体射线", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(1, 0)
        ));

        predefinedPatterns.add(new SpellPattern("爆炸", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(0, -1),
                new HexPoint(1, -1),
                new HexPoint(2, -2),
                new HexPoint(2, -1),
                new HexPoint(3, -1),
                new HexPoint(2, 0),
                new HexPoint(2, 1),
                new HexPoint(1, 1),
                new HexPoint(0, 2),
                new HexPoint(0, 1),
                new HexPoint(-1, 1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("点燃", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 1),
                new HexPoint(0, 1),
                new HexPoint(1, 1),
                new HexPoint(1, 0),
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, 0)
        ));

        predefinedPatterns.add(new SpellPattern("槽位引用", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(1, 1),
                new HexPoint(2, 0)
        ));

        predefinedPatterns.add(new SpellPattern("连锁", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(2, -2),
                new HexPoint(1, -1),
                new HexPoint(1, 0)
        ));


        predefinedPatterns.add(new SpellPattern("魔法护盾", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(1, -1),
                new HexPoint(0, 0),
                new HexPoint(0, -1),
                new HexPoint(1, -2),
                new HexPoint(2, -2),
                new HexPoint(2, -1),
                new HexPoint(1, 0),
                new HexPoint(0, 1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("净化", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(2, -2),
                new HexPoint(1, -1),
                new HexPoint(0, 0),
                new HexPoint(-1, 0),
                new HexPoint(-1, -1),
                new HexPoint(0, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("修复", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(1, 1),
                new HexPoint(2, 1),
                new HexPoint(2, 0),
                new HexPoint(2, -1),
                new HexPoint(3, -1),
                new HexPoint(2, 0)
        ));

        predefinedPatterns.add(new SpellPattern("方块放置", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(1, -1),
                new HexPoint(0, 0)
        ));

        predefinedPatterns.add(new SpellPattern("物品转移", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(2, -1),
                new HexPoint(2, 0)
        ));

        predefinedPatterns.add(new SpellPattern("饱腹", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(2, -2),
                new HexPoint(2, -1),
                new HexPoint(1, -1)
        ));

        predefinedPatterns.add(new SpellPattern("相对坐标转换", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(-1, 2),
                new HexPoint(-1, 1),
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, 0)
        ));

        predefinedPatterns.add(new SpellPattern("交互", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(-1, -1)
        ));

        predefinedPatterns.add(new SpellPattern("播种", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, 0),
                new HexPoint(2, -1),
                new HexPoint(1, -1),
                new HexPoint(0, 0),
                new HexPoint(0, -1),
                new HexPoint(1, -2),
                new HexPoint(1, -1)
        ));

        predefinedPatterns.add(new SpellPattern("表长", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(-1, 0),
                new HexPoint(-2, 1),
                new HexPoint(-2, 2),
                new HexPoint(-1, 2),
                new HexPoint(0, 2),
                new HexPoint(1, 1),
                new HexPoint(1, 0),
                new HexPoint(0, 0),
                new HexPoint(0, -1),
                new HexPoint(1, -2)
        ));

        predefinedPatterns.add(new SpellPattern("物品数量", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(0, 1),
                new HexPoint(1, 1),
                new HexPoint(2, 0),
                new HexPoint(2, -1),
                new HexPoint(3, -2)
        ));

        predefinedPatterns.add(new SpellPattern("魔源转换", 0XFF0000FF,
                new HexPoint(0, 0),
                new HexPoint(1, -1),
                new HexPoint(1, -2),
                new HexPoint(0, -1),
                new HexPoint(0, 0),
                new HexPoint(1, 0),
                new HexPoint(2, -1),
                new HexPoint(2, -2),
                new HexPoint(1, -2)
        ));
    }
}
