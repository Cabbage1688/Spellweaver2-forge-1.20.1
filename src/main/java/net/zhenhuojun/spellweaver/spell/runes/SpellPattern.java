package net.zhenhuojun.spellweaver.spell.runes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

//预设图案类，这个也直接从一代继承
public class SpellPattern {
    private final String name;
    private final List<HexPoint> path;
    public final List<HexPoint> normalizedPath;
    private final int color;//符文颜色

    public SpellPattern(String name,int color, HexPoint... points) {
        this.name = name;
        this.path = Arrays.asList(points);
        this.normalizedPath = normalizePath(this.path);
        //新增符文颜色
        this.color=color;
    }

    public int getColor(){return color;}

    public String getName() {
        return name;
    }

    public List<HexPoint> getPath() {
        return path;
    }

    public boolean matches(List<HexPoint> playerPath) {
        // 长度不同直接不匹配
        if (playerPath.size() != normalizedPath.size()) {
            return false;
        }

        // 检查每个点是否匹配
        for (int i = 0; i < normalizedPath.size(); i++) {
            HexPoint patternPoint = normalizedPath.get(i);
            HexPoint playerPoint = playerPath.get(i);

            if (patternPoint.q != playerPoint.q || patternPoint.r != playerPoint.r) {
                return false;
            }
        }
        return true;
    }

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
    //该类独立出来后新增的新增的代码
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpellPattern that = (SpellPattern) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }

}
