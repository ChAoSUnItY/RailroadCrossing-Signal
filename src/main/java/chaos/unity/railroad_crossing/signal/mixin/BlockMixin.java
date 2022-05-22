package chaos.unity.railroad_crossing.signal.mixin;

import chaos.unity.railroad_crossing.signal.common.world.IntervalData;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "onBroken", at = @At(value = "HEAD"))
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (world.isClient())
            return;

        if (world instanceof ServerWorld serverWorld && state.getBlock() instanceof AbstractRailBlock) {
            var intervalData = IntervalData.getOrCreate(serverWorld);
            var interval = intervalData.removeByIntervalPath(pos);

            if (interval != null) {
                intervalData.markDirty();
                interval.unbindAllRelatives(serverWorld);
            }
        }
    }
}
