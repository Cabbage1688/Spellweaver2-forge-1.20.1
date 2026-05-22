package net.zhenhuojun.spellweaver.spell.node;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
//节点注册和反序列化工厂
public class NodeRegistry {
    private static final Map<String, Supplier<Node>> NODE_FACTORIES = new HashMap<>();

    static {
        register("sequence", SequenceNode::new);
        register("normal", NormalNode::new);
        register("loop", LoopNode::new);
        register("condition", ConditionNode::new);
        register("wait",WaitNode::new);
    }

    private static void register(String type, Supplier<Node> factory) {
        NODE_FACTORIES.put(type, factory);
    }

    public static Node deserialize(CompoundTag tag) {
        String type = tag.getString("type");
        Supplier<Node> factory = NODE_FACTORIES.get(type);

        if (factory != null) {
            Node node = factory.get();
            node.deserializeNBT(tag);
            return node;
        }

        throw new IllegalArgumentException("未知节点类型: " + type);
    }
}