package net.zhenhuojun.spellweaver.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.zhenhuojun.spellweaver.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
//导出法术时发这个包，如果手持纸则生成魔法书页
public class MagicPageC2SPacket {
    private final String note;
    private final List<String> authors;
    private final String spellContent;
    private final String name;

    public MagicPageC2SPacket(String note, List<String>authors, String spellContent,String name){
        this.note=note;
        this.authors=authors;
        this.spellContent=spellContent;
        this.name=name;
    }

    public MagicPageC2SPacket(FriendlyByteBuf friendlyByteBuf){
        this.note=friendlyByteBuf.readUtf();
        int size = friendlyByteBuf.readInt();
        this.authors = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.authors.add(friendlyByteBuf.readUtf(64));
        }
        this.spellContent=friendlyByteBuf.readUtf();
        this.name=friendlyByteBuf.readUtf();
    }

    public void toByte(FriendlyByteBuf friendlyByteBuf){
        friendlyByteBuf.writeUtf(note, 256);
        friendlyByteBuf.writeInt(authors.size());
        for (String author : authors) {
            friendlyByteBuf.writeUtf(author, 64);
        }
        friendlyByteBuf.writeUtf(spellContent,10000);
        friendlyByteBuf.writeUtf(name,64);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack paper =player.getMainHandItem();
                if(paper.is(Items.PAPER)){
                    paper.shrink(1);
                    ItemStack page=new ItemStack(ModItems.MAGIC_PAGE.get());
                    CompoundTag tag = new CompoundTag();
                    tag.putString("note",note);
                    ListTag authorTag=new ListTag();
                    for (String author : authors) {
                        authorTag.add(StringTag.valueOf(author));
                    }
                    tag.put("authors", authorTag);
                    tag.putString("spellContent", spellContent);
                    tag.putString("name",name);
                    page.setTag(tag);
                    if(!player.addItem(page)){
                        player.drop(page,false);
                    }
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
