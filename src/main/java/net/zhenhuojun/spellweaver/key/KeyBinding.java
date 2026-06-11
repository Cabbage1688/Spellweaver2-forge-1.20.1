package net.zhenhuojun.spellweaver.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final String KEY_CATEGORY_TEST="key.category.spellweaver.test";
    public static final String KEY_TEST = "key.spellweaver.test";

    public static final String KEY_CATEGORY_SPELL="key.category.spellweaver.spell";
    public static final String KEY_CAST_SPELL1="key.spellweaver.spell1";
    public static final String KEY_CAST_SPELL2="key.spellweaver.spell2";
    public static final String KEY_CAST_SPELL3="key.spellweaver.spell3";
    public static final String KEY_CAST_SPELL4="key.spellweaver.spell4";
    public static final String KEY_CAST_SPELL5="key.spellweaver.spell5";
    public static final String KEY_CAST_SPELL6="key.spellweaver.spell6";
    public static final String KEY_CAST_SPELL7="key.spellweaver.spell7";
    public static final String KEY_CAST_SPELL8="key.spellweaver.spell8";
    public static final String KEY_CAST_SPELL9="key.spellweaver.spell9";
    public static final String KEY_CAST_SPELL10="key.spellweaver.spell10";

    public static final String KEY_OVERLOAD="key.spellweaver.overload";
    public static final String KEY_OVERLOAD_UP="key.spellweaver.overload_up";
    public static final String KEY_OVERLOAD_DOWN="key.spellweaver.overload_down";

    public static final KeyMapping TEST_KEY=new KeyMapping(KEY_TEST, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O,KEY_CATEGORY_TEST);

    public static final KeyMapping SPELL_CAST_KEY1=new KeyMapping(KEY_CAST_SPELL1,KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_R,KEY_CATEGORY_SPELL);

    public static final KeyMapping SPELL_CAST_KEY2=new KeyMapping(KEY_CAST_SPELL2,KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_CAPS_LOCK,KEY_CATEGORY_SPELL);

    public static final KeyMapping SPELL_CAST_KEY3=new KeyMapping(KEY_CAST_SPELL3,KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_C,KEY_CATEGORY_SPELL);

    public static final KeyMapping SPELL_CAST_KEY4=new KeyMapping(KEY_CAST_SPELL4,KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_X,KEY_CATEGORY_SPELL);

    public static final KeyMapping SPELL_CAST_KEY5=new KeyMapping(KEY_CAST_SPELL5,KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_Z,KEY_CATEGORY_SPELL);

    public static final KeyMapping SPELL_CAST_KEY6=new KeyMapping(KEY_CAST_SPELL6,KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_G,KEY_CATEGORY_SPELL);

    public static final KeyMapping SPELL_CAST_KEY7=new KeyMapping(KEY_CAST_SPELL7,KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_V,KEY_CATEGORY_SPELL);

    public static final KeyMapping SPELL_CAST_KEY8=new KeyMapping(KEY_CAST_SPELL8,KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_Y,KEY_CATEGORY_SPELL);

    public static final KeyMapping SPELL_CAST_KEY9=new KeyMapping(KEY_CAST_SPELL9,KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_H,KEY_CATEGORY_SPELL);


    public static KeyMapping[] SPELL_KEYS={SPELL_CAST_KEY1,SPELL_CAST_KEY2,SPELL_CAST_KEY3,SPELL_CAST_KEY4,SPELL_CAST_KEY5
            ,SPELL_CAST_KEY6,SPELL_CAST_KEY7,SPELL_CAST_KEY8,SPELL_CAST_KEY9};

    public static final KeyMapping OVERLOAD_KEY=new KeyMapping(KEY_OVERLOAD,KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_M,KEY_CATEGORY_TEST);

    public static final KeyMapping OVERLOAD_UP_KEY=new KeyMapping(KEY_OVERLOAD_UP,KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_UP,KEY_CATEGORY_TEST);

    public static final KeyMapping OVERLOAD_DOWN_KEY=new KeyMapping(KEY_OVERLOAD_DOWN,KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_DOWN,KEY_CATEGORY_TEST);


}
