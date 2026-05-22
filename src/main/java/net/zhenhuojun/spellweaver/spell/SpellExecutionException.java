package net.zhenhuojun.spellweaver.spell;

public class SpellExecutionException extends RuntimeException {
    public SpellExecutionException(String message) {
        super(message);
    }

    public SpellExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}