package net.zhenhuojun.spellweaver.spell.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RelativeCoordinate {
    private final Vec3 vec3;
    private final Entity entity;

    public RelativeCoordinate(Vec3 vec3,Entity entity){
        this.vec3=vec3;
        this.entity=entity;
    }
    //相对坐标转换为世界坐标
    public Vec3 toWorldCoordinate(){
        //原点
        Vec3 origin=entity.getEyePosition();
        //视线方向为y轴
        Vec3 y=entity.getLookAngle();
        Vec3 z=entity.getUpVector(1.0F);
        Vec3 x=y.cross(z).normalize();
        Vec3 newZ= x.cross(y).normalize();
        return origin
                .add(x.scale(vec3.x))
                .add(y.scale(vec3.y))
                .add(newZ.scale(vec3.z));
    }
}
