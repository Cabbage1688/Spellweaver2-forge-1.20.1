package net.zhenhuojun.spellweaver.client.data_util;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class ClientSpellBlockData {
    private static final Set<BlockPos> spellBlocks = new HashSet<>();

    public static void clear() {
        spellBlocks.clear();
    }

    public static void addBlock(BlockPos pos) {
        spellBlocks.add(pos);
    }

    public static void removeBlock(BlockPos pos) {
        spellBlocks.remove(pos);
    }

    public static void setBlocks(Set<BlockPos> blocks) {
        spellBlocks.clear();
        spellBlocks.addAll(blocks);
    }

    public static Set<BlockPos> getSpellBlocks() {
        return spellBlocks;
    }

    public static boolean hasSpell(BlockPos pos) {
        return spellBlocks.contains(pos);
    }
}