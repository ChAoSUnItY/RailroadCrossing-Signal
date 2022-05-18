package chaos.unity.signal.common.blockentity;

import chaos.unity.signal.common.block.SignalBlocks;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SignalBlockEntity extends BlockEntity {
    public BlockPos railBindPos;
    public BlockPos pairedSignalPos;

    public SignalBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.SIGNAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("rail_bound_pos"))
            railBindPos = NbtHelper.toBlockPos(nbt.getCompound("rail_bound_pos"));
        if (nbt.contains("paired_signal_pos"))
            pairedSignalPos = NbtHelper.toBlockPos(nbt.getCompound("paired_signal_pos"));
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (railBindPos != null)
            nbt.put("rail_bound_pos", NbtHelper.fromBlockPos(railBindPos));
        if (pairedSignalPos != null)
            nbt.put("paired_signal_pos", NbtHelper.fromBlockPos(pairedSignalPos));
        super.writeNbt(nbt);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
