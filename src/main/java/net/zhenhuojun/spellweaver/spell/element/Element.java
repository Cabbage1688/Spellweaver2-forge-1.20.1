package net.zhenhuojun.spellweaver.spell.element;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.entity.impl.FrozenIceEntity;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.ManaBallEffectS2CPacket;
import net.zhenhuojun.spellweaver.network.packet.ReactionEffectS2CPacket;
import net.zhenhuojun.spellweaver.network.packet.SpreadReactionS2CPacket;
import net.zhenhuojun.spellweaver.network.packet.VoidErosionS2CPacket;

//元素附着相关
public class Element {


    public static CompoundTag elementEntryToNBT(ElementType type, int remainingTicks) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.name());
        tag.putInt("remainingTicks", remainingTicks);
        return tag;
    }

    public static ElementType getElementTypeFromNBT(CompoundTag tag) {
        return ElementType.valueOf(tag.getString("type"));
    }

    public static void applyElement(LivingEntity entity, ElementType type, int durationTicks) {
        CompoundTag persistentData = entity.getPersistentData();
        // 获取元素栈列表，若不存在则创建
        ListTag elementStack = persistentData.getList("spellweaver:element_stack", Tag.TAG_COMPOUND);

        // 获取当前栈顶元素
        ElementType topType = ElementType.NULL;
        if (!elementStack.isEmpty()) {
            CompoundTag topEntry = elementStack.getCompound(elementStack.size() - 1);
            topType = getElementTypeFromNBT(topEntry);
        }

        // 如果栈顶元素存在且与新元素不同，尝试触发反应
        if (topType != ElementType.NULL && topType != type) {
            boolean shouldAdd = handleReaction(entity, elementStack, topType, type, durationTicks);
            if (!shouldAdd) {
                // 反应消耗了新元素，直接更新数据
                persistentData.put("spellweaver:element_stack", elementStack);
                return;
            }
            // 反应后新元素需要保留，继续执行添加逻辑（栈已被反应修改）
        }

        // 添加新元素：先移除所有同类型元素（保证无重复）
        for (int i = 0; i < elementStack.size(); i++) {
            CompoundTag entry = elementStack.getCompound(i);
            if (getElementTypeFromNBT(entry) == type) {
                elementStack.remove(i);
                i--; // 移除后索引回退
            }
        }
        // 新建条目并入栈
        CompoundTag newEntry = elementEntryToNBT(type, durationTicks);
        elementStack.add(newEntry);
        persistentData.put("spellweaver:element_stack", elementStack);
    }

    //检测元素栈栈顶元素
    public static ElementType checkElement(LivingEntity entity){
        CompoundTag persistentData = entity.getPersistentData();
        // 获取元素栈列表，若不存在则创建
        ListTag elementStack = persistentData.getList("spellweaver:element_stack", Tag.TAG_COMPOUND);
        if(!elementStack.isEmpty()){
            CompoundTag entry = elementStack.getCompound(elementStack.size()-1);
            return getElementTypeFromNBT(entry);
        }
        return ElementType.NULL;
    }

    // 处理元素反应，返回true表示新元素需要保留（添加到栈），false表示新元素被消耗
    private static boolean handleReaction(LivingEntity entity, ListTag elementStack,
                                          ElementType topType, ElementType newType, int newDuration) {
        // 感电：雷（新） + 水（顶） → 雷伤提升50%，清除两者
        if (newType == ElementType.LIGHTING && topType == ElementType.WATER) {
            Spellweaver.getLOGGER().debug("感电反应：雷伤提升50%");
            elementStack.remove(elementStack.size() - 1); // 移除栈顶水

            entity.getPersistentData().putInt("lightning_attack_up",2);


            /*if(entity.level() instanceof ServerLevel serverLevel){
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,entity.getX(),entity.getY()+1,
                        entity.getZ(),20,0,0,0,0.05);
            }

             */
            Vec3 center = entity.getBoundingBox().getCenter();
            ModMessage.sendToClients(new ReactionEffectS2CPacket(center,  ReactionEffectS2CPacket.ReactionType.ELECTROCUTE));
            return false; // 新雷被消耗
        }
        // 传导：水（新） + 雷（顶） → 附加一段雷伤（固定值），清除两者
        else if (newType == ElementType.WATER && topType == ElementType.LIGHTING) {
            entity.invulnerableTime = 0;
            entity.hurtTime = 0; //重置 hurt 动画计时
            Vec3 center = entity.getBoundingBox().getCenter();
            ModMessage.sendToClients(new ReactionEffectS2CPacket(center,  ReactionEffectS2CPacket.ReactionType.CONDUCT));

            float damage = 10.0f; // 可调整
            entity.hurt(entity.damageSources().magic(),damage);
            elementStack.remove(elementStack.size() - 1); // 移除栈顶雷

            entity.invulnerableTime = 0;
            entity.hurtTime = 0; //重置 hurt 动画计时
            return false; // 新水被消耗
        }
        // 超载：火+雷 或 雷+火 → 爆炸范围伤害，清除两者
        else if ((newType == ElementType.FIRE && topType == ElementType.LIGHTING) ||
                (newType == ElementType.LIGHTING && topType == ElementType.FIRE)) {

            entity.invulnerableTime = 0;
            entity.hurtTime = 0; //重置 hurt 动画计时
            //explode方法的第一个参数可以填null,即没有爆炸源。填了爆炸源实体会导致爆炸源不受伤。
            entity.level().explode(null, entity.getX(), entity.getY(), entity.getZ(),
                    3.0f, false, Level.ExplosionInteraction.NONE);
            elementStack.remove(elementStack.size() - 1); // 移除栈顶元素
            entity.invulnerableTime = 0;
            entity.hurtTime = 0; //重置 hurt 动画计时
            return false;
        }
        // 冻结：水+冰 或 冰+水 → 目标定身（5秒高额缓慢），清除两者
        else if ((newType == ElementType.WATER && topType == ElementType.ICE) ||
                (newType == ElementType.ICE && topType == ElementType.WATER)) {
            // 末影龙不参与冻结
            if (entity.getType() == EntityType.ENDER_DRAGON) {
                elementStack.remove(elementStack.size() - 1);
                return false;
            }
            CompoundTag persistentData = entity.getPersistentData();
            int frozenTime = persistentData.getInt("FrozenTime"); // 不存在时默认 0
            // 冻结抗性已满，不再冻结
            if (frozenTime >= 15) {
                elementStack.remove(elementStack.size() - 1);
                return false;
            }
            // 计算冻结时长
            int freezeDuration;
            if (frozenTime >= 11) { // 11-14 时，动态缩短
                freezeDuration = (15 - frozenTime) * 20;
            } else {
                freezeDuration = 100; // 默认 5 秒
            }
            // 记录冻结位置与截止时间
            persistentData.putDouble("FrozenX", entity.getX());
            persistentData.putDouble("FrozenY", entity.getY());
            persistentData.putDouble("FrozenZ", entity.getZ());
            persistentData.putLong("FrozenUntil", entity.level().getGameTime() + freezeDuration);
            // 增加 FrozenTime，上限 15
            frozenTime = Math.min(frozenTime + 5, 15);
            persistentData.putInt("FrozenTime", frozenTime);

            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, freezeDuration, 7));
            entity.addEffect(new MobEffectInstance(MobEffects.JUMP, freezeDuration, -7));
            FrozenIceEntity frozenIceEntity = new FrozenIceEntity(entity.level(), entity, freezeDuration);
            entity.level().addFreshEntity(frozenIceEntity);

            if (entity instanceof Mob mob) {
                mob.setNoAi(true);
            }
            elementStack.remove(elementStack.size() - 1);

            entity.invulnerableTime = 0;
            entity.hurtTime = 0; //重置 hurt 动画计时
            return false;
        }
        // 中和：水+火 或 火+水 → 攻击伤害降低30%，清除两者
        else if ((newType == ElementType.WATER && topType == ElementType.FIRE) ||
                (newType == ElementType.FIRE && topType == ElementType.WATER)) {
            Spellweaver.getLOGGER().debug("中和反应：攻击伤害降低30%");
            elementStack.remove(elementStack.size() - 1);

            if(entity.level() instanceof ServerLevel serverLevel){
                serverLevel.sendParticles(ParticleTypes.POOF,entity.getX(),entity.getY()+1,
                        entity.getZ(),20,0.2,0.3,0.2,0.05);
            }

            entity.getPersistentData().putInt("water_or_fire_attack_down",2);
            return false;
        }
        // 抵消：冰+火 或 火+冰
        else if ((newType == ElementType.ICE && topType == ElementType.FIRE) ||
                (newType == ElementType.FIRE && topType == ElementType.ICE)) {
            if (newType == ElementType.ICE) {
                Spellweaver.getLOGGER().debug("抵消：冰伤降低30%");
            } else {
                Spellweaver.getLOGGER().debug("抵消：火伤降低30%");
            }
            elementStack.remove(elementStack.size() - 1);
            if(entity.level() instanceof ServerLevel serverLevel){
                serverLevel.sendParticles(ParticleTypes.POOF,entity.getX(),entity.getY()+1,
                        entity.getZ(),20,0.2,0.3,0.2,0.05);
            }

            entity.getPersistentData().putInt("fire_or_ice_attack_down",2);
            return false;
        }
        // 助燃：火（新） + 风（顶） → 火伤提升30%，清除风，火保留
        else if (newType == ElementType.FIRE && topType == ElementType.WIND) {
            Vec3 center = entity.getBoundingBox().getCenter();
            ModMessage.sendToClients(new ReactionEffectS2CPacket(center,  ReactionEffectS2CPacket.ReactionType.COMBUST));

            Spellweaver.getLOGGER().debug("助燃：火伤提升30%");
            elementStack.remove(elementStack.size() - 1); // 移除风
            entity.setRemainingFireTicks(100);

            entity.getPersistentData().putInt("fire_attack_up",2);
            return false;
        }
        // 扩散（火）：风（新） + 火（顶） → 造成范围火伤，清除风，火消耗,点燃触发反应的实体
        else if (newType == ElementType.WIND && topType == ElementType.FIRE) {

            elementStack.remove(elementStack.size() - 1); // 移除火
            entity.invulnerableTime = 0;
            entity.hurtTime = 0; //重置 hurt 动画计时
            ModMessage.sendToClients(new SpreadReactionS2CPacket(entity.position(),0xFF6A00));
            //ModMessage.sendToClients(new ManaBallEffectS2CPacket(entity.position(),0xFF6A00));

            AABB aabb = entity.getBoundingBox().inflate(3.0);
            for (LivingEntity target : entity.level().getEntitiesOfClass(LivingEntity.class, aabb, e -> e != entity)) {
                target.hurt(entity.damageSources().magic(), 5.0f);
                target.invulnerableTime = 0;
                target.hurtTime = 0; //重置 hurt 动画计时
                //target.setRemainingFireTicks(100);
                applyElement(target,ElementType.FIRE,100);
            }

            entity.hurt(entity.damageSources().magic(), 5.0f);
            entity.invulnerableTime = 0;
            entity.hurtTime = 0; //重置 hurt 动画计时
            entity.setRemainingFireTicks(100);



            return false; // 风被消耗
        }
        // 风寒：冰（新） + 风（顶） → 冰伤提升30%，清除风，冰保留
        else if (newType == ElementType.ICE && topType == ElementType.WIND) {
            Vec3 center = entity.getBoundingBox().getCenter();
            ModMessage.sendToClients(new ReactionEffectS2CPacket(center,  ReactionEffectS2CPacket.ReactionType.CHILL));

            Spellweaver.getLOGGER().debug("风寒：冰伤提升30%");
            elementStack.remove(elementStack.size() - 1); // 移除风
            entity.getPersistentData().putInt("ice_attack_up",2);
            return true; // 冰保留
        }
        // 扩散（冰）：风（新） + 冰（顶） → 造成范围冰伤，清除风，冰消耗
        else if (newType == ElementType.WIND && topType == ElementType.ICE) {

            elementStack.remove(elementStack.size() - 1); // 移除冰
            entity.invulnerableTime = 0;
            entity.hurtTime = 0; //重置 hurt 动画计时
            ModMessage.sendToClients(new SpreadReactionS2CPacket(entity.position(),0xF0F8FF));
           //ModMessage.sendToClients(new ManaBallEffectS2CPacket(entity.position(),0xF0F8FF));

            AABB aabb = entity.getBoundingBox().inflate(3.0);
            for (LivingEntity target : entity.level().getEntitiesOfClass(LivingEntity.class, aabb, e -> true)) {
                target.hurt(entity.damageSources().magic(), 5.0f);
                target.invulnerableTime = 0;
                target.hurtTime = 0; //重置 hurt 动画计时
                applyElement(target,ElementType.ICE,100);
            }

            return false;
        }
        // 扩散（水）：风（新） + 水（顶） → 将水附着扩散至周围实体，清除风，水保留
        else if (newType == ElementType.WIND && topType == ElementType.WATER) {

            elementStack.remove(elementStack.size() - 1);//水消耗
            entity.invulnerableTime = 0;
            entity.hurtTime = 0; //重置 hurt 动画计时
            ModMessage.sendToClients(new SpreadReactionS2CPacket(entity.position(),0x1E90FF));
            //ModMessage.sendToClients(new ManaBallEffectS2CPacket(entity.position(),0x1E90FF));

            int duration = 100; // 5秒
            AABB aabb = entity.getBoundingBox().inflate(3.0);
            for (LivingEntity target : entity.level().getEntitiesOfClass(LivingEntity.class, aabb, e -> true)) {
                target.hurt(entity.damageSources().magic(), 2.5f);
                target.invulnerableTime = 0;
                target.hurtTime = 0; //重置 hurt 动画计时
                applyElement(target, ElementType.WATER, duration); // 递归施加水
            }

            // 风被消耗，
            return false;
            //扩散相关都把附着元素清除放在第一句，也是因为设计缺陷，导致可能左脚踩右脚
        } else if (newType == ElementType.WATER && topType == ElementType.WIND) {
            int duration = 100; // 5秒
            //因为我的元素系统设计缺陷，如果包含触这个反应的实体，会导致无限递归
            entity.hurt(entity.damageSources().magic(), 2.5f);
            entity.invulnerableTime = 0;
            entity.hurtTime = 0; //重置 hurt 动画计时
            elementStack.remove(elementStack.size() - 1);//风消耗
            applyElement(entity, ElementType.WATER, duration);//消耗掉风才能给水
            ModMessage.sendToClients(new SpreadReactionS2CPacket(entity.position(),0x1E90FF));
            AABB aabb = entity.getBoundingBox().inflate(3.0);
            for (LivingEntity target : entity.level().getEntitiesOfClass(LivingEntity.class, aabb, e -> e!=entity)) {
                target.hurt(entity.damageSources().magic(), 2.5f);
                target.invulnerableTime = 0;
                target.hurtTime = 0; //重置 hurt 动画计时
                applyElement(target, ElementType.WATER, duration); // 递归施加水
            }
            return false;
        }
        // 虚空侵蚀(水侵反应)：水+末影 或 末影+水 → 附加虚空伤，清除两者
        else if ((newType == ElementType.WATER && topType == ElementType.ENDER) ||
                (newType == ElementType.ENDER && topType == ElementType.WATER)) {
            entity.invulnerableTime = 0;
            entity.hurtTime = 0; //重置 hurt 动画计时
            float damage = 10.0f;
            /*float health=entity.getHealth();
            if(health-damage>0){
                entity.setHealth(health-damage);
            }else{
                entity.die(entity.damageSources().fellOutOfWorld());
            }
             */
            Spellweaver.getLOGGER().debug("[Spellweaver:Element/handleReaction]水侵反应，尝试造成虚空伤害{}"
                    ,entity.hurt(entity.damageSources().fellOutOfWorld(),damage));
            Spellweaver.getLOGGER().debug("[Spellweaver:Element/handleReaction]反应后血量{}",entity.getHealth());
            entity.invulnerableTime = 0;
            entity.hurtTime = 0; //重置 hurt 动画计时

            elementStack.remove(elementStack.size() - 1);
            if(entity.level() instanceof ServerLevel serverLevel){
               // serverLevel.sendParticles(ParticleTypes.PORTAL,entity.getX(),entity.getY()+1,
                       // entity.getZ(),400,0.2,0.3,0.2,0.05);

                ModMessage.sendToClients(new VoidErosionS2CPacket(entity.position()));
                Spellweaver.getLOGGER().debug("[Spellweaver:Element/handleReaction]粒子包发送");
            }
            return false;
        }
        // 无反应情况：新元素将压入栈顶，旧元素保留在下层
        // 风+雷
        else if (newType == ElementType.WIND && topType == ElementType.LIGHTING) return true;
            // 雷+风
        else if (newType == ElementType.LIGHTING && topType == ElementType.WIND) return true;
            // 末影+任何非水
        else if (newType == ElementType.ENDER && topType != ElementType.WATER) return true;
            // 任何非水+末影
        else if (newType != ElementType.WATER && topType == ElementType.ENDER) return true;
            // 岩+任何
        else if (newType == ElementType.STONE) return true;
            // 任何+岩
        else if (topType == ElementType.STONE) return true;
            // 其他未定义情况默认无反应
        else return true;
    }
    public static void handleAmplificationMarkers(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();


        String[] markerKeys = {
                "lightning_attack_up",
                "water_or_fire_attack_down",
                "fire_attack_up",
                "ice_attack_up",
                "fire_or_ice_attack_down"
        };

        for (String key : markerKeys) {
            if (data.contains(key, CompoundTag.TAG_INT)) {
                int value = data.getInt(key);
                if (value <= 0) {
                    data.remove(key);
                } else {
                    value--;
                    if (value <= 0) {
                        data.remove(key);
                    } else {
                        // 更新剩余持续时间
                        data.putInt(key, value);
                    }
                }
            }
        }
    }
}
