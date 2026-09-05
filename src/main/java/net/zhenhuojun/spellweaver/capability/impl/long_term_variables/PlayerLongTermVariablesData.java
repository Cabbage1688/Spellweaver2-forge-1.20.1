package net.zhenhuojun.spellweaver.capability.impl.long_term_variables;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerLongTermVariablesData {
    private final Map<String, Object> persistentVariables = new HashMap<>();

    public Map<String, Object> getPersistentVariables() { return persistentVariables; }

    public CompoundTag saveNBT(CompoundTag tag) {
        CompoundTag varTag = new CompoundTag();
        for (Map.Entry<String, Object> entry : persistentVariables.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof EntityType<?> type) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("type", "entity_type");
                entryTag.putString("value",
                        Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(type)).toString());
                varTag.put(key, entryTag);
            } else if (value instanceof Vec3 vec) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("type", "vec3");
                entryTag.putDouble("x", vec.x);
                entryTag.putDouble("y", vec.y);
                entryTag.putDouble("z", vec.z);
                varTag.put(key, entryTag);
            }else if (value instanceof UUID uuid) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("type", "uuid");
                entryTag.putString("value", uuid.toString());
                varTag.put(key, entryTag);
            } else if (value instanceof Double d) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("type", "double");
                entryTag.putDouble("value", d);
                varTag.put(key, entryTag);
            }
        }
        tag.put("PersistentVariables", varTag);
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        persistentVariables.clear();
        CompoundTag varTag = tag.getCompound("PersistentVariables");
        for (String key : varTag.getAllKeys()) {
            CompoundTag entryTag = varTag.getCompound(key);
            String type = entryTag.getString("type");
            switch (type) {
                case "entity_type" -> {
                    String regName = entryTag.getString("value");
                    ResourceLocation rl = ResourceLocation.tryParse(regName);
                    if (rl != null && ForgeRegistries.ENTITY_TYPES.containsKey(rl)) {
                        persistentVariables.put(key,ForgeRegistries.ENTITY_TYPES.getValue(rl));
                    }
                }
                case "vec3" -> {
                    double x = entryTag.getDouble("x");
                    double y = entryTag.getDouble("y");
                    double z = entryTag.getDouble("z");
                    persistentVariables.put(key, new Vec3(x, y, z));
                }case "uuid" -> {
                    String uuidStr = entryTag.getString("value");
                    try {
                        persistentVariables.put(key, UUID.fromString(uuidStr));
                    } catch (IllegalArgumentException ignored) {
                        // 无效 UUID 则忽略该项
                    }
                }
                case "double" -> {
                    double val = entryTag.getDouble("value");
                    persistentVariables.put(key, val);
                }
            }
        }
    }

    public boolean storeVariable(String key, Object value) {
        boolean supported = value instanceof EntityType<?>
                || value instanceof Vec3
                || value instanceof UUID
                || value instanceof Double;
        if (supported) {
            persistentVariables.put(key, value);
            return true;
        }
        return false;
    }
    public CompoundTag serialize(){
        return saveNBT(new CompoundTag());
    }
    public void deserialize(CompoundTag tag){
        loadNBT(tag);
    }
}
