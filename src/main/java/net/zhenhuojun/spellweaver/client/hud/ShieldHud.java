package net.zhenhuojun.spellweaver.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaUtil;
import net.zhenhuojun.spellweaver.client.gui.util.ClientManaShieldData;

public class ShieldHud {

    public static final IGuiOverlay HUD_SHIELD = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        int x = screenWidth / 2;
        int y = screenHeight;
       if(ClientManaShieldData.isShieldActive()){
           Font font= Minecraft.getInstance().font;
           //MutableComponent text= net.minecraft.network.chat.Component.literal("护盾："+ ClientManaShieldData.getShieldAmount());
           MutableComponent text= net.minecraft.network.chat.Component.literal("护盾："+ ManaUtil.formatMana((long) ClientManaShieldData.getShieldAmount()));
           int color=0x477CFF;
           if(y>=250){
               guiGraphics.drawString(font,text,x+130,252-20,color);
           }else {
               guiGraphics.drawString(font,text,x+130,232-20,color);
           }
       }
    };
}
