package net.zhenhuojun.spellweaver.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.block.custom.InscriptionTableBlock;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaUtil;
import net.zhenhuojun.spellweaver.capability.provider.mana.*;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.item.util.SpellBlockStorage;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.*;
import net.zhenhuojun.spellweaver.spell.SpellTreeExecuteManager;
import net.zhenhuojun.spellweaver.spell.element.Element;
import net.zhenhuojun.spellweaver.spell.element.ElementType;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.zhenhuojun.spellweaver.spell.element.Element.applyElement;
import static net.zhenhuojun.spellweaver.spell.element.Element.getElementTypeFromNBT;
import static net.zhenhuojun.spellweaver.spell.util.RunesExecuteMethod.triggerSpell;


public class ModEvent {
    @Mod.EventBusSubscriber(modid = Spellweaver.MODID)
    public static class ForgeEvents{
        //用于缓存玩家数据
        private static final Map<UUID, CompoundTag> DIMENSION_TRANSFER_DATA = new HashMap<>();

        private static final String KEY = "spellweaver_has_book";
        private static final String MOON_KEY="spellweaver_gotten_moon_pearl";

        @SubscribeEvent//这byd事件会在玩家加入世界之前就执行，如果要进行玩家相关操作一定要记得空值检查
        public static void onServerTick(TickEvent.ServerTickEvent event){
            /// spellweaver的核心逻辑
              if(event.phase== TickEvent.Phase.END){
                  //运行法术执行表中的法术树
                  SpellTreeExecuteManager.getInstance().tick();
              }
        }
        /*@SubscribeEvent
        public static void manaRecoverTick(TickEvent.PlayerTickEvent event){
            if(event.phase==TickEvent.Phase.END&&event.side == LogicalSide.SERVER){
                event.player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana->{
                    if (mana.getMana() < mana.getMaxMana()  && event.player.getHealth() > 0) {
                        mana.addMana((double) (mana.getMana_level() + 4) /100);
                        ModMessage.sendToPlayer(new ManaChangeS2CPacket(mana.getMana(), mana.getMaxMana()
                                ,mana.getMana_level()), (ServerPlayer) event.player);
                    }
                });
            }
        }

         */

