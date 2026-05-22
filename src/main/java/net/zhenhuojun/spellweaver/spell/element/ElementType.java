package net.zhenhuojun.spellweaver.spell.element;

import java.util.Random;

public enum ElementType {
    WATER,//水元素
    FIRE,//火元素
    LIGHTING,//雷元素
    ICE,//冰元素
    WIND,//风元素
    STONE,//岩元素
    ENDER,//末影元素
    NULL;//无元素附着返回NULL


    // 缓存非NULL的枚举值
    private static final ElementType[] NON_NULL_VALUES;
    private static final Random RANDOM = new Random();

    static {
        // 获取所有枚举值
        ElementType[] allValues = ElementType.values();
        // 统计非NULL的数量
        int count = 0;
        for (ElementType type : allValues) {
            if (type != NULL) {
                count++;
            }
        }
        // 填充非NULL数组
        NON_NULL_VALUES = new ElementType[count];
        int index = 0;
        for (ElementType type : allValues) {
            if (type != NULL) {
                NON_NULL_VALUES[index++] = type;
            }
        }
    }

    /**
     * 随机返回一个非NULL的ElementType枚举值，每个值概率相等。
     *
     * @return 非NULL的ElementType枚举值
     */
    public static ElementType randomElement() {
        return NON_NULL_VALUES[RANDOM.nextInt(NON_NULL_VALUES.length)];
    }

}
