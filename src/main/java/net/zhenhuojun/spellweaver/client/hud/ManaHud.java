package net.zhenhuojun.spellweaver.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerManaData;

public class ManaHud {

    private static final ResourceLocation MANA=ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"textures/hud/mana2.png");
    private static final ResourceLocation MANA_BAR1=ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"textures/hud/mana_bar1.png");
    private static final ResourceLocation MANA_BAR2=ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"textures/hud/mana_bar2.png");
    private static final ResourceLocation MANA_TOP=ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"textures/hud/mana_top.png");

    public static final IGuiOverlay HUD_MANA = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        //有等级才显示魔力条
       if(ClientPlayerManaData.getManaLevel()>0){
           int x = screenWidth / 2;
           int y = screenHeight;
           //Spellweaver.getLOGGER().debug("[Spellweaver:ManaHud]hud已渲染");
           //这个y坐标就很傻逼，它tm是反直觉的，和平常的y轴是反过来的？！
           if(y>=250){
               guiGraphics.blit(MANA_BAR2,x+84  , 194 , 0, 0, 128, 128, 128, 128);
               for(int i = 0; i < 100; i++) {
                   if(ClientPlayerManaData.getPlayerMana() > i*0.01* ClientPlayerManaData.getMaxMana()) {
                       guiGraphics.blit(MANA, x + 91 +  i, 250,1, 0, 0, 16, 16, 16, 16);
                   }else{
                       break;
                   }
               }
               guiGraphics.blit(MANA_TOP,x+90,250,2,0,0,16,16,16,16);

               Font font= Minecraft.getInstance().font;
               MutableComponent text= net.minecraft.network.chat.Component.literal("魔力值："+(int)ClientPlayerManaData.getPlayerMana());
               //String.format("%f", ClientPlayerManaData.getPlayerMana())
               int color=0x477CFF;
               guiGraphics.drawString(font,text,x+130,242,color);
           }else {
               guiGraphics.blit(MANA_BAR2,x+84  , 174 , 0, 0, 128, 128, 128, 128);
               for(int i = 0; i < 100; i++) {
                   if(ClientPlayerManaData.getPlayerMana() > i*0.01* ClientPlayerManaData.getMaxMana()) {
                       guiGraphics.blit(MANA, x + 91 +  i, 230,1, 0, 0, 16, 16, 16, 16);
                   }else{
                       break;
                   }
               }
               guiGraphics.blit(MANA_TOP,x+90,230,2,0,0,16,16,16,16);

               Font font= Minecraft.getInstance().font;
               MutableComponent text= net.minecraft.network.chat.Component.literal("魔力值："+(int)ClientPlayerManaData.getPlayerMana());
               //String.format("%f", ClientPlayerManaData.getPlayerMana())
               int color=0x477CFF;
               guiGraphics.drawString(font,text,x+130,222,color);
           }
       }
    };
}
