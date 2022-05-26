package chaos.unity.railroad_crossing.signal.common.block.entity;

import chaos.unity.railroad_crossing.signal.common.block.SignalBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SignalBlockEntities {
    public static BlockEntityType<SingleHeadSignalBlockEntity> SINGLE_HEAD_SIGNAL_BLOCK_ENTITY;
    public static BlockEntityType<DistantSingleHeadSignalBlockEntity> DISTANT_SINGLE_HEAD_SIGNAL_BLOCK_ENTITY;
    public static BlockEntityType<DualHeadSignalBlockEntity> DUAL_HEAD_SIGNAL_BLOCK_ENTITY;
    public static BlockEntityType<SignalBoxReceiverBlockEntity> SIGNAL_BOX_RECEIVER_BLOCK_ENTITY;
    public static BlockEntityType<SignalBoxEmitterBlockEntity> SIGNAL_BOX_EMITTER_BLOCK_ENTITY;

    public static void register() {
        SINGLE_HEAD_SIGNAL_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier("rc_signal", "single_head_signal_block_entity"),
                FabricBlockEntityTypeBuilder.create(SingleHeadSignalBlockEntity::new, SignalBlocks.SINGLE_HEAD_SIGNAL).build(null)
        );
        DISTANT_SINGLE_HEAD_SIGNAL_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier("rc_signal", "distant_single_head_signal_block_entity"),
                FabricBlockEntityTypeBuilder.create(DistantSingleHeadSignalBlockEntity::new, SignalBlocks.DISTANT_SINGLE_HEAD_SIGNAL).build(null)
        );
        DUAL_HEAD_SIGNAL_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier("rc_signal", "dual_head_signal_block_entity"),
                FabricBlockEntityTypeBuilder.create(DualHeadSignalBlockEntity::new, SignalBlocks.DUAL_HEAD_SIGNAL_BLOCK).build(null)
        );
        SIGNAL_BOX_RECEIVER_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier("rc_signal", "signal_box_receiver_block_entity"),
                FabricBlockEntityTypeBuilder.create(SignalBoxReceiverBlockEntity::new, SignalBlocks.SIGNAL_BOX_RECEIVER).build(null)
        );
        SIGNAL_BOX_EMITTER_BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier("rc_signal", "signal_box_emitter_block_entity"),
                FabricBlockEntityTypeBuilder.create(SignalBoxEmitterBlockEntity::new, SignalBlocks.SIGNAL_BOX_EMITTER).build(null)
        );
    }
}
