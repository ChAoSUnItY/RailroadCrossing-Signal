package chaos.unity.signal.common.blockentity;

import chaos.unity.signal.common.block.SignalBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SignalBlockEntities {
    public static BlockEntityType<SingleHeadSignalBlockEntity> SIGNAL_BLOCK_ENTITY;

    public static void register() {
        SIGNAL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier("signal", "signal_block_entity"), FabricBlockEntityTypeBuilder.create(SingleHeadSignalBlockEntity::new, SignalBlocks.SINGLE_HEAD_SIGNAL).build(null));
    }
}
