package chaos.unity.signal.common.block;

import chaos.unity.signal.common.blockentity.SignalBlockEntity;
import chaos.unity.signal.common.world.IntervalData;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
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

                if (world.getBlockEntity(removedInterval.signalPosA()) instanceof SignalBlockEntity sbeA) {
                    sbeA.pairedSignalPos = null;
                    sbeA.markDirty();
                }
                if (world.getBlockEntity(removedInterval.signalPosB()) instanceof  SignalBlockEntity sbeB) {
                    sbeB.pairedSignalPos = null;
                    sbeB.markDirty();
                }
            }
        }
        super.onBroken(world, pos, state);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SignalBlockEntity(pos, state);
    }
}
