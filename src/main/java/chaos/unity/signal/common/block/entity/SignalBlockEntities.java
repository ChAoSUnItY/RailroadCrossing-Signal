package chaos.unity.signal.common.block.entity;

import chaos.unity.signal.common.block.SignalBlocks;
import chaos.unity.signal.common.block.SignalBoxReceiverBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SignalBlockEntities {
    public static BlockEntityType<SingleHeadSignalBlockEntity> SIGNAL_BLOCK_ENTITY;
    public static BlockEntityType<SignalBoxReceiverBlockEntity> SIGNAL_BOX_RECEIVER_BLOCK_ENTITY;

    public static void register() {
        SIGNAL_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier("signal", "single_head_signal_block_entity"),
                FabricBlockEntityTypeBuilder.create(SingleHeadSignalBlockEntity::new, SignalBlocks.SINGLE_HEAD_SIGNAL).build(null)
        );
        SIGNAL_BOX_RECEIVER_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier("signal", "signal_box_receiver_block_entity"),
                FabricBlockEntityTypeBuilder.create(SignalBoxReceiverBlockEntity::new, SignalBlocks.SIGNAL_BOX_RECEIVER).build(null)
        );
    }
}
