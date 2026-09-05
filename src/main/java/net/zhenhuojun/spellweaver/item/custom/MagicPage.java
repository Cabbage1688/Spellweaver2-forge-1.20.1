package net.zhenhuojun.spellweaver.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.PlayerSpellStorage;
import net.zhenhuojun.spellweaver.capability.impl.spell_storage.StoredSpell;
import net.zhenhuojun.spellweaver.client.gui.SpellStorageScreen;
import net.zhenhuojun.spellweaver.client.gui.util.ClientPlayerStorageData;
import net.zhenhuojun.spellweaver.item.ModItems;
import net.zhenhuojun.spellweaver.network.ModMessage;
import net.zhenhuojun.spellweaver.network.packet.ImportSpellC2SPacket;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MagicPage extends Item {

    public MagicPage(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("spellContent")) {
            return;
        }
        String spellContent = tag.getString("spellContent");
        if (!spellContent.startsWith("SPELLWEAVER_SPELL:")) {
            return;
        }

        String name = tag.getString("name");
        String note = tag.getString("note");

        // 读取作者列表
        ListTag authorsTag = tag.getList("authors", Tag.TAG_STRING);
        List<String> authors = new ArrayList<>();
        for (Tag t : authorsTag) {
            authors.add(t.getAsString());
        }

        /*byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(spellContent.substring("SPELLWEAVER_SPELL:".length()));
        } catch (IllegalArgumentException e) {
            return;
        }
        CompoundTag importTag;
        try {
            importTag = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            return;
        }
        StoredSpell spell;
        try {
            spell = StoredSpell.deserialize(importTag);
        } catch (Exception e) {
            return;
        }

         */
        // 法术名
        tooltip.add(Component.translatable("gui.spellweaver.spell_name")
                .append(": ")
                .append(name)
                .withStyle(ChatFormatting.AQUA));
        // 作者列表

        Component authorValue = authors.isEmpty()
                ? Component.translatable("message.spellweaver.anonymous")
                : Component.literal(String.join(", ", authors));
        tooltip.add(Component.translatable("gui.spellweaver.all_authors")
                .append(": ")
                .append(authorValue)
                .withStyle(ChatFormatting.GOLD));
        // 备注

        Component noteValue = note.isEmpty()
                ? Component.translatable("gui.spellweaver.no_note")
                : Component.literal(note);
        tooltip.add(Component.translatable("gui.note")
                .append(": ")
                .append(noteValue)
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);
        if(!pLevel.isClientSide) {
            if(itemstack.is(ModItems.MAGIC_PAGE.get())){
                 net.minecraft.nbt.CompoundTag tag =itemstack.getTag();
                 if(tag!=null){
                     String string=tag.getString("spellContent");
                     importSpell(string,player,itemstack);
                 }
            }
            return InteractionResultHolder.sidedSuccess(itemstack, false);
        }
        return InteractionResultHolder.sidedSuccess(itemstack, true);
    }

    private boolean importSpell(String input,Player player,ItemStack itemStack) {
        if (!input.startsWith("SPELLWEAVER_SPELL:")) {
            player.displayClientMessage(Component.translatable("message.spellweaver.invalid_spell_data"), false);
            return false;
        }
        String base64 = input.substring("SPELLWEAVER_SPELL:".length());
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            player.displayClientMessage(Component.translatable("message.spellweaver.decode_failed"), false);
            return false;
        }

        CompoundTag importTag;
        try {
            importTag = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            player.displayClientMessage(Component.translatable("message.spellweaver.cannot_read_spell"), false);
            return false;
        }

        StoredSpell importedSpell;
        try {
            importedSpell = StoredSpell.deserialize(importTag);
        } catch (Exception e) {
            player.displayClientMessage(Component.translatable("message.spellweaver.spell_corrupted"), false);
            return false;
        }

        PlayerSpellStorage storage = ClientPlayerStorageData.getPlayerSpellStorage();
        // 直接新增
        if (storage.getSpells().size() >=  PlayerSpellStorage.MAX_STORED_SPELLS) {
            player.displayClientMessage(Component.translatable("message.spellweaver.cannot_import"), false);
            return false;
        }
        /// 优先使用缓存的信息来避免备注信息要写中英双语的冗余
        /// 我真聪明hhhh:)
        CompoundTag tag=itemStack.getTag();
        if(tag!=null){
            if(tag.contains("name")&&tag.contains("note")&&tag.contains("authors")){
                String name = tag.getString("name");
                String note = tag.getString("note");
                ListTag authorsTag = tag.getList("authors", Tag.TAG_STRING);
                List<String> authors = new ArrayList<>();
                for (Tag t : authorsTag) {
                    authors.add(t.getAsString());
                }
                ModMessage.sendToServer(new ImportSpellC2SPacket(
                        name,
                        importedSpell.getSequenceNode().serializeNBT(),
                        authors,
                        note,
                        null
                ));
            }else {
                ModMessage.sendToServer(new ImportSpellC2SPacket(
                        importedSpell.getName(),
                        importedSpell.getSequenceNode().serializeNBT(),
                        importedSpell.getAuthors(),
                        importedSpell.getNote(),
                        null
                ));
            }
        }else {
            ModMessage.sendToServer(new ImportSpellC2SPacket(
                    importedSpell.getName(),
                    importedSpell.getSequenceNode().serializeNBT(),
                    importedSpell.getAuthors(),
                    importedSpell.getNote(),
                    null
            ));
        }

            // 本地更新
        storage.getSpells().put(importedSpell.getId(), importedSpell);
        player.displayClientMessage(Component.translatable("message.spellweaver.import_success", importedSpell.getName()), false);
        return true;

    }

}
