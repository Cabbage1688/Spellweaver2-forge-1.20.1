package net.zhenhuojun.spellweaver.capability.impl.spell_storage;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.zhenhuojun.spellweaver.spell.node.SequenceNode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StoredSpell {
    private UUID id;//法术的唯一标识符
    private String name; //法术的名称
    private SequenceNode sequenceNode;//法术树根节点
    private List<String> authors;  // 作者链，2026.5.24
    private String note;//法术备注，2026.5.24

    //用于创建新法术
    public StoredSpell(String name,SequenceNode sequenceNode){
        this.id = UUID.randomUUID();
        this.name = name;
        this.sequenceNode=sequenceNode;
         //2026.5.24导入导出更新
        this.authors = new ArrayList<>();
        this.note = "";
    }

    // 反序列化用
    public StoredSpell(UUID id, String name, SequenceNode sequenceNode, List<String> authors ,String note) {
        this.id = id;
        this.name = name;
        this.sequenceNode = sequenceNode;
        this.authors = authors != null ? authors : new ArrayList<>();
        this.note = note != null ? note : "";
    }

    // 兼容旧的反序列化
    public StoredSpell(UUID id, String name, SequenceNode sequenceNode) {
        this(id, name, sequenceNode, new ArrayList<>(),"");
    }
    //这个用来反序列化
    /*public StoredSpell(UUID id,String name,SequenceNode sequenceNode){
        this.id=id;
        this.name=name;
        this.sequenceNode=sequenceNode;
    }

     */

    public String getNote() { return note; }

    // 设置备注
    public void setNote(String note) {
        this.note = (note != null) ? note : "";
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public SequenceNode getSequenceNode() {
        return sequenceNode;
    }

    public void setSequenceNode(SequenceNode sequenceNode) {
        this.sequenceNode = sequenceNode;
    }

    // 重命名法术
    public void rename(String newName) {
        this.name = newName;
    }

    public List<String> getAuthors() { return authors; }

    // 2026.5.24导入导出更新添加作者
    public void addAuthor(String author) {
        if (author == null || author.isEmpty()) {
            author = "佚名";
        }
        if(author.equals("Cabbage1688")||author.equals("真火菌")||author.equals("逍遥子1688")){
            if (Minecraft.getInstance().player != null) {
                Player player=Minecraft.getInstance().player;
                if(!player.getDisplayName().getString().equals("Cabbage1688")||!player.getDisplayName().getString().equals("Dev")){
                    author=Minecraft.getInstance().player.getDisplayName().getString();
                    player.sendSystemMessage(Component.literal("§c不可以填我的名字哦~"));
                }
            }
        }
        if (this.authors == null) {
            this.authors = new ArrayList<>();
        }
        if(!this.authors.isEmpty()){
            String lastName=this.authors.get(this.authors.size()-1);
            if(lastName.equals(author)){
                return;
            }
        }
        this.authors.add(author);
    }

    // 序列化方法
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putString("name", name);
        tag.put("sequence_node",sequenceNode.serializeNBT());

        // 2026.5.24导入导出更新新增序列化authors
        ListTag authorsTag = new ListTag();
        for (String author : authors) {
            authorsTag.add(StringTag.valueOf(author));
        }
        tag.put("authors", authorsTag);

        // 2026.5.24序列化 note（非空才写入）
        if (!note.isEmpty()) {
            tag.putString("note", note);
        }
        return tag;
    }
    //反序列化
    public static StoredSpell deserialize(CompoundTag tag) {
        UUID id = tag.getUUID("id");
        String name = tag.getString("name");
        CompoundTag sequenceTag= (CompoundTag) tag.get("sequence_node");
        SequenceNode sequenceNode=new SequenceNode();
        sequenceNode.deserializeNBT(sequenceTag);

        // 2026.5.24导入导出更新新增反序列化 authors
        List<String> authors = new ArrayList<>();
        if (tag.contains("authors")) {
            ListTag authorsTag = tag.getList("authors", 8);
            for (int i = 0; i < authorsTag.size(); i++) {
                authors.add(authorsTag.getString(i));
            }
        }

        String note = tag.contains("note") ? tag.getString("note") : "";

        //return new StoredSpell(id,name,sequenceNode);
        return new StoredSpell(id, name, sequenceNode, authors,note);
    }
}
