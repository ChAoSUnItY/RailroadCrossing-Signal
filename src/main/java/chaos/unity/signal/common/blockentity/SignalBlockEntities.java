package chaos.unity.signal.common.blockentity;

import chaos.unity.signal.common.block.SignalBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public final class SignalBlockEntities {
    public static BlockEntityType<SignalBlockEntity> SIGNAL_BLOCK_ENTITY;

    public static void register() {
        SIGNAL_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "signal:signal_block_entity", FabricBlockEntityTypeBuilder.create(SignalBlockEntity::new, SignalBlocks.SIGNAL_BLOCK).build(null));
    }
}
