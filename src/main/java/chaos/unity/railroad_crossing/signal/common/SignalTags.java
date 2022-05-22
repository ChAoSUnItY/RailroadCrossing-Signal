package chaos.unity.railroad_crossing.signal.common;

import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SignalTags {
    public static final TagKey<Block> SIGNAL_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier("c", "signal"));
    public static final TagKey<Block> SIGNAL_BOX_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier("c", "signal_box"));
    public static final TagKey<Block> SIGNAL_EMITTER_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier("c", "signal_emitter"));
    public static final TagKey<Block> SIGNAL_RECEIVER_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier("c", "signal_receiver"));
}
