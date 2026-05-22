package net.zhenhuojun.spellweaver.spell.runes;

import net.minecraft.util.Mth;

import java.util.Objects;

// 六边形网格点坐标类,继承自一代
public class HexPoint {
    public final int q; // 立方体坐标q（水平轴坐标）
    public final int r; // 立方体坐标r（ 斜60°轴坐标）
    public int screenX;
    public int screenY;

    public double phase; // 闪烁初始相位 [0, 2π)


    public HexPoint(int q, int r) {
        this.q = q;
        this.r = r;
        this.phase = Math.random() * 2 * Math.PI;
    }

    public int s() {
        return -q - r; // 立方体坐标s（隐式第三轴（满足 q+r+s=0））
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HexPoint hexPoint = (HexPoint) o;
        return q == hexPoint.q && r == hexPoint.r;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }
}