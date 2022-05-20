package chaos.unity.signal.common.block.entity;

import chaos.unity.signal.common.data.SignalMode;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SignalBoxReceiverBlockEntity extends BlockEntity implements ISyncable, ISignalReceiver {
    public @Nullable BlockPos receivingOwnerPos;
    public @NotNull SignalMode detectMode = SignalMode.RED;

    public SignalBoxReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(SignalBlockEntities.SIGNAL_BOX_RECEIVER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, SignalBoxReceiverBlockEntity blockEntity) {
        if (blockEntity.receivingOwnerPos != null && world.getBlockEntity(blockEntity.receivingOwnerPos) instanceof ISignalEmitter emitter) {

        }
    }

    @Nullable
    @Override
    public BlockPos getReceivingOwnerPos() {
        return receivingOwnerPos;
    }

    @Override
    public void setReceivingOwnerPos(@NotNull BlockPos receivingOwnerPos) {
        this.receivingOwnerPos = receivingOwnerPos;

        markDirtyAndSync();
        world.updateNeighbors(getPos(), world.getBlockState(getPos()).getBlock());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("receiving_owner_pos"))
            receivingOwnerPos = NbtHelper.toBlockPos(nbt.getCompound("receiving_owner_pos"));
        detectMode = SignalMode.values[nbt.getInt("detect_mode")];
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (receivingOwnerPos != null)
            nbt.put("receiving_owner_pos", NbtHelper.fromBlockPos(receivingOwnerPos));
        nbt.putInt("detect_mode", detectMode.ordinal());
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
