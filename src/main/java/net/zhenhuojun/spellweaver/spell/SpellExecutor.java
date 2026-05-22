package net.zhenhuojun.spellweaver.spell;

@FunctionalInterface
public interface SpellExecutor {
    void execute(SpellContext context) throws SpellExecutionException;
}

