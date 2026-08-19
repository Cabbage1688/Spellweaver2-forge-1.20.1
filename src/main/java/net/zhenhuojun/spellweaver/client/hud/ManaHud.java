package net.zhenhuojun.spellweaver.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.zhenhuojun.spellweaver.Config;
import net.zhenhuojun.spellweaver.Spellweaver;
import net.zhenhuojun.spellweaver.capability.impl.mana.ManaUtil;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerManaData;

public class ManaHud {

    private static final ResourceLocation MANA=ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"textures/hud/mana2.png");
    private static final ResourceLocation MANA_BAR1=ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"textures/hud/mana_bar1.png");
    private static final ResourceLocation MANA_BAR2=ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"textures/hud/mana_bar2.png");
    public static final ResourceLocation MANA_TOP=ResourceLocation.fromNamespaceAndPath(Spellweaver.MODID,"textures/hud/mana_top.png");

    public static final IGuiOverlay HUD_MANA = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        //有等级才显示魔力条
       if(ClientPlayerManaData.getManaLevel()>0){
           int x = screenWidth / 2;
           int y = screenHeight;
           /// 全屏下我的长427,宽267
           /// 非全屏427，249
           /// 小窗返回427，240，但实际肯定没这么大
           //默认赋值的坐标屏幕为右下角，这里先赋好默认值防一手玩家瞎填
           int manaBarX=x+84;
           int manaBarY=y-73;
           int manaXWithoutI=x + 91;
           int manaAndManaTopY=y-17;
           int manaTopX=x+90;
           int fontX=x+130;
           int fontY=y-25;
           switch (Config.manaBarPosition){
               case 0->{
                    manaBarX=x+84;
                    manaBarY=y-73;
                    manaXWithoutI=x + 91;
                    manaAndManaTopY=y-17;
                    manaTopX=x+90;
                    fontX=x+130;
                    fontY=y-25;
               }
               case 1->{//左下
                   manaBarX = x - 214;
                   manaBarY = y - 73;
                   manaXWithoutI = x - 207;
                   manaAndManaTopY = y - 17;
                   manaTopX = x - 208;
                   fontX = x - 168;
                   fontY = y - 25;
               }
               case 2 -> {   // 左上
                   manaBarX = x -  214;
                   manaBarY = -44;
                   manaXWithoutI = x - 207;
                   manaAndManaTopY = 12;
                   manaTopX = x - 208;
                   fontX = x -168;
                   fontY = 4;
               }
               case 3 -> {   // 右上
                   manaBarX = x + 84;
                   manaBarY = -44;
                   manaXWithoutI = x + 91;
                   manaAndManaTopY = 12;
                   manaTopX = x + 90;
                   fontX = x + 130;
                   fontY = 4;
               }
           }
           guiGraphics.blit(MANA_BAR2,manaBarX  , manaBarY , 0, 0, 128, 128, 128, 128);
           for(int i = 0; i < 100; i++) {
               if(ClientPlayerManaData.getPlayerMana() > i*0.01* ClientPlayerManaData.getMaxMana()) {
                   guiGraphics.blit(MANA, manaXWithoutI +  i, manaAndManaTopY,1, 0, 0, 16, 16, 16, 16);
               }else{
                   break;
               }
           }
           guiGraphics.blit(MANA_TOP,manaTopX,manaAndManaTopY,2,0,0,16,16,16,16);

           Font font= Minecraft.getInstance().font;
           //MutableComponent text= net.minecraft.network.chat.Component.translatable("hud.spellweaver.mana",(long)ClientPlayerManaData.getPlayerMana());
           MutableComponent text= net.minecraft.network.chat.Component.translatable("hud.spellweaver.mana", ManaUtil.formatMana((long) ClientPlayerManaData.getPlayerMana()));
           int color=0x477CFF;
           guiGraphics.drawString(font,text,fontX,fontY,color);
       }
    };


}
