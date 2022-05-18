package chaos.unity.signal.common.block;

import chaos.unity.signal.common.blockentity.SignalBlockEntities;
import chaos.unity.signal.common.blockentity.SignalBlockEntity;
import chaos.unity.signal.common.world.IntervalData;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class SignalBlock extends Block implements BlockEntityProvider {
    public SignalBlock() {
        super(FabricBlockSettings.of(Material.METAL));
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        if (world instanceof ServerWorld serverWorld && serverWorld.getBlockEntity(pos) instanceof SignalBlockEntity sbe && sbe.pairedSignalPos != null) {
            var intervalData = IntervalData.getOrCreate(serverWorld);
            var removedInterval = intervalData.removeBySignal(pos);

            if (removedInterval != null) {
                intervalData.markDirty();
                removedInterval.unbindAllRelatives(serverWorld);
            }
        }
        super.onBroken(world, pos, state);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SignalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BlockWithEntity.checkType(type, SignalBlockEntities.SIGNAL_BLOCK_ENTITY, SignalBlockEntity::tick);
    }
}
