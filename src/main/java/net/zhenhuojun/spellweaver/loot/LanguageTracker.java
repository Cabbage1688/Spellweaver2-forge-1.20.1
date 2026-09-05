package net.zhenhuojun.spellweaver.loot;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端追踪玩家客户端语言代码。
 * 1.20.1 中 ServerPlayer 不直接暴露客户端语言，需通过自定义网络包同步。
 */
public class LanguageTracker {
    private static final Map<UUID, String> PLAYER_LANGUAGES = new ConcurrentHashMap<>();

    public static void setLanguage(UUID uuid, String language) {
        PLAYER_LANGUAGES.put(uuid, language);
    }

    public static String getLanguage(UUID uuid) {
        return PLAYER_LANGUAGES.getOrDefault(uuid, "en_us");
    }

    public static void removeLanguage(UUID uuid) {
        PLAYER_LANGUAGES.remove(uuid);
    }
}
