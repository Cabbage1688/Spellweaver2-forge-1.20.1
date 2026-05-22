package net.zhenhuojun.spellweaver.spell;

//import com.ibm.icu.impl.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.IItemHandler;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.block.custom.SpellMachineBlockEntity;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaSource;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaUtil;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerLongTermVariablesProvider;
import net.zhenhuojun.spellweaver.capability.provider.mana.PlayerManaProvider;
import net.zhenhuojun.spellweaver.entity.ModEntities;
import net.zhenhuojun.spellweaver.entity.impl.ManaBall;
import net.zhenhuojun.spellweaver.entity.util.MagicLightUtils;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.item.Util;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.RayS2CPacket;
import net.zhenhuojun.spellweaver.network.packet.ScrollSpellCastingC2SPacket;
import net.zhenhuojun.spellweaver.network.packet.TeleportParticleS2CPacket;
import net.zhenhuojun.spellweaver.spell.element.Element;
import net.zhenhuojun.spellweaver.spell.element.ElementType;
import net.zhenhuojun.spellweaver.spell.util.SlotReference;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SpellExecutorManager {
    private final Map<String, SpellExecutor> executors = new HashMap<>();

    private static final SpellExecutorManager INSTANCE = new SpellExecutorManager();


    //法术执行管理器构造方法
    public SpellExecutorManager() {
        initExecutors();
    }

    public static SpellExecutorManager getInstance() {
        return  INSTANCE;
    }

    public SpellExecutor getExecutor(String spellName) {
        return executors.get(spellName);
    }
    private void initExecutors() {
        executors.put("丢弃",context -> {
            context.pop(Object.class);
        });
        executors.put("交换",context -> {
            Object a=context.pop(Object.class);
            Object b=context.pop(Object.class);
            context.push(a);
            context.push(b);
        });
        executors.put("自我", context -> {
                context.push(context.player);
        });
        executors.put("脚下坐标",context -> {
            Entity entity = context.pop(Entity.class);
            Vec3 vecPos=entity.blockPosition().below().getCenter();
            context.push(vecPos);
        });
        //符文“是”
        executors.put("是",context -> {
            context.push(true);
        });
        //符文“非”
        executors.put("非",context -> {
            context.push(false);
        });
        //生成两个输入之间的随机数
        //2026.2.24更新，现在支持随机向量
        //怎么这么傻逼卧槽，两边的数不能相等还得我自己写判断逻辑？
        executors.put("随机数",context -> {
            if(context.isTop(Double.class)){
                double a=context.pop(Double.class);
                double b=context.pop(Double.class);
                double min = Math.min(a, b);
                double max = Math.max(a, b);
                Random random=new Random();
                if (min == max) {
                    context.push(min);  // 相等时直接返回该值
                } else {
                    context.push(random.nextDouble(min, max));
                }
            }else if(context.isTop(Vec3.class)){
                Vec3 a = context.pop(Vec3.class);
                Vec3 b = context.pop(Vec3.class);
                double minX = Math.min(a.x, b.x);
                double maxX = Math.max(a.x, b.x);
                double minY = Math.min(a.y, b.y);
                double maxY = Math.max(a.y, b.y);
                double minZ = Math.min(a.z, b.z);
                double maxZ = Math.max(a.z, b.z);
                Random random=new Random();
                double rx = minX==maxX?minX:random.nextDouble(minX, maxX);
                double ry = minY==maxY?minY:random.nextDouble(minY, maxY);
                double rz = minZ==maxZ?minZ:random.nextDouble(minZ, maxZ);
                context.push(new Vec3(rx, ry, rz));
            }
        });

        // 视线方向 - 从栈顶弹出实体，压入其视线方向
        executors.put("视线方向", context -> {
            Entity entity = context.pop(Entity.class);
            context.push(entity.getLookAngle());
        });
        //归一向量
        executors.put("向量归一化", context -> {
            Vec3 vec3=context.pop(Vec3.class);
            context.push(vec3.normalize());
        });
        //眼部坐标
        executors.put("眼坐标",context -> {
            Entity entity = context.pop(Entity.class);
            Vec3 vecEyePos=entity.blockPosition().above().getCenter();
            context.push(vecEyePos);
        });
        //弹出实体，获取其生命值，将数值压入栈
        executors.put("检测生命值",context -> {
            Entity entity=context.pop(Entity.class);
            if(entity instanceof LivingEntity){
                context.push((double)(((LivingEntity) entity).getHealth()));
            }
        });
        //弹出实体，检测其是否为玩家，如果是则检测魔力，将魔力数值压入栈
        executors.put("检测魔力值",context -> {
            Entity entity= context.pop(Entity.class);
            if(entity instanceof ServerPlayer player){
                player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(playerMana -> {
                    context.push(playerMana.getMana());
                });
            }
        });
        //弹出实体，压入其速度向量
        executors.put("检测速度向量",context->{
            Entity entity=context.pop(Entity.class);
            Vec3 speed=entity.getDeltaMovement();
            context.push(speed);
            //Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/initExecutors方法]检测速度向量，速度为{}",speed);
        });

        /*executors.put("距离",context -> {
            Entity entity1=context.pop(Entity.class);
            Entity entity2=context.pop(Entity.class);
            context.push((double)(entity1.distanceTo(entity2)));
        });

         */
        executors.put("距离", context -> {
            if (context.isTop(Entity.class)) {
                Entity first = context.pop(Entity.class);
                if (context.isTop(Entity.class)) {
                    Entity second = context.pop(Entity.class);
                    double dist = first.distanceTo(second);
                    context.push(dist);
                }
                else if (context.isTop(Vec3.class)) {
                    Vec3 point = context.pop(Vec3.class);
                    double dist = first.position().distanceTo(point);
                    context.push(dist);
                }
            }
            else if (context.isTop(Vec3.class)) {
                Vec3 first = context.pop(Vec3.class);
                if (context.isTop(Entity.class)) {
                    Entity entity = context.pop(Entity.class);
                    double dist = first.distanceTo(entity.position());
                    context.push(dist);
                }
                else if (context.isTop(Vec3.class)) {
                    Vec3 second = context.pop(Vec3.class);
                    double dist = first.distanceTo(second);
                    context.push(dist);
                }
            }
        });
        // 比较器符文比较ab，根据结果输出布尔值
        executors.put("比较器", context -> {
            Object a = context.pop(Object.class);
            Object b = context.pop(Object.class);
            context.push(a.equals(b));
        });
        // 复制符文
        executors.put("复制", context -> {
            Object top = context.stack.peek();
            if (top != null) {
                context.push(top);
                Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/initExecutors方法]复制{}",top);
            }
        });

        // 大于
        executors.put(">", context -> {
            Double a = context.pop(Double.class);
            Double b = context.pop(Double.class);
            context.push(b > a);
        });//小于
        executors.put("<", context -> {
            Double a = context.pop(Double.class);
            Double b = context.pop(Double.class);
            context.push(b < a);
        });//加
        executors.put("+",context -> {
            if(context.isTop(Double.class)){
                double a=context.pop(Double.class);
                double b=context.pop(Double.class);
                context.push(a+b);
            } else if (context.isTop(Vec3.class)) {
                Vec3 vec3a=context.pop(Vec3.class);
                Vec3 vec3b=context.pop(Vec3.class);
                context.push(vec3a.add(vec3b));
            }
        });//减
        executors.put("-",context -> {
            if(context.isTop(Double.class)){
                double a=context.pop(Double.class);
                double b=context.pop(Double.class);
                context.push(b-a);
            } else if (context.isTop(Vec3.class)) {
                Vec3 vec3a = context.pop(Vec3.class);
                Vec3 vec3b = context.pop(Vec3.class);
                context.push(vec3b.subtract(vec3a));
            }
        });//乘
        executors.put("*",context -> {
            //如果栈顶元素为double,弹出并检测下一个元素
            if(context.isTop(Double.class)){
                double a=context.pop(Double.class);
                //如果第二个元素为double，此次行动为两个数相乘,将积压入栈
                if(context.isTop(Double.class)){
                    double b=context.pop(Double.class);
                    context.push(a*b);
                }
                //如果第二个元素为向量，则此次行动为向量的数乘
                else if (context.isTop(Vec3.class)){
                    Vec3 vec3=context.pop(Vec3.class);
                    Vec3 result=new Vec3(vec3.x()*a, vec3.y()*a, vec3.z()*a);
                    context.push(result);
                }
            }
            //如果第一个数为向量，此次行动为向量数乘
            else if(context.isTop(Vec3.class)){
                Vec3 vec3=context.pop(Vec3.class);
                double a=context.pop(Double.class);
                Vec3 result=new Vec3(vec3.x()*a, vec3.y()*a, vec3.z()*a);
                context.push(result);
            }
        });
        //除
        executors.put("÷",context -> {
            //如果栈顶元素为double,弹出并检测下一个元素
            if(context.isTop(Double.class)){
                double a=context.pop(Double.class);
                //如果第二个元素为double，此次行动为两个数相除,将商压入栈
                if(context.isTop(Double.class)){
                    double b=context.pop(Double.class);
                    context.push(b/a);
                }
                //如果第二个元素为向量，则此次行动为向量的数乘
                else if(context.isTop(Vec3.class)){
                    context.player.sendSystemMessage(Component.literal("孩子们，这样做并不规范"));
                    Vec3 vec3=context.pop(Vec3.class);
                    Vec3 result=new Vec3(vec3.x()/a, vec3.y()/a, vec3.z()/a);
                    context.push(result);
                }
            }
            //如果第一个数为向量，此次行动为向量数乘
            else if(context.isTop(Vec3.class)){
                context.player.sendSystemMessage(Component.literal("孩子们，这样做并不规范"));
                Vec3 vec3=context.pop(Vec3.class);
                double a=context.pop(Double.class);
                Vec3 result=new Vec3(vec3.x()/a, vec3.y()/a, vec3.z()/a);
                context.push(result);
            }
        });

        executors.put("sin", context -> {
            Double a = context.pop(Double.class);
            context.push(Math.sin(a));
        });

        executors.put("cos", context -> {
            Double a = context.pop(Double.class);
            context.push(Math.cos(a));
        });
        //输入三个数，组合为向量
        executors.put("组合向量",context -> {
            double c=context.pop(Double.class);
            double b=context.pop(Double.class);
            double a=context.pop(Double.class);
            context.push(new Vec3(a,b,c));
        });
        // 变量存储，接受一个值和字符串以创建一个键值对
        executors.put("存储变量", context -> {
            /*Object value = context.pop(Object.class);
            String varName = context.pop(String.class);
            context.variables.put(varName, value);
             */
            String varName;
            Object value;
            if (context.isTop(String.class)) {
                varName = context.pop(String.class);
                value = context.pop(Object.class);
            } else {
                value = context.pop(Object.class);
                varName = context.pop(String.class);
            }
            if(context.variables.containsKey(varName)){
                context.variables.remove(varName);
            }
            context.variables.put(varName, value);
        });

        // 变量读取，通过名称从键值对中读取存储的东西
        executors.put("读取变量", context -> {
            String varName = context.pop(String.class);
            Object value = context.variables.get(varName);
            if (value != null) {
                context.push(value);
                Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/initExecutors方法]成功读取变量，变量名{},变量值{}",varName,value);
            }
        });
        //直接将寄存器中的符文使用掉
        executors.put("符文加载器",context -> {
            RuneRegister runeRegister=context.pop(RuneRegister.class);
            List<String> spellList=new ArrayList<>();
            if(runeRegister!=null){
                spellList=runeRegister.getSpellList();
            }
            if(spellList!=null){
                for(String rune :spellList){
                    SpellExecutor executor = SpellExecutorManager.getInstance().getExecutor(rune);
                    if (executor != null) {
                        try {
                            executor.execute(context);
                        } catch (SpellExecutionException e) {
                            context.player.sendSystemMessage(
                                    Component.literal("§c法术执行错误 [" + rune + "]: " + e.getMessage())
                            );
                            break;
                        }
                    } else {
                        //尝试解析为字符串常量，这里引号防一手玩家的String和符文重名
                        if (rune.startsWith("\"") && rune.endsWith("\"")) {
                            String string = rune.substring(1, rune.length() - 1);
                            context.push(string);
                        }
                        //尝试解析为数字常量
                        else {
                            try {
                                double number = Double.parseDouble(rune);
                                context.push(number);
                            } catch (NumberFormatException ex) {
                                context.player.sendSystemMessage(
                                        Component.literal("§6未知符文: " + rune)
                                );
                            }
                        }
                    }
                }
            }
        });

        executors.put("坐标实体",context -> {
            Vec3 vec3=context.pop(Vec3.class);
            Vec3 startVec3=new Vec3(vec3.x-0.5, vec3.y-0.5,vec3.z-0.5);
            Vec3 endVec3=new Vec3(vec3.x+0.5, vec3.y+0.5,vec3.z+0.5);
            //AABB aabb=new AABB(BlockPos.containing(vec3));
            //AABB aabb=new AABB(startVec3,endVec3);
            AABB aabb=new AABB(vec3, vec3).inflate(0.5);
            List<Entity> entities = context.level.getEntities((Entity) null, aabb, (entity) -> true); // 第三个参数是可选的条件过滤
            if(!entities.isEmpty()){
                context.push(entities.get(0));
            }
        });
        //TODO：还没实装到图案,可能也不会实装，这个符文有点超过我符文序列不应包含法术结构的初衷
        /*executors.put("跳转",context -> {
            if(context.isTop(Boolean.class)){
                boolean condition=context.pop(Boolean.class);
                double jumpTarget=context.pop(Double.class);
                if(condition){
                    context.jumpTarget=(int)jumpTarget;
                }
            }else if(context.isTop(Double.class)){
                double jumpTarget=context.pop(Double.class);
                boolean condition=context.pop(Boolean.class);
                if(condition){
                    context.jumpTarget=(int)jumpTarget;
                }
            }
        });

         */
        /*executors.put("实体列表",context -> {
            Vec3 startPos=context.pop(Vec3.class);
            Vec3 endPos=context.pop(Vec3.class);
            AABB aabb=new AABB(startPos,endPos);
            List<Entity> entities = context.level.getEntities((Entity) null, aabb, (entity) -> true);
            if(!entities.isEmpty()){
                context.push(entities);
                Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/initExecutors]获取实体数目{}",entities.size());
            }
        });

         */

        executors.put("实体列表", context -> {
            EntityType<?> filterType;
            Vec3 vec1 = null;
            Vec3 vec2 = null;

            // 分支1：栈顶为 EntityType → 顺序：EntityType, Vec3, Vec3
            if (context.isTop(EntityType.class)) {
                filterType = context.pop(EntityType.class);
                vec1 = context.pop(Vec3.class);
                vec2 = context.pop(Vec3.class);
            }
            // 分支2：栈顶为 Vec3
            else if (context.isTop(Vec3.class)) {
                vec1 = context.pop(Vec3.class);
                // 子分支2a：第二个为 EntityType → 顺序：Vec3, EntityType, Vec3
                if (context.isTop(EntityType.class)) {
                    filterType = context.pop(EntityType.class);
                    vec2 = context.pop(Vec3.class);
                }
                // 子分支2b：第二个为 Vec3 → 顺序：Vec3, Vec3, (EntityType可选)
                else if (context.isTop(Vec3.class)) {
                    vec2 = context.pop(Vec3.class);
                    // 检查是否还有 EntityType
                    if (context.isTop(EntityType.class)) {
                        filterType = context.pop(EntityType.class);
                    } else {
                        filterType = null;
                    }
                }
                // 参数不足或类型错误
                else {
                    filterType = null;
                    Spellweaver.getLOGGER().error("Invalid parameters for '实体列表': expected two Vec3 and optional EntityType");
                    return;
                }
            }
            // 栈顶类型不匹配
            else {
                filterType = null;
                Spellweaver.getLOGGER().error("Invalid parameters for '实体列表': expected Vec3 or EntityType at top");
                return;
            }

            // 确保两个 Vec3 均已获取
            if (vec1 == null || vec2 == null) {
                Spellweaver.getLOGGER().error("Missing Vec3 parameters for '实体列表'");
                return;
            }

            // 构造 AABB 并获取实体（根据 filterType 过滤）
            AABB aabb = new AABB(vec1, vec2);
            List<Entity> entities = context.level.getEntities((Entity) null, aabb,
                    entity -> filterType == null || entity.getType() == filterType
            );

            if (!entities.isEmpty()) {
                context.push(entities);
                Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/initExecutors]获取实体数目{}", entities.size());
            }
        });

       /* executors.put("弹出",context -> {
           List list=context.pop(List.class);
           if(!list.isEmpty()){
               Object entity=list.get(list.size()-1);
               list.remove(list.get(list.size()-1));
               context.push(list);
               context.push(entity);
               Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/initExecutors方法]从列表弹出实体{}",entity);
           }
        });

        */
        executors.put("弹出", context -> {
            List list = context.pop(List.class);
            if (!list.isEmpty()) {
                int index = ThreadLocalRandom.current().nextInt(list.size());
                Object entity = list.get(index);
                list.remove(index);
                context.push(list);
                context.push(entity);
                Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/initExecutors方法]从列表随机弹出实体{}", entity);
            }
        });

        executors.put("实体类型", context -> {
            Entity entity = context.pop(Entity.class);
            if (entity != null) {
                context.push(entity.getType());
            }
        });

        executors.put("存储持久变量",context -> {
            String varName;
            Object value;
            if (context.isTop(String.class)) {
                varName = context.pop(String.class);
                value = context.pop(Object.class);
            } else {
                value = context.pop(Object.class);
                varName = context.pop(String.class);
            }
            if (value != null && varName != null) {
                context.player.getCapability(PlayerLongTermVariablesProvider.PLAYER_LONG_TERM_VARIABLES).ifPresent(playerLongTermVariablesData -> {
                    playerLongTermVariablesData.getPersistentVariables().put(varName,value);
                    context.player.sendSystemMessage(Component.literal("持久变量"+varName+"="+value+"已存储"));
                });
            }
        });

        executors.put("读取持久变量",context -> {
            String varName = context.pop(String.class);
            context.player.getCapability(PlayerLongTermVariablesProvider.PLAYER_LONG_TERM_VARIABLES).ifPresent(playerLongTermVariablesData -> {
                if(playerLongTermVariablesData.getPersistentVariables().containsKey(varName)){
                    Object valve=playerLongTermVariablesData.getPersistentVariables().get(varName);
                    context.push(valve);
                }
            });
        });

        executors.put("栈清空",context -> {
            context.stack.clear();
            Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/initExecutors方法]栈清空");
        });

        executors.put("真名",context -> {
            if(context.isTop(Entity.class)){
                UUID uuid=context.pop(Entity.class).getUUID();
                context.push(uuid);
            }else if(context.isTop(UUID.class)){
                if(context.level instanceof ServerLevel serverLevel){
                    UUID uuid=context.pop(UUID.class);
                    context.push(serverLevel.getEntity(uuid));
                }
            }
        });

        executors.put("生物判断器",context -> {
            if(context.isTop(LivingEntity.class)){
                context.push(true);
            }else context.push(false);
            Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/initExecutors方法]生物判断器");
        });
        executors.put("自我判断器",context -> {
           Player player=context.pop(Player.class);
           boolean isThePlayer=false;
           if(player==context.player){
               isThePlayer=true;
           }
           context.push(player);
           context.push(isThePlayer);
        });

        executors.put("掉落物判断器",context -> {
           if(context.isTop(ItemEntity.class)) {
               context.push(true);
           }else context.push(false);
        });

        executors.put("列表判断器",context -> {
            if(context.isTop(List.class)) {
                context.push(true);
            }else context.push(false);
        });

        executors.put("滞空判断器",context -> {
           Entity entity=context.pop(Entity.class);
           boolean bool=entity.onGround();
           context.push(entity);
           context.push(!bool);
        });
        executors.put("向量分解",context -> {
           Vec3 vec3=context.pop(Vec3.class);
           Vec3 a=new Vec3(vec3.x,0,0);
           Vec3 b=new Vec3(0, vec3.y, 0);
           Vec3 c=new Vec3(0,0, vec3.z);
           context.push(a);
           context.push(b);
           context.push(c);
        });

        executors.put("栈状态", context -> {
            if (context.player == null) return;

            Deque<Object> stack = context.stack;
            if (stack.isEmpty()) {
                context.player.sendSystemMessage(Component.literal("§7[栈状态] 栈为空"));
                return;
            }

            StringBuilder sb = new StringBuilder("§a[栈状态] §f");
            int i = 0;
            for (Object obj : stack) {
                if (i > 0) sb.append(" §7|§f ");
                sb.append(i++).append(": ");
                if (obj == null) {
                    sb.append("§onull");
                } else {
                    String typeName = obj.getClass().getSimpleName();
                    String valueStr = obj.toString();
                    // 截断过长的字符串（例如坐标或列表）
                    if (valueStr.length() > 30) {
                        valueStr = valueStr.substring(0, 27) + "...";
                    }
                    sb.append(typeName).append(" §7(§f").append(valueStr).append("§7)");
                }
            }
            // 如果一条消息太长，可分段发送，但通常栈不会过大
            context.player.sendSystemMessage(Component.literal(sb.toString()));
        });


        executors.put("或", context -> {
            Boolean b2 = context.pop(Boolean.class);
            Boolean b1 = context.pop(Boolean.class);
            context.push(b1 || b2);
        });


        executors.put("与", context -> {
            Boolean b2 = context.pop(Boolean.class);
            Boolean b1 = context.pop(Boolean.class);
            context.push(b1 && b2);
        });


        executors.put("槽位引用", context -> {
            if (context.stack.size() < 2) {
                //context.push(SlotReference.EMPTY);
                return;
            }
            Object first = context.pop(Object.class);
            Object second = context.pop(Object.class);
            Object containerObj;
            double rawSlot;
            // 兼容两种参数顺序
            if (first instanceof Double) {
                rawSlot = (Double) first;
                containerObj = second;
            } else if (second instanceof Double) {
                rawSlot = (Double) second;
                containerObj = first;
            } else {
                // 参数类型不对
                // context.push(SlotReference.EMPTY);
                return;
            }
            int slot = (int) rawSlot;
            if (slot < 0) {
                //context.push(SlotReference.EMPTY);
                return;
            }
            Level level = context.level;
            if (level.isClientSide) {
                // 客户端不处理物品栏数据
                //context.push(SlotReference.EMPTY);
                return;
            }
            IItemHandler inventory = null;
            Entity entitySource = null;
            BlockPos posSource = null;
            try {
                if (containerObj instanceof Entity entity) {
                    // 尝试通过 Forge 能力系统获取物品栏
                    inventory = entity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                            .resolve().orElse(null);
                    if (inventory != null) entitySource = entity;

                } else if (containerObj instanceof Vec3 vec) {
                    BlockPos pos = BlockPos.containing(vec);
                    BlockEntity be = level.getBlockEntity(pos);
                    if(be==null){
                        be=level.getBlockEntity(pos.above());
                    }
                    if (be != null) {
                        inventory = be.getCapability(ForgeCapabilities.ITEM_HANDLER)
                                .resolve().orElse(null);
                        if (inventory != null) posSource = pos;
                    }
                }
            } catch (Exception e) {
                Spellweaver.getLOGGER().error("[槽位引用] 获取物品栏失败", e);
            }
            if (inventory == null || slot >= inventory.getSlots()) {
                //context.push(SlotReference.EMPTY);
                return;
            }
            context.push(new SlotReference(level, inventory, slot, entitySource, posSource));
        });

        //初始化魔力相关的Runes
        initRunesNeedingMana();
    }

    private <T> List<T> createList(Class<T> clas) {
        return new ArrayList<T>();
    }

    private void initRunesNeedingMana(){
        executors.put("破坏",context -> {
            Vec3 vecPos = context.pop(Vec3.class);
            BlockPos pos=BlockPos.containing(vecPos);
            BlockState state = context.level.getBlockState(pos);
            if(!context.level.isClientSide){
                if(context.player!=null){
                    //获取方块硬度
                    float hardness=context.level.getBlockState(pos).getDestroySpeed(context.level,pos);
                    Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/破坏]破坏了{}",pos);
                    //防止玩家破坏基岩，众所周知那玩意硬度-1
                    if(hardness>=0){
                        //2026.4.7调整魔力消耗，增设上限防止硬度过高方块耗费巨额魔力
                        double manaCost=Math.pow(hardness,3)>100?100:Math.pow(hardness,3);
                        if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                            context.level.destroyBlock(pos,true,context.player);
                               //给所有玩家发送特效包
                            if(context.level instanceof ServerLevel){
                                   ((ServerLevel) context.level).sendParticles(ParticleTypes.INSTANT_EFFECT,pos.getX()+0.5,
                                           pos.getY()+0.5,pos.getZ()+0.5,15,0,0,0,0.03);

                            }

                            //destroy没法监听只能这里补个逻辑了
                            if (state.getBlock() == Blocks.LAPIS_ORE || state.getBlock() == Blocks.DEEPSLATE_LAPIS_ORE) {
                                final float chance=0.1f;
                                if(context.level.random.nextFloat()<=chance){
                                    ItemStack pearl = new ItemStack(ModItems.MOON_PEARL.get());
                                    ItemEntity itemEntity = new ItemEntity(
                                            context.level,
                                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                            pearl
                                    );
                                    context.level.addFreshEntity(itemEntity);
                                }
                            }
                        }
                    }
                }
            }
        });

        executors.put("驱动",context -> {
            if(context.isTop(Vec3.class)){
                Vec3 velocity = context.pop(Vec3.class);
                Entity entity = context.pop(Entity.class);
                drive(velocity,entity, context.player,context.level,context);
                context.push(entity);
            } else if (context.isTop(Entity.class)) {
                Entity entity = context.pop(Entity.class);
                Vec3 velocity = context.pop(Vec3.class);
                drive(velocity,entity, context.player,context.level,context);
                context.push(entity);
            }
        });


        executors.put("水",context -> {
            Vec3 vec3=context.pop(Vec3.class);
            //vec3安全转换为BlockPos
            BlockPos pos=BlockPos.containing(vec3.x,vec3.y,vec3.z);
            BlockState waterState = Blocks.WATER.defaultBlockState();
            BlockState blockState =context.level.getBlockState(pos);
            int manaCost=15;
            if (!context.level.isClientSide) {
                if (context.player != null) {
                    if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                        context.level.setBlock(pos,waterState, Block.UPDATE_ALL);
                        if(context.level instanceof ServerLevel){
                            ((ServerLevel) context.level).sendParticles(ParticleTypes.INSTANT_EFFECT,pos.getX()+0.5,
                                    pos.getY()+0.5,pos.getZ()+0.5,15,0,0,0,0.03);
                        }
                    }
                }
            }
        });

        executors.put("闪电",context -> {
            if(context.isTop(Vec3.class)){
                Vec3 vec3 = context.pop(Vec3.class);
                double attack_level=context.pop(Double.class);
                lightningBolt(vec3,attack_level, context.player ,context.level,context);
            } else if (context.isTop(Double.class)) {
                double attack_level=context.pop(Double.class);
                Vec3 vec3 = context.pop(Vec3.class);
                lightningBolt(vec3,attack_level, context.player ,context.level,context);
            }
        });

        executors.put("音爆",context -> {
            //音波起始位置
            Vec3 startPos=context.pop(Vec3.class);
            //音波距离和方向
            Vec3 direction=context.pop(Vec3.class);
            //攻击强度
            double attackLevel=context.pop(Double.class);
            //对direction归一化处理，用于处理击退
            Vec3 normalizedDir = direction.normalize();
            //起始位置转换为BlockPos,下面要用
            BlockPos pos=new BlockPos((int)startPos.x(),(int)startPos.y(),(int)startPos.z());
            double manaCost=5*Math.pow(23+attackLevel,1+attackLevel/10);
            if(!context.level.isClientSide){
                if(context.player!=null){
                    if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                        //播放声音
                        context.level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                                SoundEvents.WARDEN_SONIC_BOOM, context.player.getSoundSource(),
                                3.0F, 1.0F);

                        if(context.level instanceof ServerLevel serverLevel) {
                            // 计算粒子数量
                            int particleCount = Mth.floor(direction.length()) + 7;

                            // 循环在直线上每个点生成一个粒子
                            for (int i = 1; i <= particleCount; ++i) {
                                Vec3 particlePos = startPos.add(normalizedDir.scale(i));
                                serverLevel.sendParticles(
                                        ParticleTypes.SONIC_BOOM,
                                        particlePos.x, particlePos.y, particlePos.z,
                                        1,
                                        0.0, 0.0, 0.0,
                                        1.0
                                );
                            }
                        }


                        //进行射线追踪，检测音波路径上的实体
                        HitResult hitResult = context.level.clip(new ClipContext(
                                startPos,
                                startPos.add(normalizedDir.scale(direction.length())), // 终点
                                ClipContext.Block.COLLIDER, // 碰撞类型：只与可碰撞方块检测
                                ClipContext.Fluid.NONE, // 不考虑流体
                                context.player // 上下文实体
                        ));

                        //确定实际命中点（如果击中方块，则以方块面为终点；否则使用最大距离点）
                        Vec3 endPos = hitResult.getType() == HitResult.Type.BLOCK ?
                                hitResult.getLocation() :
                                startPos.add(normalizedDir.scale(direction.length()));

                        // 3. 获取音波路径上的所有实体
                        AABB effectArea = new AABB(startPos, endPos).inflate(1.0); // 创建一个包围盒，稍微膨胀以确保捕捉到附近的实体
                        List<LivingEntity> entities = context.level.getEntitiesOfClass(
                                LivingEntity.class,
                                effectArea,
                                entity -> entity != context.player && entity.isAlive() // 过滤条件：不是施法者自己且存活
                        );

                        // 4. 对每个命中的实体造成伤害和击退
                        for (LivingEntity entity : entities) {
                            // 造成伤害
                            float damage = (float) (45.0 * attackLevel); // 基础伤害乘以强度
                            entity.hurt(context.level.damageSources().sonicBoom(context.player), damage);
                        }
                    }
                }
            }
        });
        executors.put("魔法光源",context -> {
            Vec3 vec3= context.pop(Vec3.class);
            //BlockPos pos= new BlockPos((int)vec3.x,(int)vec3.y,(int)vec3.z);
            BlockPos pos=BlockPos.containing(vec3);
            double manaCost=5;
            if(!context.level.isClientSide){
                if(context.player!=null){
                    if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                        MagicLightUtils.spawnMagicLight(context.level,pos);
                    }
                }
            }
        });
        executors.put("传送",context -> {
            if(context.isTop(Vec3.class)){
                Vec3 vec3=context.pop(Vec3.class);
                Entity entity=context.pop(Entity.class);
                tp(entity,vec3, context.player,context.level,context);
                //实体压回
                context.push(entity);
            } else if (context.isTop(Entity.class)) {
                Entity entity=context.pop(Entity.class);
                if(context.isTop(Vec3.class)){
                    Vec3 vec3=context.pop(Vec3.class);
                    tp(entity,vec3, context.player,context.level,context);
                    //实体压回
                    context.push(entity);
                } else if (context.isTop(Double.class)) {
                    double distance=context.pop(Double.class);
                    // 计算位移向量（沿实体视线方向）
                    Vec3 lookVec = entity.getLookAngle();
                    Vec3 displacement = lookVec.scale(distance);
                    Vec3 vec3 = entity.position().add(displacement);
                    tp(entity,vec3, context.player,context.level,context);
                    //实体压回
                    context.push(entity);
                }
            } else if (context.isTop(Double.class)) {
                double distance=context.pop(Double.class);
                Entity entity=context.pop(Entity.class);
                // 计算位移向量（沿实体视线方向）
                Vec3 lookVec = entity.getLookAngle();
                Vec3 displacement = lookVec.scale(distance);
                Vec3 vec3 = entity.position().add(displacement);
                tp(entity,vec3, context.player,context.level,context);
                //实体压回
                context.push(entity);
            }
        });
        executors.put("生长",context -> {
            if(context.isTop(Vec3.class)){
                Vec3 centerPos=context.pop(Vec3.class);
                double radius=context.pop(Double.class);
                grow(centerPos,radius, context.player,context.level,context);
            } else if (context.isTop(Double.class)) {
                double radius=context.pop(Double.class);
                Vec3 centerPos=context.pop(Vec3.class);
                grow(centerPos,radius, context.player,context.level,context);
            }
        });

        executors.put("治疗",context -> {
            if(context.isTop(Double.class)){
                double healCount=context.pop(Double.class);
                Entity entity=context.pop(Entity.class);
                health(healCount,entity, context.player ,context.level,context);
                context.push(entity);
            } else if (context.isTop(Entity.class)) {
                Entity entity=context.pop(Entity.class);
                double healCount=context.pop(Double.class);
                health(healCount,entity, context.player ,context.level,context);
                context.push(entity);
            }
        });

        executors.put("缓降",context -> {
            if(context.isTop(Double.class)){
                double time=context.pop(Double.class);
                Entity entity=context.pop(Entity.class);
                slowDown(time,entity, context.player,context.level,context);
                context.push(entity);
            } else if (context.isTop(Entity.class)) {
                Entity entity=context.pop(Entity.class);
                double time=context.pop(Double.class);
                slowDown(time,entity, context.player,context.level,context);
                context.push(entity);
            }
        });
        executors.put("泥土",context -> {
            if(!context.level.isClientSide){
                if(context.player!=null){
                    double manaCost=10;
                    Vec3 pos=context.pop(Vec3.class);
                    BlockPos blockPos=BlockPos.containing(pos);//安全转换
                    BlockState state = context.level.getBlockState(blockPos);
                    if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                        if((state.isAir()||state.canBeReplaced())){
                            BlockState Dirt = Blocks.DIRT.defaultBlockState();
                            context.level.setBlock(blockPos, Dirt, 3);
                            if(context.level instanceof ServerLevel){
                                ((ServerLevel) context.level).sendParticles(ParticleTypes.INSTANT_EFFECT,blockPos.getX()+0.5,
                                        blockPos.getY()+0.5,blockPos.getZ()+0.5,15,0,0,0,0.03);
                            }
                        }
                    }
                }
            }
        });
        executors.put("细雪",context -> {
            if(!context.level.isClientSide){
                if(context.player!=null){
                    double manaCost=20;
                    Vec3 pos=context.pop(Vec3.class);
                    BlockPos blockPos=BlockPos.containing(pos);//安全转换
                    BlockState state = context.level.getBlockState(blockPos);
                    if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                        if((state.isAir()||state.canBeReplaced())){
                            BlockState snow = Blocks.POWDER_SNOW.defaultBlockState();
                            context.level.setBlock(blockPos, snow, 3);
                            if(context.level instanceof ServerLevel){
                                ((ServerLevel) context.level).sendParticles(ParticleTypes.INSTANT_EFFECT,blockPos.getX()+0.5,
                                        blockPos.getY()+0.5,blockPos.getZ()+0.5,15,0,0,0,0.03);
                            }
                        }
                    }
                }
            }
        });
        executors.put("沙",context -> {
            if(!context.level.isClientSide){
                if(context.player!=null){
                    double manaCost=10;
                    Vec3 pos=context.pop(Vec3.class);
                    BlockPos blockPos=BlockPos.containing(pos);//安全转换
                    BlockState state = context.level.getBlockState(blockPos);
                    if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                        if((state.isAir()||state.canBeReplaced())){
                            BlockState dripstone = Blocks.SAND.defaultBlockState();
                            context.level.setBlock(blockPos, dripstone, 3);
                            if(context.level instanceof ServerLevel){
                                ((ServerLevel) context.level).sendParticles(ParticleTypes.INSTANT_EFFECT,blockPos.getX()+0.5,
                                        blockPos.getY()+0.5,blockPos.getZ()+0.5,15,0,0,0,0.03);
                            }
                        }
                    }
                }
            }
        });
        executors.put("岩浆",context -> {
            if(!context.level.isClientSide){
                if(context.player!=null){
                    double manaCost=10;
                    Vec3 vec3=context.pop(Vec3.class);
                    //vec3安全转换为BlockPos
                    BlockPos pos=BlockPos.containing(vec3.x,vec3.y,vec3.z);
                    BlockState lavaState = Blocks.LAVA.defaultBlockState();
                    BlockState blockState =context.level.getBlockState(pos);;
                    if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                        if ((blockState.isAir() || blockState.canBeReplaced())) {
                            context.level.setBlock(pos,lavaState,Block.UPDATE_ALL);
                            if(context.level instanceof ServerLevel){
                                ((ServerLevel) context.level).sendParticles(ParticleTypes.INSTANT_EFFECT,pos.getX()+0.5,
                                        pos.getY()+0.5,pos.getZ()+0.5,15,0,0,0,0.03);
                            }
                        }
                    }
                }
            }
        });
        executors.put("魔法飞弹",context -> {
            if(context.isTop(RuneRegister.class)){
                RuneRegister runeRegister=context.pop(RuneRegister.class);
                Vec3 vec3=context.pop(Vec3.class);
                if(shouldManaBall(vec3,runeRegister, context.player,context.level,context)){
                    ManaBall manaBall=new ManaBall(runeRegister,context.player, ModEntities.MANA_BALL.get(),context.level);
                    manaBall.setPos(vec3);
                    context.level.addFreshEntity(manaBall);
                    context.push(manaBall);
                }
            } else if (context.isTop(Vec3.class)) {
                Vec3 vec3=context.pop(Vec3.class);
                RuneRegister runeRegister=context.pop(RuneRegister.class);
                if(shouldManaBall(vec3,runeRegister, context.player,context.level,context)){
                    ManaBall manaBall=new ManaBall(runeRegister,context.player, ModEntities.MANA_BALL.get(),context.level);
                    manaBall.setPos(vec3);
                    context.level.addFreshEntity(manaBall);
                    context.push(manaBall);
                }
            }
        });
        //元素常量
        executors.put("火元素", context -> context.push(ElementType.FIRE));

        executors.put("水元素", context -> context.push(ElementType.WATER));
        executors.put("雷元素", context -> context.push(ElementType.LIGHTING));
        executors.put("冰元素", context -> context.push(ElementType.ICE));
        executors.put("风元素", context -> context.push(ElementType.WIND));
        //这个元素暂且搁置吧
        executors.put("岩元素", context -> context.push(ElementType.STONE));

        executors.put("末影元素", context -> context.push(ElementType.ENDER));
        //
        executors.put("元素伤害",context -> {
            if(context.isTop(ElementType.class)){
                ElementType type=context.pop(ElementType.class);
                if(context.isTop(Double.class)){
                    double attackLevel=context.pop(Double.class);
                    Entity entity=context.pop(Entity.class);
                    if(entity instanceof LivingEntity livingEntity){
                        elementAttack(type,attackLevel,livingEntity, context.player,context.level,context);
                    }
                    //实体压回栈
                    context.push(entity);
                } else if (context.isTop(Entity.class)) {
                    Entity entity=context.pop(Entity.class);
                    double attackLevel=context.pop(Double.class);
                    if(entity instanceof LivingEntity livingEntity){
                        elementAttack(type,attackLevel,livingEntity, context.player,context.level,context);
                    }
                    //实体压回栈
                    context.push(entity);
                }
            }else if(context.isTop(Double.class)){
                double attackLevel=context.pop(Double.class);
                if(context.isTop(ElementType.class)){
                    ElementType type=context.pop(ElementType.class);
                    Entity entity=context.pop(Entity.class);
                    if(entity instanceof LivingEntity livingEntity){
                        elementAttack(type,attackLevel,livingEntity, context.player,context.level,context);
                    }
                    //实体压回栈
                    context.push(entity);
                } else if (context.isTop(Entity.class)) {
                    Entity entity=context.pop(Entity.class);
                    ElementType type=context.pop(ElementType.class);
                    if(entity instanceof LivingEntity livingEntity){
                        elementAttack(type,attackLevel,livingEntity, context.player,context.level,context);
                    }
                    //实体压回栈
                    context.push(entity);
                }
            } else if (context.isTop(Entity.class)) {
                Entity entity=context.pop(Entity.class);
                if(entity instanceof LivingEntity livingEntity){
                    if(context.isTop(ElementType.class)){
                        ElementType type=context.pop(ElementType.class);
                        double attackLevel=context.pop(Double.class);
                        elementAttack(type,attackLevel,livingEntity, context.player,context.level,context);
                        context.push(entity);
                    } else if (context.isTop(Double.class)) {
                        double attackLevel=context.pop(Double.class);
                        ElementType type=context.pop(ElementType.class);
                        elementAttack(type,attackLevel,livingEntity, context.player,context.level,context);
                        context.push(entity);
                    }
                }
            }
        });
        executors.put("幻化之剑",context -> {
            RuneRegister runeRegister=context.pop(RuneRegister.class);
            double manaCost=25;
            if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                ItemStack manaSword=Util.summonManaSword(runeRegister);
                if (!context.player.addItem(manaSword)) {
                    context.player.drop(manaSword, false);
                }
            }
        });
        executors.put("幻化之弓", context -> {
            RuneRegister runeRegister = context.pop(RuneRegister.class);
            double manaCost=25;
            if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)) {
                ItemStack manaBow = Util.summonManaBow(runeRegister);
                if (!context.player.addItem(manaBow)) {
                    context.player.drop(manaBow, false);
                }
            }
        });

        executors.put("爆炸",context -> {
            if(context.isTop(Vec3.class)){
                Vec3 vec3=context.pop(Vec3.class);
                if(context.isTop(Double.class)){
                   double radius= context.pop(Double.class);
                   boom(vec3,radius, context.player,context.player.level(),context);
                }
            } else if (context.isTop(Double.class)) {
                double radius= context.pop(Double.class);
                if(context.isTop(Vec3.class)){
                    Vec3 vec3=context.pop(Vec3.class);
                    boom(vec3,radius, context.player,context.player.level(),context);
                }
            }
        });



        executors.put("方块射线", context -> {
            double distance = context.pop(Double.class);
            if (!context.level.isClientSide) {
                Player player = context.player;
                if (player != null) {
                    double manaCost = Math.pow(distance, 1.5);
                    if (ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)) {
                        // 使用原版 pick，精确获取方块命中
                        BlockHitResult hitResult = (BlockHitResult) player.pick(distance, 0.0F, false);
                        Vec3 eyePos = player.getEyePosition();
                        if (hitResult.getType() != HitResult.Type.MISS) {
                            Vec3 hitCenter = Vec3.atCenterOf(hitResult.getBlockPos());
                            ModMessage.sendToClients(new RayS2CPacket(eyePos, hitCenter, 0xE9FAFF));
                            context.push(hitCenter);
                            Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/方块射线]压入{}", hitCenter);
                        } else {
                            // 未命中，特效画满整条射线
                            Vec3 endPos = eyePos.add(player.getViewVector(1.0F).scale(distance));
                            ModMessage.sendToClients(new RayS2CPacket(eyePos, endPos, 0xE9FAFF));
                            context.push(null);
                        }
                    }
                }
            }
        });


        executors.put("实体射线", context -> {
            double distance = context.pop(Double.class);

            if (!context.level.isClientSide) {
                Player player = context.player;
                if (player != null) {
                    double manaCost = Math.pow(distance, 1.5);
                    if (ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)) {
                        Vec3 eyePos = player.getEyePosition();
                        Vec3 viewVec = player.getViewVector(1.0F);

                        BlockHitResult blockHit = (BlockHitResult) player.pick(distance, 0.0F, false);
                        Vec3 endPos = (blockHit.getType() != HitResult.Type.MISS)
                                ? blockHit.getLocation()
                                : eyePos.add(viewVec.scale(distance));

                        //List<Entity> hitEntities = new ArrayList<>();
                        /*AABB rayAABB = new AABB(eyePos, endPos).inflate(1.0); // 扩大范围避免漏检
                        for (Entity entity : context.level.getEntities(player, rayAABB, e -> e.isPickable() && !e.isSpectator())) {
                            Optional<Vec3> hitPoint = entity.getBoundingBox().inflate(0.2).clip(eyePos, endPos);
                            if (hitPoint.isPresent()) {
                                hitEntities.add(entity);
                            }
                        }

                        hitEntities.sort(Comparator.comparingDouble(e ->
                               // e.getBoundingBox().clip(eyePos, endPos).get().distanceToSqr(eyePos) ));
                                /// 2026.5.21修复空值崩溃问题
                            e.getBoundingBox().clip(eyePos, endPos).orElse(new Vec3(10000,10000,10000)).distanceToSqr(eyePos)
                        ));

                        if(!hitEntities.isEmpty()){
                            context.push(hitEntities);
                        }

                         */
                        /// 2026.5.21修复空值崩溃问题
                        List<org.apache.commons.lang3.tuple.Pair<Entity, Vec3>> hitPairs = new ArrayList<>();
                        AABB rayAABB = new AABB(eyePos, endPos).inflate(1.0);
                        for (Entity entity : context.level.getEntities(player, rayAABB, e -> e.isPickable() && !e.isSpectator())) {
                            entity.getBoundingBox().inflate(0.2).clip(eyePos, endPos)
                                    .ifPresent(hitPos -> hitPairs.add(Pair.of(entity, hitPos)));
                        }
                        hitPairs.sort(Comparator.comparingDouble(p -> p.getRight().distanceToSqr(eyePos)));

                        List<Entity> hitEntities = hitPairs.stream().map(Pair::getLeft).collect(Collectors.toList());
                        if (!hitEntities.isEmpty()) {
                            context.push(hitEntities);
                        }
                         ModMessage.sendToClients(new RayS2CPacket(eyePos, endPos, 0xE9FAFF));
                        Spellweaver.getLOGGER().debug("[实体射线] 命中{}个实体", hitEntities.size());
                    }
                }
            }
        });

        executors.put("点燃",context -> {
           Vec3 vec3=context.pop(Vec3.class);
           Level level=context.level;
           if(level!=null&&vec3!=null&&!level.isClientSide){
               BlockPos pos = BlockPos.containing(vec3).above();
               BlockState state = context.level.getBlockState(pos);
               if ((state.isAir()||state.canBeReplaced())) {
                  double manaCost=5;
                  if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost,context)){
                      level.playSound(context.player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                      BlockState blockstate = BaseFireBlock.getState(level, pos);
                      level.setBlock(pos, blockstate, 11);
                      level.gameEvent(context.player, GameEvent.BLOCK_PLACE, pos);
                  }
               }
           }
        });




        executors.put("充能",context -> {
            if(!context.level.isClientSide){
              Object source=context.pop(Object.class);
              double amount=context.pop(Double.class);
              Object target=context.pop(Object.class);
              if(source instanceof SlotReference slotRef){
                  ItemStack stack = slotRef.getItem();
                  if (stack.is(Items.AMETHYST_SHARD) || stack.is(Items.LAPIS_LAZULI)||stack.is(Items.AMETHYST_BLOCK)||stack.is(Items.LAPIS_BLOCK)) {
                      ItemStack extracted = slotRef.extract(1, false);
                      if (extracted.isEmpty()) {
                          return;
                      }
                      if(stack.is(Items.AMETHYST_SHARD) || stack.is(Items.LAPIS_LAZULI)){
                          amount =  Math.min(25, amount);
                      } else if (stack.is(Items.AMETHYST_BLOCK)) {
                          amount =  Math.min(100, amount);
                      } else if (stack.is(Items.LAPIS_BLOCK)) {
                          amount =  Math.min(225, amount);
                      }
                  }
              }else {
                  //这里后续还能做抛出异常
                  amount=0;
              }
              if(target instanceof Player player)
                 ManaUtil.addManaAndSendPacket(amount, (ServerPlayer) player);
            }
        });
    }
    //这些方法传入的context仅用把于信息传递到魔力消耗方法
    public void drive(Vec3 velocity, Entity entity, Player player, Level level, SpellContext context){
        if(!level.isClientSide()){
            if(player!=null){


                double manaCost= Math.abs(2*0.5*((entity.getDeltaMovement().add(velocity))
                        .lengthSqr()-entity.getDeltaMovement().lengthSqr()));
                if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                    //获取实体当前速度
                    Vec3 currentVelocity = entity.getDeltaMovement();
                    //计算新速度（在原有基础上增加）
                    Vec3 newVelocity = currentVelocity.add(velocity);
                    //应用新速度
                    entity.setDeltaMovement(newVelocity);
                    // entity.setDeltaMovement(velocity);//直接设置速度,现在改为在原有速度上增加，更科学一些
                    Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/drive方法]为实体{}应用驱动",entity.getDisplayName());
                    entity.hurtMarked = true;
                    if(level instanceof ServerLevel){
                        ((ServerLevel)level).sendParticles(ParticleTypes.INSTANT_EFFECT,entity.getX(),entity.getY()+1,
                                entity.getZ(),20,0.2,0.3,0.2,0.05);
                    }
                }
            }
        }
    }
    public void lightningBolt(Vec3 vec3,double attack_level,Player player, Level level,SpellContext context){
        double manaCost=4*Math.pow(23+attack_level,1+attack_level/10);
        if(!level.isClientSide){
            if(player!=null&&attack_level>0){
                if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                    LightningBolt lightningBolt=new LightningBolt(EntityType.LIGHTNING_BOLT, level);
                    lightningBolt.setDamage(40*(float)attack_level);
                    lightningBolt.moveTo(vec3);
                    level.addFreshEntity(lightningBolt);
                }
            }
        }
    }
    public void tp(Entity entity,Vec3 targetPos,Player player,Level level,SpellContext context){
        Vec3 position=entity.position();//起点
        double x_sqrt=Math.pow(targetPos.x-position.x,2);
        double y_sqrt=Math.pow(targetPos.y-position.y,2);
        double z_sqrt=Math.pow(targetPos.z-position.z,2);
        int manaCost=(int)Math.sqrt(x_sqrt+y_sqrt+z_sqrt)<200?3*(int)Math.sqrt(x_sqrt+y_sqrt+z_sqrt):600;
        if(!level.isClientSide){
            if(player!=null){
                if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                    //起点和终点粒子特效
                    ModMessage.sendToClients(new TeleportParticleS2CPacket(position,targetPos));
                    //起点传送声音特效
                    level.playSound(null, entity.position().x(), entity.position().y(), entity.position().z(),
                            SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(),
                            3.0F, 1.0F);
                    entity.teleportTo(targetPos.x,targetPos.y,targetPos.z);//执行传送
                    //终点传送声音特效
                    level.playSound(null,targetPos.x,targetPos.y,targetPos.z,
                            SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(),
                            3.0F, 1.0F);

                }
            }
        }
    }
    public void grow(Vec3 centerPos,double radius,Player player,Level level,SpellContext context){
        double manaCost=  5*radius*radius;
        if(!level.isClientSide){
            if(player!=null){
                if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                    // 创建一个虚拟骨粉堆栈
                    ItemStack dummyBoneMealStack = new ItemStack(Items.BONE_MEAL);
                    // 获取虚拟玩家
                    Player fakePlayer = FakePlayerFactory.getMinecraft((ServerLevel) level);
                    BlockPos pos=BlockPos.containing(centerPos);
                    // 如果中心方块是耕地，将坐标上移 0.5，使取整后落到耕地上方的作物层
                    if (level.getBlockState(pos).is(Blocks.FARMLAND)) {
                        centerPos = centerPos.add(0, 0.5, 0);
                        pos = BlockPos.containing(centerPos);
                    }
                    // 遍历区域内的所有方块
                    BlockPos.betweenClosedStream(
                            new BlockPos(pos.getX()-(int)radius,pos.getY(),pos.getZ()-(int)radius),new BlockPos(pos.getX()+(int)radius,pos.getY(),pos.getZ()+(int)radius)
                    ).forEach(blockPos -> {
                        // 调用骨粉催熟逻辑
                        boolean success = BoneMealItem.applyBonemeal(dummyBoneMealStack, level, blockPos, fakePlayer);
                        if (success) {
                            level.levelEvent(1505, blockPos, 0); // 原版骨粉效果的事件ID
                        }
                    });
                    level.playSound(null, BlockPos.containing(centerPos), SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }
    public void health(double healCount,Entity entity,Player player,Level level,SpellContext context){
        //为什么是5魔力消耗？因为这是小巧思。众所周知一级生命提升是4点生命，一级魔力是20点，哎这不就对应了吗
        double manaCost=(5*healCount);
        if(!level.isClientSide){
            if(player!=null){
                if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                    ((LivingEntity) entity).heal((float)healCount);
                    if(level instanceof ServerLevel serverWorld){
                        int particleCount = 10;
                        double radius = 1.5;
                        double centerX = entity.getX();
                        double centerY = entity.getY() + 1;
                        double centerZ = entity.getZ();

                        for(int i = 0; i < particleCount; i++){
                            // 计算角度（弧度）
                            double angle = 2 * Math.PI * i / particleCount;
                            // 计算粒子在圆上的位置
                            double x = centerX + radius * Math.cos(angle);
                            double z = centerZ + radius * Math.sin(angle);
                            serverWorld.sendParticles(ParticleTypes.HEART,
                                    x, centerY, z,
                                    1,
                                    0, 0, 0,
                                    0.1 // 速度
                            );
                        }
                        serverWorld.sendParticles(ParticleTypes.HAPPY_VILLAGER,entity.getX(),
                                entity.getY(),entity.getZ(),40,1,1,1,0.2);
                    }
                }
            }
        }
    }
    public void slowDown(double time,Entity entity,Player player,Level level,SpellContext context){
        double manaCost=4+time;
        if(!level.isClientSide){
            if(player!=null){
                if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                    MobEffect effect = MobEffects.SLOW_FALLING;
                    int duration = (int) (20*time);
                    int amplifier = 0;
                    MobEffectInstance effectInstance = new MobEffectInstance(effect, duration, amplifier);
                    ((LivingEntity) entity).addEffect(effectInstance);
                }
            }
        }
    }
    public boolean shouldManaBall(Vec3 vec3,RuneRegister runeRegister,Player player,Level level,SpellContext context){
        double manaCost=runeRegister.getSpellList().size();
        if(!level.isClientSide) {
            if (player != null) {
                if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *由于增幅反应需要标记，逻辑上元素附着和元素反应判定先于直伤，因此应该在反应后消除无敌帧而不是在这里
     */
    public void elementAttack(ElementType type,double attackLevel,LivingEntity entity,Player player,Level level,SpellContext context){
        double manaCost=Math.pow(5+19+attackLevel,1+attackLevel/10);
        if(!level.isClientSide) {
            if (player != null&&attackLevel>0) {
                if (ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)) {
                    DamageSource magicSource = player.damageSources().indirectMagic(player, player); // 第一个参数是直接来源（魔法本身），第二个是责任实体
                    switch (type) {
                        case WATER -> {
                            Element.applyElement(entity,ElementType.WATER,200);
                            float damage= (float) (8*attackLevel);
                            if(entity.getPersistentData().contains("water_or_fire_attack_down")){
                                entity.getPersistentData().remove("water_or_fire_attack_down");
                                //entity.hurt(entity.damageSources().magic(),0.7f*damage);
                                entity.hurt(magicSource, 0.7f*damage);
                            }else {
                                //entity.hurt(entity.damageSources().magic(),damage);
                                entity.hurt(magicSource, damage);
                            }
                            if(entity.isOnFire()){
                                entity.extinguishFire();
                            }
                        }
                        case FIRE -> {
                            Element.applyElement(entity,ElementType.FIRE,200);
                            float damage= (float) (12*attackLevel);
                            if(entity.getPersistentData().contains("fire_attack_up")){
                                entity.getPersistentData().remove("fire_attack_up");
                                //entity.hurt(entity.damageSources().magic(),1.3f*damage);
                                entity.hurt(magicSource, 1.3f*damage);
                            }else if(entity.getPersistentData().contains("water_or_fire_attack_down")){
                                entity.getPersistentData().remove("water_or_fire_attack_down");
                                //entity.hurt(entity.damageSources().magic(),0.7f*damage);
                                entity.hurt(magicSource, 0.7f*damage);
                            } else if (entity.getPersistentData().contains("fire_or_ice_attack_down")) {
                                entity.getPersistentData().remove("fire_or_ice_attack_down");
                               // entity.hurt(entity.damageSources().magic(),0.7f*damage);
                                entity.hurt(magicSource, 0.7f*damage);
                            }else {
                                //entity.hurt(entity.damageSources().magic(),damage);
                                entity.hurt(magicSource, damage);
                            }
                        }
                        case LIGHTING -> {
                            Element.applyElement(entity,ElementType.LIGHTING,200);
                            float damage= (float) (10*attackLevel);
                            if(entity.getPersistentData().contains("lightning_attack_up")){
                                entity.getPersistentData().remove("lightning_attack_up");
                                //entity.hurt(entity.damageSources().magic(),1.5f*damage);
                                entity.hurt(magicSource, 1.5f*damage);
                            }else{
                                //entity.hurt(entity.damageSources().magic(),damage);
                                entity.hurt(magicSource, damage);
                            }
                        }
                        case ICE -> {
                            Element.applyElement(entity,ElementType.ICE,200);
                            float damage= (float) (12*attackLevel);
                            if(entity.getPersistentData().contains("fire_or_ice_attack_down")){
                                entity.getPersistentData().remove("fire_or_ice_attack_down");
                                //entity.hurt(entity.damageSources().magic(),0.7f*damage);
                                entity.hurt(magicSource, 0.7f*damage);
                            } else if (entity.getPersistentData().contains("ice_attack_up")) {
                                entity.getPersistentData().remove("ice_attack_up");
                                //entity.hurt(entity.damageSources().magic(),1.3f*damage);
                                entity.hurt(magicSource, 1.3f*damage);
                            }else {
                                //entity.hurt(entity.damageSources().magic(),damage);
                                entity.hurt(magicSource, damage);
                            }
                        }
                        case WIND -> {
                            Element.applyElement(entity,ElementType.WIND,100);
                            float damage= (float) (9*attackLevel);
                            //entity.hurt(entity.damageSources().magic(),damage);
                            entity.hurt(magicSource, damage);

                        }
                        /*case STONE -> {
                            Element.applyElement(entity,ElementType.STONE,300);
                            float damage= (float) (12*attackLevel);
                            //entity.hurt(entity.damageSources().magic(),damage);
                            entity.hurt(magicSource, damage);

                        }

                         */
                        case ENDER -> {
                            Element.applyElement(entity,ElementType.ENDER,200);
                            float damage= (float) (10*attackLevel);
                            //entity.hurt(entity.damageSources().magic(),damage);
                            Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/elementAttack]末影元素伤害前血量{}",entity.getHealth());
                            entity.hurt(magicSource, damage);
                            Spellweaver.getLOGGER().debug("[Spellweaver:SpellExecutorManager/elementAttack]末影元素伤害后血量{}",entity.getHealth());
                            //entity.hurt(entity.damageSources().fellOutOfWorld(),damage);
                        }
                    }
                    //entity.invulnerableTime = 0;
                    //entity.hurtTime = 0; //重置 hurt 动画计时
                }
            }
        }
    }
    public void boom(Vec3 vec3,double radius,Player player,Level level,SpellContext context){
        double manaCost=10*Math.pow(radius,1.5);
        if(!level.isClientSide){
            if(player!=null&&radius>0){
                if(ManaUtil.subManaAndAddExpAndSendPacket(manaCost, context)){
                    level.explode(player, vec3.x,vec3.y,vec3.z,
                            (float)radius, false, Level.ExplosionInteraction.TNT);
                }
            }
        }
    }
}