        @SubscribeEvent
        public static void manaRecoverTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
                event.player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
                    if (mana.getMana() < mana.getMaxMana() && event.player.getHealth() > 0) {
                        double baseRegen = mana.getMaxMana() / 2400.0;
                        double regen = baseRegen;
                        // 夜晚判断
                        if (event.player.level().isNight()) {
                            int moonPhase = event.player.level().getMoonPhase(); // 0~7
                            // 0=满月，4=新月
                            int distanceFromFull = Math.min(moonPhase, 8 - moonPhase);
                            // 倍率满月2.0，新月1.0
                            double rate = 2.0 - distanceFromFull / 4.0;
                            regen = baseRegen * rate;
                        }
                        mana.addMana(regen);
                        ModMessage.sendToPlayer(
                                new ManaChangeS2CPacket(mana.getMana(), mana.getMaxMana(), mana.getMana_level(),mana.getMana_exp(),mana.getPresent_exp()),
                                (ServerPlayer) event.player
                        );
                    }
                });
            }
        }
       @SubscribeEvent
        public static void shieldCostMana(TickEvent.PlayerTickEvent event){
            if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
                event.player.getCapability(ManaShieldProvider.MANA_SHIELD).ifPresent(manaShield -> {
                    if(!manaShield.isActive()) return;
                    double cost=manaShield.getShieldAmount()/400;
                    if(!ManaUtil.subManaAndAddExpAndSendPacket(cost, (ServerPlayer) event.player)){
                        manaShield.setActive(false);
                    }
                    ModMessage.sendToClients(new ManaShieldChangeS2CPacket(manaShield.isActive(),manaShield.getShieldAmount()));
                });
            }
        }

        @SubscribeEvent//玩家在绝境中会觉醒魔力能力
        public static void manaGetUpByHurt(LivingDamageEvent event){
            if(event.getEntity() instanceof ServerPlayer player){
                float preHealth=player.getHealth()-event.getAmount();
                if(preHealth<=4){
                    player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
                        if(mana.getMana_level()==0){
                            mana.setMana_level(1);
                            ModMessage.sendToPlayer(new ManaChangeS2CPacket(mana.getMana(), mana.getMaxMana()
                                    ,mana.getMana_level(),mana.getMana_exp(),mana.getPresent_exp()), player);
                            player.heal(10);
                            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0, false, true, true));
                            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 600, 0, false, true, true));
                            player.displayClientMessage(
                                    Component.literal("你感觉身体的深处涌出了一股暖流。").withStyle(ChatFormatting.LIGHT_PURPLE),
                                    true
                            );
                            // 播放图腾音效
                            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                    SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                            //ModMessage.sendToClients(new ManaBallEffectS2CPacket(player.getEyePosition(),0xE9FAFF));
                            ModMessage.sendToClients(new SpreadReactionS2CPacket(player.position(),0xE9FAFF));
                            //击退并伤害周围生物
                            AABB aabb = player.getBoundingBox().inflate(3.0);
                            for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, aabb, e -> e != player)) {
                                target.hurt(player.damageSources().magic(), 5.0f);
                            }
                        }
                    });
                }
            }
        }
        @SubscribeEvent
        public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
            if(!event.getLevel().isClientSide()) {
                if(event.getEntity() instanceof ServerPlayer player) {
                    //这个变量是给超载用的
                    AtomicInteger mana_level = new AtomicInteger();
                    //加载魔力数据
                    player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
                        mana_level.set(mana.getMana_level());
                        ModMessage.sendToPlayer(new ManaChangeS2CPacket(mana.getMana(), mana.getMaxMana()
                                ,mana.getMana_level(),mana.getMana_exp(),mana.getPresent_exp()), player);
                        Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPLayerJoinWorld]已发送魔力同步包，魔力等级{}，魔力值{}",mana.getMana_level(),mana.getMana());
                    });
                    player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(playerSpellStorage -> {
                        ModMessage.sendToPlayer(new SpellStorageSyncS2CPacket(playerSpellStorage.serialize()),player);
                        Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPLayerJoinWorld]已发送法术存储同步包，内容:{}",playerSpellStorage.serialize());
                    });
                    player.getCapability(PlayerLongTermVariablesProvider.PLAYER_LONG_TERM_VARIABLES).ifPresent(playerLongTermVariablesData -> {
                        ModMessage.sendToPlayer(new PlayerVariableS2CPacket(playerLongTermVariablesData.serialize()),player);
                    });
                    player.getCapability(PlayerManaOverloadProvider.PLAYER_MANA_OVERLOAD).ifPresent(playerManaOverload -> {

                        ModMessage.sendToPlayer(new OverloadDataS2CPacket(playerManaOverload.isEnabled(), playerManaOverload.getCurrentMultiplier(), playerManaOverload.getMaxMultiplier()),player);
                        Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPLayerJoinWorld]已发送法术魔力超载同步包，是否启用{}，" +
                                        "当前超载倍数{}，最大超载倍数{}",playerManaOverload.isEnabled(),
                                playerManaOverload.getCurrentMultiplier(), playerManaOverload.getMaxMultiplier());
                        playerManaOverload.updateMaxMultiplier(mana_level.get());
                       // Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPLayerJoinWorld]我没调用updateMaxMultiplier");
                    });

                    player.getCapability(ManaShieldProvider.MANA_SHIELD).ifPresent(manaShield -> {
                        ModMessage.sendToClients(new ManaShieldChangeS2CPacket(manaShield.isActive(),manaShield.getShieldAmount()));
                    });


                    ServerLevel level = player.serverLevel();
                    SpellBlockStorage storage = SpellBlockStorage.get(level);
                    ModMessage.sendToPlayer(new SpellBlockSyncS2CPacket(storage.getSpellBlockPositions()), player);
                }
            }
        }

        //沟槽的，写从末地返回玩家实体重建这一块逻辑的mojang程序员的妈死了，我操它冯
        //傻逼玩意害我写日志查一晚上，草泥马个狗东西我日你先人
        /**致阅读此代码的人：
         * 玩家维度切换事件在玩家从末地返回主世界时，触发次数是不等的，我目前观察到的是1-4次，或许有时会更多
         * 如果采用简单的缓存机制，后续的事件可能会因为玩家能力已不可用而保存空数据，覆盖之前存储的有效数据。
         * 因此，这里使用 hasData 标志检查能力数据是否非空，只有至少一个能力有有效数据时才更新缓存，
         * 从而阻止空数据覆盖。
         */
        @SubscribeEvent
        public static void onPlayerTravelToDimension(EntityTravelToDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                CompoundTag data = new CompoundTag();

                AtomicBoolean hasData = new AtomicBoolean(false);

                player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
                    CompoundTag manaTag = new CompoundTag();
                    mana.saveNBTData(manaTag);
                    if(!manaTag.isEmpty()){
                        data.put("Mana", manaTag);
                        hasData.set(true);
                    }
                });

                player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(spell -> {
                    CompoundTag spellTag = spell.serialize();
                    if(!spellTag.isEmpty()){
                        data.put("SpellStorage", spellTag);
                        hasData.set(true);
                    }
                });

                player.getCapability(PlayerLongTermVariablesProvider.PLAYER_LONG_TERM_VARIABLES).ifPresent(playerLongTermVariablesData -> {
                    CompoundTag variableTag=playerLongTermVariablesData.serialize();
                    if(!variableTag.isEmpty()){
                        data.put("Variable",variableTag);
                        hasData.set(true);
                    }
                });

                player.getCapability(PlayerManaOverloadProvider.PLAYER_MANA_OVERLOAD).ifPresent(playerManaOverload -> {
                    CompoundTag overloadTag=playerManaOverload.serializeNBT();
                    if(!overloadTag.isEmpty()){
                        data.put("Overload",overloadTag);
                        hasData.set(true);
                    }
                });
                player.getCapability(ManaShieldProvider.MANA_SHIELD).ifPresent(manaShield -> {
                    CompoundTag shieldTag=manaShield.serialize();
                    if(!shieldTag.isEmpty()){
                        data.put("ManaShield",shieldTag);
                        hasData.set(true);
                    }
                });


                if (hasData.get()) {
                    DIMENSION_TRANSFER_DATA.put(player.getUUID(), data);
                    Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPlayerTravelToDimension]玩家数据已缓存");
                }else{
                    Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPlayerTravelToDimension]警告！数据异常，不予缓存！！！");
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerDie(LivingDeathEvent event){
            if (event.getEntity() instanceof ServerPlayer player) {
                CompoundTag data = new CompoundTag();
                AtomicBoolean hasData = new AtomicBoolean(false);

                player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
                    CompoundTag manaTag = new CompoundTag();
                    mana.saveNBTData(manaTag);
                    if(!manaTag.isEmpty()){
                        data.put("Mana", manaTag);
                        hasData.set(true);
                    }
                });

                player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(spell -> {
                    CompoundTag spellTag = spell.serialize();
                    if(!spellTag.isEmpty()){
                        data.put("SpellStorage", spellTag);
                        hasData.set(true);
                    }
                });
                player.getCapability(PlayerLongTermVariablesProvider.PLAYER_LONG_TERM_VARIABLES).ifPresent(playerLongTermVariablesData -> {
                    CompoundTag variableTag=playerLongTermVariablesData.serialize();
                    if(!variableTag.isEmpty()){
                        data.put("Variable",variableTag);
                        hasData.set(true);
                    }
                });

                player.getCapability(PlayerManaOverloadProvider.PLAYER_MANA_OVERLOAD).ifPresent(playerManaOverload -> {
                    CompoundTag overloadTag=playerManaOverload.serializeNBT();
                    if(!overloadTag.isEmpty()){
                        data.put("Overload",overloadTag);
                        hasData.set(true);
                    }
                });

                player.getCapability(ManaShieldProvider.MANA_SHIELD).ifPresent(manaShield -> {
                    CompoundTag shieldTag=manaShield.serialize();
                    if(!shieldTag.isEmpty()){
                        data.put("ManaShield",shieldTag);
                        hasData.set(true);
                    }
                });

                //DIMENSION_TRANSFER_DATA.put(player.getUUID(), data);
                //Spellweaver.getLOGGER().debug("玩家死亡，数据已缓存");

                if (hasData.get()) {
                    DIMENSION_TRANSFER_DATA.put(player.getUUID(), data);
                    Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPlayerDie]玩家死亡，数据已缓存");
                }else{
                    Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPlayerDie]警告！数据异常，不予缓存！！！");
                }
            }
        }
        @SubscribeEvent
        public static void onPlayerClone(PlayerEvent.Clone event) {
            Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPlayerClone]玩家克隆事件触发");
            Player original = event.getOriginal();
            Player player = event.getEntity();
            UUID uuid = original.getUUID();

            boolean copied = false;
            // 从缓存恢复
            if (!copied && DIMENSION_TRANSFER_DATA.containsKey(uuid)) {
                CompoundTag data = DIMENSION_TRANSFER_DATA.remove(uuid);
                if (data != null) {
                    if (data.contains("Mana")) {
                        player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(mana -> {
                            mana.loadNBTData(data.getCompound("Mana"));
                            Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPlayerClone]" +
                                    "尝试从缓存恢复魔力数据");
                            ModMessage.sendToPlayer(new ManaChangeS2CPacket(mana.getMana(), mana.getMaxMana()
                                    ,mana.getMana_level(),mana.getMana_exp(),mana.getPresent_exp()), (ServerPlayer) player);
                            Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPlayerClone]" +
                                    "已发送魔力同步包，魔力等级{}，魔力值{}",mana.getMana_level(),mana.getMana());
                        });
                    }
                    if (data.contains("SpellStorage")) {
                        player.getCapability(PlayerSpellStorageProvider.PLAYER_SPELL_STORAGE).ifPresent(spell -> {
                            spell.deserialize(data.getCompound("SpellStorage"));
                            Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPlayerClone]" +
                                    "尝试从缓存恢复法术数据");
                            ModMessage.sendToPlayer(new SpellStorageSyncS2CPacket(spell.serialize()), (ServerPlayer) player);
                            Spellweaver.getLOGGER().debug("" +
                                    "[Spellweaver:ModEvent/onPlayerClone]已发送法术存储同步包，内容:{}",spell.serialize());
                        });
                    }
                    if(data.contains("Variable")){
                        player.getCapability(PlayerLongTermVariablesProvider.PLAYER_LONG_TERM_VARIABLES).ifPresent(playerLongTermVariablesData -> {
                            playerLongTermVariablesData.deserialize(data.getCompound("Variable"));
                            ModMessage.sendToPlayer(new PlayerVariableS2CPacket(playerLongTermVariablesData.serialize()),(ServerPlayer) player);
                        });
                    }
                    if(data.contains("Overload")){
                        player.getCapability(PlayerManaOverloadProvider.PLAYER_MANA_OVERLOAD).ifPresent(playerManaOverload -> {
                            playerManaOverload.deserializeNBT(data.getCompound("Overload"));
                            ModMessage.sendToPlayer(new OverloadDataS2CPacket(playerManaOverload.isEnabled(),
                                    playerManaOverload.getCurrentMultiplier(),
                                    playerManaOverload.getMaxMultiplier()),(ServerPlayer) player);
                        });
                    }
                    if(data.contains("ManaShield")){
                        player.getCapability(ManaShieldProvider.MANA_SHIELD).ifPresent(manaShield -> {
                            manaShield.deserialize(data.getCompound("ManaShield"));
                            ModMessage.sendToPlayer(new ManaShieldChangeS2CPacket(manaShield.isActive(),manaShield.getShieldAmount()),(ServerPlayer)player);
                        });
                    }

                    ServerLevel newLevel = ((ServerPlayer) player).serverLevel();
                    SpellBlockStorage newStorage = SpellBlockStorage.get(newLevel);
                    ModMessage.sendToPlayer(new SpellBlockSyncS2CPacket(newStorage.getSpellBlockPositions()), (ServerPlayer) player);
                }
            }
        }

        // 3. 玩家登出时清理缓存
        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            DIMENSION_TRANSFER_DATA.remove(event.getEntity().getUUID());
            Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onPlayerLogout]数据缓存清理完成");
        }


        @SubscribeEvent//处理元素自然消退,以及环境元素附着
        public static void onLivingTick(LivingEvent.LivingTickEvent event) {
            LivingEntity entity = event.getEntity();

            // 水中水环境附着
            if (entity.tickCount % 5 == 0 && entity.isInWater()) {
                //Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onLivingTick]环境附着水元素");
                int duration = 200;      // 10秒
                applyElement(entity, ElementType.WATER, duration);
            }
            //雨天附着，雪天不给水附着
            if(entity.tickCount % 5 == 0&&entity.isInWaterOrRain()){
                //防止一手冗余计算
                if(entity.isInWater()) return;

                BlockPos pos = entity.blockPosition();
                Biome biome = entity.level().getBiome(pos).value();
                //雪天不附着水元素
                if (!biome.coldEnoughToSnow(pos)) {
                    int duration = 200;
                    applyElement(entity, ElementType.WATER, duration);
                }
            }
            // 下界火环境附着
            if (entity.tickCount % 5 == 0 && entity.level().dimension() == Level.NETHER) {
                //Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onLivingTick]环境附着火元素");
                int duration = 200;
                applyElement(entity, ElementType.FIRE, duration);

            }
            //燃烧火附着
            if(entity.tickCount % 5 == 0 &&entity.isOnFire()){
                int duration = 200;
                applyElement(entity, ElementType.FIRE, duration);
            }
            // 冰环境附着（寒冷生物群系且实体不在水中，防止冰湖里的🐟冻上生成太多冰块实体,且能看到天空不给地穴里的附着）
            if (entity.tickCount % 5 == 0&&!entity.isInWater()&&entity.level().canSeeSky(entity.blockPosition().above())) {
                BlockPos pos = entity.blockPosition();
                Biome biome = entity.level().getBiome(pos).value();
                // 判断该位置是否寒冷到可以下雪
                if (biome.coldEnoughToSnow(pos)) {
                    //Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onLivingTick]环境附着冰元素");
                    int duration = 200;
                    applyElement(entity, ElementType.ICE, duration);
                }
            }

            CompoundTag persistentData = entity.getPersistentData();
            if (!persistentData.contains("spellweaver:element_stack")) return;

            ListTag elementStack = persistentData.getList("spellweaver:element_stack", Tag.TAG_COMPOUND);
            boolean changed = false;
            // 遍历所有条目，减少剩余刻数
            for (int i = elementStack.size() - 1; i >= 0; i--) {
                CompoundTag entry = elementStack.getCompound(i);
                int remaining = entry.getInt("remainingTicks") - 1;
                if (remaining <= 0) {
                    elementStack.remove(i);
                    changed = true;
                } else {
                    entry.putInt("remainingTicks", remaining);
                    changed = true;
                }
            }

            if (changed) {
                if (elementStack.isEmpty()) {
                    persistentData.remove("spellweaver:element_stack");
                } else {
                    persistentData.put("spellweaver:element_stack", elementStack);
                }
            }


            //元素附着粒子
            if (entity.tickCount % 5 == 0 && persistentData.contains("spellweaver:element_stack")) {
                ListTag stack = persistentData.getList("spellweaver:element_stack", Tag.TAG_COMPOUND);
                if (!stack.isEmpty()) {
                   // Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onLivingTick]生成元素粒子");
                    CompoundTag top = stack.getCompound(stack.size() - 1);
                    ElementType type = getElementTypeFromNBT(top);
                    ParticleOptions particle = getParticleForType(type);
                    if(particle!=null){
                        spawnSurroundingParticles(entity, particle);
                    }
                }
            }
            /*if (persistentData.contains("FrozenUntil")) {
                long frozenUntil = persistentData.getLong("FrozenUntil");
                if (entity.level().getGameTime() < frozenUntil) {
                    double fx = persistentData.getDouble("FrozenX");
                    double fy = persistentData.getDouble("FrozenY");
                    double fz = persistentData.getDouble("FrozenZ");
                    if (!entity.level().isClientSide) {
                        if (entity.onGround() || entity.isInWater()) {
                            entity.teleportTo(fx, fy, fz);
                            entity.setDeltaMovement(0, 0, 0);
                        } else {
                            if (entity.isNoGravity()) {
                                entity.setNoGravity(false);
                            }
                            entity.setPos(fx, entity.getY(), fz);
                            Vec3 motion = entity.getDeltaMovement();
                            double newMotionY = motion.y - 0.08;
                            entity.setDeltaMovement(0, newMotionY, 0);
                            entity.move(MoverType.SELF, entity.getDeltaMovement());
                        }
                    }
                    } else {
                    // 冻结时间结束，清理冻结状态
                    if (entity instanceof Mob mob) {
                        mob.setNoAi(false);
                    }
                    if (entity instanceof FlyingMob) {
                        entity.setNoGravity(true);
                    } else {
                        entity.setNoGravity(false);
                    }
                    persistentData.remove("FrozenX");
                    persistentData.remove("FrozenY");
                    persistentData.remove("FrozenZ");
                    persistentData.remove("FrozenUntil");
                }
            }

             */
             if (persistentData.contains("FrozenUntil")) {
                long frozenUntil = persistentData.getLong("FrozenUntil");
                if (!(entity.level().getGameTime() < frozenUntil)) {
                    if(entity instanceof ServerPlayer player){
                        UUID FROZEN_UUID = UUID.nameUUIDFromBytes("spellweaver:frozen_speed".getBytes(StandardCharsets.UTF_8));
                        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                        if (speedAttr != null && speedAttr.getModifier(FROZEN_UUID) != null) {
                            speedAttr.removeModifier(FROZEN_UUID);

                        }
                    }
                    entity.removeEffect(MobEffects.JUMP);
                    if (entity instanceof Mob mob) {
                        mob.setNoAi(false);
                    }
                    if (entity instanceof FlyingMob) {
                        entity.setNoGravity(true);
                    } else {
                        entity.setNoGravity(false);
                    }
                    persistentData.remove("FrozenX");
                    persistentData.remove("FrozenY");
                    persistentData.remove("FrozenZ");
                    persistentData.remove("FrozenUntil");
                }else {
                    //游泳速度没修饰符只能这样了
                    if(entity instanceof  Player player){
                        if(player.isInWater()){
                            double fx = persistentData.getDouble("FrozenX");
                            double fy = persistentData.getDouble("FrozenY");
                            double fz = persistentData.getDouble("FrozenZ");
                            entity.teleportTo(fx, fy, fz);
                            entity.setDeltaMovement(0, 0, 0);
                        }
                    }else if(entity instanceof FlyingMob mob){
                        if(!(mob.onGround()||mob.isInWater())){
                            entity.addDeltaMovement(new Vec3(0,-0.05,0));
                            entity.move(MoverType.SELF, entity.getDeltaMovement());
                        }
                    }
                }
            }


            // 非冻结状态下，每 20 tick 减少 1 点 FrozenTime
            if (!persistentData.contains("FrozenUntil")) {
                int frozenTime = persistentData.getInt("FrozenTime");
                if (frozenTime > 0 && entity.level().getGameTime() % 20 == 0) {
                    persistentData.putInt("FrozenTime", frozenTime - 1);
                }
            }
            //清理环境造成的增幅反应标记
            Element.handleAmplificationMarkers(entity);
        }

        @SubscribeEvent//闪电的感电反应，一道存在时间较长的闪电好像会劈两到三下，不过一般是一下。这应该就是原版机制，跟我没关系
        public static void onLightningBoltHurt(LivingHurtEvent event){
            if(event.getSource().is(DamageTypeTags.IS_LIGHTNING)){
                Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onLightningBoltHurt]闪电击中");
               LivingEntity entity= event.getEntity();
               applyElement(entity,ElementType.LIGHTING,400);
                if(entity.getPersistentData().contains("lightning_attack_up")) {
                    entity.getPersistentData().remove("lightning_attack_up");
                    float amount= event.getAmount();
                    event.setAmount(1.5f*amount);
                    //真伤
                    float realHurt=0.1f*amount;
                    float health=entity.getHealth()-realHurt;
                    if(health>0){
                        entity.setHealth(health);
                    }else {
                        entity.die(event.getSource());
                    }
                }else {
                    float amount= event.getAmount();
                    float realHurt=0.1f*amount;
                    float health=entity.getHealth()-realHurt;
                    if(health>0){
                        entity.setHealth(health);
                    }else {
                        entity.die(event.getSource());
                    }
                }
            }
        }
        //TODO以后再说，我还要考虑考虑
        public static void onElementHurt(LivingHurtEvent event){
            DamageType type=event.getSource().type();
            //switch (type)
        }

        @SubscribeEvent
        public static void onLivingHurtByElement(LivingHurtEvent event) {
            // 只在服务端处理
            if (event.getEntity().level().isClientSide) return;
            LivingEntity entity = event.getEntity();
            DamageSource source = event.getSource();
            Entity attacker = source.getEntity();
            String msgId = source.getMsgId();
            Entity directAttacker = source.getDirectEntity();
            // 岩浆、热地板、恶魂/烈焰人火球，营火
            if ("lava".equals(msgId) || "hotFloor".equals(msgId) || "fireball".equals(msgId)
                    || "campfire".equals(msgId) || "inFire".equals(msgId)
                    || (attacker instanceof Blaze && "mob".equals(msgId)) ) {
                Element.applyElement(entity, ElementType.FIRE, 200);
            }
            //水元素
            if ( (attacker instanceof Guardian || attacker instanceof ElderGuardian)) {
                Element.applyElement(entity, ElementType.WATER, 200);
            }
            // 冰元素
            if ("freeze".equals(msgId)) {
                Element.applyElement(entity, ElementType.ICE, 200);
            }
            if (directAttacker instanceof AbstractArrow arrow && arrow.getOwner() instanceof Stray) {
                Element.applyElement(entity, ElementType.ICE, 200);
            }

            // 风元素
            if (directAttacker instanceof ShulkerBullet) {
                Element.applyElement(entity, ElementType.WIND, 150);
            }

            // 末影元素
            if (attacker instanceof EnderMan && "mob".equals(msgId)) {
                Element.applyElement(entity, ElementType.ENDER, 200);
            }
            if (attacker instanceof EnderDragon && "mob".equals(msgId)) {
                Element.applyElement(entity, ElementType.ENDER, 200);
            }
            if (attacker instanceof Endermite && "mob".equals(msgId)) {
                Element.applyElement(entity, ElementType.ENDER, 200);
            }

        }
        @SubscribeEvent
        public static void onLazuliBroken(BlockEvent.BreakEvent event){
            if (event.getPlayer().level().isClientSide) return;
            if(event.getPlayer().isCreative()) return;
            Block brokenBlock = event.getState().getBlock();
            BlockPos pos=event.getPos();
            if(brokenBlock.equals(Blocks.LAPIS_ORE)||brokenBlock.equals(Blocks.DEEPSLATE_LAPIS_ORE)){
                Level level=event.getPlayer().level();
                final float chance=0.005f;
                //2026.5.26更新，如果玩家没有获得过魔珠，则必定获取
                if(!event.getPlayer().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(MOON_KEY)){
                    event.getPlayer().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).putBoolean(MOON_KEY,true);
                    ItemEntity itemEntity=new ItemEntity( level,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            new ItemStack(ModItems.MANA_PEARL.get()));
                    level.addFreshEntity(itemEntity);
                    return;
                }
                if(level.random.nextFloat()<chance){
                    ItemEntity itemEntity=new ItemEntity( level,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            new ItemStack(ModItems.MANA_PEARL.get()));
                    level.addFreshEntity(itemEntity);
                }
                if(level.random.nextFloat()<chance*10){
                    ItemEntity itemEntity=new ItemEntity( level,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            new ItemStack(ModItems.DIM_MANA_PEARL.get()));
                    level.addFreshEntity(itemEntity);
                }
            }
        }
        /// 为什么跳跃事件不可取消！！！！！！！！！！我日内瓦！！！！
        /*@SubscribeEvent
        public static void StopJumpWhenFrozen(LivingEvent.LivingJumpEvent event){
            LivingEntity entity=event.getEntity();
            if(entity.getPersistentData().contains("FrozenUntil")){
                entity.setDeltaMovement(entity.getDeltaMovement().x, 0, entity.getDeltaMovement().z);
            }
        }

         */





        private static ParticleOptions getParticleForType(ElementType type) {
            return switch (type) {
                case WATER -> ParticleTypes.FALLING_WATER;
                case FIRE -> ParticleTypes.FLAME;
                case STONE -> ParticleTypes.MYCELIUM;
                case ICE -> ParticleTypes.SNOWFLAKE;
                case WIND -> ParticleTypes.CLOUD;
                case LIGHTING -> ParticleTypes.ELECTRIC_SPARK;
                case ENDER -> ParticleTypes.PORTAL;
                case NULL -> null;
            };
        }


        private static void spawnSurroundingParticles(LivingEntity entity, ParticleOptions particle) {
            //性能优化，水中不生成粒子，避免被数量众多的鱼类卡爆
            if(!entity.isInWater()){
               // Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/spawnSurroundingParticles]方法被调用");
                Level level = entity.level();
                double radius = entity.getBbWidth() * 0.8 + 0.5-0.25;
                double height = entity.getBbHeight();
                double yBase = entity.getY() + height * 0.4; // 稍微偏下，让粒子环绕身体中下部
                int count = (int)(radius * 10);

                for (int i = 0; i < count; i++) {
                    double angle = 2 * Math.PI * i / count + (entity.tickCount * 0.02); // 缓慢旋转
                    double x = entity.getX() + radius * Math.cos(angle);
                    double z = entity.getZ() + radius * Math.sin(angle);
                    double y = yBase + (Math.sin(angle * 3) * height * 0.25); // 上下波动

                    //level.addParticle(particle, x, y, z, 0, 0, 0);
                    if(level instanceof ServerLevel serverLevel){
                        serverLevel.sendParticles(particle, x, y, z, 1,0, 0, 0,0);
                    }
                }
            }
        }
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            // 只在 tick 阶段结束时处理，避免重复
            if (event.phase != TickEvent.Phase.END) return;
            Player player = event.player;
            if (player.level().isClientSide) return;
            // 统计背包中幻化之剑的数量
            int count = countManaSwords(player);
            if (count > 0) {
                // 每 tick 扣除 0.05 * 数量 的魔力
                float cost = 0.05f * count;
                deductMana(player, cost);
            }
            // 统计幻化之弓数量
            int bowCount = countManaBows(player);
            if (bowCount > 0) {
                // 每 tick 扣除 (0.05 * 数量) 魔力
                float cost = 0.05f * bowCount;
                deductManaForBows(player, cost);
            }

        }


        @SubscribeEvent
        public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            // 客户端直接跳过
            if (event.getEntity().level().isClientSide) return;
            // Patchouli 未安装则不执行
            if (!ModList.get().isLoaded("patchouli")) return;
            var player = event.getEntity();
            // 玩家永久 NBT
            var root = player.getPersistentData();
            var persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
            // 已经给过书则直接返回
            if (persisted.getBoolean(KEY)) return;
            // 通过反射安全获取 Patchouli 手册
            ItemStack book = getPatchouliBookSafely(
                   ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID, "magician_books")
            );
            if (book.isEmpty()) return;
            // 优先放入背包，失败则掉落到地面
            if (!player.getInventory().add(book)) {
                player.drop(book, false);
            }
            // 写入标记，确保只发一次
            persisted.putBoolean(KEY, true);
            root.put(Player.PERSISTED_NBT_TAG, persisted);
        }

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event){
            CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
            // 注册 setmanalevel 命令
            dispatcher.register(
                    Commands.literal("setmanalevel")
                            .requires(source -> source.hasPermission(2))
                            // 分支1：指定目标玩家和等级
                            .then(Commands.argument("target", EntityArgument.players())
                                    .then(Commands.argument("level", IntegerArgumentType.integer(0, 32767))
                                            .executes(context -> {
                                                return setManaLevel(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "target"),
                                                        IntegerArgumentType.getInteger(context, "level")
                                                );
                                            })
                                    )
                            )
                            // 分支2：不指定目标，默认作用于自己
                            .then(Commands.argument("level", IntegerArgumentType.integer(0, 32767))
                                    .executes(context -> {
                                        CommandSourceStack source = context.getSource();
                                        // 检查执行者是否为玩家
                                        if (source.getEntity() instanceof ServerPlayer) {
                                            ServerPlayer player = (ServerPlayer) source.getEntity();
                                            return setManaLevel(
                                                    source,
                                                    Collections.singleton(player),
                                                    IntegerArgumentType.getInteger(context, "level")
                                            );
                                        } else {
                                            // 如果不是玩家（如命令方块），发送错误信息
                                            source.sendFailure(Component.literal("此命令只能由玩家执行"));
                                            return 0;
                                        }
                                    })
                            )
            );
        }
        @SubscribeEvent
        public  static void onShieldProtected(LivingHurtEvent event){
            if(!(event.getEntity() instanceof Player player)) return;
            player.getCapability(ManaShieldProvider.MANA_SHIELD).ifPresent(manaShield -> {
               if(manaShield.isActive()){
                   double shieldAmount=manaShield.getShieldAmount();
                   double hurt=event.getAmount();
                   if(shieldAmount>hurt){
                       event.setCanceled(true);
                       manaShield.setShieldAmount(shieldAmount-hurt);
                   }else if(shieldAmount==hurt){
                       event.setCanceled(true);
                       manaShield.setShieldAmount(0);
                       manaShield.setActive(false);
                   }else {
                       event.setAmount((float) (hurt-shieldAmount));
                       manaShield.setShieldAmount(0);
                       manaShield.setActive(false);
                   }
                   ModMessage.sendToPlayer(new ManaShieldChangeS2CPacket(manaShield.isActive(),manaShield.getShieldAmount()), (ServerPlayer) player);
               }
            });
        }
        //护甲法术
        @SubscribeEvent
        public static void onLivingWhoWearArmorWithSpellHurt(LivingHurtEvent event) {
            LivingEntity victim = event.getEntity();
            if (!(victim instanceof ServerPlayer player)) return;
            if (player.isSpectator()) return;

            Level level = player.level();
            if (level.isClientSide) return;

            // 遍历所有盔甲槽
            for (ItemStack armorStack : player.getArmorSlots()) {
                CompoundTag tag = armorStack.getOrCreateTag();
                CompoundTag spellData = tag.getCompound("SpellData");
                if (spellData.isEmpty()) continue;
                triggerSpell(player, level, spellData, context -> {
                    context.entity = event.getSource().getEntity();
                    //context.setData("damage_source", event.getSource());
                    if(event.getSource().getEntity()!=null){
                        context.push(event.getSource().getEntity());
                    }
                });
                // tag.remove("SpellData");
            }
        }
        //写入法术的物品被使用
        @SubscribeEvent
        public static void onItemWithSPellUse(PlayerInteractEvent.RightClickItem event) {
            Player player = event.getEntity();
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            if (player.isSpectator()) return;

            Level level = player.level();
            if (level.isClientSide) return;

            BlockHitResult lookingAt = (BlockHitResult) player.pick(4.5D, 0.0F, false);
            if (lookingAt.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = lookingAt.getBlockPos();
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof InscriptionTableBlock inscriptionTableBlock && player.isCrouching()) {
                    Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onItemWithSPellUse]正在与刻写台交互，跳过法术执行");
                    /// 这里手动调用use是因为，物品的使用好像会直接截断整个流程，对着方块摁不出来use方法的
                    /// 手持物品蹲下右键好像就是不能调用方块的use方法，我试了
                    inscriptionTableBlock.use(state,event.getLevel(),pos,player,InteractionHand.MAIN_HAND,lookingAt);
                    return; // 跳过法术执行
                }
            }

            ItemStack stack = event.getItemStack();
            //方块物品不再能直接触发法术
            if(stack.getItem() instanceof BlockItem) return;
            CompoundTag tag = stack.getOrCreateTag();
            CompoundTag spellData = tag.getCompound("SpellData");
            if (spellData.isEmpty()) return;

            triggerSpell(serverPlayer, level, spellData, context -> {
                context.push(player);
            });
            // tag.remove("SpellData");
        }

        /*@SubscribeEvent
        public static void onBlockWithSpellPlace(BlockEvent.EntityPlaceEvent event) {
            if(event.getEntity() instanceof ServerPlayer player){
                Level level = player.level();
                if (level.isClientSide) return;
                ItemStack stack = player.getItemInHand();
                CompoundTag tag = stack.getOrCreateTag();
                CompoundTag spellData = tag.getCompound("SpellData");
                if (spellData.isEmpty()) return;
                triggerSpell(player, level, spellData, context -> {
                });
                // tag.remove("SpellData");
            }
        }

         */
        @SubscribeEvent//法术方块物品放置时，将法术写入
        public static void onBlockWithSpellPlace(PlayerInteractEvent.RightClickBlock event) {
            Level level = event.getLevel();
            if (level.isClientSide) return;
            Player player = event.getEntity();
            InteractionHand hand = event.getHand();
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof BlockItem)) return;
            CompoundTag tag = stack.getOrCreateTag();
            CompoundTag spellData = tag.getCompound("SpellData");
            if (spellData.isEmpty()) return;
            // 计算新方块位置
            BlockPos clickedPos = event.getPos();
            BlockState clickedState = level.getBlockState(clickedPos);
            BlockPos newPos = clickedState.canBeReplaced()
                    ? clickedPos
                    : clickedPos.relative(event.getHitVec().getDirection());
            FeatherPenSpellC2SPacket.writeSpellToBlock((ServerLevel) level, newPos, spellData);
        }

        //方块法术
        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onBlockBreak]方块破坏事件触发？");
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            if (player.isSpectator()) return;

            ServerLevel level = (ServerLevel) event.getPlayer().level();
            BlockPos pos = event.getPos();
            Vec3 vec3=new Vec3(pos.getX(),pos.getY(),pos.getZ());

            SpellBlockStorage storage = SpellBlockStorage.get(level);
            CompoundTag spellData = storage.get(pos);
            Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onBlockBreak]法术是空的吗？");
            if (spellData == null) return;
            Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/onBlockBreak]法术不为空，即将执行");

            triggerSpell(player, level, spellData, context -> {
                context.push(vec3);
            });

            // 触发后，清除该坐标的绑定
            storage.remove(pos);
            ModMessage.sendToClientsInLevel(new SpellBlockSyncS2CPacket(storage.getSpellBlockPositions()), level);
        }


        //这个事件很神秘，活塞推动一次服务端客户端各触发两次
        /*@SubscribeEvent
        public static void onPistonMove(PistonEvent event) {
            if (event.getLevel().isClientSide()) return;
            Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/ForgeEvents/onPistonMove]活塞事件在服务端触发");
            ServerLevel level = (ServerLevel) event.getLevel();
            SpellBlockStorage storage = SpellBlockStorage.get(level);
            Direction dir = event.getDirection();
            //这个事件的getPos()给的居然是推动后的位置， Direction指向原坐标
            BlockPos pos=event.getPos().relative(dir);
            Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/ForgeEvents/onPistonMove]活塞事件触发位置{}",pos);
            CompoundTag spell = storage.get(pos);
            Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/ForgeEvents/onPistonMove]校验法术{}",spell);
            if(spell!=null){
                BlockPos newPos = event.getPos();
                storage.put(newPos, spell.copy());
                storage.remove(pos);
                Spellweaver.getLOGGER().debug("[Spellweaver:活塞推动] 法术从 {} 迁移到 {}", pos, newPos);
            }

        }
         */@SubscribeEvent
        public static void onPistonPre(PistonEvent.Pre event) {
            if (event.getLevel().isClientSide()) return;
            ServerLevel level = (ServerLevel) event.getLevel();
            SpellBlockStorage storage = SpellBlockStorage.get(level);
            PistonStructureResolver resolver = event.getStructureHelper();
            if (resolver == null || !resolver.resolve()) return;
            List<BlockPos> toPush = resolver.getToPush();
            if (toPush.isEmpty()) return;
            Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/ForgeEvents/onPistonPre]要推动{}个方块 ",toPush.size());
            int i=1;
            for (BlockPos pos : toPush) {
                Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/ForgeEvents/onPistonPre]正在检查第{}个方块 ",i++);
                CompoundTag spellTag = storage.get(pos);
                if (spellTag == null) continue;
                //Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/ForgeEvents/onPistonPre]第{}个方块法术非空，try一下 ",i++);
                //Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10.0, null);
                //if (player instanceof ServerPlayer serverPlayer) {
                    //triggerSpell(serverPlayer, level, spellTag, context -> {
                     //   context.push(pos);
                       // Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/ForgeEvents/onPistonPre]压入坐标{} ",pos);
                //});
                //} else {
                  //  Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/ForgeEvents/onPistonPre]周围没用玩家，没法施法");
               // }
                storage.remove(pos);
                ModMessage.sendToClientsInLevel(new SpellBlockSyncS2CPacket(storage.getSpellBlockPositions()), level);
                //level.destroyBlock(pos, true);
                //level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                      //  pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                       // 1, 0, 0, 0, 0);
                //Spellweaver.getLOGGER().debug("[Spellweaver:ModEvent/ForgeEvents/onPistonPre] 方块 {} 上的法术因活塞推动而崩碎执行", pos);
            }
        }//最后还是改成了直接破坏法术更省事


        //通过爆炸破坏带法术的方块
        @SubscribeEvent
        public static void onExplosionDetonate(net.minecraftforge.event.level.ExplosionEvent.Detonate event) {
            if (event.getLevel().isClientSide) return;

            ServerLevel level = (ServerLevel) event.getLevel();
            SpellBlockStorage storage = SpellBlockStorage.get(level);

            Entity exploder = event.getExplosion().getExploder();
            ServerPlayer player = null;
            if (exploder instanceof ServerPlayer) {
                player = (ServerPlayer) exploder;
            }
            if(player==null){
                if (exploder != null) {
                    player= (ServerPlayer) level.getNearestPlayer(exploder,64);
                }
            }

            for (BlockPos pos : event.getExplosion().getToBlow()) {
                CompoundTag spellData = storage.get(pos);
                if (spellData == null) continue;

                Vec3 vec3 = new Vec3(pos.getX(), pos.getY(), pos.getZ());
                if(player!=null){
                   triggerSpell(player, level, spellData, context -> {
                       context.push(vec3);
                   });
               }
                storage.remove(pos);
            }

            ModMessage.sendToClientsInLevel(new SpellBlockSyncS2CPacket(storage.getSpellBlockPositions()), level);
        }

        /**
         * 通过反射调用 Patchouli API 获取书本
         * 避免直接依赖 Patchouli
         * 任何异常直接返回 EMPTY，保证安全
         */
        private static ItemStack getPatchouliBookSafely(ResourceLocation bookId) {
            try {
                Class<?> apiClass = Class.forName("vazkii.patchouli.api.PatchouliAPI");
                Method getMethod = apiClass.getMethod("get");
                Object api = getMethod.invoke(null);

                Method getBookStackMethod =
                        api.getClass().getMethod("getBookStack", ResourceLocation.class);
                Object result = getBookStackMethod.invoke(api, bookId);

                return (result instanceof ItemStack stack) ? stack : ItemStack.EMPTY;
            } catch (Throwable t) {
                // 任何反射 / API 错误都直接吞掉，保证不会炸服
                return ItemStack.EMPTY;
            }
        }

        private static int setManaLevel(CommandSourceStack source, Collection<ServerPlayer> targets, int level) {
            for (ServerPlayer player : targets) {
                if (player != null) {
                    player.getCapability(PlayerManaProvider.PLAYER_MANA).ifPresent(playerMana -> {
                        playerMana.setMana_level(level);
                        ModMessage.sendToPlayer(new ManaChangeS2CPacket(playerMana.getMana(),playerMana.getMaxMana(),playerMana.getMana_level(),playerMana.getMana_exp(), playerMana.getPresent_exp()),player);
                        source.sendSuccess(() -> Component.literal("已设置玩家 " + player.getDisplayName().getString() + " 的魔力等级为 " + level), true);
                    });
                }
            }
            return targets.size();
        }

        private static int countManaSwords(Player player) {
            int count = 0;
            // 主物品栏（36个槽位，包括快捷栏）
            for (ItemStack stack : player.getInventory().items) {
                if (isManaSword(stack)) count++;
            }
            // 副手
            for (ItemStack stack : player.getInventory().offhand) {
                if (isManaSword(stack)) count++;
            }
            return count;
        }

        private static boolean isManaSword(ItemStack stack) {
            return stack.is(ModItems.MANA_SWORD.get());
        }

        private static void deductMana(Player player, float amount) {
            if (player.level().isClientSide) return;
            if(!ManaUtil.subManaAndAddExpAndSendPacket((double) amount, (ServerPlayer) player)){
                // 移除所有幻化之剑
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (isManaSword(stack)) {
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                    }
                }
            }
        }

        private static int countManaBows(Player player) {
            int count = 0;
            // 主物品栏
            for (ItemStack stack : player.getInventory().items) {
                if (isManaBow(stack)) count++;
            }
            // 副手
            for (ItemStack stack : player.getInventory().offhand) {
                if (isManaBow(stack)) count++;
            }
            return count;
        }

        private static boolean isManaBow(ItemStack stack) {
            return stack.is(ModItems.MANA_BOW.get());
        }

        private static void deductManaForBows(Player player, float amount) {
            if (player.level().isClientSide) return;
            if (!ManaUtil.subManaAndAddExpAndSendPacket((double) amount, (ServerPlayer) player)) {
                // 魔力不足，移除所有幻化之弓
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (isManaBow(stack)) {
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                    }
                }
            }
        }

    }

    private static void spawnTotemParticles(ServerLevel level, LivingEntity entity) {
        RandomSource random = level.random;
        for (int i = 0; i < 30; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 1.2;
            double offsetY = random.nextDouble() * 1.5;
            double offsetZ = (random.nextDouble() - 0.5) * 1.2;
            double vx = (random.nextDouble() - 0.5) * 0.1;
            double vy = random.nextDouble() * 0.2;
            double vz = (random.nextDouble() - 0.5) * 0.1;
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    entity.getX() + offsetX,
                    entity.getY() + offsetY,
                    entity.getZ() + offsetZ,
                    1,
                    vx, vy, vz,
                    0.05);
        }
    }
}
