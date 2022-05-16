package chaos.unity.signal.common.block;

import chaos.unity.signal.common.blockentity.SignalBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SignalBlock extends Block implements BlockEntityProvider {
    public SignalBlock() {
        super(FabricBlockSettings.of(Material.METAL));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SignalBlockEntity(pos, state);
    }
}
