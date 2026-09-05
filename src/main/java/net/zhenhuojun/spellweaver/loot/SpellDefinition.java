package net.zhenhuojun.spellweaver.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 法术定义，存储中英文双语版本的名称、备注及法术数据。
 * 根据玩家客户端语言选择对应语言版本。
 */
public class SpellDefinition {
    private final String nameZh;
    private final String nameEn;
    private final String noteZh;
    private final String noteEn;
    private final String spellContent;
    private final List<String> authors;

    public SpellDefinition(String nameZh, String nameEn, String noteZh, String noteEn,
                           String spellContent, List<String> authors) {
        this.nameZh = nameZh;
        this.nameEn = nameEn;
        this.noteZh = noteZh;
        this.noteEn = noteEn;
        this.spellContent = spellContent;
        this.authors = authors;
    }

    /**
     * 根据语言代码返回对应语言的法术名称。
     * zh_cn / zh_tw / zh_hk 等以 zh 开头的语言代码返回中文版本，其余返回英文版本。
     */
    public String getName(String languageCode) {
        return isChinese(languageCode) ? nameZh : nameEn;
    }

    public String getNote(String languageCode) {
        return isChinese(languageCode) ? noteZh : noteEn;
    }

    public String getSpellContent() {
        return spellContent;
    }

    public List<String> getAuthors() {
        return authors;
    }

    private static boolean isChinese(String languageCode) {
        return languageCode != null && languageCode.startsWith("zh");
    }

    public static SpellDefinition fromJson(JsonObject json) {
        String nameZh = GsonHelper.getAsString(json, "name_zh");
        String nameEn = GsonHelper.getAsString(json, "name_en");
        String noteZh = GsonHelper.getAsString(json, "note_zh", "");
        String noteEn = GsonHelper.getAsString(json, "note_en", "");
        String spellContent = GsonHelper.getAsString(json, "spellContent");

        List<String> authors = new ArrayList<>();
        if (json.has("authors")) {
            for (JsonElement e : json.getAsJsonArray("authors")) {
                authors.add(e.getAsString());
            }
        }

        return new SpellDefinition(nameZh, nameEn, noteZh, noteEn, spellContent, authors);
    }
}
