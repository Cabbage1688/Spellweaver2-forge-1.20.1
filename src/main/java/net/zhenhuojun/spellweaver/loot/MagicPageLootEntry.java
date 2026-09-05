package net.zhenhuojun.spellweaver.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.zhenhuojun.spellweaver.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 自定义战利品条目：从法术池中随机挑选一个法术，生成带对应语言NBT的魔法书页。
 * 根据开启箱子的玩家客户端语言（zh_cn/zh_tw等返回中文，其余返回英文）设置name和note。
 */
public class MagicPageLootEntry extends LootPoolSingletonContainer {

    private final List<SpellDefinition> spells;

    public MagicPageLootEntry(List<SpellDefinition> spells, int weight, int quality,
                              LootItemCondition[] conditions, LootItemFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.spells = spells;
    }

    @Override
    public LootPoolEntryType getType() {
        return ModLootPoolEntries.MAGIC_PAGE.get();
    }

    @Override
    public void createItemStack(Consumer<ItemStack> stackConsumer, LootContext context) {
        if (spells.isEmpty()) return;

        // 从法术池中随机挑选一个
        SpellDefinition spell = spells.get(context.getRandom().nextInt(spells.size()));

        // 获取玩家客户端语言（通过 LanguageTracker 追踪）
        String language = "en_us";
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity instanceof ServerPlayer serverPlayer) {
            language = LanguageTracker.getLanguage(serverPlayer.getUUID());
        }

        // 创建物品并设置NBT
        ItemStack stack = new ItemStack(ModItems.MAGIC_PAGE.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("name", spell.getName(language));
        tag.putString("note", spell.getNote(language));
        tag.putString("spellContent", spell.getSpellContent());

        ListTag authorsTag = new ListTag();
        for (String author : spell.getAuthors()) {
            authorsTag.add(StringTag.valueOf(author));
        }
        tag.put("authors", authorsTag);

        stack.setTag(tag);
        stackConsumer.accept(stack);
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<MagicPageLootEntry> {

        @Override
        public void serializeCustom(JsonObject json, MagicPageLootEntry entry, JsonSerializationContext context) {
            JsonArray spellsArray = new JsonArray();
            for (SpellDefinition spell : entry.spells) {
                JsonObject spellJson = new JsonObject();
                spellJson.addProperty("name_zh", spell.getName("zh_cn"));
                spellJson.addProperty("name_en", spell.getName("en_us"));
                spellJson.addProperty("note_zh", spell.getNote("zh_cn"));
                spellJson.addProperty("note_en", spell.getNote("en_us"));
                spellJson.addProperty("spellContent", spell.getSpellContent());
                JsonArray authorsArray = new JsonArray();
                for (String author : spell.getAuthors()) {
                    authorsArray.add(author);
                }
                spellJson.add("authors", authorsArray);
                spellsArray.add(spellJson);
            }
            json.add("spells", spellsArray);
        }

        @Override
        public MagicPageLootEntry deserialize(JsonObject json, JsonDeserializationContext context,
                                               int weight, int quality, LootItemCondition[] conditions,
                                               LootItemFunction[] functions) {
            List<SpellDefinition> spells = new ArrayList<>();
            if (json.has("spells")) {
                for (JsonElement element : json.getAsJsonArray("spells")) {
                    spells.add(SpellDefinition.fromJson(element.getAsJsonObject()));
                }
            }
            return new MagicPageLootEntry(spells, weight, quality, conditions, functions);
        }
    }
}
